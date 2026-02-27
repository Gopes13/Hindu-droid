package dev.gopes.hinducalendar.domain.repository

import dev.gopes.hinducalendar.domain.model.*

/**
 * Contract for loading and querying sacred text data.
 * Backed by [SacredTextService] in production; fake in tests.
 */
interface SacredTextRepository {
    fun loadGita(): GitaData?
    fun loadChalisa(): ChalisaData?
    fun loadJapji(): JapjiData?
    fun loadGurbani(): GurbaniData?
    fun loadSukhmani(): SukhmaniData?
    fun loadRudram(): RudramData?
    fun loadJainPrayers(): JainPrayersData?
    fun loadEpisodeText(textId: String): EpisodeTextData?
    fun loadShlokaText(textId: String): ShlokaTextData?
    fun loadVerseText(textId: String): VerseTextData?
    fun loadChapterText(textId: String): ChapterTextData?
    fun loadSutraText(textId: String): SutraTextData?
    fun loadDiscourseText(textId: String): DiscourseTextData?
    fun getGitaVerseSequential(globalIndex: Int, lang: AppLanguage = AppLanguage.ENGLISH): DailyVerse?
    fun getChalisaVerse(index: Int, lang: AppLanguage = AppLanguage.ENGLISH): DailyVerse?
    fun getJapjiPauri(number: Int, lang: AppLanguage = AppLanguage.ENGLISH): DailyVerse?
    fun getVerseForText(textType: SacredTextType, progress: ReadingProgress, lang: AppLanguage = AppLanguage.ENGLISH): DailyVerse?
    fun textTypeForId(textId: String): SacredTextType?
    fun totalCount(textType: SacredTextType): Int
    fun getDailyContent(path: DharmaPath, progress: ReadingProgress, lang: AppLanguage = AppLanguage.ENGLISH): DailyContent
}
