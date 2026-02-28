package dev.gopes.hinducalendar.domain.model

import com.google.gson.annotations.SerializedName

// ── Gita ────────────────────────────────────────────────────────────────────

data class GitaVerse(
    @SerializedName("verse") val verse: Int,
    @SerializedName("sanskrit") val sanskrit: String,
    @SerializedName("transliteration") val transliteration: String? = null,
    @SerializedName("translations") val translations: Map<String, String> = emptyMap()
) {
    fun translation(lang: AppLanguage): String = translations.localized(lang)
}

data class GitaChapter(
    @SerializedName("chapter") val chapter: Int,
    @SerializedName("titleEnglish") val title: String? = null,
    @SerializedName("titleSanskrit") val sanskritTitle: String? = null,
    @SerializedName("totalVerses") val verseCount: Int,
    @SerializedName("verses") val verses: List<GitaVerse>
)

data class GitaData(
    @SerializedName("chapters") val chapters: List<GitaChapter>
) {
    val totalVerses: Int get() = chapters.sumOf { it.verseCount }
}

// ── Hanuman Chalisa ─────────────────────────────────────────────────────────

data class ChalisaVerse(
    @SerializedName("verse") val verse: Int,
    @SerializedName("type") val type: String? = null,
    @SerializedName("sanskrit") val sanskrit: String = "",
    @SerializedName("transliteration") val transliteration: String? = null,
    @SerializedName("translations") val translations: Map<String, String> = emptyMap()
) {
    fun translation(lang: AppLanguage): String = translations.localized(lang)
}

data class ChalisaData(
    @SerializedName("dohas") val dohas: List<ChalisaVerse> = emptyList(),
    @SerializedName("chaupais") val chaupais: List<ChalisaVerse> = emptyList(),
    @SerializedName("doha_closing") val closingDoha: ChalisaVerse? = null
) {
    val allVerses: List<ChalisaVerse>
        get() = dohas + chaupais + listOfNotNull(closingDoha)
}

// ── Episode-Based Texts (Bhagavata, Shiva Purana) ───────────────────────────

data class RelatedVerse(
    @SerializedName("sanskrit") val sanskrit: String = "",
    @SerializedName("transliteration") val transliteration: String? = null,
    @SerializedName("translations") val translations: Map<String, String> = emptyMap()
) {
    fun translation(lang: AppLanguage): String = translations.localized(lang)
}

data class RelatedMantra(
    @SerializedName("sanskrit") val sanskrit: String = "",
    @SerializedName("transliteration") val transliteration: String? = null,
    @SerializedName("meanings") val meanings: Map<String, String> = emptyMap()
) {
    fun meaning(lang: AppLanguage): String = meanings.localized(lang)
}

data class Episode(
    @SerializedName("episode") val episode: Int,
    @SerializedName("titles") val titles: Map<String, String> = emptyMap(),
    @SerializedName("summaries") val summaries: Map<String, String> = emptyMap(),
    @SerializedName("keyTeachings") val keyTeachings: Map<String, String> = emptyMap(),
    @SerializedName("relatedVerse") val relatedVerse: RelatedVerse? = null,
    @SerializedName("relatedMantra") val relatedMantra: RelatedMantra? = null
) {
    fun title(lang: AppLanguage): String = titles.localized(lang)
    fun summary(lang: AppLanguage): String = summaries.localized(lang)
    fun keyTeaching(lang: AppLanguage): String = keyTeachings.localized(lang)
}

data class EpisodeTextData(
    @SerializedName("episodes") val episodes: List<Episode> = emptyList()
)

// ── Shloka-Based Texts (Vishnu Sahasranama, Shikshapatri) ───────────────────

data class ShlokaName(
    @SerializedName("name") val name: String = "",
    @SerializedName("meanings") val meanings: Map<String, String> = emptyMap()
) {
    fun meaning(lang: AppLanguage): String = meanings.localized(lang)
}

data class Shloka(
    @SerializedName("shloka") val shloka: Int,
    @SerializedName("sanskrit") val sanskrit: String = "",
    @SerializedName("transliteration") val transliteration: String? = null,
    @SerializedName("translations") val translations: Map<String, String> = emptyMap(),
    @SerializedName("commentaries") val commentaries: Map<String, String>? = null,
    @SerializedName("explanations") val explanations: Map<String, String>? = null,
    @SerializedName("names") val names: List<ShlokaName>? = null
) {
    fun translation(lang: AppLanguage): String = translations.localized(lang)
    fun commentary(lang: AppLanguage): String = commentaries?.localized(lang) ?: ""
    fun explanation(lang: AppLanguage): String = explanations?.localized(lang) ?: ""
}

data class DhyanaShloka(
    @SerializedName("sanskrit") val sanskrit: String = "",
    @SerializedName("transliteration") val transliteration: String? = null,
    @SerializedName("translations") val translations: Map<String, String> = emptyMap()
) {
    fun translation(lang: AppLanguage): String = translations.localized(lang)
}

data class ShlokaTextData(
    @SerializedName("dhyanaShloka") val dhyanaShloka: DhyanaShloka? = null,
    @SerializedName("shlokas") val shlokas: List<Shloka> = emptyList()
)

// ── Verse-Based Texts (Soundarya Lahari) ────────────────────────────────────

data class NumberedVerse(
    @SerializedName("verse") val verse: Int,
    @SerializedName("section") val section: String? = null,
    @SerializedName("sanskrit") val sanskrit: String = "",
    @SerializedName("transliteration") val transliteration: String? = null,
    @SerializedName("translations") val translations: Map<String, String> = emptyMap(),
    @SerializedName("themes") val themes: Map<String, String>? = null
) {
    fun translation(lang: AppLanguage): String = translations.localized(lang)
    fun theme(lang: AppLanguage): String = themes?.localized(lang) ?: ""
}

data class VerseTextData(
    @SerializedName("verses") val verses: List<NumberedVerse> = emptyList()
)

// ── Chapter-Based Texts (Devi Mahatmya) ─────────────────────────────────────

data class KeyVerse(
    @SerializedName("sanskrit") val sanskrit: String = "",
    @SerializedName("transliteration") val transliteration: String? = null,
    @SerializedName("translations") val translations: Map<String, String> = emptyMap()
) {
    fun translation(lang: AppLanguage): String = translations.localized(lang)
}

data class Kavach(
    @SerializedName("sanskrit") val sanskrit: String = "",
    @SerializedName("transliteration") val transliteration: String? = null,
    @SerializedName("translations") val translations: Map<String, String> = emptyMap()
) {
    fun translation(lang: AppLanguage): String = translations.localized(lang)
}

data class TextChapter(
    @SerializedName("chapter") val chapter: Int,
    @SerializedName("titles") val titles: Map<String, String> = emptyMap(),
    @SerializedName("summaries") val summaries: Map<String, String> = emptyMap(),
    @SerializedName("keyVerses") val keyVerses: List<KeyVerse>? = null
) {
    fun title(lang: AppLanguage): String = titles.localized(lang)
    fun summary(lang: AppLanguage): String = summaries.localized(lang)
}

data class ChapterTextData(
    @SerializedName("kavach") val kavach: Kavach? = null,
    @SerializedName("chapters") val chapters: List<TextChapter> = emptyList()
)

// ── Rudram ───────────────────────────────────────────────────────────────────

data class Anuvaka(
    @SerializedName("anuvaka") val anuvaka: Int,
    @SerializedName("sanskrit") val sanskrit: String = "",
    @SerializedName("transliteration") val transliteration: String? = null,
    @SerializedName("translations") val translations: Map<String, String> = emptyMap(),
    @SerializedName("themes") val themes: Map<String, String>? = null
) {
    fun translation(lang: AppLanguage): String = translations.localized(lang)
    fun theme(lang: AppLanguage): String = themes?.localized(lang) ?: ""
}

data class RudramSection(
    @SerializedName("anuvakas") val anuvakas: List<Anuvaka> = emptyList()
)

data class RudramData(
    @SerializedName("namakam") val namakam: RudramSection? = null,
    @SerializedName("chamakam") val chamakam: RudramSection? = null
)

// ── Japji Sahib ─────────────────────────────────────────────────────────────

data class JapjiPauri(
    @SerializedName("pauri") val pauri: Int,
    @SerializedName("punjabi") val punjabi: String = "",
    @SerializedName("transliteration") val transliteration: String = "",
    @SerializedName("translations") val translations: Map<String, String> = emptyMap()
) {
    fun translation(lang: AppLanguage): String = translations.localized(lang)
}

data class MoolMantar(
    @SerializedName("punjabi") val punjabi: String = "",
    @SerializedName("transliteration") val transliteration: String = "",
    @SerializedName("translations") val translations: Map<String, String> = emptyMap()
) {
    fun translation(lang: AppLanguage): String = translations.localized(lang)
}

data class JapjiSalok(
    @SerializedName("punjabi") val punjabi: String = "",
    @SerializedName("hindi") val hindi: String = "",
    @SerializedName("transliteration") val transliteration: String = "",
    @SerializedName("translation") val translation: String = ""
)

data class JapjiData(
    @SerializedName("moolMantar") val moolMantar: MoolMantar? = null,
    @SerializedName("pauris") val pauris: List<JapjiPauri> = emptyList(),
    @SerializedName("salok") val salok: JapjiSalok? = null
)

// ── Gurbani ─────────────────────────────────────────────────────────────────

data class Shabad(
    @SerializedName("day") val day: Int,
    @SerializedName("page") val page: Int? = null,
    @SerializedName("author") val author: String = "",
    @SerializedName("punjabi") val punjabi: String = "",
    @SerializedName("transliteration") val transliteration: String = "",
    @SerializedName("translations") val translations: Map<String, String> = emptyMap(),
    @SerializedName("themes") val themes: Map<String, String>? = null
) {
    fun translation(lang: AppLanguage): String = translations.localized(lang)
    fun theme(lang: AppLanguage): String = themes?.localized(lang) ?: ""
}

data class GurbaniData(
    @SerializedName("shabads") val shabads: List<Shabad> = emptyList()
)

// ── Sukhmani Sahib ──────────────────────────────────────────────────────────

data class SukhmaniSalok(
    @SerializedName("punjabi") val punjabi: String = "",
    @SerializedName("transliteration") val transliteration: String = "",
    @SerializedName("translations") val translations: Map<String, String> = emptyMap()
) {
    fun translation(lang: AppLanguage): String = translations.localized(lang)
}

data class SukhmaniStanza(
    @SerializedName("stanza") val stanza: Int,
    @SerializedName("punjabi") val punjabi: String = "",
    @SerializedName("transliteration") val transliteration: String = "",
    @SerializedName("translations") val translations: Map<String, String> = emptyMap()
) {
    fun translation(lang: AppLanguage): String = translations.localized(lang)
}

data class SukhmaniSection(
    @SerializedName("ashtpadi") val ashtpadi: Int,
    @SerializedName("salok") val salok: SukhmaniSalok? = null,
    @SerializedName("summaries") val summaries: Map<String, String>? = null,
    @SerializedName("themes") val themes: Map<String, String>? = null,
    @SerializedName("stanzas") val stanzas: List<SukhmaniStanza> = emptyList()
) {
    fun summary(lang: AppLanguage): String = summaries?.localized(lang) ?: ""
    fun theme(lang: AppLanguage): String = themes?.localized(lang) ?: ""
}

data class SukhmaniData(
    @SerializedName("ashtpadis") val ashtpadis: List<SukhmaniSection> = emptyList()
)

// ── Tattvartha Sutra ────────────────────────────────────────────────────────

data class Sutra(
    @SerializedName("sutra") val sutra: Int,
    @SerializedName("sanskrit") val sanskrit: String = "",
    @SerializedName("transliteration") val transliteration: String? = null,
    @SerializedName("translations") val translations: Map<String, String> = emptyMap(),
    @SerializedName("commentaries") val commentaries: Map<String, String>? = null
) {
    fun translation(lang: AppLanguage): String = translations.localized(lang)
    fun commentary(lang: AppLanguage): String = commentaries?.localized(lang) ?: ""
}

data class SutraChapter(
    @SerializedName("chapter") val chapter: Int,
    @SerializedName("titles") val titles: Map<String, String> = emptyMap(),
    @SerializedName("sutras") val sutras: List<Sutra> = emptyList()
) {
    fun title(lang: AppLanguage): String = titles.localized(lang)
}

data class SutraTextData(
    @SerializedName("chapters") val chapters: List<SutraChapter> = emptyList()
)

// ── Vachanamrut (Discourse-Based) ───────────────────────────────────────────

data class Discourse(
    @SerializedName("discourse") val discourse: Int,
    @SerializedName("section") val section: String? = null,
    @SerializedName("titles") val titles: Map<String, String> = emptyMap(),
    @SerializedName("summaries") val summaries: Map<String, String> = emptyMap(),
    @SerializedName("keyQuestions") val keyQuestions: Map<String, String>? = null,
    @SerializedName("keyTeachings") val keyTeachings: Map<String, String>? = null,
    @SerializedName("quotes") val quotes: Map<String, String>? = null
) {
    fun title(lang: AppLanguage): String = titles.localized(lang)
    fun summary(lang: AppLanguage): String = summaries.localized(lang)
    fun keyQuestion(lang: AppLanguage): String = keyQuestions?.localized(lang) ?: ""
    fun keyTeaching(lang: AppLanguage): String = keyTeachings?.localized(lang) ?: ""
    fun quote(lang: AppLanguage): String = quotes?.localized(lang) ?: ""
}

data class DiscourseTextData(
    @SerializedName("discourses") val discourses: List<Discourse> = emptyList()
)

// ── Jain Prayers ────────────────────────────────────────────────────────────

data class NamokarLine(
    @SerializedName("line") val line: Int,
    @SerializedName("sanskrit") val sanskrit: String = "",
    @SerializedName("transliteration") val transliteration: String? = null,
    @SerializedName("translations") val translations: Map<String, String> = emptyMap(),
    @SerializedName("significances") val significances: Map<String, String>? = null
) {
    fun translation(lang: AppLanguage): String = translations.localized(lang)
    fun significance(lang: AppLanguage): String = significances?.localized(lang) ?: ""
}

data class NamokarMantra(
    @SerializedName("sanskrit") val sanskrit: String = "",
    @SerializedName("transliteration") val transliteration: String? = null,
    @SerializedName("translations") val translations: Map<String, String> = emptyMap(),
    @SerializedName("descriptions") val descriptions: Map<String, String>? = null,
    @SerializedName("lineByLine") val lineByLine: List<NamokarLine> = emptyList()
) {
    fun translation(lang: AppLanguage): String = translations.localized(lang)
    fun description(lang: AppLanguage): String = descriptions?.localized(lang) ?: ""
}

data class MahaviraTeaching(
    @SerializedName("episode") val episode: Int,
    @SerializedName("titles") val titles: Map<String, String> = emptyMap(),
    @SerializedName("contents") val contents: Map<String, String> = emptyMap(),
    @SerializedName("keyQuotes") val keyQuotes: Map<String, String>? = null,
    @SerializedName("lessons") val lessons: Map<String, String>? = null
) {
    fun title(lang: AppLanguage): String = titles.localized(lang)
    fun content(lang: AppLanguage): String = contents.localized(lang)
    fun keyQuote(lang: AppLanguage): String = keyQuotes?.localized(lang) ?: ""
    fun lesson(lang: AppLanguage): String = lessons?.localized(lang) ?: ""
}

data class JainPrayersData(
    @SerializedName("namokarMantra") val namokarMantra: NamokarMantra? = null,
    @SerializedName("mahaviraTeachings") val mahaviraTeachings: List<MahaviraTeaching> = emptyList()
)

// ── Daily Content Result ────────────────────────────────────────────────────

data class DailyVerse(
    val textType: SacredTextType,
    val title: String,
    val subtitle: String,
    val sanskrit: String?,
    val transliteration: String?,
    val translation: String,
    val commentary: String?,
    val position: Int,
    val totalCount: Int
)

data class DailyContent(
    val primaryVerse: DailyVerse?,
    val secondaryVerse: DailyVerse?
)
