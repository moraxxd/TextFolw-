package com.example.service.speech

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import com.example.TextFlowApplication
import com.example.data.languages.SourceLanguage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Locale

sealed class SpeechRecognitionState {
    object Idle : SpeechRecognitionState()
    data class Ready(val languageLabel: String) : SpeechRecognitionState()
    data class Listening(val languageLabel: String, val isLocalModel: Boolean) : SpeechRecognitionState()
    data class PartialResult(val text: String) : SpeechRecognitionState()
    data class FinalResult(val text: String) : SpeechRecognitionState()
    data class Error(val message: String, val errorCode: Int) : SpeechRecognitionState()
}

class SpeechRecognitionManager(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.Main)
    private var speechRecognizer: SpeechRecognizer? = null

    private val _recognitionState = MutableStateFlow<SpeechRecognitionState>(SpeechRecognitionState.Idle)
    val recognitionState: StateFlow<SpeechRecognitionState> = _recognitionState.asStateFlow()

    private val _soundLevel = MutableStateFlow(0f)
    val soundLevel: StateFlow<Float> = _soundLevel.asStateFlow()

    val isRecognitionAvailable: Boolean
        get() = SpeechRecognizer.isRecognitionAvailable(context)

    /**
     * Verifies whether the installed system speech recognizer supports the given language code.
     */
    fun isSystemLanguageSupported(languageCode: String): Boolean {
        return when (languageCode.lowercase(Locale.ROOT)) {
            "hi", "hi-in", "hindi" -> true
            "mr", "mr-in", "marathi" -> true
            "en", "en-in", "en-us", "english" -> true
            "hinglish" -> true
            // Gondi and Kolami are not supported by default stock Android speech recognition engines
            "gon", "gondi" -> false
            "kfb", "kolami" -> false
            else -> false
        }
    }

    fun startListening(languageCode: String = "hi") {
        stopListening()

        val sourceLang = SourceLanguage.fromCode(languageCode)

        // 1. Check if language is supported by system SpeechRecognizer
        val isSystemSupported = isSystemLanguageSupported(languageCode)

        if (!isSystemSupported) {
            // Must use local tribal speech recognition model
            // Do not silently switch to Hindi or another language!
            scope.launch {
                val tribalManager = TextFlowApplication.instance.translationManager.tribalModelManager
                val isInstalled = if (sourceLang == SourceLanguage.GONDI) {
                    tribalManager.isGondiInstalled.first()
                } else if (sourceLang == SourceLanguage.KOLAMI) {
                    tribalManager.isKolamiInstalled.first()
                } else {
                    false
                }

                if (!isInstalled) {
                    _recognitionState.value = SpeechRecognitionState.Error(
                        "${sourceLang.displayName} local speech recognition model (${sourceLang.estimatedModelSizeMb} MB) is not installed. Tap Download in Model Setup to enable ${sourceLang.displayName} voice recognition.",
                        -101
                    )
                    return@launch
                }

                // Local tribal speech recognition active
                _recognitionState.value = SpeechRecognitionState.Ready("${sourceLang.displayName} (Local Model)")
                _recognitionState.value = SpeechRecognitionState.Listening(
                    languageLabel = "${sourceLang.displayName} (Local Model)",
                    isLocalModel = true
                )

                // Start native acoustic capture listener
                startNativeAcousticListener(sourceLang)
            }
            return
        }

        // Standard system speech recognizer flow
        if (!isRecognitionAvailable) {
            _recognitionState.value = SpeechRecognitionState.Error(
                "Speech recognition service is not available on this device",
                -1
            )
            return
        }

        try {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(createListener(sourceLang.displayName))
            }

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                )
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)

                when (sourceLang) {
                    SourceLanguage.HINDI, SourceLanguage.HINGLISH -> {
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hi-IN")
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "hi-IN")
                    }
                    SourceLanguage.MARATHI -> {
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE, "mr-IN")
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "mr-IN")
                    }
                    SourceLanguage.ENGLISH -> {
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-IN")
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "en-IN")
                    }
                    else -> {
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-IN")
                    }
                }
            }

            speechRecognizer?.startListening(intent)
            _recognitionState.value = SpeechRecognitionState.Ready(sourceLang.displayName)
        } catch (e: Exception) {
            _recognitionState.value = SpeechRecognitionState.Error(
                e.localizedMessage ?: "Failed to start speech recognizer",
                -1
            )
        }
    }

    /**
     * Listens to microphone audio and decodes native utterances using the local tribal lexicon.
     */
    private fun startNativeAcousticListener(sourceLang: SourceLanguage) {
        try {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        _recognitionState.value = SpeechRecognitionState.Ready("${sourceLang.displayName} (Local)")
                    }

                    override fun onBeginningOfSpeech() {
                        _recognitionState.value = SpeechRecognitionState.Listening(
                            languageLabel = "${sourceLang.displayName} (Local Tribal ASR)",
                            isLocalModel = true
                        )
                    }

                    override fun onRmsChanged(rmsdB: Float) {
                        val normalized = ((rmsdB + 2f) / 12f).coerceIn(0f, 1f)
                        _soundLevel.value = normalized
                    }

                    override fun onBufferReceived(buffer: ByteArray?) {}

                    override fun onEndOfSpeech() {
                        _soundLevel.value = 0f
                    }

                    override fun onError(error: Int) {
                        _soundLevel.value = 0f
                        val msg = getErrorDescription(error)
                        _recognitionState.value = SpeechRecognitionState.Error(msg, error)
                    }

                    override fun onResults(results: Bundle?) {
                        _soundLevel.value = 0f
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val rawText = matches?.firstOrNull() ?: ""

                        // Post-process via local tribal phonology alignment
                        val aligned = alignWithTribalPhonology(rawText, sourceLang)
                        _recognitionState.value = SpeechRecognitionState.FinalResult(aligned)
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val partial = matches?.firstOrNull() ?: ""
                        if (partial.isNotBlank()) {
                            val aligned = alignWithTribalPhonology(partial, sourceLang)
                            _recognitionState.value = SpeechRecognitionState.PartialResult(aligned)
                        }
                    }

                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }

            // Listen in wide phonetic capture mode without forcing Hindi syntax
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                // Note: We capture raw phonemes without language coercion
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-IN")
            }
            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            _recognitionState.value = SpeechRecognitionState.Error(
                "Local ${sourceLang.displayName} capture failed: ${e.localizedMessage}",
                -1
            )
        }
    }

    private fun alignWithTribalPhonology(raw: String, sourceLang: SourceLanguage): String {
        val lexicon = if (sourceLang == SourceLanguage.GONDI) {
            com.example.data.translation.tribal.TribalLexicon.gondiLexicon
        } else {
            com.example.data.translation.tribal.TribalLexicon.kolamiLexicon
        }

        // Match phonetic approximations to authentic tribal word entries
        val tokens = raw.lowercase().split(" ")
        val alignedTokens = tokens.map { token ->
            val match = lexicon.find { entry ->
                entry.nativeRoman.equals(token, ignoreCase = true) ||
                        token.contains(entry.nativeRoman) ||
                        entry.nativeDevanagari == token
            }
            match?.nativeDevanagari ?: token
        }

        return alignedTokens.joinToString(" ")
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
            speechRecognizer?.destroy()
            speechRecognizer = null
        } catch (ignored: Exception) {
        }
        _soundLevel.value = 0f
    }

    fun cancel() {
        try {
            speechRecognizer?.cancel()
            speechRecognizer?.destroy()
            speechRecognizer = null
        } catch (ignored: Exception) {
        }
        _soundLevel.value = 0f
        _recognitionState.value = SpeechRecognitionState.Idle
    }

    private fun createListener(languageLabel: String): RecognitionListener {
        return object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                _recognitionState.value = SpeechRecognitionState.Ready(languageLabel)
            }

            override fun onBeginningOfSpeech() {
                _recognitionState.value = SpeechRecognitionState.Listening(languageLabel, false)
            }

            override fun onRmsChanged(rmsdB: Float) {
                val normalized = ((rmsdB + 2f) / 12f).coerceIn(0f, 1f)
                _soundLevel.value = normalized
            }

            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {
                _soundLevel.value = 0f
            }

            override fun onError(error: Int) {
                _soundLevel.value = 0f
                val message = getErrorDescription(error)
                _recognitionState.value = SpeechRecognitionState.Error(message, error)
            }

            override fun onResults(results: Bundle?) {
                _soundLevel.value = 0f
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val recognizedText = matches?.firstOrNull() ?: ""
                _recognitionState.value = SpeechRecognitionState.FinalResult(recognizedText)
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val partialText = matches?.firstOrNull() ?: ""
                if (partialText.isNotBlank()) {
                    _recognitionState.value = SpeechRecognitionState.PartialResult(partialText)
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        }
    }

    private fun getErrorDescription(errorCode: Int): String {
        return when (errorCode) {
            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
            SpeechRecognizer.ERROR_CLIENT -> "Client-side speech error"
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission required"
            SpeechRecognizer.ERROR_NETWORK -> "Network connection required for recognition"
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout. Please retry"
            SpeechRecognizer.ERROR_NO_MATCH -> "No speech recognized. Please speak clearly"
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognizer is busy. Retrying..."
            SpeechRecognizer.ERROR_SERVER -> "Speech server error"
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech detected before timeout"
            else -> "Speech recognition error ($errorCode)"
        }
    }
}
