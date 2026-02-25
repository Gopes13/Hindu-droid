package dev.gopes.hinducalendar.data.model

/** Kirtan categories — Aartis and Bhajans. */
enum class KirtanCategory(val jsonKey: String, val displayNameKey: String, val icon: String) {
    AARTI("aarti", "kirtans_aartis", "flame"),
    BHAJAN("bhajan", "kirtans_bhajans", "music_note");

    companion object {
        fun fromJson(key: String): KirtanCategory =
            entries.find { it.jsonKey == key } ?: BHAJAN
    }
}

/** Top-level JSON wrapper. */
data class KirtanData(
    val metadata: KirtanMetadata,
    val kirtans: List<Kirtan>
)

data class KirtanMetadata(
    val title: String,
    val totalKirtans: Int,
    val source: String
)

/** A single kirtan (aarti or bhajan). */
data class Kirtan(
    val id: String,
    val type: String,
    val originLanguage: String,
    val titleSanskrit: String,
    val titles: Map<String, String>,
    val author: String?,
    val stanzas: List<KirtanStanza>
) {
    val category: KirtanCategory get() = KirtanCategory.fromJson(type)

    fun title(language: AppLanguage): String =
        titles[language.code] ?: titles["en"] ?: titleSanskrit

    val stanzaCount: Int get() = stanzas.size

    val audioId: String get() = "kirtans_$id"
}

/** A single stanza within a kirtan. */
data class KirtanStanza(
    val stanza: Int,
    val lyrics: String,
    val translations: Map<String, String>
) {
    fun translation(language: AppLanguage): String =
        translations[language.code] ?: translations["en"] ?: ""
}
