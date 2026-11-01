package com.example.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationCompat
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.MainActivity
import com.example.R
import com.example.TextFlowApplication
import com.example.data.db.TranslationHistoryItem
import com.example.data.translation.ModelDownloadState
import com.example.service.overlay.FloatingBubbleContent
import com.example.service.overlay.FloatingMiniWindowContent
import com.example.service.overlay.OverlayLifecycleOwner
import com.example.service.speech.SpeechRecognitionManager
import com.example.service.speech.SpeechRecognitionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs

class TextFlowOverlayService : Service() {

    enum class OverlayMode {
        BUBBLE,
        EXPANDED_WINDOW
    }

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var windowManager: WindowManager? = null

    private var currentMode = OverlayMode.BUBBLE
    private var overlayLifecycleOwner: OverlayLifecycleOwner? = null

    private var bubbleView: ComposeView? = null
    private var windowView: ComposeView? = null

    private lateinit var bubbleParams: WindowManager.LayoutParams
    private lateinit var windowParams: WindowManager.LayoutParams

    private lateinit var speechManager: SpeechRecognitionManager

    // Overlay Reactive State
    private var isListening by mutableStateOf(false)
    private var soundLevel by mutableFloatStateOf(0f)
    private var statusText by mutableStateOf("Ready to speak")
    private var recognizedText by mutableStateOf("")
    private var translatedText by mutableStateOf("")
    private var sourceLanguage by mutableStateOf("hi")
    private var targetLanguage by mutableStateOf("en")
    private var isTranslating by mutableStateOf(false)
    private var isModelDownloaded by mutableStateOf(false)

    // Touch coordinate tracking for draggable bubble
    private var initialX = 0
    private var initialY = 0
    private var initialTouchX = 0f
    private var initialTouchY = 0f

    companion object {
        const val ACTION_START = "com.example.textflow.START_OVERLAY"
        const val ACTION_STOP = "com.example.textflow.STOP_OVERLAY"
        const val ACTION_TOGGLE = "com.example.textflow.TOGGLE_OVERLAY"

        var isServiceRunning = false
            private set
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        isServiceRunning = true

        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        overlayLifecycleOwner = OverlayLifecycleOwner()
        speechManager = SpeechRecognitionManager(this)

        initListeners()
        loadPreferences()
    }

    private fun loadPreferences() {
        serviceScope.launch {
            val app = TextFlowApplication.instance
            sourceLanguage = app.preferencesRepository.sourceLanguage.first()
            targetLanguage = app.preferencesRepository.targetLanguage.first()

            val pos = app.preferencesRepository.bubblePosition.first()
            initialX = pos.first
            initialY = pos.second
        }

        serviceScope.launch {
            TextFlowApplication.instance.translationManager.hindiModelStatus.collect { state ->
                isModelDownloaded = state is ModelDownloadState.Downloaded
            }
        }
    }

    private fun initListeners() {
        serviceScope.launch {
            speechManager.recognitionState.collect { state ->
                when (state) {
                    is SpeechRecognitionState.Idle -> {
                        isListening = false
                        statusText = "Ready to speak"
                    }
                    is SpeechRecognitionState.Ready -> {
                        isListening = true
                        statusText = "Listening... Speak naturally"
                    }
                    is SpeechRecognitionState.Listening -> {
                        isListening = true
                        statusText = "Listening... Speak naturally"
                    }
                    is SpeechRecognitionState.PartialResult -> {
                        isListening = true
                        recognizedText = state.text
                        statusText = "Transcribing speech..."
                        // Trigger fast partial translation
                        translateCurrentSpeech(state.text)
                    }
                    is SpeechRecognitionState.FinalResult -> {
                        isListening = false
                        recognizedText = state.text
                        statusText = "Speech recognized"
                        translateCurrentSpeech(state.text, isFinal = true)
                    }
                    is SpeechRecognitionState.Error -> {
                        isListening = false
                        statusText = state.message
                    }
                }
            }
        }

        serviceScope.launch {
            speechManager.soundLevel.collect { level ->
                soundLevel = level
            }
        }
    }

    private fun translateCurrentSpeech(text: String, isFinal: Boolean = false) {
        if (text.isBlank()) return
        serviceScope.launch {
            isTranslating = true
            val app = TextFlowApplication.instance
            val autoPunctuate = app.preferencesRepository.autoPunctuation.first()
            val result = app.translationManager.translateText(
                inputText = text,
                sourceLang = sourceLanguage,
                targetLang = targetLanguage,
                autoPunctuate = autoPunctuate
            )

            result.onSuccess { output ->
                translatedText = output
                isTranslating = false
                if (isFinal && output.isNotBlank()) {
                    // Save to Room Database
                    val historyItem = TranslationHistoryItem(
                        originalText = text,
                        translatedText = output,
                        sourceLanguage = sourceLanguage,
                        targetLanguage = targetLanguage
                    )
                    withContext(Dispatchers.IO) {
                        app.database.historyDao().insert(historyItem)
                    }
                }
            }.onFailure { ex ->
                isTranslating = false
                if (translatedText.isBlank()) {
                    translatedText = text // fallback preview
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopSelf()
            return START_NOT_STICKY
        }

        startForegroundServiceNotification()

        if (Settings.canDrawOverlays(this)) {
            showBubble()
        } else {
            Toast.makeText(this, "Overlay permission required to display TextFlow", Toast.LENGTH_LONG).show()
            stopSelf()
            return START_NOT_STICKY
        }

        return START_STICKY
    }

    private fun startForegroundServiceNotification() {
        val stopIntent = Intent(this, TextFlowOverlayService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            1,
            stopIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            this,
            2,
            openAppIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification: Notification = NotificationCompat.Builder(
            this,
            TextFlowApplication.OVERLAY_NOTIFICATION_CHANNEL_ID
        )
            .setContentTitle("TextFlow Floating Overlay Active")
            .setContentText("Tap the floating microphone bubble to dictate & translate")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(openAppPendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop Service", stopPendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                TextFlowApplication.NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
            )
        } else {
            startForeground(TextFlowApplication.NOTIFICATION_ID, notification)
        }
    }

    private fun showBubble() {
        if (!Settings.canDrawOverlays(this)) return

        removeViews()
        currentMode = OverlayMode.BUBBLE

        val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        bubbleParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = initialX.coerceAtLeast(30)
            y = initialY.coerceAtLeast(150)
        }

        bubbleView = ComposeView(this).apply {
            overlayLifecycleOwner?.let { owner ->
                setViewTreeLifecycleOwner(owner)
                setViewTreeViewModelStoreOwner(owner)
                setViewTreeSavedStateRegistryOwner(owner)
            }

            setContent {
                val app = TextFlowApplication.instance
                val bubbleSizePref by app.preferencesRepository.bubbleSize.collectAsState(initial = "medium")
                val bubbleDp = when (bubbleSizePref) {
                    "small" -> 46.dp
                    "large" -> 66.dp
                    else -> 56.dp
                }

                FloatingBubbleContent(
                    bubbleSize = bubbleDp,
                    isListening = isListening,
                    onClick = { expandToMiniWindow() }
                )
            }

            setOnTouchListener(createBubbleTouchListener())
        }

        try {
            windowManager?.addView(bubbleView, bubbleParams)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun createBubbleTouchListener(): View.OnTouchListener {
        return object : View.OnTouchListener {
            private var isDragging = false

            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                if (event == null) return false

                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = bubbleParams.x
                        initialY = bubbleParams.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        isDragging = false
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val dx = (event.rawX - initialTouchX).toInt()
                        val dy = (event.rawY - initialTouchY).toInt()

                        if (abs(dx) > 10 || abs(dy) > 10) {
                            isDragging = true
                            bubbleParams.x = initialX + dx
                            bubbleParams.y = initialY + dy
                            try {
                                windowManager?.updateViewLayout(bubbleView, bubbleParams)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                        return true
                    }
                    MotionEvent.ACTION_UP -> {
                        if (!isDragging) {
                            // User tapped the bubble!
                            triggerHapticFeedback()
                            expandToMiniWindow()
                        } else {
                            // Save new position
                            serviceScope.launch {
                                TextFlowApplication.instance.preferencesRepository
                                    .saveBubblePosition(bubbleParams.x, bubbleParams.y)
                            }
                        }
                        return true
                    }
                }
                return false
            }
        }
    }

    private fun expandToMiniWindow() {
        if (!Settings.canDrawOverlays(this)) return

        removeViews()
        currentMode = OverlayMode.EXPANDED_WINDOW

        val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        windowParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.CENTER_HORIZONTAL or Gravity.TOP
            y = (bubbleParams.y - 40).coerceAtLeast(100)
        }

        windowView = ComposeView(this).apply {
            overlayLifecycleOwner?.let { owner ->
                setViewTreeLifecycleOwner(owner)
                setViewTreeViewModelStoreOwner(owner)
                setViewTreeSavedStateRegistryOwner(owner)
            }

            setContent {
                FloatingMiniWindowContent(
                    isListening = isListening,
                    soundLevel = soundLevel,
                    statusText = statusText,
                    recognizedText = recognizedText,
                    translatedText = translatedText,
                    sourceLanguage = sourceLanguage,
                    targetLanguage = targetLanguage,
                    isModelDownloaded = isModelDownloaded,
                    isTranslating = isTranslating,
                    onToggleListening = { toggleListening() },
                    onSourceLangChange = { lang ->
                        sourceLanguage = lang
                        serviceScope.launch {
                            TextFlowApplication.instance.preferencesRepository.setSourceLanguage(lang)
                        }
                        if (recognizedText.isNotBlank()) {
                            translateCurrentSpeech(recognizedText, isFinal = true)
                        }
                    },
                    onTargetLangChange = { lang ->
                        targetLanguage = lang
                        serviceScope.launch {
                            TextFlowApplication.instance.preferencesRepository.setTargetLanguage(lang)
                        }
                        if (recognizedText.isNotBlank()) {
                            translateCurrentSpeech(recognizedText, isFinal = true)
                        }
                    },
                    onTranslatedTextEdited = { newText ->
                        translatedText = newText
                    },
                    onCopyText = { text ->
                        copyToClipboard(text)
                    },
                    onShareText = { text ->
                        shareText(text)
                    },
                    onClearAll = {
                        recognizedText = ""
                        translatedText = ""
                        statusText = "Cleared"
                        speechManager.cancel()
                    },
                    onMinimize = {
                        minimizeToBubble()
                    },
                    onClose = {
                        stopSelf()
                    },
                    onDownloadModel = {
                        startModelDownload()
                    },
                    onRequestImeFocus = {
                        enableImeFocus()
                    }
                )
            }
        }

        try {
            windowManager?.addView(windowView, windowParams)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun enableImeFocus() {
        try {
            windowParams.flags = windowParams.flags and WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE.inv()
            windowManager?.updateViewLayout(windowView, windowParams)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun minimizeToBubble() {
        speechManager.stopListening()
        showBubble()
    }

    private fun toggleListening() {
        triggerHapticFeedback()
        if (isListening) {
            speechManager.stopListening()
            isListening = false
            statusText = "Speech processing..."
        } else {
            recognizedText = ""
            translatedText = ""
            speechManager.startListening(
                when (sourceLanguage) {
                    "hi" -> "hi-IN"
                    "hinglish" -> "hinglish"
                    else -> "en-IN"
                }
            )
        }
    }

    private fun copyToClipboard(text: String) {
        triggerHapticFeedback()
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("TextFlow", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this, "Copied to clipboard! Ready to paste.", Toast.LENGTH_SHORT).show()
    }

    private fun shareText(text: String) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(Intent.createChooser(shareIntent, "Share translated text").apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        })
    }

    private fun startModelDownload() {
        statusText = "Downloading Hindi offline model (~30MB)..."
        TextFlowApplication.instance.translationManager.downloadHindiModel(
            requireWifi = false,
            onSuccess = {
                statusText = "Model downloaded! Offline conversion ready"
                isModelDownloaded = true
                if (recognizedText.isNotBlank()) {
                    translateCurrentSpeech(recognizedText, isFinal = true)
                }
            },
            onError = { err ->
                statusText = "Model download failed: $err"
            }
        )
    }

    private fun triggerHapticFeedback() {
        serviceScope.launch {
            if (TextFlowApplication.instance.preferencesRepository.hapticEnabled.first()) {
                val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val manager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                    manager.defaultVibrator
                } else {
                    @Suppress("DEPRECATION")
                    getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(30)
                }
            }
        }
    }

    private fun removeViews() {
        try {
            bubbleView?.let {
                windowManager?.removeView(it)
                bubbleView = null
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            windowView?.let {
                windowManager?.removeView(it)
                windowView = null
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        isServiceRunning = false
        speechManager.cancel()
        removeViews()
        overlayLifecycleOwner?.destroy()
        overlayLifecycleOwner = null
        serviceScope.cancel()
    }
}
