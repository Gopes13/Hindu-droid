package dev.gopes.hinducalendar.domain.model

/**
 * Grammar teaching content for Kāṇḍas 3-7.
 * Rich content cards with sections, tables, examples, and rules.
 */
data class GrammarCard(
    val titleKey: String,
    val sections: List<GrammarSection>
)

data class GrammarSection(
    val type: GrammarSectionType,
    val content: Map<String, String> = emptyMap(),
    val tableData: GrammarTable? = null,
    val examples: List<GrammarExample> = emptyList(),
    val ruleText: Map<String, String> = emptyMap()
)

enum class GrammarSectionType {
    TEXT,
    TABLE,
    EXAMPLE,
    RULE,
    DIAGRAM
}

data class GrammarTable(
    val headers: List<String>,
    val rows: List<List<String>>
)

data class GrammarExample(
    val sanskrit: String,
    val transliteration: String,
    val translation: Map<String, String>,
    val annotation: Map<String, String> = emptyMap()
)

/**
 * A sentence with per-word grammatical annotations.
 * Used in CaseDetective and sentence analysis exercises.
 */
data class AnnotatedSentence(
    val words: List<AnnotatedWord>,
    val translation: Map<String, String>
)

data class AnnotatedWord(
    val sanskrit: String,
    val transliteration: String,
    val meaning: Map<String, String>,
    val vibhakti: String? = null,
    val partOfSpeech: String? = null
)
