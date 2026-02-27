package dev.gopes.hinducalendar.data.repository

import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import com.google.gson.Gson
import dev.gopes.hinducalendar.R
import dev.gopes.hinducalendar.domain.model.*
import dev.gopes.hinducalendar.domain.repository.SacredTextRepository
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SacredTextService @Inject constructor(
    private val context: Context
) : SacredTextRepository {
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

    private fun getLocalizedContext(): Context {
        val locales = AppCompatDelegate.getApplicationLocales()
        if (locales.isEmpty) return context
        val locale = locales[0] ?: return context
        val config = Configuration(context.resources.configuration).apply { setLocale(locale) }
        return context.createConfigurationContext(config)
    }

    private fun s(id: Int): String = getLocalizedContext().getString(id)
    private fun s(id: Int, vararg args: Any): String = getLocalizedContext().getString(id, *args)

    private fun localizedTextName(textType: SacredTextType): String = when (textType) {
        SacredTextType.GITA -> s(R.string.text_name_gita)
        SacredTextType.HANUMAN_CHALISA -> s(R.string.text_name_hanuman_chalisa)
        SacredTextType.JAPJI_SAHIB -> s(R.string.text_name_japji_sahib)
        SacredTextType.BHAGAVATA -> s(R.string.text_name_bhagavata)
        SacredTextType.VISHNU_SAHASRANAMA -> s(R.string.text_name_vishnu_sahasranama)
        SacredTextType.SHIVA_PURANA -> s(R.string.text_name_shiva_purana)
        SacredTextType.RUDRAM -> s(R.string.text_name_rudram)
        SacredTextType.DEVI_MAHATMYA -> s(R.string.text_name_devi_mahatmya)
        SacredTextType.SOUNDARYA_LAHARI -> s(R.string.text_name_soundarya_lahari)
        SacredTextType.SHIKSHAPATRI -> s(R.string.text_name_shikshapatri)
        SacredTextType.VACHANAMRUT -> s(R.string.text_name_vachanamrut)
        SacredTextType.SUKHMANI -> s(R.string.text_name_sukhmani)
        SacredTextType.GURBANI -> s(R.string.text_name_gurbani)
        SacredTextType.TATTVARTHA_SUTRA -> s(R.string.text_name_tattvartha_sutra)
        SacredTextType.JAIN_PRAYERS -> s(R.string.text_name_jain_prayers)
    }

    private fun <T> loadJson(fileName: String, clazz: Class<T>): T? {
        return try {
            val json = context.assets.open("$fileName.json").bufferedReader().use { it.readText() }
            gson.fromJson(json, clazz)
        } catch (e: Exception) {
            Timber.e(e, "Failed to load JSON asset: %s.json", fileName)
            null
        }
    }

    override fun loadGita(): GitaData? {
        if (cachedGita != null) return cachedGita
        return loadJson("gita", GitaData::class.java).also { cachedGita = it }
    }

    override fun loadChalisa(): ChalisaData? {
        if (cachedChalisa != null) return cachedChalisa
        return loadJson("hanuman_chalisa", ChalisaData::class.java).also { cachedChalisa = it }
    }

    override fun loadJapji(): JapjiData? {
        if (cachedJapji != null) return cachedJapji
        return loadJson("japji_sahib", JapjiData::class.java).also { cachedJapji = it }
    }

    override fun loadGurbani(): GurbaniData? {
        if (cachedGurbani != null) return cachedGurbani
        return loadJson("gurbani", GurbaniData::class.java).also { cachedGurbani = it }
    }

    override fun loadSukhmani(): SukhmaniData? {
        if (cachedSukhmani != null) return cachedSukhmani
        return loadJson("sukhmani", SukhmaniData::class.java).also { cachedSukhmani = it }
    }

    override fun loadRudram(): RudramData? {
        if (cachedRudram != null) return cachedRudram
        return loadJson("rudram", RudramData::class.java).also { cachedRudram = it }
    }

    override fun loadJainPrayers(): JainPrayersData? {
        if (cachedJainPrayers != null) return cachedJainPrayers
        return loadJson("jain_prayers", JainPrayersData::class.java).also { cachedJainPrayers = it }
    }

    override fun loadEpisodeText(textId: String): EpisodeTextData? {
        cachedEpisodeTexts[textId]?.let { return it }
        return loadJson(textId, EpisodeTextData::class.java)?.also { cachedEpisodeTexts[textId] = it }
    }

    override fun loadShlokaText(textId: String): ShlokaTextData? {
        cachedShlokaTexts[textId]?.let { return it }
        return loadJson(textId, ShlokaTextData::class.java)?.also { cachedShlokaTexts[textId] = it }
    }

    override fun loadVerseText(textId: String): VerseTextData? {
        cachedVerseTexts[textId]?.let { return it }
        return loadJson(textId, VerseTextData::class.java)?.also { cachedVerseTexts[textId] = it }
    }

    override fun loadChapterText(textId: String): ChapterTextData? {
        cachedChapterTexts[textId]?.let { return it }
        return loadJson(textId, ChapterTextData::class.java)?.also { cachedChapterTexts[textId] = it }
    }

    override fun loadSutraText(textId: String): SutraTextData? {
        cachedSutraTexts[textId]?.let { return it }
        return loadJson(textId, SutraTextData::class.java)?.also { cachedSutraTexts[textId] = it }
    }

    override fun loadDiscourseText(textId: String): DiscourseTextData? {
        cachedDiscourseTexts[textId]?.let { return it }
        return loadJson(textId, DiscourseTextData::class.java)?.also { cachedDiscourseTexts[textId] = it }
    }

    override fun getGitaVerseSequential(globalIndex: Int, lang: AppLanguage): DailyVerse? {
        val gita = loadGita() ?: return null
        val total = gita.totalVerses
        if (total <= 0) return null
        val safeIndex = ((globalIndex - 1) % total) + 1

        var runningCount = 0
        for (chapter in gita.chapters) {
            if (runningCount + chapter.verseCount >= safeIndex) {
                val verseIndex = safeIndex - runningCount - 1
                val verse = chapter.verses.getOrNull(verseIndex) ?: return null
                return DailyVerse(
                    textType = SacredTextType.GITA,
                    title = s(R.string.text_name_gita),
                    subtitle = s(R.string.chapter_verse_format, chapter.chapter, verse.verse),
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

    override fun getChalisaVerse(index: Int, lang: AppLanguage): DailyVerse? {
        val chalisa = loadChalisa() ?: return null
        val allVerses = chalisa.allVerses
        val total = allVerses.size
        if (total <= 0) return null
        val safeIndex = ((index - 1) % total)
        val verse = allVerses.getOrNull(safeIndex) ?: return null
        return DailyVerse(
            textType = SacredTextType.HANUMAN_CHALISA,
            title = s(R.string.text_name_hanuman_chalisa),
            subtitle = s(R.string.verse_ref_format, (verse.type ?: s(R.string.text_verse)).replaceFirstChar { it.uppercase() }, verse.verse),
            sanskrit = verse.sanskrit,
            transliteration = verse.transliteration,
            translation = verse.translation(lang),
            commentary = null,
            position = safeIndex + 1,
            totalCount = total
        )
    }

    override fun getJapjiPauri(number: Int, lang: AppLanguage): DailyVerse? {
        val japji = loadJapji() ?: return null
        val total = japji.pauris.size
        if (total <= 0) return null
        val safeNumber = ((number - 1) % total) + 1
        val pauri = japji.pauris.firstOrNull { it.pauri == safeNumber } ?: return null
        return DailyVerse(
            textType = SacredTextType.JAPJI_SAHIB,
            title = s(R.string.text_name_japji_sahib),
            subtitle = s(R.string.verse_ref_format, s(R.string.text_pauri), safeNumber),
            sanskrit = pauri.punjabi,
            transliteration = pauri.transliteration,
            translation = pauri.translation(lang),
            commentary = null,
            position = safeNumber,
            totalCount = total
        )
    }

    override fun getVerseForText(textType: SacredTextType, progress: ReadingProgress, lang: AppLanguage): DailyVerse? {
        val position = progress.currentPosition(textType)
        return when (textType) {
            SacredTextType.GITA -> getGitaVerseSequential(position, lang)
            SacredTextType.HANUMAN_CHALISA -> getChalisaVerse(position, lang)
            SacredTextType.JAPJI_SAHIB -> getJapjiPauri(position, lang)
            else -> getGenericVerse(textType, position, lang)
        }
    }

    private fun getGenericVerse(textType: SacredTextType, index: Int, lang: AppLanguage): DailyVerse? {
        val fileName = textType.jsonFileName

        // Episode-based
        loadEpisodeText(fileName)?.let { data ->
            if (data.episodes.isNotEmpty()) {
                val total = data.episodes.size
                val safeIndex = ((index - 1) % total) + 1
                val ep = data.episodes.firstOrNull { it.episode == safeIndex } ?: return null
                return DailyVerse(
                    textType = textType,
                    title = localizedTextName(textType),
                    subtitle = "${s(R.string.text_episode)} $safeIndex: ${ep.title(lang)}",
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
                    title = localizedTextName(textType),
                    subtitle = s(R.string.verse_ref_format, s(R.string.text_shloka), shloka.shloka),
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
                    title = localizedTextName(textType),
                    subtitle = s(R.string.verse_ref_format, s(R.string.text_verse), verse.verse),
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

    override fun textTypeForId(textId: String): SacredTextType? {
        return SacredTextType.entries.find { it.jsonFileName == textId }
    }

    override fun totalCount(textType: SacredTextType): Int {
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

    override fun getDailyContent(path: DharmaPath, progress: ReadingProgress, lang: AppLanguage): DailyContent {
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
