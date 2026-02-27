package dev.gopes.hinducalendar.data.repository

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.gopes.hinducalendar.domain.model.*
import dev.gopes.hinducalendar.domain.service.GamificationService
import dev.gopes.hinducalendar.domain.service.gamification.BadgeChecker
import dev.gopes.hinducalendar.data.repository.ChallengeGenerator
import dev.gopes.hinducalendar.domain.service.gamification.DailyRewardsCalculator
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GamificationServiceImpl @Inject constructor(
    @ApplicationContext context: Context
) : GamificationService {

    private val challenges = ChallengeGenerator(context)

    // Daily rewards
    override fun rewardAppOpen(data: GamificationData, streak: StreakData) =
        DailyRewardsCalculator.rewardAppOpen(data, streak)
    override fun rewardVerseView(data: GamificationData) =
        DailyRewardsCalculator.rewardVerseView(data)
    override fun rewardVerseRead(data: GamificationData) =
        DailyRewardsCalculator.rewardVerseRead(data)
    override fun rewardChallenge(data: GamificationData) =
        DailyRewardsCalculator.rewardChallenge(data)
    override fun rewardFestivalStory(data: GamificationData, festivalId: String) =
        DailyRewardsCalculator.rewardFestivalStory(data, festivalId)
    override fun rewardTextCompletion(data: GamificationData, textType: SacredTextType) =
        DailyRewardsCalculator.rewardTextCompletion(data, textType)
    override fun rewardReflection(data: GamificationData) =
        DailyRewardsCalculator.rewardReflection(data)
    override fun rewardDeepStudy(data: GamificationData) =
        DailyRewardsCalculator.rewardDeepStudy(data)

    // Tracking
    override fun trackExplanationView(data: GamificationData) =
        DailyRewardsCalculator.trackExplanationView(data)
    override fun trackPanchangCheck(data: GamificationData) =
        DailyRewardsCalculator.trackPanchangCheck(data)
    override fun trackDharmaPath(data: GamificationData, pathId: String) =
        DailyRewardsCalculator.trackDharmaPath(data, pathId)
    override fun trackLanguage(data: GamificationData, language: String) =
        DailyRewardsCalculator.trackLanguage(data, language)

    // Badges
    override fun checkAndAwardBadges(data: GamificationData, streak: StreakData) =
        BadgeChecker.checkAndAwardBadges(data, streak)
    override fun checkJapaBadges(data: GamificationData, japaState: JapaState) =
        BadgeChecker.checkJapaBadges(data, japaState)
    override fun checkDiyaBadges(data: GamificationData, diyaState: DiyaState) =
        BadgeChecker.checkDiyaBadges(data, diyaState)
    override fun checkSanskritBadges(data: GamificationData, progress: SanskritProgress) =
        BadgeChecker.checkSanskritBadges(data, progress)

    // Japa & Diya
    override fun rewardJapaRound(data: GamificationData, japaState: JapaState) =
        DailyRewardsCalculator.rewardJapaRound(data, japaState)
    override fun rewardDiyaLighting(data: GamificationData, diyaState: DiyaState) =
        DailyRewardsCalculator.rewardDiyaLighting(data, diyaState)

    // Sanskrit
    override fun rewardSanskritLesson(data: GamificationData, points: Int) =
        DailyRewardsCalculator.rewardSanskritLesson(data, points)
    override fun rewardSanskritLetter(data: GamificationData) =
        DailyRewardsCalculator.rewardSanskritLetter(data)
    override fun rewardSanskritModule(data: GamificationData) =
        DailyRewardsCalculator.rewardSanskritModule(data)
    override fun rewardSanskritVerse(data: GamificationData) =
        DailyRewardsCalculator.rewardSanskritVerse(data)

    // Challenge
    override fun generateDailyChallenge() =
        challenges.generateDailyChallenge()
}
