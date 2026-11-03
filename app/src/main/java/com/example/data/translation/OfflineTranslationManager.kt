package com.example.data.translation

import android.content.Context
import com.example.data.languages.SourceLanguage
import com.example.data.languages.TargetLanguage
import com.example.data.translation.tribal.TribalModelManager
import com.example.data.translation.tribal.TribalTranslationEngine
import com.google.android.gms.tasks.Task
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.TranslateRemoteModel
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

sealed class ModelDownloadState {
    object NotDownloaded : ModelDownloadState()
    object Downloading : ModelDownloadState()
    object Downloaded : ModelDownloadState()
    data class Error(val error: String) : ModelDownloadState()
}

class OfflineTranslationManager(private val context: Context) {

    private val modelManager by lazy { RemoteModelManager.getInstance() }
    private val hindiModel by lazy { TranslateRemoteModel.Builder(TranslateLanguage.HINDI).build() }
    private val marathiModel by lazy { TranslateRemoteModel.Builder(TranslateLanguage.MARATHI).build() }

    val tribalEngine = TribalTranslationEngine()
    val tribalModelManager = TribalModelManager(context)

    private var hindiToEnglishTranslator: Translator? = null
    private var englishToHindiTranslator: Translator? = null
    private var marathiToEnglishTranslator: Translator? = null
    private var hindiToMarathiTranslator: Translator? = null

    private val _hindiModelStatus = MutableStateFlow<ModelDownloadState>(ModelDownloadState.NotDownloaded)
    val hindiModelStatus: StateFlow<ModelDownloadState> = _hindiModelStatus.asStateFlow()

    init {
        checkModelStatus()
    }

    fun checkModelStatus() {
        try {
            modelManager.isModelDownloaded(hindiModel)
                .addOnSuccessListener { isDownloaded ->
                    if (isDownloaded) {
                        _hindiModelStatus.value = ModelDownloadState.Downloaded
                        initTranslators()
                    } else {
                        _hindiModelStatus.value = ModelDownloadState.NotDownloaded
                    }
                }
                .addOnFailureListener {
                    _hindiModelStatus.value = ModelDownloadState.NotDownloaded
                }
        } catch (e: Throwable) {
            _hindiModelStatus.value = ModelDownloadState.NotDownloaded
        }
    }

    private fun initTranslators() {
        if (hindiToEnglishTranslator == null) {
            val optionsHiEn = TranslatorOptions.Builder()
                .setSourceLanguage(TranslateLanguage.HINDI)
                .setTargetLanguage(TranslateLanguage.ENGLISH)
                .build()
            hindiToEnglishTranslator = Translation.getClient(optionsHiEn)
        }

        if (englishToHindiTranslator == null) {
            val optionsEnHi = TranslatorOptions.Builder()
                .setSourceLanguage(TranslateLanguage.ENGLISH)
                .setTargetLanguage(TranslateLanguage.HINDI)
                .build()
            englishToHindiTranslator = Translation.getClient(optionsEnHi)
        }
    }

    fun downloadHindiModel(
        requireWifi: Boolean = false,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        _hindiModelStatus.value = ModelDownloadState.Downloading

        val conditions = DownloadConditions.Builder().apply {
            if (requireWifi) requireWifi()
        }.build()

        val options = TranslatorOptions.Builder()
            .setSourceLanguage(TranslateLanguage.HINDI)
            .setTargetLanguage(TranslateLanguage.ENGLISH)
            .build()
        val translator = Translation.getClient(options)
        hindiToEnglishTranslator = translator

        translator.downloadModelIfNeeded(conditions)
            .addOnSuccessListener {
                _hindiModelStatus.value = ModelDownloadState.Downloaded
                initTranslators()
                onSuccess()
            }
            .addOnFailureListener { exception ->
                val errorMsg = exception.localizedMessage ?: "Failed to download offline translation model"
                _hindiModelStatus.value = ModelDownloadState.Error(errorMsg)
                onError(errorMsg)
            }
    }

    fun deleteHindiModel(onComplete: () -> Unit = {}) {
        try {
            modelManager.deleteDownloadedModel(hindiModel)
                .addOnCompleteListener {
                    hindiToEnglishTranslator?.close()
                    hindiToEnglishTranslator = null
                    englishToHindiTranslator?.close()
                    englishToHindiTranslator = null
                    _hindiModelStatus.value = ModelDownloadState.NotDownloaded
                    onComplete()
                }
        } catch (e: Exception) {
            _hindiModelStatus.value = ModelDownloadState.NotDownloaded
            onComplete()
        }
    }

    /**
     * Translates input text offline on device.
     * Supports:
     * - Gondi -> English & Marathi
     * - Kolami -> English & Marathi
     * - Hindi -> English & Marathi
     * - Marathi -> English
     * - Hinglish -> English & Marathi
     */
    suspend fun translateText(
        inputText: String,
        sourceLang: String = "hi",
        targetLang: String = "en",
        autoPunctuate: Boolean = true
    ): Result<String> {
        val trimmed = inputText.trim()
        if (trimmed.isEmpty()) return Result.success("")

        val source = SourceLanguage.fromCode(sourceLang)
        val target = TargetLanguage.fromCode(targetLang)

        // 1. Handle Tribal Languages: Gondi & Kolami
        if (source == SourceLanguage.GONDI || source == SourceLanguage.KOLAMI) {
            val isInstalled = if (source == SourceLanguage.GONDI) {
                tribalModelManager.isGondiInstalled.first()
            } else {
                tribalModelManager.isKolamiInstalled.first()
            }

            if (!isInstalled) {
                return Result.failure(
                    IllegalStateException("${source.displayName} offline pack is not installed. Please download it from Model Setup.")
                )
            }

            return tribalEngine.translate(trimmed, source, target, autoPunctuate)
        }

        // 2. Handle Marathi source
        if (source == SourceLanguage.MARATHI) {
            val tribalResult = tribalEngine.translate(trimmed, source, target, autoPunctuate)
            if (tribalResult.isSuccess) {
                return tribalResult
            }
        }

        // 3. Handle Standard Hindi / Hinglish translation via local ML Kit
        val textToTranslate = if (sourceLang == "hinglish") {
            HinglishPreprocessor.normalizeHinglish(trimmed)
        } else {
            trimmed
        }

        val isDownloaded = isModelDownloadedSync()
        if (!isDownloaded && (sourceLang == "hi" || targetLang == "hi" || sourceLang == "hinglish")) {
            // Check if tribal lexicon can parse this directly before giving error
            val fallbackResult = tribalEngine.translate(textToTranslate, SourceLanguage.HINDI, target, autoPunctuate)
            if (fallbackResult.isSuccess) {
                return fallbackResult
            }
            return Result.failure(
                IllegalStateException("Hindi offline model is not downloaded. Please download it from the Model Setup screen.")
            )
        }

        initTranslators()

        val translator = if (sourceLang == "en" && targetLang == "hi") {
            englishToHindiTranslator ?: run {
                val opts = TranslatorOptions.Builder()
                    .setSourceLanguage(TranslateLanguage.ENGLISH)
                    .setTargetLanguage(TranslateLanguage.HINDI)
                    .build()
                Translation.getClient(opts).also { englishToHindiTranslator = it }
            }
        } else {
            hindiToEnglishTranslator ?: run {
                val opts = TranslatorOptions.Builder()
                    .setSourceLanguage(TranslateLanguage.HINDI)
                    .setTargetLanguage(TranslateLanguage.ENGLISH)
                    .build()
                Translation.getClient(opts).also { hindiToEnglishTranslator = it }
            }
        }

        return suspendCancellableCoroutine { continuation ->
            translator.translate(textToTranslate)
                .addOnSuccessListener { translatedText ->
                    val formatted = if (autoPunctuate) {
                        formatAndPunctuate(translatedText)
                    } else {
                        translatedText.trim()
                    }
                    if (continuation.isActive) {
                        continuation.resume(Result.success(formatted))
                    }
                }
                .addOnFailureListener { exception ->
                    if (continuation.isActive) {
                        continuation.resume(Result.failure(exception))
                    }
                }
        }
    }

    private suspend fun isModelDownloadedSync(): Boolean {
        return suspendCancellableCoroutine { continuation ->
            try {
                modelManager.isModelDownloaded(hindiModel)
                    .addOnSuccessListener { if (continuation.isActive) continuation.resume(it) }
                    .addOnFailureListener { if (continuation.isActive) continuation.resume(false) }
            } catch (e: Throwable) {
                if (continuation.isActive) continuation.resume(false)
            }
        }
    }

    fun formatAndPunctuate(input: String): String {
        if (input.isBlank()) return ""
        var text = input.trim()
        text = text.replace(Regex("\\s+"), " ")
        text = text.replace(Regex("\\s+([.,!?:;])"), "$1")
        text = text.replace(Regex("([.,!?:;])([A-Za-z0-9])"), "$1 $2")

        val sentences = text.split(Regex("(?<=[.!?])\\s+"))
        val formatted = sentences.joinToString(" ") { sentence ->
            val s = sentence.trim()
            if (s.isNotEmpty()) {
                s.substring(0, 1).uppercase() + s.substring(1)
            } else {
                ""
            }
        }

        return if (formatted.isNotEmpty() && !formatted.endsWith(".") &&
            !formatted.endsWith("?") && !formatted.endsWith("!")) {
            "$formatted."
        } else {
            formatted
        }
    }

    fun close() {
        hindiToEnglishTranslator?.close()
        hindiToEnglishTranslator = null
        englishToHindiTranslator?.close()
        englishToHindiTranslator = null
        marathiToEnglishTranslator?.close()
        marathiToEnglishTranslator = null
    }
}
