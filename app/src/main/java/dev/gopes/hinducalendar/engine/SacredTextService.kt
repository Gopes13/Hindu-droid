package dev.gopes.hinducalendar.engine

import android.content.Context
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import dev.gopes.hinducalendar.data.model.AppLanguage
import dev.gopes.hinducalendar.data.model.DharmaPath
import dev.gopes.hinducalendar.data.model.ReadingProgress
import dev.gopes.hinducalendar.data.model.SacredTextType
import dev.gopes.hinducalendar.data.model.localized
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

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
    @SerializedName("title") val title: String? = null,
    @SerializedName("sanskrit_title") val sanskritTitle: String? = null,
    @SerializedName("verse_count") val verseCount: Int,
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
    @SerializedName("closingDoha") val closingDoha: ChalisaVerse? = null
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

data class JapjiData(
    @SerializedName("moolMantar") val moolMantar: MoolMantar? = null,
    @SerializedName("pauris") val pauris: List<JapjiPauri> = emptyList()
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

// ── Service ─────────────────────────────────────────────────────────────────

@Singleton
class SacredTextService @Inject constructor(
    private val context: Context
) {
    private val gson = Gson()

    // Cached data to avoid re-parsing
    private var cachedGita: GitaData? = null
    private var cachedChalisa: ChalisaData? = null
    private var cachedJapji: JapjiData? = null
    private var cachedGurbani: GurbaniData? = null
    private var cachedSukhmani: SukhmaniData? = null
    private var cachedRudram: RudramData? = null
    private var cachedJainPrayers: JainPrayersData? = null
    private val cachedEpisodeTexts = mutableMapOf<String, EpisodeTextData>()
    private val cachedShlokaTexts = mutableMapOf<String, ShlokaTextData>()
    private val cachedVerseTexts = mutableMapOf<String, VerseTextData>()
    private val cachedChapterTexts = mutableMapOf<String, ChapterTextData>()
    private val cachedSutraTexts = mutableMapOf<String, SutraTextData>()
    private val cachedDiscourseTexts = mutableMapOf<String, DiscourseTextData>()

    private fun <T> loadJson(fileName: String, clazz: Class<T>): T? {
        return try {
            val json = context.assets.open("$fileName.json").bufferedReader().use { it.readText() }
            gson.fromJson(json, clazz)
        } catch (e: Exception) {
            Timber.e(e, "Failed to load JSON asset: %s.json", fileName)
            null
        }
    }

    fun loadGita(): GitaData? {
        if (cachedGita != null) return cachedGita
        return loadJson("gita", GitaData::class.java).also { cachedGita = it }
    }

    fun loadChalisa(): ChalisaData? {
        if (cachedChalisa != null) return cachedChalisa
        return loadJson("hanuman_chalisa", ChalisaData::class.java).also { cachedChalisa = it }
    }

    fun loadJapji(): JapjiData? {
        if (cachedJapji != null) return cachedJapji
        return loadJson("japji_sahib", JapjiData::class.java).also { cachedJapji = it }
    }

    fun loadGurbani(): GurbaniData? {
        if (cachedGurbani != null) return cachedGurbani
        return loadJson("gurbani", GurbaniData::class.java).also { cachedGurbani = it }
    }

    fun loadSukhmani(): SukhmaniData? {
        if (cachedSukhmani != null) return cachedSukhmani
        return loadJson("sukhmani", SukhmaniData::class.java).also { cachedSukhmani = it }
    }

    fun loadRudram(): RudramData? {
        if (cachedRudram != null) return cachedRudram
        return loadJson("rudram", RudramData::class.java).also { cachedRudram = it }
    }

    fun loadJainPrayers(): JainPrayersData? {
        if (cachedJainPrayers != null) return cachedJainPrayers
        return loadJson("jain_prayers", JainPrayersData::class.java).also { cachedJainPrayers = it }
    }

    fun loadEpisodeText(textId: String): EpisodeTextData? {
        cachedEpisodeTexts[textId]?.let { return it }
        return loadJson(textId, EpisodeTextData::class.java)?.also { cachedEpisodeTexts[textId] = it }
    }

    fun loadShlokaText(textId: String): ShlokaTextData? {
        cachedShlokaTexts[textId]?.let { return it }
        return loadJson(textId, ShlokaTextData::class.java)?.also { cachedShlokaTexts[textId] = it }
    }

    fun loadVerseText(textId: String): VerseTextData? {
        cachedVerseTexts[textId]?.let { return it }
        return loadJson(textId, VerseTextData::class.java)?.also { cachedVerseTexts[textId] = it }
    }

    fun loadChapterText(textId: String): ChapterTextData? {
        cachedChapterTexts[textId]?.let { return it }
        return loadJson(textId, ChapterTextData::class.java)?.also { cachedChapterTexts[textId] = it }
    }

    fun loadSutraText(textId: String): SutraTextData? {
        cachedSutraTexts[textId]?.let { return it }
        return loadJson(textId, SutraTextData::class.java)?.also { cachedSutraTexts[textId] = it }
    }

    fun loadDiscourseText(textId: String): DiscourseTextData? {
        cachedDiscourseTexts[textId]?.let { return it }
        return loadJson(textId, DiscourseTextData::class.java)?.also { cachedDiscourseTexts[textId] = it }
    }

    /**
     * Returns a Gita verse by sequential (global) index across all chapters.
     */
    fun getGitaVerseSequential(globalIndex: Int, lang: AppLanguage = AppLanguage.ENGLISH): DailyVerse? {
        val gita = loadGita() ?: return null
        val total = gita.totalVerses
        val safeIndex = ((globalIndex - 1) % total) + 1

        var runningCount = 0
        for (chapter in gita.chapters) {
            if (runningCount + chapter.verseCount >= safeIndex) {
                val verseIndex = safeIndex - runningCount - 1
                val verse = chapter.verses.getOrNull(verseIndex) ?: return null
                return DailyVerse(
                    textType = SacredTextType.GITA,
                    title = "Bhagavad Gita",
                    subtitle = "Chapter ${chapter.chapter} - Verse ${verse.verse}",
                    sanskrit = verse.sanskrit,
                    transliteration = verse.transliteration,
                    translation = verse.translation(lang),
                    commentary = null,
                    position = safeIndex,
                    totalCount = total
                )
            }
            runningCount += chapter.verseCount
        }
        return null
    }

    /**
     * Returns a Chalisa verse by index (1-based).
     */
    fun getChalisaVerse(index: Int, lang: AppLanguage = AppLanguage.ENGLISH): DailyVerse? {
        val chalisa = loadChalisa() ?: return null
        val allVerses = chalisa.allVerses
        val total = allVerses.size
        val safeIndex = ((index - 1) % total)
        val verse = allVerses.getOrNull(safeIndex) ?: return null
        return DailyVerse(
            textType = SacredTextType.HANUMAN_CHALISA,
            title = "Hanuman Chalisa",
            subtitle = "${(verse.type ?: "Verse").replaceFirstChar { it.uppercase() }} ${verse.verse}",
            sanskrit = verse.sanskrit,
            transliteration = verse.transliteration,
            translation = verse.translation(lang),
            commentary = null,
            position = safeIndex + 1,
            totalCount = total
        )
    }

    /**
     * Returns a Japji Sahib pauri by number (1-based).
     */
    fun getJapjiPauri(number: Int, lang: AppLanguage = AppLanguage.ENGLISH): DailyVerse? {
        val japji = loadJapji() ?: return null
        val total = japji.pauris.size
        val safeNumber = ((number - 1) % total) + 1
        val pauri = japji.pauris.firstOrNull { it.pauri == safeNumber } ?: return null
        return DailyVerse(
            textType = SacredTextType.JAPJI_SAHIB,
            title = "Japji Sahib",
            subtitle = "Pauri $safeNumber",
            sanskrit = pauri.punjabi,
            transliteration = pauri.transliteration,
            translation = pauri.translation(lang),
            commentary = null,
            position = safeNumber,
            totalCount = total
        )
    }

    /**
     * Returns a verse from any text type.
     */
    fun getVerseForText(textType: SacredTextType, progress: ReadingProgress, lang: AppLanguage = AppLanguage.ENGLISH): DailyVerse? {
        val position = progress.currentPosition(textType)
        return when (textType) {
            SacredTextType.GITA -> getGitaVerseSequential(position, lang)
            SacredTextType.HANUMAN_CHALISA -> getChalisaVerse(position, lang)
            SacredTextType.JAPJI_SAHIB -> getJapjiPauri(position, lang)
            else -> getGenericVerse(textType, position, lang)
        }
    }

    /**
     * Returns a verse from a generic text (episode, shloka, verse, chapter, etc.) by index.
     */
    private fun getGenericVerse(textType: SacredTextType, index: Int, lang: AppLanguage): DailyVerse? {
        // Try each data format and extract appropriately
        val fileName = textType.jsonFileName

        // Episode-based
        loadEpisodeText(fileName)?.let { data ->
            if (data.episodes.isNotEmpty()) {
                val total = data.episodes.size
                val safeIndex = ((index - 1) % total) + 1
                val ep = data.episodes.firstOrNull { it.episode == safeIndex } ?: return null
                return DailyVerse(
                    textType = textType,
                    title = textType.displayName,
                    subtitle = "Episode $safeIndex: ${ep.title(lang)}",
                    sanskrit = ep.relatedVerse?.sanskrit ?: ep.relatedMantra?.sanskrit,
                    transliteration = ep.relatedVerse?.transliteration ?: ep.relatedMantra?.transliteration,
                    translation = ep.summary(lang),
                    commentary = null,
                    position = safeIndex,
                    totalCount = total
                )
            }
        }

        // Shloka-based
        loadShlokaText(fileName)?.let { data ->
            if (data.shlokas.isNotEmpty()) {
                val total = data.shlokas.size
                val safeIndex = ((index - 1) % total) + 1
                val shloka = data.shlokas.getOrNull(safeIndex - 1) ?: return null
                return DailyVerse(
                    textType = textType,
                    title = textType.displayName,
                    subtitle = "Shloka ${shloka.shloka}",
                    sanskrit = shloka.sanskrit,
                    transliteration = shloka.transliteration,
                    translation = shloka.translation(lang),
                    commentary = shloka.commentary(lang).ifEmpty { null },
                    position = safeIndex,
                    totalCount = total
                )
            }
        }

        // Verse-based
        loadVerseText(fileName)?.let { data ->
            if (data.verses.isNotEmpty()) {
                val total = data.verses.size
                val safeIndex = ((index - 1) % total) + 1
                val verse = data.verses.getOrNull(safeIndex - 1) ?: return null
                return DailyVerse(
                    textType = textType,
                    title = textType.displayName,
                    subtitle = "Verse ${verse.verse}",
                    sanskrit = verse.sanskrit,
                    transliteration = verse.transliteration,
                    translation = verse.translation(lang),
                    commentary = null,
                    position = safeIndex,
                    totalCount = total
                )
            }
        }

        return null
    }

    fun textTypeForId(textId: String): SacredTextType? {
        return SacredTextType.entries.find { it.jsonFileName == textId }
    }

    fun totalCount(textType: SacredTextType): Int {
        return when (textType) {
            SacredTextType.GITA -> loadGita()?.totalVerses ?: 0
            SacredTextType.HANUMAN_CHALISA -> loadChalisa()?.allVerses?.size ?: 0
            else -> {
                val fileName = textType.jsonFileName
                loadEpisodeText(fileName)?.episodes?.size
                    ?: loadShlokaText(fileName)?.shlokas?.size
                    ?: loadVerseText(fileName)?.verses?.size
                    ?: 0
            }
        }
    }

    /**
     * Produces the daily content for a user's dharma path and reading progress.
     */
    fun getDailyContent(path: DharmaPath, progress: ReadingProgress, lang: AppLanguage = AppLanguage.ENGLISH): DailyContent {
        val primaryTextType = textTypeForId(path.primaryTextId)
        val primaryVerse = primaryTextType?.let { getVerseForText(it, progress, lang) }

        val secondaryTextId = path.availableTextIds.getOrNull(1)
        val secondaryTextType = secondaryTextId?.let { textTypeForId(it) }
        val secondaryVerse = if (secondaryTextType != null && secondaryTextType != primaryTextType) {
            getVerseForText(secondaryTextType, progress, lang)
        } else {
            null
        }

        return DailyContent(
            primaryVerse = primaryVerse,
            secondaryVerse = secondaryVerse
        )
    }
}
