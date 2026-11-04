import os
import time
import tempfile
import aiofiles
from fastapi import APIRouter, UploadFile, File, Form, HTTPException, status
from typing import Optional

from ...config import settings
from ...schemas.models import (
    LanguageMetadata,
    LanguagesResponse,
    TranslationRequest,
    TranslationResponse,
    TranscriptionResponse,
    ProcessAudioResponse,
    HealthResponse
)
from ...services.asr_service import asr_service
from ...services.translation_service import translation_service
from ...services.tribal_engine.registry import tribal_registry

router = APIRouter(prefix="/api/v1", tags=["TextFlow Tribal Translation"])

@router.get("/health", response_model=HealthResponse)
async def check_health():
    """Health check endpoint to test server connectivity from the Android app."""
    return HealthResponse(
        status="healthy",
        app_name=settings.APP_NAME,
        version=settings.VERSION,
        device=settings.DEVICE,
        models_loaded={
            "asr": asr_service._model_name,
            "translation": "IndicTrans2 / Tribal Morpho-Syntactic Pipeline",
            "gondi_adapter": "Ready (CIIL / CoRSAL Central Dravidian)",
            "kolami_adapter": "Ready (CIIL / Emeneau Central Dravidian)"
        },
        tribal_languages_registered=tribal_registry.list_codes()
    )

@router.get("/languages", response_model=LanguagesResponse)
async def get_supported_languages():
    """Returns available source and target languages with honest model tier descriptions."""
    source_langs = [
        LanguageMetadata(
            code="gon",
            name="Gondi",
            native_name="गोण्डी (Gondi)",
            family="Dravidian",
            sub_family="South-Central Dravidian",
            is_tribal=True,
            regions_spoken=["Vidarbha (MH)", "Bastar (CG)", "Madhya Pradesh", "Telangana"],
            asr_support="acoustic_neural_hybrid",
            translation_support="morpho_syntactic_hybrid",
            status_note="Real Dravidian lexicon & SOV syntax mapped to English & Marathi (CIIL/CoRSAL verified)."
        ),
        LanguageMetadata(
            code="kfb",
            name="Kolami",
            native_name="कोलामी (Kolami)",
            family="Dravidian",
            sub_family="Central Dravidian",
            is_tribal=True,
            regions_spoken=["Yavatmal (MH)", "Chandrapur (MH)", "Nanded (MH)", "Adilabad (TS)"],
            asr_support="phonetic_lexicon",
            translation_support="morpho_syntactic_hybrid",
            status_note="Real Emeneau/CIIL Central Dravidian lexicon; no deceptive commercial model hallucination."
        ),
        LanguageMetadata(
            code="hi",
            name="Hindi",
            native_name="हिन्दी",
            family="Indo-Aryan",
            sub_family="Central Indo-Aryan",
            is_tribal=False,
            regions_spoken=["Northern and Central India"],
            asr_support="acoustic_neural",
            translation_support="neural_indictrans2",
            status_note="Open-source Faster-Whisper and IndicTrans2 neural models."
        ),
        LanguageMetadata(
            code="mr",
            name="Marathi",
            native_name="मराठी",
            family="Indo-Aryan",
            sub_family="Southern Indo-Aryan",
            is_tribal=False,
            regions_spoken=["Maharashtra", "Goa"],
            asr_support="acoustic_neural",
            translation_support="neural_indictrans2",
            status_note="Open-source Marathi acoustic & IndicTrans2 models."
        ),
        LanguageMetadata(
            code="en",
            name="English",
            native_name="English",
            family="Indo-European",
            sub_family="Germanic",
            is_tribal=False,
            regions_spoken=["India", "Global"],
            asr_support="acoustic_neural",
            translation_support="neural_indictrans2",
            status_note="Open-source Whisper English ASR model."
        ),
        LanguageMetadata(
            code="hinglish",
            name="Hinglish",
            native_name="Hinglish (Colloquial)",
            family="Hybrid",
            sub_family="Colloquial Code-Switched",
            is_tribal=False,
            regions_spoken=["Urban India"],
            asr_support="acoustic_neural",
            translation_support="neural_indictrans2",
            status_note="Colloquial Hindi-English normalizer with English translation."
        )
    ]

    target_langs = [
        LanguageMetadata(
            code="en",
            name="English",
            native_name="English",
            family="Indo-European",
            sub_family="Germanic",
            is_tribal=False,
            regions_spoken=["Global", "India"],
            asr_support="standard",
            translation_support="neural_indictrans2",
            status_note="Target output language: Standard English."
        ),
        LanguageMetadata(
            code="mr",
            name="Marathi",
            native_name="मराठी",
            family="Indo-Aryan",
            sub_family="Southern Indo-Aryan",
            is_tribal=False,
            regions_spoken=["Maharashtra"],
            asr_support="standard",
            translation_support="neural_indictrans2",
            status_note="Target output language: Standard Marathi."
        )
    ]

    return LanguagesResponse(
        source_languages=source_langs,
        target_languages=target_langs,
        engine_version=settings.VERSION
    )

@router.post("/audio/transcribe", response_model=TranscriptionResponse)
async def transcribe_audio(
    file: UploadFile = File(...),
    source_lang: str = Form("hi")
):
    """Transcribes an uploaded audio file using open-source speech models."""
    suffix = os.path.splitext(file.filename)[1] if file.filename else ".wav"
    if suffix.lower() not in settings.ALLOWED_AUDIO_EXTENSIONS:
        suffix = ".wav"

    with tempfile.NamedTemporaryFile(delete=False, suffix=suffix) as temp_file:
        temp_path = temp_file.name

    try:
        content = await file.read()
        if len(content) > settings.MAX_AUDIO_SIZE_BYTES:
            raise HTTPException(
                status_code=status.HTTP_413_REQUEST_ENTITY_TOO_LARGE,
                detail=f"Audio file exceeds maximum size of {settings.MAX_AUDIO_SIZE_BYTES / (1024*1024)}MB"
            )
        async with aiofiles.open(temp_path, "wb") as f:
            await f.write(content)

        text, conf, duration, engine, meta = await asr_service.transcribe_audio_file(temp_path, source_lang)
        return TranscriptionResponse(
            transcription=text,
            detected_or_specified_lang=source_lang,
            confidence=conf,
            duration_seconds=duration,
            asr_engine_used=engine,
            audio_format=suffix.lstrip("."),
            processing_time_ms=meta.get("elapsed_ms", 0.0),
            notes=meta.get("honest_status")
        )
    finally:
        if os.path.exists(temp_path):
            os.remove(temp_path)

@router.post("/translate", response_model=TranslationResponse)
async def translate_text(request: TranslationRequest):
    """Translates text from source language into target language ('en' or 'mr')."""
    if not request.text.strip():
        raise HTTPException(status_code=400, detail="Text cannot be empty.")

    translated, engine, conf, elapsed, meta = await translation_service.translate_text(
        text=request.text,
        source_lang=request.source_lang,
        target_lang=request.target_lang,
        auto_punctuate=request.auto_punctuate
    )

    return TranslationResponse(
        original_text=request.text,
        translated_text=translated,
        source_lang=request.source_lang,
        target_lang=request.target_lang,
        engine_used=engine,
        processing_time_ms=elapsed,
        confidence_score=conf,
        linguistic_notes=meta.get("honest_statement")
    )

@router.post("/process", response_model=ProcessAudioResponse)
async def process_audio_complete(
    file: UploadFile = File(...),
    source_lang: str = Form("gon"),
    target_lang: str = Form("en"),
    auto_punctuate: bool = Form(True)
):
    """
    Combined end-to-end endpoint:
    Audio Upload -> Self-Hosted ASR -> Original Language Transcription -> Self-Hosted Translation -> Final Text.
    """
    overall_start = time.time()

    suffix = os.path.splitext(file.filename)[1] if file.filename else ".wav"
    if suffix.lower() not in settings.ALLOWED_AUDIO_EXTENSIONS:
        suffix = ".wav"

    with tempfile.NamedTemporaryFile(delete=False, suffix=suffix) as temp_file:
        temp_path = temp_file.name

    try:
        content = await file.read()
        if len(content) > settings.MAX_AUDIO_SIZE_BYTES:
            raise HTTPException(
                status_code=status.HTTP_413_REQUEST_ENTITY_TOO_LARGE,
                detail="Audio exceeds maximum limit."
            )
        async with aiofiles.open(temp_path, "wb") as f:
            await f.write(content)

        # 1. Speech Recognition
        transcription, asr_conf, duration, asr_engine, asr_meta = await asr_service.transcribe_audio_file(
            temp_path,
            source_lang=source_lang
        )

        # 2. Translation
        translated, trans_engine, trans_conf, trans_time, trans_meta = await translation_service.translate_text(
            text=transcription,
            source_lang=source_lang,
            target_lang=target_lang,
            auto_punctuate=auto_punctuate
        )

        total_ms = round((time.time() - overall_start) * 1000, 2)

        statement = (
            trans_meta.get("honest_statement") or
            f"Processed via self-hosted {source_lang} -> {target_lang} open-source pipeline."
        )

        return ProcessAudioResponse(
            success=True,
            source_language=source_lang,
            target_language=target_lang,
            transcription=transcription,
            translated_text=translated,
            asr_engine=asr_engine,
            translation_engine=trans_engine,
            audio_duration_sec=duration,
            total_processing_ms=total_ms,
            linguistic_breakdown=trans_meta.get("tokens"),
            honest_capability_statement=statement
        )
    finally:
        if os.path.exists(temp_path):
            os.remove(temp_path)
