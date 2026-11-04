package com.example.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.TextFlowApplication
import com.example.data.db.TranslationHistoryItem
import com.example.data.translation.ModelDownloadState
import com.example.service.TextFlowOverlayService
import com.example.service.speech.SpeechRecognitionManager
import com.example.service.speech.SpeechRecognitionState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as TextFlowApplication
    private val db = app.database
    private val prefs = app.preferencesRepository
    private val translationManager = app.translationManager

    // Permission states
    private val _hasOverlayPermission = MutableStateFlow(false)
    val hasOverlayPermission: StateFlow<Boolean> = _hasOverlayPermission.asStateFlow()

    private val _hasMicPermission = MutableStateFlow(false)
    val hasMicPermission: StateFlow<Boolean> = _hasMicPermission.asStateFlow()

    private val _isFloatingServiceActive = MutableStateFlow(false)
    val isFloatingServiceActive: StateFlow<Boolean> = _isFloatingServiceActive.asStateFlow()

    // Online connectivity & Server configuration
    val isOnline: StateFlow<Boolean> = app.networkMonitor.observeIsOnline()
        .stateIn(viewModelScope, SharingStarted.Lazily, app.networkMonitor.isOnline)

    val serverUrl: StateFlow<String> = prefs.serverUrl
        .stateIn(viewModelScope, SharingStarted.Lazily, com.example.data.preferences.UserPreferencesRepository.DEFAULT_SERVER_URL)

    sealed class ServerHealthState {
        object Idle : ServerHealthState()
        object Checking : ServerHealthState()
        data class Connected(
            val appName: String,
            val version: String,
            val device: String,
            val tribalLangs: List<String>
        ) : ServerHealthState()
        data class Error(val message: String) : ServerHealthState()
    }

    private val _serverHealthState = MutableStateFlow<ServerHealthState>(ServerHealthState.Idle)
    val serverHealthState: StateFlow<ServerHealthState> = _serverHealthState.asStateFlow()

    // Model Download State
    val modelDownloadState: StateFlow<ModelDownloadState> = translationManager.hindiModelStatus

    // Preferences
    val sourceLanguage = prefs.sourceLanguage.stateIn(viewModelScope, SharingStarted.Lazily, "gon")
    val targetLanguage = prefs.targetLanguage.stateIn(viewModelScope, SharingStarted.Lazily, "en")
    val bubbleSize = prefs.bubbleSize.stateIn(viewModelScope, SharingStarted.Lazily, "medium")
    val hapticEnabled = prefs.hapticEnabled.stateIn(viewModelScope, SharingStarted.Lazily, true)
    val autoPunctuation = prefs.autoPunctuation.stateIn(viewModelScope, SharingStarted.Lazily, true)
    val onboardingCompleted = prefs.onboardingCompleted.stateIn(viewModelScope, SharingStarted.Lazily, false)
    val themeMode = prefs.themeMode.stateIn(viewModelScope, SharingStarted.Lazily, "system")

    // History & Search
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val historyItems: StateFlow<List<TranslationHistoryItem>> = _searchQuery.flatMapLatest { query ->
        if (query.isBlank()) {
            db.historyDao().getAll()
        } else {
            db.historyDao().search(query)
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // In-App Voice Testing state
    private val inAppSpeechManager = SpeechRecognitionManager(application)
    private val _inAppListening = MutableStateFlow(false)
    val inAppListening: StateFlow<Boolean> = _inAppListening.asStateFlow()

    val inAppSoundLevel: StateFlow<Float> = inAppSpeechManager.soundLevel

    private val _inAppRecognized = MutableStateFlow("")
    val inAppRecognized: StateFlow<String> = _inAppRecognized.asStateFlow()

    private val _inAppTranslated = MutableStateFlow("")
    val inAppTranslated: StateFlow<String> = _inAppTranslated.asStateFlow()

    private val _inAppStatus = MutableStateFlow("Tap microphone to test voice-to-text")
    val inAppStatus: StateFlow<String> = _inAppStatus.asStateFlow()

    private val _inAppIsTranslating = MutableStateFlow(false)
    val inAppIsTranslating: StateFlow<Boolean> = _inAppIsTranslating.asStateFlow()

    init {
        checkPermissions()
        observeInAppSpeech()
    }

    fun checkPermissions() {
        val context = getApplication<Application>()
        _hasOverlayPermission.value = Settings.canDrawOverlays(context)
        _hasMicPermission.value = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
        _isFloatingServiceActive.value = TextFlowOverlayService.isServiceRunning
    }

    fun toggleFloatingService(): Boolean {
        checkPermissions()
        val context = getApplication<Application>()

        if (!_hasOverlayPermission.value || !_hasMicPermission.value) {
            return false // Needs permission
        }

        if (TextFlowOverlayService.isServiceRunning) {
            val stopIntent = Intent(context, TextFlowOverlayService::class.java).apply {
                action = TextFlowOverlayService.ACTION_STOP
            }
            context.stopService(stopIntent)
            _isFloatingServiceActive.value = false
        } else {
            val startIntent = Intent(context, TextFlowOverlayService::class.java).apply {
                action = TextFlowOverlayService.ACTION_START
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(startIntent)
            } else {
                context.startService(startIntent)
            }
            _isFloatingServiceActive.value = true
        }
        return true
    }

    // Model Download Actions
    fun downloadOfflineModel(onSuccess: () -> Unit = {}, onError: (String) -> Unit = {}) {
        translationManager.downloadHindiModel(
            requireWifi = false,
            onSuccess = onSuccess,
            onError = onError
        )
    }

    fun deleteOfflineModel() {
        translationManager.deleteHindiModel()
    }

    // History Actions
    fun deleteHistoryItem(item: TranslationHistoryItem) {
        viewModelScope.launch(Dispatchers.IO) {
            db.historyDao().delete(item)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            db.historyDao().clearAll()
        }
    }

    fun toggleFavorite(item: TranslationHistoryItem) {
        viewModelScope.launch(Dispatchers.IO) {
            db.historyDao().updateFavorite(item.id, !item.isFavorite)
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    // In-App Voice Testing
    private fun observeInAppSpeech() {
        viewModelScope.launch {
            inAppSpeechManager.recognitionState.collect { state ->
                when (state) {
                    is SpeechRecognitionState.Idle -> {
                        _inAppListening.value = false
                        _inAppStatus.value = "Ready to test"
                    }
                    is SpeechRecognitionState.Ready -> {
                        _inAppListening.value = true
                        _inAppStatus.value = "Listening... Speak now"
                    }
                    is SpeechRecognitionState.Listening -> {
                        _inAppListening.value = true
                        _inAppStatus.value = "Listening... Speak naturally"
                    }
                    is SpeechRecognitionState.PartialResult -> {
                        _inAppRecognized.value = state.text
                        translateInAppSpeech(state.text, isFinal = false)
                    }
                    is SpeechRecognitionState.FinalResult -> {
                        _inAppListening.value = false
                        _inAppRecognized.value = state.text
                        _inAppStatus.value = "Finished recognition"
                        translateInAppSpeech(state.text, isFinal = true)
                    }
                    is SpeechRecognitionState.Error -> {
                        _inAppListening.value = false
                        _inAppStatus.value = state.message
                    }
                }
            }
        }
    }

    fun toggleInAppListening() {
        if (_inAppListening.value) {
            inAppSpeechManager.stopListening()
            _inAppListening.value = false
        } else {
            _inAppRecognized.value = ""
            _inAppTranslated.value = ""
            val lang = sourceLanguage.value
            inAppSpeechManager.startListening(
                when (lang) {
                    "hi" -> "hi-IN"
                    "hinglish" -> "hinglish"
                    else -> "en-IN"
                }
            )
        }
    }

    private fun translateInAppSpeech(text: String, isFinal: Boolean) {
        if (text.isBlank()) return
        viewModelScope.launch {
            _inAppIsTranslating.value = true
            val result = translationManager.translateText(
                inputText = text,
                sourceLang = sourceLanguage.value,
                targetLang = targetLanguage.value,
                autoPunctuate = autoPunctuation.value
            )
            result.onSuccess { output ->
                _inAppTranslated.value = output
                _inAppIsTranslating.value = false
                if (isFinal && output.isNotBlank()) {
                    withContext(Dispatchers.IO) {
                        db.historyDao().insert(
                            TranslationHistoryItem(
                                originalText = text,
                                translatedText = output,
                                sourceLanguage = sourceLanguage.value,
                                targetLanguage = targetLanguage.value
                            )
                        )
                    }
                }
            }.onFailure {
                _inAppIsTranslating.value = false
            }
        }
    }

    fun setSourceLang(lang: String) = viewModelScope.launch { prefs.setSourceLanguage(lang) }
    fun setTargetLang(lang: String) = viewModelScope.launch { prefs.setTargetLanguage(lang) }
    fun setBubbleSizePref(size: String) = viewModelScope.launch { prefs.setBubbleSize(size) }
    fun setHapticPref(enabled: Boolean) = viewModelScope.launch { prefs.setHapticEnabled(enabled) }
    fun setAutoPunctuationPref(enabled: Boolean) = viewModelScope.launch { prefs.setAutoPunctuation(enabled) }
    fun setOnboardingCompleted(completed: Boolean) = viewModelScope.launch { prefs.setOnboardingCompleted(completed) }
    fun setThemeModePref(mode: String) = viewModelScope.launch { prefs.setThemeMode(mode) }

    fun updateServerUrl(url: String) {
        viewModelScope.launch {
            prefs.setServerUrl(url)
            testServerConnection(url)
        }
    }

    fun testServerConnection(url: String? = null) {
        val targetUrl = url ?: serverUrl.value
        viewModelScope.launch {
            _serverHealthState.value = ServerHealthState.Checking
            try {
                val api = app.apiClientManager.getApi(targetUrl)
                val health = withContext(Dispatchers.IO) {
                    api.checkHealth()
                }
                _serverHealthState.value = ServerHealthState.Connected(
                    appName = health.appName,
                    version = health.version,
                    device = health.device,
                    tribalLangs = health.tribalLanguagesRegistered ?: listOf("gon", "kfb")
                )
            } catch (e: Exception) {
                _serverHealthState.value = ServerHealthState.Error(
                    e.localizedMessage ?: "Failed to connect to self-hosted server"
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        inAppSpeechManager.cancel()
    }
}
