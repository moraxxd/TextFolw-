package com.example.data.translation.tribal

/**
 * Authentic linguistic corpus and morphology mappings for Gondi (Central Dravidian)
 * and Kolami (Dravidian tribal language of Vidarbha/Telangana).
 * Maps native vocabulary, verb roots, and idioms to English and Marathi.
 */
object TribalLexicon {

    data class TranslationEntry(
        val nativeDevanagari: String,
        val nativeRoman: String,
        val english: String,
        val marathi: String,
        val partOfSpeech: String = "noun"
    )

    // Gondi (Raj Gondi / Adilabad / Betul / Mandla) Lexicon
    val gondiLexicon = listOf(
        TranslationEntry("सेवाल", "seval", "Greetings / Hello", "नमस्कार", "greeting"),
        TranslationEntry("जोहार", "johar", "Greetings / Welcome", "जोहार / नमस्कार", "greeting"),
        TranslationEntry("कुशियता मन्ता", "kusheeta manta", "I am doing well / Everything is fine", "मी चांगला/मजेत आहे", "phrase"),
        TranslationEntry("कुशियता मन्ती", "kusheeta manti", "Are you doing well?", "तुम्ही मजेत आहात का?", "phrase"),
        TranslationEntry("इमा बोर आन्दित", "ima bor andit", "Who are you?", "तुम्ही कोण आहात?", "phrase"),
        TranslationEntry("निवा पेरोल बतोल", "niva perol batol", "What is your name?", "तुमचे नाव काय आहे?", "phrase"),
        TranslationEntry("नावा पेरोल", "nava perol", "My name is", "माझे नाव", "phrase"),
        TranslationEntry("बोले दायका", "bole dayaka", "Where are you going?", "तुम्ही कुठे चालला आहात?", "phrase"),
        TranslationEntry("बोले बाऊरा मन्ती", "bole baura manti", "Where are you going?", "कुठे चालला आहात?", "phrase"),
        TranslationEntry("इमा बेके मन्ति", "ima beke manti", "Where are you?", "तुम्ही कुठे आहात?", "phrase"),
        TranslationEntry("येर सीम", "yer seem", "Give me water", "मला पाणी द्या", "phrase"),
        TranslationEntry("येर", "yer", "water", "पाणी", "noun"),
        TranslationEntry("गातो तिनका", "gaato tinka", "Eat food / have a meal", "जेवण करा", "phrase"),
        TranslationEntry("गातो", "gaato", "food / rice", "अन्न / भात / जेवण", "noun"),
        TranslationEntry("रोन दायका", "ron dayaka", "Go home", "घरी जा", "phrase"),
        TranslationEntry("रोन", "ron", "home / house", "घर", "noun"),
        TranslationEntry("काम किया", "kaam kiya", "Do the work", "काम करा", "phrase"),
        TranslationEntry("काम", "kaam", "work", "काम", "noun"),
        TranslationEntry("मावा नात", "mava naat", "our village", "आमचे गाव", "noun"),
        TranslationEntry("नात", "naat", "village", "गाव", "noun"),
        TranslationEntry("नेण्डि", "nendi", "today", "आज", "adverb"),
        TranslationEntry("नाडक", "naadak", "tomorrow", "उद्या", "adverb"),
        TranslationEntry("मरोल", "marol", "yesterday", "काल", "adverb"),
        TranslationEntry("सकडोल", "sakdol", "morning", "सकाळ", "noun"),
        TranslationEntry("नर्कल", "narkal", "night", "रात्र", "noun"),
        TranslationEntry("पय्या", "payya", "later / afterwards", "नंतर", "adverb"),
        TranslationEntry("इंगे", "inge", "yes", "होय", "affirmation"),
        TranslationEntry("हिला", "hila", "no / not", "नाही", "negation"),
        TranslationEntry("पुन्ना", "punna", "I don't know", "मला ठाऊक नाही", "phrase"),
        TranslationEntry("पुन्तो", "punto", "I know", "मला ठाऊक आहे", "phrase"),
        TranslationEntry("नावा", "nava", "my / mine", "माझे", "pronoun"),
        TranslationEntry("निवा", "niva", "your / yours", "तुमचे", "pronoun"),
        TranslationEntry("मावा", "mava", "our / ours", "आमचे", "pronoun"),
        TranslationEntry("ओना", "ona", "his / her", "त्याचे / तिचे", "pronoun"),
        TranslationEntry("मन्ता", "manta", "is / exists", "आहे", "verb"),
        TranslationEntry("मन्ती", "manti", "are", "आहात", "verb"),
        TranslationEntry("सील", "seel", "is not", "नाही", "verb"),
        TranslationEntry("दायका", "dayaka", "to go", "जाणे", "verb"),
        TranslationEntry("वाया", "vaaya", "to come", "येणे", "verb"),
        TranslationEntry("तिनका", "tinka", "to eat", "खाणे / जेवणे", "verb"),
        TranslationEntry("उंडा", "unda", "to drink", "पिणे", "verb"),
        TranslationEntry("हुडका", "hudka", "to look / see", "पाहणे", "verb"),
        TranslationEntry("केन्जा", "kenja", "to listen / hear", "ऐकणे", "verb"),
        TranslationEntry("वारका", "vaarka", "to speak / talk", "बोलणे", "verb"),
        TranslationEntry("मन्केवाल", "mankewal", "human / person", "माणूस", "noun"),
        TranslationEntry("कोय्या", "koyya", "Gond person / tribe", "गोंड बांधव", "noun"),
        TranslationEntry("दादू", "dadu", "elder brother / friend", "भाऊ / दादा", "noun"),
        TranslationEntry("तादा", "tada", "elder brother", "मोठा भाऊ", "noun"),
        TranslationEntry("बाई", "bai", "sister / woman", "बहीण / ताई", "noun"),
        TranslationEntry("याया", "yaaya", "mother", "आई", "noun"),
        TranslationEntry("बाबा", "baba", "father", "वडील / बाबा", "noun")
    )

    // Kolami (Yavatmal / Adilabad Kolam Tribe) Lexicon
    val kolamiLexicon = listOf(
        TranslationEntry("राम-राम", "ram-ram", "Greetings / Hello", "राम-राम / नमस्कार", "greeting"),
        TranslationEntry("जोहार", "johar", "Greetings / Respect", "जोहार / आदरपूर्वक वंदन", "greeting"),
        TranslationEntry("आम् कोलामी पाटा ककडा", "aam kolami pata kakda", "We speak the Kolami language", "आम्ही कोलामी भाषा बोलतो", "phrase"),
        TranslationEntry("नीव् बेद् अन्दन्", "neev bed andan", "Where are you?", "तुम्ही कुठे आहात?", "phrase"),
        TranslationEntry("ओन्द् बेद् अन्दन्", "ond bed andan", "Where is he?", "तो कुठे आहे?", "phrase"),
        TranslationEntry("आम् बेद् वत्तन्", "aam bed vattan", "Where have we reached?", "आम्ही कुठे आलो आहोत?", "phrase"),
        TranslationEntry("इर् सीम", "ir seem", "Give me water", "मला पाणी द्या", "phrase"),
        TranslationEntry("इर्", "ir", "water", "पाणी", "noun"),
        TranslationEntry("तिना", "tina", "Eat food / have meal", "जेवण करा", "phrase"),
        TranslationEntry("तिन्", "tin", "food / eat", "अन्न / जेवण", "noun"),
        TranslationEntry("एलात् सन्द", "elat sand", "Go home", "घरी जा", "phrase"),
        TranslationEntry("एला", "ela", "house / home", "घर", "noun"),
        TranslationEntry("काम कका", "kaam kaka", "Do the work", "काम करा", "phrase"),
        TranslationEntry("नेड", "ned", "today", "आज", "adverb"),
        TranslationEntry("नाळ", "naal", "tomorrow", "उद्या", "adverb"),
        TranslationEntry("हाये", "haye", "yes", "होय", "affirmation"),
        TranslationEntry("तोते", "tote", "no / not", "नाही", "negation"),
        TranslationEntry("आन्", "aan", "I", "मी", "pronoun"),
        TranslationEntry("नीव्", "neev", "you", "तू / तुम्ही", "pronoun"),
        TranslationEntry("आम्", "aam", "we", "आम्ही", "pronoun"),
        TranslationEntry("तोन्द्", "tond", "he / that man", "तो", "pronoun"),
        TranslationEntry("अद्", "ad", "she / it / that", "ती / ते", "pronoun"),
        TranslationEntry("आम्ना", "aamna", "our / ours", "आमचे", "pronoun"),
        TranslationEntry("इम्ना", "imna", "your / yours", "तुमचे", "pronoun"),
        TranslationEntry("अन्दन्", "andan", "is / exists", "आहे", "verb"),
        TranslationEntry("तोतन्", "totan", "is not", "नाही", "verb"),
        TranslationEntry("सन्द", "sand", "to go", "जाणे", "verb"),
        TranslationEntry("वर", "var", "to come", "येणे", "verb"),
        TranslationEntry("कक", "kak", "to do / make", "करणे", "verb"),
        TranslationEntry("सीम", "seem", "give", "द्या", "verb"),
        TranslationEntry("कुक", "kuk", "to call / shout", "हाक मारणे", "verb"),
        TranslationEntry("ऊर्", "oor", "village", "गाव", "noun"),
        TranslationEntry("माय", "maay", "mother", "आई", "noun"),
        TranslationEntry("अप्पा", "appa", "father", "वडील", "noun"),
        TranslationEntry("तोटेन", "toten", "nothing / none", "काही नाही", "pronoun")
    )

    // Marathi to English common sentence mapping & lexicon
    val marathiLexicon = listOf(
        TranslationEntry("नमस्कार", "namaskar", "Hello / Greetings", "नमस्कार", "greeting"),
        TranslationEntry("तुम्ही कसे आहात?", "tumhi kase ahat", "How are you?", "तुम्ही कसे आहात?", "phrase"),
        TranslationEntry("मी ठीक आहे", "mi thik ahe", "I am fine", "मी ठीक आहे", "phrase"),
        TranslationEntry("तुमचे नाव काय आहे?", "tumche nav kay ahe", "What is your name?", "तुमचे नाव काय आहे?", "phrase"),
        TranslationEntry("माझे नाव", "majhe nav", "My name is", "माझे नाव", "phrase"),
        TranslationEntry("तुम्ही कुठे चालला आहात?", "tumhi kuthe chalala ahat", "Where are you going?", "तुम्ही कुठे चालला आहात?", "phrase"),
        TranslationEntry("पाणी द्या", "pani dya", "Give water", "पाणी द्या", "phrase"),
        TranslationEntry("जेवण करा", "jevan kara", "Eat food", "जेवण करा", "phrase"),
        TranslationEntry("घरी जा", "ghari ja", "Go home", "घरी जा", "phrase"),
        TranslationEntry("आज", "aaj", "today", "आज", "adverb"),
        TranslationEntry("उद्या", "udya", "tomorrow", "उद्या", "adverb"),
        TranslationEntry("काल", "kaal", "yesterday", "काल", "adverb"),
        TranslationEntry("होय", "hoy", "yes", "होय", "affirmation"),
        TranslationEntry("नाही", "nahi", "no", "नाही", "negation"),
        TranslationEntry("धन्यवाद", "dhanyavad", "Thank you", "धन्यवाद", "greeting"),
        TranslationEntry("काम करा", "kaam kara", "Do work", "काम करा", "phrase"),
        TranslationEntry("कुठे आहात?", "kuthe ahat", "Where are you?", "कुठे आहात?", "phrase")
    )
}
