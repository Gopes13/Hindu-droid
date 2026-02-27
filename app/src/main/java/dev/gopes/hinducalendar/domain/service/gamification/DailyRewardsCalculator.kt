package dev.gopes.hinducalendar.domain.service.gamification

import dev.gopes.hinducalendar.domain.model.*
import dev.gopes.hinducalendar.domain.service.GamificationService
import java.time.LocalDate

internal object DailyRewardsCalculator {

    fun rewardAppOpen(data: GamificationData, streak: StreakData): GamificationData {
        if (!data.isEnabled) return data
        val today = LocalDate.now().toString()
        if (data.lastAppOpenRewardDate == today) return data
        var updated = data
            .addPoints(GamificationService.POINTS_APP_OPEN)
            .copy(lastAppOpenRewardDate = today)
        if (data.lastStreakBonusDate != today) {
            updated = updated
                .addPoints(streak.streakBonus)
                .copy(lastStreakBonusDate = today)
        }
        return updated
    }

    fun rewardVerseView(data: GamificationData): GamificationData {
        if (!data.isEnabled) return data
        val today = LocalDate.now().toString()
        if (data.lastVerseViewRewardDate == today) return data
        return data
            .addPoints(GamificationService.POINTS_VERSE_VIEW)
            .copy(lastVerseViewRewardDate = today)
    }

    fun rewardVerseRead(data: GamificationData): GamificationData {
        if (!data.isEnabled) return data
        val today = LocalDate.now().toString()
        if (data.lastVerseReadRewardDate == today) return data
        return data
            .addPoints(GamificationService.POINTS_VERSE_READ)
            .copy(lastVerseReadRewardDate = today)
    }

    fun rewardChallenge(data: GamificationData): GamificationData {
        if (!data.isEnabled) return data
        val today = LocalDate.now().toString()
        if (data.lastChallengeDate == today && data.lastChallengeCompleted) return data
        return data
            .addPoints(GamificationService.POINTS_CHALLENGE)
            .copy(
                lastChallengeDate = today,
                lastChallengeCompleted = true,
                challengesSolved = data.challengesSolved + 1
            )
    }

    fun rewardFestivalStory(data: GamificationData, festivalId: String): GamificationData {
        if (!data.isEnabled) return data
        if (festivalId in data.festivalStoriesRead) return data
        return data
            .addPoints(GamificationService.POINTS_FESTIVAL_STORY)
            .copy(festivalStoriesRead = data.festivalStoriesRead + festivalId)
    }

    fun rewardTextCompletion(data: GamificationData, textType: SacredTextType): GamificationData {
        if (!data.isEnabled) return data
        if (textType.name in data.textsCompleted) return data
        return data
            .addPoints(GamificationService.POINTS_TEXT_COMPLETION)
            .copy(textsCompleted = data.textsCompleted + textType.name)
    }

    fun rewardReflection(data: GamificationData): GamificationData {
        if (!data.isEnabled) return data
        return data
            .addPoints(GamificationService.POINTS_REFLECTION)
            .copy(reflectionsWritten = data.reflectionsWritten + 1)
    }

    fun rewardDeepStudy(data: GamificationData): GamificationData {
        if (!data.isEnabled) return data
        val today = LocalDate.now().toString()
        val todayPoints = if (data.lastDeepStudyRewardDate == today) data.deepStudyPointsToday else 0
        if (todayPoints >= GamificationService.MAX_DEEP_STUDY_POINTS_PER_DAY) return data
        val reward = GamificationService.POINTS_DEEP_STUDY.coerceAtMost(GamificationService.MAX_DEEP_STUDY_POINTS_PER_DAY - todayPoints)
        return data
            .addPoints(reward)
            .copy(
                deepStudySessions = data.deepStudySessions + 1,
                lastDeepStudyRewardDate = today,
                deepStudyPointsToday = todayPoints + reward
            )
    }

    fun trackExplanationView(data: GamificationData): GamificationData {
        if (!data.isEnabled) return data
        return data.copy(versesExplained = data.versesExplained + 1)
    }

    fun trackPanchangCheck(data: GamificationData): GamificationData {
        if (!data.isEnabled) return data
        val today = LocalDate.now().toString()
        if (data.lastPanchangCheckDate == today) return data
        return data.copy(
            panchangDaysChecked = data.panchangDaysChecked + 1,
            lastPanchangCheckDate = today
        )
    }

    fun trackDharmaPath(data: GamificationData, pathId: String): GamificationData {
        if (!data.isEnabled) return data
        return data.copy(dharmaPathsExplored = data.dharmaPathsExplored + pathId)
    }

    fun trackLanguage(data: GamificationData, language: String): GamificationData {
        if (!data.isEnabled) return data
        return data.copy(languagesUsed = data.languagesUsed + language)
    }

    fun rewardJapaRound(data: GamificationData, japaState: JapaState): GamificationData {
        if (!data.isEnabled) return data
        if (japaState.roundsRewardedToday >= GamificationService.MAX_JAPA_ROUNDS_REWARDED) return data
        return data.addPoints(GamificationService.POINTS_JAPA_ROUND)
    }

    fun rewardDiyaLighting(data: GamificationData, diyaState: DiyaState): GamificationData {
        if (!data.isEnabled) return data
        val today = LocalDate.now().toString()
        if (diyaState.lastDiyaRewardDate == today) return data
        var updated = data.addPoints(GamificationService.POINTS_DIYA_LIGHTING)
        if (diyaState.lightingStreak > 0 && diyaState.lightingStreak % 7 == 0) {
            updated = updated.addPoints(GamificationService.POINTS_DIYA_STREAK_BONUS)
        }
        return updated
    }

    fun rewardSanskritLesson(data: GamificationData, points: Int): GamificationData {
        if (!data.isEnabled) return data
        return data.addPoints(points)
    }

    fun rewardSanskritLetter(data: GamificationData): GamificationData {
        if (!data.isEnabled) return data
        return data.addPoints(GamificationService.POINTS_SANSKRIT_LETTER)
    }

    fun rewardSanskritModule(data: GamificationData): GamificationData {
        if (!data.isEnabled) return data
        return data.addPoints(GamificationService.POINTS_SANSKRIT_MODULE)
    }

    fun rewardSanskritVerse(data: GamificationData): GamificationData {
        if (!data.isEnabled) return data
        return data.addPoints(GamificationService.POINTS_SANSKRIT_VERSE)
    }
}
