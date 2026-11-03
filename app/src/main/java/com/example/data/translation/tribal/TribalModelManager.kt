package com.example.data.translation.tribal

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.example.data.preferences.dataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.io.File

sealed class TribalDownloadStatus {
    object Idle : TribalDownloadStatus()
    data class Downloading(val progress: Float) : TribalDownloadStatus()
    object Installed : TribalDownloadStatus()
    data class Error(val message: String) : TribalDownloadStatus()
}

class TribalModelManager(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.IO)

    companion object {
        val KEY_GONDI_INSTALLED = booleanPreferencesKey("tribal_gondi_installed")
        val KEY_KOLAMI_INSTALLED = booleanPreferencesKey("tribal_kolami_installed")
    }

    private val _gondiStatus = MutableStateFlow<TribalDownloadStatus>(TribalDownloadStatus.Idle)
    val gondiStatus: StateFlow<TribalDownloadStatus> = _gondiStatus.asStateFlow()

    private val _kolamiStatus = MutableStateFlow<TribalDownloadStatus>(TribalDownloadStatus.Idle)
    val kolamiStatus: StateFlow<TribalDownloadStatus> = _kolamiStatus.asStateFlow()

    val isGondiInstalled: Flow<Boolean> = context.dataStore.data.map {
        it[KEY_GONDI_INSTALLED] ?: true // Pack is bundled by default for immediate native access
    }

    val isKolamiInstalled: Flow<Boolean> = context.dataStore.data.map {
        it[KEY_KOLAMI_INSTALLED] ?: true // Pack is bundled by default for immediate native access
    }

    init {
        checkInstalledModels()
    }

    private fun checkInstalledModels() {
        scope.launch {
            isGondiInstalled.collect { installed ->
                _gondiStatus.value = if (installed) TribalDownloadStatus.Installed else TribalDownloadStatus.Idle
            }
        }
        scope.launch {
            isKolamiInstalled.collect { installed ->
                _kolamiStatus.value = if (installed) TribalDownloadStatus.Installed else TribalDownloadStatus.Idle
            }
        }
    }

    fun downloadGondiPack(onSuccess: () -> Unit = {}, onError: (String) -> Unit = {}) {
        scope.launch {
            _gondiStatus.value = TribalDownloadStatus.Downloading(0.1f)
            try {
                // Simulate progressive local storage allocation of 18 MB model weights
                for (step in 1..5) {
                    delay(300)
                    _gondiStatus.value = TribalDownloadStatus.Downloading(step * 0.2f)
                }

                // Write marker to internal storage
                val modelFile = File(context.filesDir, "models/gondi_dravidian_v1.bin")
                modelFile.parentFile?.mkdirs()
                if (!modelFile.exists()) {
                    modelFile.writeText("GONDI_CENTRAL_DRAVIDIAN_ASR_LEXICON_V1")
                }

                context.dataStore.edit { it[KEY_GONDI_INSTALLED] = true }
                _gondiStatus.value = TribalDownloadStatus.Installed
                onSuccess()
            } catch (e: Exception) {
                _gondiStatus.value = TribalDownloadStatus.Error(e.localizedMessage ?: "Download failed")
                onError(e.localizedMessage ?: "Download failed")
            }
        }
    }

    fun downloadKolamiPack(onSuccess: () -> Unit = {}, onError: (String) -> Unit = {}) {
        scope.launch {
            _kolamiStatus.value = TribalDownloadStatus.Downloading(0.1f)
            try {
                for (step in 1..5) {
                    delay(300)
                    _kolamiStatus.value = TribalDownloadStatus.Downloading(step * 0.2f)
                }

                val modelFile = File(context.filesDir, "models/kolami_tribal_v1.bin")
                modelFile.parentFile?.mkdirs()
                if (!modelFile.exists()) {
                    modelFile.writeText("KOLAMI_YAVATMAL_ADILABAD_ASR_LEXICON_V1")
                }

                context.dataStore.edit { it[KEY_KOLAMI_INSTALLED] = true }
                _kolamiStatus.value = TribalDownloadStatus.Installed
                onSuccess()
            } catch (e: Exception) {
                _kolamiStatus.value = TribalDownloadStatus.Error(e.localizedMessage ?: "Download failed")
                onError(e.localizedMessage ?: "Download failed")
            }
        }
    }

    fun deleteGondiPack() {
        scope.launch {
            val modelFile = File(context.filesDir, "models/gondi_dravidian_v1.bin")
            if (modelFile.exists()) modelFile.delete()
            context.dataStore.edit { it[KEY_GONDI_INSTALLED] = false }
            _gondiStatus.value = TribalDownloadStatus.Idle
        }
    }

    fun deleteKolamiPack() {
        scope.launch {
            val modelFile = File(context.filesDir, "models/kolami_tribal_v1.bin")
            if (modelFile.exists()) modelFile.delete()
            context.dataStore.edit { it[KEY_KOLAMI_INSTALLED] = false }
            _kolamiStatus.value = TribalDownloadStatus.Idle
        }
    }
}
