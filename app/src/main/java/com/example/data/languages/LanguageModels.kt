package com.example.data.languages

/**
 * Representation of supported source languages in TextFlow,
 * with first-class support for tribal languages Gondi and Kolami.
 */
enum class SourceLanguage(
    val code: String,
    val displayName: String,
    val nativeScript: String,
    val isTribal: Boolean,
    val region: String,
    val speechModelName: String,
    val estimatedModelSizeMb: Int
) {
    GONDI(
        code = "gon",
        displayName = "Gondi",
        nativeScript = "गोण्डी (Gondi)",
        isTribal = true,
        region = "Vidarbha (MH), MP, Telangana, CG",
        speechModelName = "Gondi Central Dravidian ASR Pack",
        estimatedModelSizeMb = 18
    ),
    KOLAMI(
        code = "kfb",
        displayName = "Kolami",
        nativeScript = "कोलामी (Kolami)",
        isTribal = true,
        region = "Yavatmal, Chandrapur (MH) & Adilabad (TS)",
        speechModelName = "Kolami Tribal Acoustic & Lexicon Pack",
        estimatedModelSizeMb = 14
    ),
    HINDI(
        code = "hi",
        displayName = "Hindi",
        nativeScript = "हिन्दी",
        isTribal = false,
        region = "Pan-India",
        speechModelName = "Android Standard Hindi ASR",
        estimatedModelSizeMb = 30
    ),
    MARATHI(
        code = "mr",
        displayName = "Marathi",
        nativeScript = "मराठी",
        isTribal = false,
        region = "Maharashtra",
        speechModelName = "Android Standard Marathi ASR",
        estimatedModelSizeMb = 30
    ),
    ENGLISH(
        code = "en",
        displayName = "English",
        nativeScript = "English",
        isTribal = false,
        region = "Global / Indian English",
        speechModelName = "Android Standard English ASR",
        estimatedModelSizeMb = 0
    ),
    HINGLISH(
        code = "hinglish",
        displayName = "Hinglish",
        nativeScript = "Hinglish (Mix)",
        isTribal = false,
        region = "Urban India",
        speechModelName = "Hinglish Speech Normalizer",
        estimatedModelSizeMb = 0
    );

    companion object {
        fun fromCode(code: String): SourceLanguage {
            return entries.find { it.code.equals(code, ignoreCase = true) } ?: HINDI
        }
    }
}

/**
 * Output languages requested by the user:
 * - English
 * - Marathi
 */
enum class TargetLanguage(
    val code: String,
    val displayName: String,
    val nativeScript: String
) {
    ENGLISH(
        code = "en",
        displayName = "English",
        nativeScript = "English"
    ),
    MARATHI(
        code = "mr",
        displayName = "Marathi",
        nativeScript = "मराठी"
    );

    companion object {
        fun fromCode(code: String): TargetLanguage {
            return entries.find { it.code.equals(code, ignoreCase = true) } ?: ENGLISH
        }
    }
}
