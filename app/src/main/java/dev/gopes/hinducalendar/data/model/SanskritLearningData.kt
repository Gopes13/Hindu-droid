package dev.gopes.hinducalendar.data.model

// ── Letter Groups ────────────────────────────────────────────────────────────
enum class SanskritLetterGroup(val displayName: String) {
    VOWEL("Vowels"),
    VELAR("Velars"),
    PALATAL("Palatals"),
    RETROFLEX("Retroflexes"),
    DENTAL("Dentals"),
    LABIAL("Labials"),
    SEMIVOWEL("Semivowels"),
    SIBILANT("Sibilants"),
    CONJUNCT("Conjuncts")
}

// ── Letter ───────────────────────────────────────────────────────────────────
data class SanskritLetter(
    val id: String,
    val character: String,
    val transliteration: String,
    val pronunciation: String,
    val exampleWord: String,
    val exampleTranslit: String,
    val exampleMeaning: String,
    val group: SanskritLetterGroup
)

// ── Word ─────────────────────────────────────────────────────────────────────
data class SanskritWord(
    val id: String,
    val sanskrit: String,
    val transliteration: String,
    val meaning: String,
    val context: String
)

// ── Syllable ─────────────────────────────────────────────────────────────────
data class SanskritSyllable(
    val id: String,
    val script: String,
    val base: String,
    val markDisplay: String,
    val markName: String,
    val transliteration: String,
    val pronunciation: String
)

// ── Shloka ───────────────────────────────────────────────────────────────────
data class ShlokaWord(
    val sanskrit: String,
    val transliteration: String,
    val meaning: String
)

data class SanskritShloka(
    val id: String,
    val source: String,
    val devanagari: String,
    val transliteration: String,
    val translation: String,
    val words: List<ShlokaWord>
)

// ── Exercise Types ───────────────────────────────────────────────────────────
sealed class SanskritExercise {
    data class LetterToSound(val letter: SanskritLetter, val distractors: List<SanskritLetter>) : SanskritExercise()
    data class SoundToLetter(val letter: SanskritLetter, val distractors: List<SanskritLetter>) : SanskritExercise()
    data class WordMeaning(val word: SanskritWord, val distractors: List<SanskritWord>) : SanskritExercise()
    data class SyllableToSound(val syllable: SanskritSyllable, val distractors: List<SanskritSyllable>) : SanskritExercise()
    data class SoundToSyllable(val syllable: SanskritSyllable, val distractors: List<SanskritSyllable>) : SanskritExercise()
    data class WordReading(val word: SanskritWord, val distractors: List<String>) : SanskritExercise()

    /** Returns the ID of the target letter/syllable for mastery tracking */
    val targetLetterId: String? get() = when (this) {
        is LetterToSound -> letter.id
        is SoundToLetter -> letter.id
        is SyllableToSound -> syllable.id
        is SoundToSyllable -> syllable.id
        else -> null
    }
}

// ── Context Card ─────────────────────────────────────────────────────────────
data class SanskritContextCard(
    val sanskrit: String,
    val transliteration: String,
    val note: String
)

// ── Lesson & Module ──────────────────────────────────────────────────────────
data class SanskritLesson(
    val id: String,
    val title: String,
    val contextCard: SanskritContextCard,
    val exercises: List<SanskritExercise>
)

data class SanskritModule(
    val id: String,
    val title: String,
    val titleSanskrit: String,
    val emoji: String,
    val lessons: List<SanskritLesson>
)

// ── Verse Category ───────────────────────────────────────────────────────────
enum class VerseTextCategory(val displayName: String) {
    GITA("Bhagavad Gita"),
    UPANISHADS("Upanishads"),
    RUDRAM("Rudram & VS"),
    CHALISA("Hanuman Chalisa")
}

// ══════════════════════════════════════════════════════════════════════════════
// Static Data
// ══════════════════════════════════════════════════════════════════════════════
object SanskritData {

    // ── All Letters ──────────────────────────────────────────────────────────
    val vowels = listOf(
        SanskritLetter("a", "अ", "a", "like 'u' in 'but'", "अग्नि", "agni", "fire", SanskritLetterGroup.VOWEL),
        SanskritLetter("aa", "आ", "\u0101", "like 'a' in 'father'", "आत्मा", "\u0101tm\u0101", "soul", SanskritLetterGroup.VOWEL),
        SanskritLetter("i", "इ", "i", "like 'i' in 'pin'", "इन्द्र", "indra", "Indra", SanskritLetterGroup.VOWEL),
        SanskritLetter("ee", "ई", "\u012B", "like 'ee' in 'seen'", "ईश्वर", "\u012B\u015Bvara", "God", SanskritLetterGroup.VOWEL),
        SanskritLetter("u", "उ", "u", "like 'u' in 'put'", "उपनिषद्", "upani\u1E63ad", "Upanishad", SanskritLetterGroup.VOWEL),
        SanskritLetter("oo", "ऊ", "\u016B", "like 'oo' in 'moon'", "ऊर्जा", "\u016Brj\u0101", "energy", SanskritLetterGroup.VOWEL),
        SanskritLetter("e", "ए", "e", "like 'ay' in 'say'", "एकम्", "ekam", "one", SanskritLetterGroup.VOWEL),
        SanskritLetter("ai", "ऐ", "ai", "like 'ai' in 'aisle'", "ऐश्वर्य", "ai\u015Bvarya", "glory", SanskritLetterGroup.VOWEL),
        SanskritLetter("o", "ओ", "o", "like 'o' in 'go'", "ओम्", "om", "Om", SanskritLetterGroup.VOWEL),
        SanskritLetter("au", "औ", "au", "like 'ow' in 'how'", "औषधि", "au\u1E63adhi", "herb", SanskritLetterGroup.VOWEL),
        SanskritLetter("am", "अं", "a\u1E43", "nasal 'm'", "संस्कृत", "sa\u1E43sk\u1E5Bta", "Sanskrit", SanskritLetterGroup.VOWEL),
        SanskritLetter("ah", "अः", "a\u1E25", "soft breath 'h'", "नमः", "nama\u1E25", "salutation", SanskritLetterGroup.VOWEL)
    )

    val velars = listOf(
        SanskritLetter("ka", "क", "ka", "like 'k' in 'kite'", "कर्म", "karma", "action", SanskritLetterGroup.VELAR),
        SanskritLetter("kha", "ख", "kha", "aspirated 'k'", "खग", "khaga", "bird", SanskritLetterGroup.VELAR),
        SanskritLetter("ga", "ग", "ga", "like 'g' in 'go'", "गुरु", "guru", "teacher", SanskritLetterGroup.VELAR),
        SanskritLetter("gha", "घ", "gha", "aspirated 'g'", "घृत", "gh\u1E5Bta", "ghee", SanskritLetterGroup.VELAR)
    )

    val palatals = listOf(
        SanskritLetter("cha", "च", "ca", "like 'ch' in 'church'", "चक्र", "cakra", "wheel", SanskritLetterGroup.PALATAL),
        SanskritLetter("ja", "ज", "ja", "like 'j' in 'joy'", "जप", "japa", "chanting", SanskritLetterGroup.PALATAL),
        SanskritLetter("tha", "थ", "tha", "aspirated 't' (dental)", "थल", "thala", "place", SanskritLetterGroup.PALATAL),
        SanskritLetter("dha", "ध", "dha", "aspirated 'd' (dental)", "धर्म", "dharma", "duty", SanskritLetterGroup.PALATAL)
    )

    val retroflexes = listOf(
        SanskritLetter("tta", "ट", "\u1E6Da", "retroflex 't'", "टीका", "\u1E6D\u012Bk\u0101", "commentary", SanskritLetterGroup.RETROFLEX),
        SanskritLetter("ttha", "ठ", "\u1E6Dha", "aspirated retroflex 't'", "ठाकुर", "\u1E6Dh\u0101kura", "lord", SanskritLetterGroup.RETROFLEX),
        SanskritLetter("dda", "ड", "\u1E0Da", "retroflex 'd'", "डमरु", "\u1E0Damaru", "drum", SanskritLetterGroup.RETROFLEX)
    )

    val dentals = listOf(
        SanskritLetter("ta", "त", "ta", "like 't' in 'top'", "तत्त्व", "tattva", "truth", SanskritLetterGroup.DENTAL),
        SanskritLetter("da", "द", "da", "like 'd' in 'dog'", "दया", "day\u0101", "compassion", SanskritLetterGroup.DENTAL),
        SanskritLetter("na", "न", "na", "like 'n' in 'name'", "नमस्ते", "namaste", "greeting", SanskritLetterGroup.DENTAL),
        SanskritLetter("pa", "प", "pa", "like 'p' in 'pen'", "पूजा", "p\u016Bj\u0101", "worship", SanskritLetterGroup.DENTAL),
        SanskritLetter("ba", "ब", "ba", "like 'b' in 'boy'", "भक्ति", "bhakti", "devotion", SanskritLetterGroup.DENTAL)
    )

    val labials = listOf(
        SanskritLetter("ma", "म", "ma", "like 'm' in 'mother'", "मन्त्र", "mantra", "mantra", SanskritLetterGroup.LABIAL),
        SanskritLetter("bha", "भ", "bha", "aspirated 'b'", "भगवान्", "bhagav\u0101n", "Lord", SanskritLetterGroup.LABIAL)
    )

    val semivowels = listOf(
        SanskritLetter("ya", "य", "ya", "like 'y' in 'yes'", "योग", "yoga", "yoga", SanskritLetterGroup.SEMIVOWEL),
        SanskritLetter("ra", "र", "ra", "like 'r' in 'run' (lightly rolled)", "राम", "r\u0101ma", "Rama", SanskritLetterGroup.SEMIVOWEL),
        SanskritLetter("la", "ल", "la", "like 'l' in 'love'", "लक्ष्मी", "lak\u1E63m\u012B", "Lakshmi", SanskritLetterGroup.SEMIVOWEL),
        SanskritLetter("va", "व", "va", "between 'v' and 'w'", "विष्णु", "vi\u1E63\u1E47u", "Vishnu", SanskritLetterGroup.SEMIVOWEL)
    )

    val sibilants = listOf(
        SanskritLetter("sha", "श", "\u015Ba", "like 'sh' in 'shine'", "शान्ति", "\u015B\u0101nti", "peace", SanskritLetterGroup.SIBILANT),
        SanskritLetter("shha", "ष", "\u1E63a", "retroflex 'sh'", "कृष्ण", "k\u1E5B\u1E63\u1E47a", "Krishna", SanskritLetterGroup.SIBILANT),
        SanskritLetter("sa", "स", "sa", "like 's' in 'sun'", "सत्य", "satya", "truth", SanskritLetterGroup.SIBILANT),
        SanskritLetter("ha", "ह", "ha", "like 'h' in 'home'", "हनुमान्", "hanum\u0101n", "Hanuman", SanskritLetterGroup.SIBILANT)
    )

    val conjuncts = listOf(
        SanskritLetter("pra", "प्र", "pra", "like 'pra' in 'practice'", "प्रणाम", "pra\u1E47\u0101ma", "salutation", SanskritLetterGroup.CONJUNCT),
        SanskritLetter("tra", "त्र", "tra", "like 'tra' in 'trunk'", "मन्त्र", "mantra", "mantra", SanskritLetterGroup.CONJUNCT),
        SanskritLetter("shra", "श्र", "\u015Bra", "like 'shra' in 'shravan'", "श्रवण", "\u015Brava\u1E47a", "listening", SanskritLetterGroup.CONJUNCT),
        SanskritLetter("shri", "श्री", "\u015Br\u012B", "like 'shree' (auspicious)", "श्रीमद्", "\u015Br\u012Bmad", "glorious", SanskritLetterGroup.CONJUNCT),
        SanskritLetter("krsha", "कृ", "k\u1E5B", "like 'kri' in 'Krishna'", "कृष्ण", "k\u1E5B\u1E63\u1E47a", "Krishna", SanskritLetterGroup.CONJUNCT),
        SanskritLetter("ksha", "क्ष", "k\u1E63a", "like 'ksha' in 'Daksha'", "क्षेत्र", "k\u1E63etra", "field", SanskritLetterGroup.CONJUNCT),
        SanskritLetter("jna", "ज्ञ", "j\u00F1a", "like 'gya' in 'Gyaan'", "ज्ञान", "j\u00F1\u0101na", "knowledge", SanskritLetterGroup.CONJUNCT),
        SanskritLetter("dva", "द्व", "dva", "like 'dva' in 'Dvaita'", "द्वैत", "dvaita", "duality", SanskritLetterGroup.CONJUNCT),
        SanskritLetter("bra", "ब्र", "bra", "like 'bra' in 'Brahma'", "ब्रह्म", "brahma", "Brahman", SanskritLetterGroup.CONJUNCT),
        SanskritLetter("kshe", "क्षे", "k\u1E63e", "like 'kshe' in 'Kshetra'", "क्षेत्र", "k\u1E63etra", "field", SanskritLetterGroup.CONJUNCT),
        SanskritLetter("dhar_vir", "धर्", "dhar", "like 'dhar' in 'Dharma'", "धर्म", "dharma", "duty", SanskritLetterGroup.CONJUNCT),
        SanskritLetter("kar_vir", "कर्", "kar", "like 'kar' in 'Karma'", "कर्म", "karma", "action", SanskritLetterGroup.CONJUNCT),
        SanskritLetter("nar_vir", "नर्", "nar", "like 'nar' in 'Narasimha'", "नरसिंह", "narasimha", "Narasimha", SanskritLetterGroup.CONJUNCT),
        SanskritLetter("shr_vir", "श्र", "\u015Br", "like 'shr' in 'Shruti'", "श्रुति", "\u015Bruti", "scripture", SanskritLetterGroup.CONJUNCT)
    )

    val allLetters: List<SanskritLetter> = vowels + velars + palatals + retroflexes + dentals + labials + semivowels + sibilants + conjuncts

    fun lettersByGroup(group: SanskritLetterGroup): List<SanskritLetter> = allLetters.filter { it.group == group }

    fun letterById(id: String): SanskritLetter? = allLetters.find { it.id == id }

    // ── Sacred Words ─────────────────────────────────────────────────────────
    val sacredWords = listOf(
        SanskritWord("om", "ॐ", "om", "sacred sound", "mantra"),
        SanskritWord("namaste", "नमस्ते", "namaste", "I bow to you", "greeting"),
        SanskritWord("dharma", "धर्म", "dharma", "duty / righteousness", "philosophy"),
        SanskritWord("karma", "कर्म", "karma", "action / deed", "philosophy"),
        SanskritWord("bhakti", "भक्ति", "bhakti", "devotion", "worship"),
        SanskritWord("yoga", "योग", "yoga", "union / discipline", "practice"),
        SanskritWord("atma", "आत्मा", "\u0101tm\u0101", "soul / self", "philosophy"),
        SanskritWord("maya", "माया", "m\u0101y\u0101", "illusion", "philosophy"),
        SanskritWord("moksha", "मोक्ष", "mok\u1E63a", "liberation", "philosophy"),
        SanskritWord("satya", "सत्य", "satya", "truth", "virtue"),
        SanskritWord("ahimsa", "अहिंसा", "ahi\u1E43s\u0101", "non-violence", "virtue"),
        SanskritWord("shakti", "शक्ति", "\u015Bakti", "power / energy", "divine"),
        SanskritWord("guru", "गुरु", "guru", "teacher / guide", "relationship"),
        SanskritWord("shiva", "शिव", "\u015Biva", "auspicious / Shiva", "deity"),
        SanskritWord("krishna", "कृष्ण", "k\u1E5B\u1E63\u1E47a", "the dark one / Krishna", "deity"),
        SanskritWord("rama", "राम", "r\u0101ma", "joy / Rama", "deity"),
        SanskritWord("deva", "देव", "deva", "divine being", "deity"),
        SanskritWord("devi", "देवी", "dev\u012B", "goddess", "deity"),
        SanskritWord("vishnu", "विष्णु", "vi\u1E63\u1E47u", "the preserver", "deity"),
        SanskritWord("lakshmi", "लक्ष्मी", "lak\u1E63m\u012B", "goddess of wealth", "deity"),
        SanskritWord("ganesha", "गणेश", "ga\u1E47e\u015Ba", "lord of hosts", "deity"),
        SanskritWord("pranama", "प्रणाम", "pra\u1E47\u0101ma", "salutation", "greeting"),
        SanskritWord("japa", "जप", "japa", "repetitive chanting", "practice"),
        SanskritWord("puja", "पूजा", "p\u016Bj\u0101", "worship ritual", "practice"),
        SanskritWord("mantra", "मन्त्र", "mantra", "sacred utterance", "practice")
    )

    val gitaWords = listOf(
        SanskritWord("kshetra", "क्षेत्र", "k\u1E63etra", "field / body", "gita"),
        SanskritWord("prakriti", "प्रकृति", "prak\u1E5Bti", "nature / matter", "gita"),
        SanskritWord("purusha", "पुरुष", "puru\u1E63a", "soul / spirit", "gita"),
        SanskritWord("guna", "गुण", "gu\u1E47a", "quality / attribute", "gita"),
        SanskritWord("sattva", "सत्त्व", "sattva", "purity / goodness", "gita"),
        SanskritWord("rajas", "रजस्", "rajas", "passion / activity", "gita"),
        SanskritWord("tamas", "तमस्", "tamas", "darkness / inertia", "gita"),
        SanskritWord("buddhi", "बुद्धि", "buddhi", "intellect", "gita"),
        SanskritWord("viveka", "विवेक", "viveka", "discrimination", "gita"),
        SanskritWord("vairagya", "वैराग्य", "vair\u0101gya", "detachment", "gita"),
        SanskritWord("jnana", "ज्ञान", "j\u00F1\u0101na", "knowledge", "gita"),
        SanskritWord("brahman", "ब्रह्म", "brahma", "ultimate reality", "gita"),
        SanskritWord("samadhi", "समाधि", "sam\u0101dhi", "absorption", "gita"),
        SanskritWord("mukti", "मुक्ति", "mukti", "liberation", "gita"),
        SanskritWord("seva", "सेवा", "sev\u0101", "selfless service", "gita"),
        SanskritWord("tyaga", "त्याग", "ty\u0101ga", "renunciation", "gita")
    )

    val stotrasWords = listOf(
        SanskritWord("ananta", "अनन्त", "ananta", "infinite", "vishnu"),
        SanskritWord("achyuta", "अच्युत", "acyuta", "imperishable", "vishnu"),
        SanskritWord("govinda", "गोविन्द", "govinda", "protector of cows", "vishnu"),
        SanskritWord("madhava", "माधव", "m\u0101dhava", "lord of knowledge", "vishnu"),
        SanskritWord("keshava", "केशव", "ke\u015Bava", "beautiful-haired", "vishnu"),
        SanskritWord("narayana", "नारायण", "n\u0101r\u0101ya\u1E47a", "refuge of man", "vishnu"),
        SanskritWord("rudra", "रुद्र", "rudra", "the roarer", "shiva"),
        SanskritWord("shankara", "शंकर", "\u015Ba\u1E45kara", "beneficent", "shiva"),
        SanskritWord("mahesvara", "महेश्वर", "mahe\u015Bvara", "great lord", "shiva"),
        SanskritWord("bhagavan", "भगवान्", "bhagav\u0101n", "blessed lord", "title"),
        SanskritWord("prabhu", "प्रभु", "prabhu", "lord / master", "title"),
        SanskritWord("svami", "स्वामी", "sv\u0101m\u012B", "master / lord", "title")
    )

    val devotionalWords = listOf(
        SanskritWord("hanuman", "हनुमान्", "hanum\u0101n", "the monkey god", "deity"),
        SanskritWord("prasad", "प्रसाद", "pras\u0101da", "divine offering", "worship"),
        SanskritWord("upasana", "उपासना", "up\u0101san\u0101", "meditation / worship", "practice"),
        SanskritWord("chakra", "चक्र", "cakra", "energy wheel", "yoga"),
        SanskritWord("nada", "नाद", "n\u0101da", "cosmic sound", "yoga"),
        SanskritWord("durga", "दुर्गा", "durg\u0101", "the invincible", "deity"),
        SanskritWord("padma", "पद्म", "padma", "lotus", "symbol")
    )

    val allWords: List<SanskritWord> = sacredWords + gitaWords + stotrasWords + devotionalWords

    // ── Syllables ────────────────────────────────────────────────────────────
    val syllables = listOf(
        SanskritSyllable("ka_aa", "का", "क", "ा", "\u0101-matra", "k\u0101", "kaa"),
        SanskritSyllable("ka_i", "कि", "क", "ि", "i-matra", "ki", "ki"),
        SanskritSyllable("ka_ii", "की", "क", "ी", "\u012B-matra", "k\u012B", "kee"),
        SanskritSyllable("ka_u", "कु", "क", "ु", "u-matra", "ku", "ku"),
        SanskritSyllable("ka_uu", "कू", "क", "ू", "\u016B-matra", "k\u016B", "koo"),
        SanskritSyllable("ka_e", "के", "क", "े", "e-matra", "ke", "ke"),
        SanskritSyllable("ka_ai", "कै", "क", "ै", "ai-matra", "kai", "kai"),
        SanskritSyllable("ka_o", "को", "क", "ो", "o-matra", "ko", "ko"),
        SanskritSyllable("ka_au", "कौ", "क", "ौ", "au-matra", "kau", "kau"),
        SanskritSyllable("na_aa", "ना", "न", "ा", "\u0101-matra", "n\u0101", "naa"),
        SanskritSyllable("na_i", "नि", "न", "ि", "i-matra", "ni", "ni"),
        SanskritSyllable("na_ii", "नी", "न", "ी", "\u012B-matra", "n\u012B", "nee"),
        SanskritSyllable("ma_aa", "मा", "म", "ा", "\u0101-matra", "m\u0101", "maa"),
        SanskritSyllable("ma_i", "मि", "म", "ि", "i-matra", "mi", "mi"),
        SanskritSyllable("ma_u", "मु", "म", "ु", "u-matra", "mu", "mu"),
        SanskritSyllable("ra_aa", "रा", "र", "ा", "\u0101-matra", "r\u0101", "raa"),
        SanskritSyllable("ra_i", "रि", "र", "ि", "i-matra", "ri", "ri"),
        SanskritSyllable("sha_aa", "शा", "श", "ा", "\u0101-matra", "\u015B\u0101", "shaa"),
        SanskritSyllable("sha_i", "शि", "श", "ि", "i-matra", "\u015Bi", "shi"),
        SanskritSyllable("ya_aa", "या", "य", "ा", "\u0101-matra", "y\u0101", "yaa"),
        SanskritSyllable("ya_u", "यु", "य", "ु", "u-matra", "yu", "yu"),
        SanskritSyllable("ga_aa", "गा", "ग", "ा", "\u0101-matra", "g\u0101", "gaa"),
        SanskritSyllable("ga_u", "गु", "ग", "ु", "u-matra", "gu", "gu"),
        SanskritSyllable("pa_aa", "पा", "प", "ा", "\u0101-matra", "p\u0101", "paa"),
        SanskritSyllable("pa_u", "पु", "प", "ु", "u-matra", "pu", "pu"),
        SanskritSyllable("ba_aa", "बा", "ब", "ा", "\u0101-matra", "b\u0101", "baa"),
        SanskritSyllable("ta_aa", "ता", "त", "ा", "\u0101-matra", "t\u0101", "taa"),
        SanskritSyllable("ta_i", "ति", "त", "ि", "i-matra", "ti", "ti"),
        SanskritSyllable("da_aa", "दा", "द", "ा", "\u0101-matra", "d\u0101", "daa"),
        SanskritSyllable("da_e", "दे", "द", "े", "e-matra", "de", "de"),
        SanskritSyllable("da_ai", "दै", "द", "ै", "ai-matra", "dai", "dai"),
        SanskritSyllable("va_aa", "वा", "व", "ा", "\u0101-matra", "v\u0101", "vaa"),
        SanskritSyllable("va_i", "वि", "व", "ि", "i-matra", "vi", "vi"),
        SanskritSyllable("ha_a", "ह", "ह", "", "base", "ha", "ha"),
        SanskritSyllable("ha_aa", "हा", "ह", "ा", "\u0101-matra", "h\u0101", "haa"),
        SanskritSyllable("la_aa", "ला", "ल", "ा", "\u0101-matra", "l\u0101", "laa"),
        SanskritSyllable("la_i", "लि", "ल", "ि", "i-matra", "li", "li"),
        SanskritSyllable("sa_aa", "सा", "स", "ा", "\u0101-matra", "s\u0101", "saa")
    )

    // ── Shlokas ──────────────────────────────────────────────────────────────
    val shlokas = listOf(
        SanskritShloka(
            id = "bg_2_47",
            source = "Bhagavad Gita 2.47",
            devanagari = "कर्मण्येवाधिकारस्ते मा फलेषु कदाचन ।\nमा कर्मफलहेतुर्भूर्मा ते सङ्गोऽस्त्वकर्मणि ॥",
            transliteration = "karma\u1E47y ev\u0101dhik\u0101ras te m\u0101 phale\u1E63u kad\u0101cana\nm\u0101 karma-phala-hetur bh\u016Br m\u0101 te sa\u1E45go\u2019stv akarma\u1E47i",
            translation = "You have a right to perform your prescribed duties, but you are not entitled to the fruits of your actions.",
            words = listOf(
                ShlokaWord("कर्मणि", "karma\u1E47i", "in action"),
                ShlokaWord("एव", "eva", "only"),
                ShlokaWord("अधिकारः", "adhik\u0101ra\u1E25", "right"),
                ShlokaWord("ते", "te", "your"),
                ShlokaWord("मा", "m\u0101", "not"),
                ShlokaWord("फलेषु", "phale\u1E63u", "in fruits")
            )
        ),
        SanskritShloka(
            id = "bg_2_20",
            source = "Bhagavad Gita 2.20",
            devanagari = "न जायते म्रियते वा कदाचिन्\nनायं भूत्वा भविता वा न भूयः ।",
            transliteration = "na j\u0101yate mriyate v\u0101 kad\u0101cin\nn\u0101ya\u1E43 bh\u016Btv\u0101 bhavit\u0101 v\u0101 na bh\u016Bya\u1E25",
            translation = "The soul is neither born, nor does it ever die; nor having once existed, does it cease to be.",
            words = listOf(
                ShlokaWord("न", "na", "not"),
                ShlokaWord("जायते", "j\u0101yate", "is born"),
                ShlokaWord("म्रियते", "mriyate", "dies"),
                ShlokaWord("आत्मा", "\u0101tm\u0101", "soul")
            )
        ),
        SanskritShloka(
            id = "bg_18_66",
            source = "Bhagavad Gita 18.66",
            devanagari = "सर्वधर्मान्परित्यज्य मामेकं शरणं व्रज ।\nअहं त्वा सर्वपापेभ्यो मोक्षयिष्यामि मा शुचः ॥",
            transliteration = "sarva-dharm\u0101n parityajya m\u0101m eka\u1E43 \u015Bara\u1E47a\u1E43 vraja\naha\u1E43 tv\u0101 sarva-p\u0101pebhyo mok\u1E63ayi\u1E63y\u0101mi m\u0101 \u015Buca\u1E25",
            translation = "Abandon all varieties of dharma and simply surrender unto Me. I shall deliver you from all sinful reactions; do not fear.",
            words = listOf(
                ShlokaWord("सर्व", "sarva", "all"),
                ShlokaWord("धर्मान्", "dharm\u0101n", "duties"),
                ShlokaWord("परित्यज्य", "parityajya", "abandoning"),
                ShlokaWord("माम्", "m\u0101m", "unto Me"),
                ShlokaWord("शरणम्", "\u015Bara\u1E47am", "surrender"),
                ShlokaWord("मोक्षयिष्यामि", "mok\u1E63ayi\u1E63y\u0101mi", "I shall liberate")
            )
        ),
        SanskritShloka(
            id = "bg_9_22",
            source = "Bhagavad Gita 9.22",
            devanagari = "अनन्याश्चिन्तयन्तो मां ये जनाः पर्युपासते ।\nतेषां नित्याभियुक्तानां योगक्षेमं वहाम्यहम् ॥",
            transliteration = "anany\u0101\u015B cintayanto m\u0101\u1E43 ye jan\u0101\u1E25 paryup\u0101sate\nte\u1E63\u0101\u1E43 nity\u0101bhiyukt\u0101n\u0101\u1E43 yogak\u1E63ema\u1E43 vah\u0101my aham",
            translation = "To those who worship Me with exclusive devotion, who think of no one else, I carry what they lack and preserve what they have.",
            words = listOf(
                ShlokaWord("अनन्य", "ananya", "exclusive"),
                ShlokaWord("चिन्तयन्तः", "cintayanta\u1E25", "thinking of"),
                ShlokaWord("योगक्षेमम्", "yogak\u1E63emam", "welfare"),
                ShlokaWord("वहामि", "vah\u0101mi", "I carry")
            )
        ),
        SanskritShloka(
            id = "isha_1",
            source = "Isha Upanishad 1",
            devanagari = "ईशा वास्यमिदं सर्वं यत्किञ्च जगत्यां जगत् ।\nतेन त्यक्तेन भुञ्जीथा मा गृधः कस्यस्विद्धनम् ॥",
            transliteration = "\u012B\u015B\u0101 v\u0101syam ida\u1E43 sarva\u1E43 yat ki\u00F1ca jagaty\u0101\u1E43 jagat\ntena tyaktena bhu\u00F1j\u012Bth\u0101 m\u0101 g\u1E5Bdha\u1E25 kasya svid dhanam",
            translation = "Whatever exists in this universe is enveloped by the Lord. Enjoy it with renunciation; do not covet another's wealth.",
            words = listOf(
                ShlokaWord("ईशा", "\u012B\u015B\u0101", "by the Lord"),
                ShlokaWord("वास्यम्", "v\u0101syam", "enveloped"),
                ShlokaWord("सर्वम्", "sarvam", "all"),
                ShlokaWord("त्यक्तेन", "tyaktena", "by renunciation"),
                ShlokaWord("भुञ्जीथा", "bhu\u00F1j\u012Bth\u0101", "enjoy")
            )
        ),
        SanskritShloka(
            id = "asato_ma",
            source = "Brihadaranyaka Upanishad 1.3.28",
            devanagari = "असतो मा सद्गमय ।\nतमसो मा ज्योतिर्गमय ।\nमृत्योर्मा अमृतं गमय ॥",
            transliteration = "asato m\u0101 sad gamaya\ntamaso m\u0101 jyotir gamaya\nm\u1E5Btyor m\u0101 am\u1E5Bta\u1E43 gamaya",
            translation = "Lead me from untruth to truth, from darkness to light, from death to immortality.",
            words = listOf(
                ShlokaWord("असतः", "asata\u1E25", "from untruth"),
                ShlokaWord("सत्", "sat", "truth"),
                ShlokaWord("गमय", "gamaya", "lead"),
                ShlokaWord("तमसः", "tamasa\u1E25", "from darkness"),
                ShlokaWord("ज्योतिः", "jyoti\u1E25", "light"),
                ShlokaWord("मृत्योः", "m\u1E5Btyo\u1E25", "from death"),
                ShlokaWord("अमृतम्", "am\u1E5Btam", "immortality")
            )
        ),
        SanskritShloka(
            id = "taittiriya_peace",
            source = "Taittiriya Upanishad",
            devanagari = "ॐ सह नाववतु ।\nसह नौ भुनक्तु ।\nसह वीर्यं करवावहै ।",
            transliteration = "o\u1E43 saha n\u0101v avatu\nsaha nau bhunaktu\nsaha v\u012Brya\u1E43 karav\u0101vahai",
            translation = "Om, may we be protected together. May we be nourished together. May we work together with great energy.",
            words = listOf(
                ShlokaWord("सह", "saha", "together"),
                ShlokaWord("नौ", "nau", "us"),
                ShlokaWord("अवतु", "avatu", "may protect"),
                ShlokaWord("भुनक्तु", "bhunaktu", "may nourish"),
                ShlokaWord("वीर्यम्", "v\u012Bryam", "energy")
            )
        ),
        SanskritShloka(
            id = "panchakshara",
            source = "Panchakshara Stotram",
            devanagari = "नागेन्द्रहाराय त्रिलोचनाय\nभस्माङ्गरागाय महेश्वराय ।",
            transliteration = "n\u0101gendr\u0101h\u0101r\u0101ya trilocan\u0101ya\nbhasm\u0101\u1E45gar\u0101g\u0101ya mahe\u015Bvar\u0101ya",
            translation = "To the one who wears the king of serpents as a garland, who has three eyes, who is smeared with sacred ash, the great Lord.",
            words = listOf(
                ShlokaWord("नागेन्द्र", "n\u0101gendra", "king of serpents"),
                ShlokaWord("हाराय", "h\u0101r\u0101ya", "garland"),
                ShlokaWord("त्रिलोचनाय", "trilocan\u0101ya", "three-eyed"),
                ShlokaWord("महेश्वराय", "mahe\u015Bvar\u0101ya", "great lord")
            )
        ),
        SanskritShloka(
            id = "om_namah_vs",
            source = "Vishnu Sahasranama",
            devanagari = "ॐ विश्वं विष्णुर्वषट्कारो भूतभव्यभवत्प्रभुः ।",
            transliteration = "o\u1E43 vi\u015Bva\u1E43 vi\u1E63\u1E47ur va\u1E63a\u1E6Dk\u0101ro bh\u016Btabhavyabhavatprabhu\u1E25",
            translation = "Om, the universe, Vishnu, the lord of all beings — past, present, and future.",
            words = listOf(
                ShlokaWord("विश्वम्", "vi\u015Bvam", "universe"),
                ShlokaWord("विष्णुः", "vi\u1E63\u1E47u\u1E25", "Vishnu"),
                ShlokaWord("प्रभुः", "prabhu\u1E25", "lord")
            )
        ),
        SanskritShloka(
            id = "hanuman_doha",
            source = "Hanuman Chalisa (Opening)",
            devanagari = "श्रीगुरु चरन सरोज रज निज मनु मुकुरु सुधारि ।\nबरनउँ रघुबर बिमल जसु जो दायकु फल चारि ॥",
            transliteration = "\u015Br\u012B-guru cara\u1E47a saroja raja nija manu mukuru sudh\u0101ri\nbaranau\u1E43 raghubara bimala jasu jo d\u0101yaku phala c\u0101ri",
            translation = "Cleansing the mirror of my mind with the dust of my Guru's lotus feet, I describe the pure glory of Rama, bestower of the four fruits.",
            words = listOf(
                ShlokaWord("श्रीगुरु", "\u015Br\u012Bguru", "divine teacher"),
                ShlokaWord("चरण", "cara\u1E47a", "feet"),
                ShlokaWord("सरोज", "saroja", "lotus"),
                ShlokaWord("रघुबर", "raghubara", "Rama")
            )
        )
    )

    fun shlokasByCategory(category: VerseTextCategory): List<SanskritShloka> = when (category) {
        VerseTextCategory.GITA -> shlokas.filter { it.id.startsWith("bg_") }
        VerseTextCategory.UPANISHADS -> shlokas.filter { it.id in listOf("isha_1", "asato_ma", "taittiriya_peace") }
        VerseTextCategory.RUDRAM -> shlokas.filter { it.id in listOf("panchakshara", "om_namah_vs") }
        VerseTextCategory.CHALISA -> shlokas.filter { it.id == "hanuman_doha" }
    }

    // ── Exercise Helpers ─────────────────────────────────────────────────────
    private fun pickDistractorLetters(target: SanskritLetter, pool: List<SanskritLetter>, count: Int = 3): List<SanskritLetter> {
        return pool.filter { it.id != target.id }.shuffled().take(count)
    }

    private fun pickDistractorWords(target: SanskritWord, pool: List<SanskritWord>, count: Int = 3): List<SanskritWord> {
        return pool.filter { it.id != target.id }.shuffled().take(count)
    }

    private fun pickDistractorSyllables(target: SanskritSyllable, pool: List<SanskritSyllable>, count: Int = 3): List<SanskritSyllable> {
        return pool.filter { it.id != target.id }.shuffled().take(count)
    }

    private fun letterExercises(letters: List<SanskritLetter>, pool: List<SanskritLetter>): List<SanskritExercise> {
        return letters.flatMap { letter ->
            listOf(
                SanskritExercise.LetterToSound(letter, pickDistractorLetters(letter, pool)),
                SanskritExercise.SoundToLetter(letter, pickDistractorLetters(letter, pool))
            )
        }
    }

    private fun wordExercises(words: List<SanskritWord>, pool: List<SanskritWord>): List<SanskritExercise> {
        return words.map { word ->
            SanskritExercise.WordMeaning(word, pickDistractorWords(word, pool))
        }
    }

    private fun syllableExercises(syllables: List<SanskritSyllable>, pool: List<SanskritSyllable>): List<SanskritExercise> {
        return syllables.flatMap { syl ->
            listOf(
                SanskritExercise.SyllableToSound(syl, pickDistractorSyllables(syl, pool)),
                SanskritExercise.SoundToSyllable(syl, pickDistractorSyllables(syl, pool))
            )
        }
    }

    // ── 13 Modules with 47 Lessons ───────────────────────────────────────────
    val modules: List<SanskritModule> by lazy {
        listOf(
            // Module 1: Vowels (Swaras)
            SanskritModule("module1", "Vowels (Swaras)", "स्वर", "~", listOf(
                SanskritLesson("module1_l1", "First Vowels",
                    SanskritContextCard("अ आ इ ई", "a \u0101 i \u012B", "The building blocks of Sanskrit — every word begins with these sounds."),
                    letterExercises(vowels.take(4), vowels)),
                SanskritLesson("module1_l2", "More Vowels",
                    SanskritContextCard("उ ऊ ए ऐ", "u \u016B e ai", "Extended vowels that shape the melody of mantras."),
                    letterExercises(vowels.subList(4, 8), vowels)),
                SanskritLesson("module1_l3", "Final Vowels",
                    SanskritContextCard("ओ औ अं अः", "o au a\u1E43 a\u1E25", "The completion sounds — nasals and breath that end sacred syllables."),
                    letterExercises(vowels.subList(8, 12), vowels)),
                SanskritLesson("module1_l4", "Vowel Review",
                    SanskritContextCard("अ आ इ ई उ ऊ ए ऐ ओ औ अं अः", "a \u0101 i \u012B u \u016B e ai o au a\u1E43 a\u1E25", "Master all 12 vowels — the foundation of Devanagari script."),
                    letterExercises(vowels, vowels)),
                SanskritLesson("module1_l5", "Sacred Words I",
                    SanskritContextCard("ॐ नमस्ते धर्म", "om namaste dharma", "Your first sacred words — greetings and cosmic principles."),
                    wordExercises(sacredWords.take(6), sacredWords))
            )),

            // Module 2: Consonants I
            SanskritModule("module2", "Consonants I", "व्यंजन १", "A", listOf(
                SanskritLesson("module2_l1", "Velars",
                    SanskritContextCard("क ख ग घ", "ka kha ga gha", "Throat sounds — like the 'k' in karma and 'g' in guru."),
                    letterExercises(velars, velars + palatals)),
                SanskritLesson("module2_l2", "Palatals & Dentals",
                    SanskritContextCard("च ज थ ध", "ca ja tha dha", "Tongue-tip sounds — from 'ch' in chakra to 'dh' in dharma."),
                    letterExercises(palatals, palatals + velars)),
                SanskritLesson("module2_l3", "Labials",
                    SanskritContextCard("म भ", "ma bha", "Lip sounds — the gentle 'm' of mantra and breathy 'bh' of bhagavan."),
                    letterExercises(labials + dentals.take(2), labials + dentals)),
                SanskritLesson("module2_l4", "Sacred Words II",
                    SanskritContextCard("भक्ति योग आत्मा माया", "bhakti yoga \u0101tm\u0101 m\u0101y\u0101", "Deeper sacred words — the paths and principles of Hindu philosophy."),
                    wordExercises(sacredWords.subList(4, 12), sacredWords))
            )),

            // Module 3: Consonants II
            SanskritModule("module3", "Consonants II", "व्यंजन २", "B", listOf(
                SanskritLesson("module3_l1", "Semivowels",
                    SanskritContextCard("य र ल व", "ya ra la va", "Flowing sounds — yoga, r\u0101ma, love, and Vishnu."),
                    letterExercises(semivowels, semivowels + sibilants)),
                SanskritLesson("module3_l2", "Sibilants",
                    SanskritContextCard("श ष स ह", "\u015Ba \u1E63a sa ha", "Breath sounds — \u015B\u0101nti, K\u1E5B\u1E63\u1E47a, satya, Hanuman."),
                    letterExercises(sibilants, sibilants + semivowels)),
                SanskritLesson("module3_l3", "Retroflexes",
                    SanskritContextCard("ट ठ ड", "\u1E6Da \u1E6Dha \u1E0Da", "Curled-tongue sounds unique to Sanskrit."),
                    letterExercises(retroflexes, retroflexes + dentals))
            )),

            // Module 4: Sacred Words
            SanskritModule("module4", "Sacred Words", "पवित्र शब्द", "P", listOf(
                SanskritLesson("module4_l1", "Greeting & Dharma",
                    SanskritContextCard("नमस्ते धर्म कर्म सत्य", "namaste dharma karma satya", "Words of virtue and cosmic order."),
                    wordExercises(sacredWords.filter { it.id in listOf("namaste", "dharma", "karma", "satya", "ahimsa") }, sacredWords)),
                SanskritLesson("module4_l2", "Divine Names",
                    SanskritContextCard("शिव कृष्ण राम", "\u015Biva k\u1E5B\u1E63\u1E47a r\u0101ma", "The names of the divine — each carries infinite meaning."),
                    wordExercises(sacredWords.filter { it.context == "deity" }, sacredWords)),
                SanskritLesson("module4_l3", "Om & Virtues",
                    SanskritContextCard("ॐ भक्ति शक्ति मोक्ष", "om bhakti \u015Bakti mok\u1E63a", "The path from devotion to liberation."),
                    wordExercises(sacredWords.filter { it.id in listOf("om", "bhakti", "shakti", "moksha", "yoga", "atma") }, sacredWords)),
                SanskritLesson("module4_l4", "Sacred Words Review",
                    SanskritContextCard("प्रणाम जप पूजा मन्त्र", "pra\u1E47\u0101ma japa p\u016Bj\u0101 mantra", "The practices that connect us to the divine."),
                    wordExercises(sacredWords.filter { it.context == "practice" || it.context == "greeting" }, sacredWords))
            )),

            // Module 5: Mantras
            SanskritModule("module5", "Mantras", "मंत्र", "O", listOf(
                SanskritLesson("module5_l1", "Om Namah Shivaya",
                    SanskritContextCard("ॐ नमः शिवाय", "o\u1E43 nama\u1E25 \u015Biv\u0101ya", "The five-syllable mantra to Lord Shiva — each syllable represents an element."),
                    wordExercises(listOf(
                        SanskritWord("om_ns", "ॐ", "o\u1E43", "sacred sound", "mantra"),
                        SanskritWord("namah_ns", "नमः", "nama\u1E25", "salutation", "mantra"),
                        SanskritWord("shivaya_ns", "शिवाय", "\u015Biv\u0101ya", "to Shiva", "mantra")
                    ), sacredWords)),
                SanskritLesson("module5_l2", "Gayatri Mantra",
                    SanskritContextCard("ॐ भूर्भुवः स्वः", "o\u1E43 bh\u016Br bhuva\u1E25 sva\u1E25", "The most sacred Vedic mantra — a prayer for illumination of the intellect."),
                    wordExercises(listOf(
                        SanskritWord("bhur_gm", "भूः", "bh\u016B\u1E25", "earth plane", "mantra"),
                        SanskritWord("bhuvah_gm", "भुवः", "bhuva\u1E25", "astral plane", "mantra"),
                        SanskritWord("svah_gm", "स्वः", "sva\u1E25", "celestial plane", "mantra")
                    ), sacredWords)),
                SanskritLesson("module5_l3", "Asato Ma",
                    SanskritContextCard("असतो मा सद्गमय", "asato m\u0101 sad gamaya", "Lead me from untruth to truth — a universal prayer from the Upanishads."),
                    wordExercises(listOf(
                        SanskritWord("asato_am", "असतः", "asata\u1E25", "from untruth", "mantra"),
                        SanskritWord("sat_am", "सत्", "sat", "truth", "mantra"),
                        SanskritWord("gamaya_am", "गमय", "gamaya", "lead", "mantra"),
                        SanskritWord("tamaso_am", "तमसः", "tamasa\u1E25", "from darkness", "mantra"),
                        SanskritWord("jyotir_am", "ज्योतिः", "jyoti\u1E25", "light", "mantra")
                    ), sacredWords))
            )),

            // Module 6: Vowel Signs (Matras)
            SanskritModule("module6", "Vowel Signs (Matras)", "मात्रा", "M", listOf(
                SanskritLesson("module6_l1", "\u0101-matra",
                    SanskritContextCard("का ना मा रा", "k\u0101 n\u0101 m\u0101 r\u0101", "The long '\u0101' sign — a vertical stroke added to the right."),
                    syllableExercises(syllables.filter { it.markName == "\u0101-matra" }.take(6), syllables)),
                SanskritLesson("module6_l2", "i/\u012B matras",
                    SanskritContextCard("कि की नि नी", "ki k\u012B ni n\u012B", "Short and long 'i' signs — hooks before and after the consonant."),
                    syllableExercises(syllables.filter { it.markName in listOf("i-matra", "\u012B-matra") }, syllables)),
                SanskritLesson("module6_l3", "u/\u016B matras",
                    SanskritContextCard("कु कू मु पु", "ku k\u016B mu pu", "The 'u' signs — curved marks below the consonant."),
                    syllableExercises(syllables.filter { it.markName in listOf("u-matra", "\u016B-matra") }, syllables)),
                SanskritLesson("module6_l4", "e/ai/o/au matras",
                    SanskritContextCard("के कै को कौ", "ke kai ko kau", "Compound vowel marks — transforming the top of each letter."),
                    syllableExercises(syllables.filter { it.markName in listOf("e-matra", "ai-matra", "o-matra", "au-matra") }, syllables)),
                SanskritLesson("module6_l5", "Review + Special Signs",
                    SanskritContextCard("ह दे दै वि", "ha de dai vi", "Mixed review of all vowel signs."),
                    syllableExercises(syllables.shuffled().take(10), syllables))
            )),

            // Module 7: Conjunct Consonants
            SanskritModule("module7", "Conjunct Consonants", "संयुक्त व्यंजन", "L", listOf(
                SanskritLesson("module7_l1", "r-forms & Sri",
                    SanskritContextCard("प्र त्र श्र श्री", "pra tra \u015Bra \u015Br\u012B", "Consonant combinations with 'r' — seen in pra\u1E47\u0101ma, mantra, and \u015Br\u012B."),
                    letterExercises(conjuncts.take(4), conjuncts)),
                SanskritLesson("module7_l2", "Key Gita conjuncts",
                    SanskritContextCard("कृ क्ष ज्ञ द्व", "k\u1E5B k\u1E63a j\u00F1a dva", "Conjuncts found throughout the Bhagavad Gita."),
                    letterExercises(conjuncts.subList(4, 9), conjuncts)),
                SanskritLesson("module7_l3", "Virama conjuncts",
                    SanskritContextCard("धर् कर् नर् श्र", "dhar kar nar \u015Br", "Half-letter forms using the virama — seen in dharma, karma."),
                    letterExercises(conjuncts.subList(10, 14), conjuncts))
            )),

            // Module 8: Reading Sanskrit Words
            SanskritModule("module8", "Reading Sanskrit Words", "शब्द पाठ", "R", listOf(
                SanskritLesson("module8_l1", "Two-syllable words",
                    SanskritContextCard("देव योग पूजा गुरु", "deva yoga p\u016Bj\u0101 guru", "Begin reading complete words — two syllables at a time."),
                    wordExercises(sacredWords.filter { it.sanskrit.length <= 4 }.take(8), sacredWords)),
                SanskritLesson("module8_l2", "Three-syllable words",
                    SanskritContextCard("नमस्ते मन्त्र प्रणाम", "namaste mantra pra\u1E47\u0101ma", "Longer words that combine everything you've learned."),
                    wordExercises(sacredWords.filter { it.sanskrit.length > 4 }.take(8), sacredWords)),
                SanskritLesson("module8_l3", "Gita vocabulary",
                    SanskritContextCard("क्षेत्र प्रकृति पुरुष गुण", "k\u1E63etra prak\u1E5Bti puru\u1E63a gu\u1E47a", "Key terms from the Bhagavad Gita."),
                    wordExercises(gitaWords.take(8), gitaWords))
            )),

            // Module 9: Gita & Upanishad Words
            SanskritModule("module9", "Gita & Upanishad Words", "गीता शब्द", "G", listOf(
                SanskritLesson("module9_l1", "Field & Three Qualities",
                    SanskritContextCard("सत्त्व रजस् तमस्", "sattva rajas tamas", "The three gu\u1E47as that govern all of nature."),
                    wordExercises(gitaWords.filter { it.id in listOf("kshetra", "prakriti", "purusha", "guna", "sattva", "rajas", "tamas") }, gitaWords)),
                SanskritLesson("module9_l2", "Self & Soul",
                    SanskritContextCard("बुद्धि विवेक वैराग्य", "buddhi viveka vair\u0101gya", "The faculties of mind and spirit."),
                    wordExercises(gitaWords.filter { it.id in listOf("buddhi", "viveka", "vairagya", "jnana", "brahman") }, gitaWords)),
                SanskritLesson("module9_l3", "Knowledge & Liberation",
                    SanskritContextCard("समाधि मुक्ति त्याग सेवा", "sam\u0101dhi mukti ty\u0101ga sev\u0101", "The culmination of spiritual practice."),
                    wordExercises(gitaWords.filter { it.id in listOf("samadhi", "mukti", "seva", "tyaga") }, gitaWords))
            )),

            // Module 10: Vishnu Sahasranama & Rudram
            SanskritModule("module10", "Vishnu Sahasranama & Rudram", "सहस्रनाम", "V", listOf(
                SanskritLesson("module10_l1", "Names of Vishnu",
                    SanskritContextCard("अनन्त अच्युत गोविन्द", "ananta acyuta govinda", "The thousand names — each an aspect of the Preserver."),
                    wordExercises(stotrasWords.filter { it.context == "vishnu" }.take(6), stotrasWords)),
                SanskritLesson("module10_l2", "Names of Shiva",
                    SanskritContextCard("रुद्र शंकर महेश्वर", "rudra \u015Ba\u1E45kara mahe\u015Bvara", "Names of the Destroyer-Transformer."),
                    wordExercises(stotrasWords.filter { it.context == "shiva" }, stotrasWords)),
                SanskritLesson("module10_l3", "Titles & Epithets",
                    SanskritContextCard("भगवान् प्रभु स्वामी", "bhagav\u0101n prabhu sv\u0101m\u012B", "Universal titles of respect for the divine."),
                    wordExercises(stotrasWords.filter { it.context == "title" }, stotrasWords))
            )),

            // Module 11: Devotional Texts
            SanskritModule("module11", "Devotional Texts", "भक्ति ग्रन्थ", "H", listOf(
                SanskritLesson("module11_l1", "Hanuman Chalisa",
                    SanskritContextCard("हनुमान् प्रसाद", "hanum\u0101n pras\u0101da", "Words from the beloved prayer to Hanuman."),
                    wordExercises(devotionalWords.filter { it.id in listOf("hanuman", "prasad") }, devotionalWords + sacredWords)),
                SanskritLesson("module11_l2", "Devi Mahatmya",
                    SanskritContextCard("दुर्गा शक्ति", "durg\u0101 \u015Bakti", "Words celebrating the divine feminine."),
                    wordExercises(devotionalWords.filter { it.id in listOf("durga", "padma", "nada", "chakra") }, devotionalWords)),
                SanskritLesson("module11_l3", "Yoga & Tantra",
                    SanskritContextCard("चक्र नाद उपासना", "cakra n\u0101da up\u0101san\u0101", "The subtle body and methods of practice."),
                    wordExercises(devotionalWords.filter { it.id in listOf("upasana", "chakra", "nada") }, devotionalWords + sacredWords))
            )),

            // Module 12: Mantra Reading Practice
            SanskritModule("module12", "Mantra Reading Practice", "मंत्र पाठ", "S", listOf(
                SanskritLesson("module12_l1", "Gita Shlokas",
                    SanskritContextCard("कर्मण्येवाधिकारस्ते", "karma\u1E47y ev\u0101dhik\u0101ras te", "Read and understand key Gita verses word by word."),
                    wordExercises(shlokas[0].words.map { SanskritWord(it.sanskrit, it.sanskrit, it.transliteration, it.meaning, "shloka") }, sacredWords + gitaWords)),
                SanskritLesson("module12_l2", "Upanishad Peace Mantras",
                    SanskritContextCard("असतो मा सद्गमय", "asato m\u0101 sad gamaya", "The beloved peace invocations."),
                    wordExercises(shlokas[5].words.map { SanskritWord(it.sanskrit, it.sanskrit, it.transliteration, it.meaning, "shloka") }, sacredWords + gitaWords)),
                SanskritLesson("module12_l3", "Invocations",
                    SanskritContextCard("ॐ सह नाववतु", "o\u1E43 saha n\u0101v avatu", "The opening prayer for study and knowledge."),
                    wordExercises(shlokas[6].words.map { SanskritWord(it.sanskrit, it.sanskrit, it.transliteration, it.meaning, "shloka") }, sacredWords))
            )),

            // Module 13: Reading the Texts
            SanskritModule("module13", "Reading the Texts", "ग्रन्थ पाठ", "T", listOf(
                SanskritLesson("module13_l1", "Bhagavad Gita — The Soul",
                    SanskritContextCard("न जायते म्रियते वा कदाचित्", "na j\u0101yate mriyate v\u0101 kad\u0101cit", "The immortal soul verse from Chapter 2."),
                    wordExercises(shlokas[1].words.map { SanskritWord(it.sanskrit, it.sanskrit, it.transliteration, it.meaning, "shloka") }, sacredWords + gitaWords)),
                SanskritLesson("module13_l2", "Vishnu Sahasranama",
                    SanskritContextCard("ॐ विश्वं विष्णुः", "o\u1E43 vi\u015Bva\u1E43 vi\u1E63\u1E47u\u1E25", "The opening verse of the thousand names."),
                    wordExercises(shlokas[8].words.map { SanskritWord(it.sanskrit, it.sanskrit, it.transliteration, it.meaning, "shloka") }, stotrasWords)),
                SanskritLesson("module13_l3", "Hanuman Chalisa — Opening",
                    SanskritContextCard("श्रीगुरु चरन सरोज", "\u015Br\u012Bguru cara\u1E47a saroja", "The opening doha that begins every Chalisa recitation."),
                    wordExercises(shlokas[9].words.map { SanskritWord(it.sanskrit, it.sanskrit, it.transliteration, it.meaning, "shloka") }, sacredWords + devotionalWords))
            ))
        )
    }

    val totalLessons: Int get() = modules.sumOf { it.lessons.size }
}
