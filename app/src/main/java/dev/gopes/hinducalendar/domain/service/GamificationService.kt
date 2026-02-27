package dev.gopes.hinducalendar.domain.service

import dev.gopes.hinducalendar.domain.model.*

/**
 * Contract for all gamification reward and badge logic.
 * Backed by [GamificationServiceImpl] in production; in-memory fake in tests.
 */
interface GamificationService {

    companion object {
        const val POINTS_APP_OPEN = 5
        const val POINTS_VERSE_VIEW = 5
        const val POINTS_VERSE_READ = 10
        const val POINTS_CHALLENGE = 15
        const val POINTS_FESTIVAL_STORY = 5
        const val POINTS_TEXT_COMPLETION = 100
        const val POINTS_REFLECTION = 5
        const val POINTS_DEEP_STUDY = 3
        const val MAX_DEEP_STUDY_POINTS_PER_DAY = 50
        const val POINTS_JAPA_ROUND = 1
        const val MAX_JAPA_ROUNDS_REWARDED = 10
        const val POINTS_DIYA_LIGHTING = 2
        const val POINTS_DIYA_STREAK_BONUS = 5
        const val POINTS_SANSKRIT_LETTER = 5
        const val POINTS_SANSKRIT_MODULE = 50
        const val POINTS_SANSKRIT_VERSE = 5
    }

    // Daily rewards
    fun rewardAppOpen(data: GamificationData, streak: StreakData): GamificationData
    fun rewardVerseView(data: GamificationData): GamificationData
    fun rewardVerseRead(data: GamificationData): GamificationData
    fun rewardChallenge(data: GamificationData): GamificationData
    fun rewardFestivalStory(data: GamificationData, festivalId: String): GamificationData
    fun rewardTextCompletion(data: GamificationData, textType: SacredTextType): GamificationData
    fun rewardReflection(data: GamificationData): GamificationData
    fun rewardDeepStudy(data: GamificationData): GamificationData

    // Tracking
    fun trackExplanationView(data: GamificationData): GamificationData
    fun trackPanchangCheck(data: GamificationData): GamificationData
    fun trackDharmaPath(data: GamificationData, pathId: String): GamificationData
    fun trackLanguage(data: GamificationData, language: String): GamificationData

    // Badge checking
    fun checkAndAwardBadges(data: GamificationData, streak: StreakData): Pair<GamificationData, List<String>>

    // Japa & Diya
    fun rewardJapaRound(data: GamificationData, japaState: JapaState): GamificationData
    fun rewardDiyaLighting(data: GamificationData, diyaState: DiyaState): GamificationData
    fun checkJapaBadges(data: GamificationData, japaState: JapaState): GamificationData
    fun checkDiyaBadges(data: GamificationData, diyaState: DiyaState): GamificationData

    // Sanskrit
    fun rewardSanskritLesson(data: GamificationData, points: Int): GamificationData
    fun rewardSanskritLetter(data: GamificationData): GamificationData
    fun rewardSanskritModule(data: GamificationData): GamificationData
    fun rewardSanskritVerse(data: GamificationData): GamificationData
    fun checkSanskritBadges(data: GamificationData, progress: SanskritProgress): GamificationData

    // Daily challenge
    fun generateDailyChallenge(): DailyChallenge
}
