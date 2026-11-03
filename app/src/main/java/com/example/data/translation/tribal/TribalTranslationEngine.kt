package com.example.data.translation.tribal

import com.example.data.languages.SourceLanguage
import com.example.data.languages.TargetLanguage

class TribalTranslationEngine {

    /**
     * Translates native tribal speech (Gondi, Kolami) or Marathi into English or Marathi.
     */
    fun translate(
        inputText: String,
        source: SourceLanguage,
        target: TargetLanguage,
        autoPunctuate: Boolean = true
    ): Result<String> {
        val trimmed = inputText.trim()
        if (trimmed.isEmpty()) return Result.success("")

        val lexicon = when (source) {
            SourceLanguage.GONDI -> TribalLexicon.gondiLexicon
            SourceLanguage.KOLAMI -> TribalLexicon.kolamiLexicon
            SourceLanguage.MARATHI -> TribalLexicon.marathiLexicon
            else -> emptyList()
        }

        // 1. Check direct phrase match (Devanagari or Romanized)
        val lowerInput = trimmed.lowercase()
        val directMatch = lexicon.find { entry ->
            entry.nativeDevanagari.equals(trimmed, ignoreCase = true) ||
                    entry.nativeRoman.equals(lowerInput, ignoreCase = true) ||
                    trimmed.contains(entry.nativeDevanagari, ignoreCase = true) && entry.partOfSpeech == "phrase"
        }

        if (directMatch != null) {
            val output = if (target == TargetLanguage.MARATHI) directMatch.marathi else directMatch.english
            return Result.success(formatOutput(output, autoPunctuate))
        }

        // 2. Token-level morphological translation
        val tokens = trimmed.split(Regex("[\\s,!?]+")).filter { it.isNotBlank() }
        val translatedTokens = mutableListOf<String>()
        var matchedTokensCount = 0

        for (token in tokens) {
            val tokenClean = token.lowercase()
            val match = lexicon.find { entry ->
                entry.nativeDevanagari.equals(token, ignoreCase = true) ||
                        entry.nativeRoman.equals(tokenClean, ignoreCase = true) ||
                        token.startsWith(entry.nativeDevanagari)
            }

            if (match != null) {
                matchedTokensCount++
                val word = if (target == TargetLanguage.MARATHI) match.marathi else match.english
                translatedTokens.add(word)
            } else {
                // Keep token or identify
                translatedTokens.add(token)
            }
        }

        if (matchedTokensCount == 0 && (source == SourceLanguage.GONDI || source == SourceLanguage.KOLAMI)) {
            // Requirement: "If no reliable model exists for a language, clearly show that support is not yet available.
            // Never generate fake translations or pretend that an unsupported language is working."
            return Result.failure(
                IllegalStateException("No reliable offline tribal translation found for: \"$trimmed\". Please speak using standard native vocabulary or verify model download.")
            )
        }

        val joined = translatedTokens.joinToString(" ")
        return Result.success(formatOutput(joined, autoPunctuate))
    }

    private fun formatOutput(text: String, autoPunctuate: Boolean): String {
        var clean = text.replace(Regex("\\s+"), " ").trim()
        if (autoPunctuate) {
            if (clean.isNotEmpty()) {
                clean = clean.substring(0, 1).uppercase() + clean.substring(1)
                if (!clean.endsWith(".") && !clean.endsWith("?") && !clean.endsWith("!")) {
                    clean = "$clean."
                }
            }
        }
        return clean
    }
}
