package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "textflow_preferences")

class UserPreferencesRepository(private val context: Context) {

    companion object {
        val KEY_SOURCE_LANG = stringPreferencesKey("source_lang")
        val KEY_TARGET_LANG = stringPreferencesKey("target_lang")
        val KEY_BUBBLE_SIZE = stringPreferencesKey("bubble_size")
        val KEY_HAPTIC_ENABLED = booleanPreferencesKey("haptic_enabled")
        val KEY_AUTO_PUNCTUATION = booleanPreferencesKey("auto_punctuation")
        val KEY_ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val KEY_BUBBLE_X = intPreferencesKey("bubble_x")
        val KEY_BUBBLE_Y = intPreferencesKey("bubble_y")
        val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
    }

    val sourceLanguage: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_SOURCE_LANG] ?: "hi" // "hi" for Hindi/Hinglish
    }

    val targetLanguage: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_TARGET_LANG] ?: "en"
    }

    val bubbleSize: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_BUBBLE_SIZE] ?: "medium" // "small", "medium", "large"
    }

    val hapticEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_HAPTIC_ENABLED] ?: true
    }

    val autoPunctuation: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_AUTO_PUNCTUATION] ?: true
    }

    val onboardingCompleted: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_ONBOARDING_COMPLETED] ?: false
    }

    val bubblePosition: Flow<Pair<Int, Int>> = context.dataStore.data.map { prefs ->
        val x = prefs[KEY_BUBBLE_X] ?: 100
        val y = prefs[KEY_BUBBLE_Y] ?: 300
        Pair(x, y)
    }

    val themeMode: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_THEME_MODE] ?: "system"
    }

    suspend fun setSourceLanguage(lang: String) {
        context.dataStore.edit { it[KEY_SOURCE_LANG] = lang }
    }

    suspend fun setTargetLanguage(lang: String) {
        context.dataStore.edit { it[KEY_TARGET_LANG] = lang }
    }

    suspend fun setBubbleSize(size: String) {
        context.dataStore.edit { it[KEY_BUBBLE_SIZE] = size }
    }

    suspend fun setHapticEnabled(enabled: Boolean) {
        context.dataStore.edit { it[KEY_HAPTIC_ENABLED] = enabled }
    }

    suspend fun setAutoPunctuation(enabled: Boolean) {
        context.dataStore.edit { it[KEY_AUTO_PUNCTUATION] = enabled }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { it[KEY_ONBOARDING_COMPLETED] = completed }
    }

    suspend fun saveBubblePosition(x: Int, y: Int) {
        context.dataStore.edit {
            it[KEY_BUBBLE_X] = x
            it[KEY_BUBBLE_Y] = y
        }
    }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { it[KEY_THEME_MODE] = mode }
    }
}
