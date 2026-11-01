package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "translation_history")
data class TranslationHistoryItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val originalText: String,
    val translatedText: String,
    val sourceLanguage: String = "hi", // "hi", "en", "hinglish"
    val targetLanguage: String = "en",
    val timestamp: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false
)
