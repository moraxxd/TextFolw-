package com.example

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.example.data.db.AppDatabase
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.translation.OfflineTranslationManager

class TextFlowApplication : Application() {

    lateinit var database: AppDatabase
        private set
    lateinit var preferencesRepository: UserPreferencesRepository
        private set
    lateinit var translationManager: OfflineTranslationManager
        private set

    companion object {
        const val OVERLAY_NOTIFICATION_CHANNEL_ID = "textflow_overlay_channel"
        const val NOTIFICATION_ID = 1001

        lateinit var instance: TextFlowApplication
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        database = AppDatabase.getInstance(this)
        preferencesRepository = UserPreferencesRepository(this)
        translationManager = OfflineTranslationManager(this)

        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                OVERLAY_NOTIFICATION_CHANNEL_ID,
                "TextFlow Floating Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Keeps the TextFlow floating microphone bubble active over apps"
                setShowBadge(false)
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }
    }
}
