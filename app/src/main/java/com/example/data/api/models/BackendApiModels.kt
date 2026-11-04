package com.example.data.api.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class HealthResponse(
    @Json(name = "status") val status: String,
    @Json(name = "app_name") val appName: String,
    @Json(name = "version") val version: String,
    @Json(name = "device") val device: String,
    @Json(name = "models_loaded") val modelsLoaded: Map<String, String>?,
    @Json(name = "tribal_languages_registered") val tribalLanguagesRegistered: List<String>?
)

@JsonClass(generateAdapter = true)
data class LanguageMetadataDto(
    @Json(name = "code") val code: String,
    @Json(name = "name") val name: String,
    @Json(name = "native_name") val nativeName: String,
    @Json(name = "family") val family: String,
    @Json(name = "sub_family") val subFamily: String?,
    @Json(name = "is_tribal") val isTribal: Boolean,
    @Json(name = "regions_spoken") val regionsSpoken: List<String>?,
    @Json(name = "asr_support") val asrSupport: String,
    @Json(name = "translation_support") val translationSupport: String,
    @Json(name = "status_note") val statusNote: String
)

@JsonClass(generateAdapter = true)
data class LanguagesResponse(
    @Json(name = "source_languages") val sourceLanguages: List<LanguageMetadataDto>,
    @Json(name = "target_languages") val targetLanguages: List<LanguageMetadataDto>,
    @Json(name = "engine_version") val engineVersion: String
)

@JsonClass(generateAdapter = true)
data class TranslationRequestDto(
    @Json(name = "text") val text: String,
    @Json(name = "source_lang") val sourceLang: String,
    @Json(name = "target_lang") val targetLang: String = "en",
    @Json(name = "auto_punctuate") val autoPunctuate: Boolean = true
)

@JsonClass(generateAdapter = true)
data class TranslationResponseDto(
    @Json(name = "original_text") val originalText: String,
    @Json(name = "translated_text") val translatedText: String,
    @Json(name = "source_lang") val sourceLang: String,
    @Json(name = "target_lang") val targetLang: String,
    @Json(name = "engine_used") val engineUsed: String,
    @Json(name = "processing_time_ms") val processingTimeMs: Float,
    @Json(name = "confidence_score") val confidenceScore: Float,
    @Json(name = "linguistic_notes") val linguisticNotes: String?
)

@JsonClass(generateAdapter = true)
data class TranscriptionResponseDto(
    @Json(name = "transcription") val transcription: String,
    @Json(name = "detected_or_specified_lang") val detectedOrSpecifiedLang: String,
    @Json(name = "confidence") val confidence: Float,
    @Json(name = "duration_seconds") val durationSeconds: Float,
    @Json(name = "asr_engine_used") val asrEngineUsed: String,
    @Json(name = "audio_format") val audioFormat: String,
    @Json(name = "processing_time_ms") val processingTimeMs: Float,
    @Json(name = "notes") val notes: String?
)

@JsonClass(generateAdapter = true)
data class ProcessAudioResponseDto(
    @Json(name = "success") val success: Boolean,
    @Json(name = "source_language") val sourceLanguage: String,
    @Json(name = "target_language") val targetLanguage: String,
    @Json(name = "transcription") val transcription: String,
    @Json(name = "translated_text") val translatedText: String,
    @Json(name = "asr_engine") val asrEngine: String,
    @Json(name = "translation_engine") val translationEngine: String,
    @Json(name = "audio_duration_sec") val audioDurationSec: Float,
    @Json(name = "total_processing_ms") val totalProcessingMs: Float,
    @Json(name = "honest_capability_statement") val honestCapabilityStatement: String
)
