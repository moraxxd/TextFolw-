package com.example.service.speech

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

sealed class SpeechRecognitionState {
    object Idle : SpeechRecognitionState()
    object Ready : SpeechRecognitionState()
    object Listening : SpeechRecognitionState()
    data class PartialResult(val text: String) : SpeechRecognitionState()
    data class FinalResult(val text: String) : SpeechRecognitionState()
    data class Error(val message: String, val errorCode: Int) : SpeechRecognitionState()
}

class SpeechRecognitionManager(private val context: Context) {

    private var speechRecognizer: SpeechRecognizer? = null

    private val _recognitionState = MutableStateFlow<SpeechRecognitionState>(SpeechRecognitionState.Idle)
    val recognitionState: StateFlow<SpeechRecognitionState> = _recognitionState.asStateFlow()

    private val _soundLevel = MutableStateFlow(0f)
    val soundLevel: StateFlow<Float> = _soundLevel.asStateFlow()

    val isRecognitionAvailable: Boolean
        get() = SpeechRecognizer.isRecognitionAvailable(context)

    fun startListening(languageCode: String = "hi-IN") {
        stopListening()

        if (!isRecognitionAvailable) {
            _recognitionState.value = SpeechRecognitionState.Error(
                "Speech recognition service is not available on this device",
                -1
            )
            return
        }

        try {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(createListener())
            }

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                )
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)

                when (languageCode.lowercase(Locale.ROOT)) {
                    "hi", "hi-in", "hindi" -> {
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hi-IN")
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "hi-IN")
                    }
                    "hinglish" -> {
                        // For Hinglish, use hi-IN with mixed vocabulary recognition
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hi-IN")
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "hi-IN")
                    }
                    "en", "en-in", "english" -> {
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-IN")
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "en-IN")
                    }
                    else -> {
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageCode)
                    }
                }
            }

            speechRecognizer?.startListening(intent)
            _recognitionState.value = SpeechRecognitionState.Ready
        } catch (e: Exception) {
            _recognitionState.value = SpeechRecognitionState.Error(
                e.localizedMessage ?: "Failed to start speech recognizer",
                -1
            )
        }
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

    private fun createListener(): RecognitionListener {
        return object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                _recognitionState.value = SpeechRecognitionState.Ready
            }

            override fun onBeginningOfSpeech() {
                _recognitionState.value = SpeechRecognitionState.Listening
            }

            override fun onRmsChanged(rmsdB: Float) {
                // rmsdB usually varies from -2 to 10
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
