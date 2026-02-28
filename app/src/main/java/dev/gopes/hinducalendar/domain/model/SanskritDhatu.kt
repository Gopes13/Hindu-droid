package dev.gopes.hinducalendar.domain.model

/**
 * A verbal root (dhātu) — the foundation of Sanskrit word formation.
 * Example: √vid (to know) → vidyā, veda, vidvān, etc.
 */
data class SanskritDhatu(
    val id: String,
    val root: String,
    val rootDevanagari: String,
    val meanings: Map<String, String>,
    val gana: Int = 1,
    val derivatives: List<DhatuDerivative> = emptyList()
)

data class DhatuDerivative(
    val wordId: String,
    val sanskrit: String,
    val transliteration: String,
    val meanings: Map<String, String>,
    val formationType: String = ""
)
