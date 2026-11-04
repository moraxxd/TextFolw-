package com.example.data.audio

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException

class AudioRecorderManager(private val context: Context) {

    private var mediaRecorder: MediaRecorder? = null
    private var currentOutputFile: File? = null
    private var isCurrentlyRecording = false

    private val _soundLevel = MutableStateFlow(0f)
    val soundLevel: StateFlow<Float> = _soundLevel.asStateFlow()

    private var amplitudePollJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    fun startRecording(): File? {
        if (isCurrentlyRecording) {
            stopRecording()
        }

        val cacheDir = context.cacheDir
        val audioFile = File(cacheDir, "textflow_record_${System.currentTimeMillis()}.m4a")
        currentOutputFile = audioFile

        try {
            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioSamplingRate(16000)
                setAudioEncodingBitRate(64000)
                setOutputFile(audioFile.absolutePath)
                prepare()
                start()
            }

            isCurrentlyRecording = true

            // Start sound level polling job for smooth waveform animation
            amplitudePollJob?.cancel()
            amplitudePollJob = scope.launch {
                while (isActive && isCurrentlyRecording) {
                    try {
                        val maxAmp = mediaRecorder?.maxAmplitude ?: 0
                        // Normalize 0 - 32767 to 0.0 - 1.0
                        val normalized = (maxAmp / 32767f).coerceIn(0f, 1f)
                        _soundLevel.value = normalized
                    } catch (e: Exception) {
                        _soundLevel.value = 0f
                    }
                    delay(50) // 20 FPS sound level updates
                }
            }

            return audioFile
        } catch (e: IOException) {
            Log.e("AudioRecorderManager", "MediaRecorder prepare() failed", e)
            cleanUp()
            return null
        } catch (e: Exception) {
            Log.e("AudioRecorderManager", "MediaRecorder start() failed", e)
            cleanUp()
            return null
        }
    }

    fun stopRecording(): File? {
        amplitudePollJob?.cancel()
        _soundLevel.value = 0f

        if (!isCurrentlyRecording) {
            return currentOutputFile
        }

        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
        } catch (e: Exception) {
            Log.e("AudioRecorderManager", "Error stopping MediaRecorder", e)
        } finally {
            mediaRecorder = null
            isCurrentlyRecording = false
        }

        return currentOutputFile
    }

    private fun cleanUp() {
        try {
            mediaRecorder?.release()
        } catch (e: Exception) {
            // ignore
        }
        mediaRecorder = null
        isCurrentlyRecording = false
        amplitudePollJob?.cancel()
        _soundLevel.value = 0f
    }
}
