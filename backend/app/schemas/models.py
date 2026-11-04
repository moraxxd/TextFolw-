from typing import List, Optional, Dict, Any
from pydantic import BaseModel, Field

class LanguageMetadata(BaseModel):
    code: str = Field(..., description="ISO 639-3 or standard language code (e.g. 'gon', 'kfb', 'hi', 'mr', 'en')")
    name: str = Field(..., description="English display name")
    native_name: str = Field(..., description="Native script name")
    family: str = Field(..., description="Language family (e.g. Dravidian / Indo-Aryan)")
    sub_family: Optional[str] = Field(None, description="Sub-branch (e.g. South-Central Dravidian, Central Dravidian)")
    is_tribal: Boolean = Field(False, description="Whether this is an indigenous/tribal language")
    regions_spoken: List[str] = Field(default_factory=list, description="Geographic regions where spoken")
    asr_support: str = Field(..., description="Engine tier: 'acoustic_neural', 'phonetic_lexicon', 'standard'")
    translation_support: str = Field(..., description="Engine tier: 'neural_nllb', 'morpho_syntactic_hybrid', 'neural_indictrans2'")
    status_note: str = Field(..., description="Honest statement on model tier & scientific grounding")

class LanguagesResponse(BaseModel):
    source_languages: List[LanguageMetadata]
    target_languages: List[LanguageMetadata]
    engine_version: str

class TranslationRequest(BaseModel):
    text: str = Field(..., min_length=1, max_length=5000, description="Source text to translate")
    source_lang: str = Field(..., description="Source language code (e.g. gon, kfb, hi, mr, en)")
    target_lang: str = Field(default="en", description="Target language code ('en' or 'mr')")
    auto_punctuate: bool = Field(default=True, description="Apply clean capitalization & punctuation")

class TranslationResponse(BaseModel):
    original_text: str
    translated_text: str
    source_lang: str
    target_lang: str
    engine_used: str
    processing_time_ms: float
    confidence_score: float
    linguistic_notes: Optional[str] = None

class TranscriptionResponse(BaseModel):
    transcription: str
    detected_or_specified_lang: str
    confidence: float
    duration_seconds: float
    asr_engine_used: str
    audio_format: str
    processing_time_ms: float
    notes: Optional[str] = None

class ProcessAudioResponse(BaseModel):
    success: bool
    source_language: str
    target_language: str
    transcription: str
    translated_text: str
    asr_engine: str
    translation_engine: str
    audio_duration_sec: float
    total_processing_ms: float
    linguistic_breakdown: Optional[Dict[str, Any]] = None
    honest_capability_statement: str

class HealthResponse(BaseModel):
    status: str
    app_name: str
    version: str
    device: str
    models_loaded: Dict[str, str]
    tribal_languages_registered: List[str]
