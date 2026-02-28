package dev.gopes.hinducalendar.domain.model

/**
 * Noun declension paradigm.
 * 8 vibhaktis × 3 vacanas = 24 forms.
 */
data class NounParadigm(
    val id: String,
    val stemWord: String,
    val stemTranslit: String,
    val gender: SanskritGender,
    val endingType: String,
    val labels: Map<String, String>,
    val forms: List<List<String>>
)

enum class SanskritGender {
    PULLINGA,
    STRILINGA,
    NAPUMSAKALINGA
}

/**
 * Verb conjugation paradigm.
 * 3 purushas × 3 vacanas = 9 forms per lakāra.
 */
data class VerbParadigm(
    val id: String,
    val dhatu: String,
    val dhatuTranslit: String,
    val lakara: SanskritLakara,
    val pada: VerbPada,
    val labels: Map<String, String>,
    val forms: List<List<String>>
)

enum class SanskritLakara {
    LAT,
    LANG,
    LRT,
    LOT,
    VIDHILING
}

enum class VerbPada {
    PARASMAIPADA,
    ATMANEPADA
}

data class PronounParadigm(
    val id: String,
    val pronoun: String,
    val pronounTranslit: String,
    val labels: Map<String, String>,
    val forms: List<List<String>>
)
