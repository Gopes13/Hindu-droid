package dev.gopes.hinducalendar.engine

import dev.gopes.hinducalendar.domain.repository.SacredTextRepository

import dev.gopes.hinducalendar.domain.model.*

/**
 * In-memory fake for unit testing ViewModels that depend on [SacredTextRepository].
 */
class FakeSacredTextRepository : SacredTextRepository {

    var gitaData: GitaData? = null
    var chalisaData: ChalisaData? = null
    var japjiData: JapjiData? = null
    var gurbaniData: GurbaniData? = null
    var sukhmaniData: SukhmaniData? = null
    var rudramData: RudramData? = null
    var jainPrayersData: JainPrayersData? = null
    var dailyContent: DailyContent? = null

    private val episodeTexts = mutableMapOf<String, EpisodeTextData>()
    private val shlokaTexts = mutableMapOf<String, ShlokaTextData>()
    private val verseTexts = mutableMapOf<String, VerseTextData>()
    private val chapterTexts = mutableMapOf<String, ChapterTextData>()
    private val sutraTexts = mutableMapOf<String, SutraTextData>()
    private val discourseTexts = mutableMapOf<String, DiscourseTextData>()

    override fun loadGita(): GitaData? = gitaData
    override fun loadChalisa(): ChalisaData? = chalisaData
    override fun loadJapji(): JapjiData? = japjiData
    override fun loadGurbani(): GurbaniData? = gurbaniData
    override fun loadSukhmani(): SukhmaniData? = sukhmaniData
    override fun loadRudram(): RudramData? = rudramData
    override fun loadJainPrayers(): JainPrayersData? = jainPrayersData

    override fun loadEpisodeText(textId: String): EpisodeTextData? = episodeTexts[textId]
    override fun loadShlokaText(textId: String): ShlokaTextData? = shlokaTexts[textId]
    override fun loadVerseText(textId: String): VerseTextData? = verseTexts[textId]
    override fun loadChapterText(textId: String): ChapterTextData? = chapterTexts[textId]
    override fun loadSutraText(textId: String): SutraTextData? = sutraTexts[textId]
    override fun loadDiscourseText(textId: String): DiscourseTextData? = discourseTexts[textId]

    override fun getGitaVerseSequential(globalIndex: Int, lang: AppLanguage): DailyVerse? = null
    override fun getChalisaVerse(index: Int, lang: AppLanguage): DailyVerse? = null
    override fun getJapjiPauri(number: Int, lang: AppLanguage): DailyVerse? = null
    override fun getVerseForText(textType: SacredTextType, progress: ReadingProgress, lang: AppLanguage): DailyVerse? = null
    override fun textTypeForId(textId: String): SacredTextType? = null
    override fun totalCount(textType: SacredTextType): Int = 0

    override fun getDailyContent(path: DharmaPath, progress: ReadingProgress, lang: AppLanguage): DailyContent {
        return dailyContent ?: DailyContent(
            primaryVerse = null,
            secondaryVerse = null
        )
    }
}
