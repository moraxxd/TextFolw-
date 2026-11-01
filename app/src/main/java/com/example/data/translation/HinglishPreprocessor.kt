package com.example.data.translation

/**
 * Normalizes Hinglish phrases (Hindi written in Latin script or conversational code-switching)
 * into Devanagari Hindi or standard expressions for high-accuracy local translation.
 */
object HinglishPreprocessor {

    // Common Hinglish words and expressions mapped to Devanagari Hindi
    private val hinglishToHindiMap = mapOf(
        "namaste" to "नमस्ते",
        "namaskar" to "नमस्कार",
        "kaise" to "कैसे",
        "kaisa" to "कैसा",
        "kaisi" to "कैसी",
        "ho" to "हो",
        "hain" to "हैं",
        "hai" to "है",
        "kya" to "क्या",
        "haal" to "हाल",
        "chal" to "चल",
        "aap" to "आप",
        "tum" to "तुम",
        "tu" to "तू",
        "main" to "मैं",
        "hum" to "हम",
        "theek" to "ठीक",
        "thik" to "ठीक",
        "hoon" to "हूँ",
        "hu" to "हूँ",
        "shukriya" to "शुक्रिया",
        "dhanyawad" to "धन्यवाद",
        "dhanyavad" to "धन्यवाद",
        "alvida" to "अलविदा",
        "phir" to "फिर",
        "milte" to "मिलते",
        "haan" to "हाँ",
        "nahi" to "नहीं",
        "nahin" to "नहीं",
        "mat" to "मत",
        "kyun" to "क्यों",
        "kyu" to "क्यों",
        "kab" to "कब",
        "kahan" to "कहाँ",
        "idhar" to "इधर",
        "udhar" to "उधर",
        "yahan" to "यहाँ",
        "wahan" to "वहाँ",
        "kaun" to "कौन",
        "kitna" to "कितना",
        "kitne" to "कितने",
        "kitni" to "कितनी",
        "kaise" to "कैसे",
        "achha" to "अच्छा",
        "accha" to "अच्छा",
        "bura" to "बुरा",
        "bahut" to "बहुत",
        "bohot" to "बहुत",
        "zyada" to "ज़्यादा",
        "kam" to "कम",
        "thoda" to "थोड़ा",
        "karo" to "करो",
        "karein" to "करें",
        "karna" to "करना",
        "kar" to "कर",
        "raha" to "रहा",
        "rahe" to "रहे",
        "rahi" to "रही",
        "tha" to "था",
        "the" to "थे",
        "thi" to "थी",
        "gaya" to "गया",
        "gaye" to "गए",
        "gayi" to "गई",
        "aao" to "आओ",
        "aaiye" to "आइए",
        "aana" to "आना",
        "jaao" to "जाओ",
        "jao" to "जाओ",
        "batao" to "बताओ",
        "bolo" to "बोलो",
        "suno" to "सुनो",
        "dekh" to "देख",
        "dekho" to "देखो",
        "chalo" to "चलो",
        "ruko" to "रुको",
        "bhai" to "भाई",
        "dost" to "दोस्त",
        "yaar" to "यार",
        "paani" to "पानी",
        "khana" to "खाना",
        "samay" to "समय",
        "waqt" to "वक़्त",
        "aaj" to "आज",
        "kal" to "कल",
        "parson" to "परसों",
        "subah" to "सुबह",
        "shaam" to "शाम",
        "raat" to "रात",
        "din" to "दिन",
        "ghar" to "घर",
        "office" to "दफ़्तर",
        "kaam" to "काम",
        "baat" to "बात",
        "mujhe" to "मुझे",
        "tujhe" to "तुझे",
        "humein" to "हमें",
        "unhe" to "उन्हें",
        "use" to "उसे",
        "iss" to "इस",
        "uss" to "उस",
        "mera" to "मेरा",
        "meri" to "मेरी",
        "mere" to "मेरे",
        "tera" to "तेरा",
        "teri" to "तेरी",
        "tere" to "तेरे",
        "apna" to "अपना",
        "apni" to "अपनी",
        "apne" to "अपने",
        "unka" to "उनका",
        "unki" to "उनकी",
        "unke" to "उनके"
    )

    /**
     * Checks if input contains Devanagari script.
     */
    fun containsDevanagari(text: String): Boolean {
        return text.any { it in '\u0900'..'\u097F' }
    }

    /**
     * If the input is in Latin/English characters but represents Hinglish,
     * this replaces Hinglish terms with proper Devanagari words so the ML Kit
     * Hindi->English translator accurately parses the sentence semantics.
     */
    fun normalizeHinglish(input: String): String {
        // If already in Devanagari, return as is
        if (containsDevanagari(input)) {
            return input
        }

        // Split by punctuation and whitespace
        val tokens = input.split(Regex("(?<=[\\s.,!?])|(?=[\\s.,!?])"))
        val converted = tokens.map { token ->
            val cleaned = token.lowercase().trim()
            hinglishToHindiMap[cleaned] ?: token
        }

        return converted.joinToString("")
    }
}
