package dev.gopes.hinducalendar.domain.model

enum class SandhiType {
    SVARA,
    VYANJANA,
    VISARGA
}

/**
 * A sandhi (euphonic combination) rule.
 * Example: a + i = e (svara sandhi, guṇa substitution)
 */
data class SandhiRule(
    val id: String,
    val type: SandhiType,
    val firstEnding: String,
    val secondBeginning: String,
    val result: String,
    val ruleName: Map<String, String>,
    val explanation: Map<String, String>,
    val examples: List<SandhiExample> = emptyList()
)

data class SandhiExample(
    val word1: String,
    val word2: String,
    val combined: String,
    val transliteration: String
)
