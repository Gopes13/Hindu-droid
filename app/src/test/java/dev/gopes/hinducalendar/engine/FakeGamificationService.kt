package dev.gopes.hinducalendar.engine

import dev.gopes.hinducalendar.domain.service.GamificationService

import dev.gopes.hinducalendar.domain.model.*

/**
 * In-memory fake for unit testing. All reward methods apply points directly
 * without date guards, making tests deterministic.
 */
class FakeGamificationService : GamificationService {

    override fun rewardAppOpen(data: GamificationData, streak: StreakData): GamificationData {
        if (!data.isEnabled) return data
        return data.addPoints(GamificationService.POINTS_APP_OPEN)
    }

    override fun rewardVerseView(data: GamificationData): GamificationData {
        if (!data.isEnabled) return data
        return data.addPoints(GamificationService.POINTS_VERSE_VIEW)
    }

    override fun rewardVerseRead(data: GamificationData): GamificationData {
        if (!data.isEnabled) return data
        return data.addPoints(GamificationService.POINTS_VERSE_READ)
    }

    override fun rewardChallenge(data: GamificationData): GamificationData {
        if (!data.isEnabled) return data
        return data.addPoints(GamificationService.POINTS_CHALLENGE)
            .copy(challengesSolved = data.challengesSolved + 1, lastChallengeCompleted = true)
    }

    override fun rewardFestivalStory(data: GamificationData, festivalId: String): GamificationData {
        if (!data.isEnabled) return data
        return data.addPoints(GamificationService.POINTS_FESTIVAL_STORY)
            .copy(festivalStoriesRead = data.festivalStoriesRead + festivalId)
    }

    override fun rewardTextCompletion(data: GamificationData, textType: SacredTextType): GamificationData {
        if (!data.isEnabled) return data
        return data.addPoints(GamificationService.POINTS_TEXT_COMPLETION)
            .copy(textsCompleted = data.textsCompleted + textType.name)
    }

    override fun rewardReflection(data: GamificationData): GamificationData {
        if (!data.isEnabled) return data
        return data.addPoints(GamificationService.POINTS_REFLECTION)
            .copy(reflectionsWritten = data.reflectionsWritten + 1)
    }

    override fun rewardDeepStudy(data: GamificationData): GamificationData {
        if (!data.isEnabled) return data
        return data.addPoints(GamificationService.POINTS_DEEP_STUDY)
            .copy(deepStudySessions = data.deepStudySessions + 1)
    }

    override fun trackExplanationView(data: GamificationData): GamificationData {
        if (!data.isEnabled) return data
        return data.copy(versesExplained = data.versesExplained + 1)
    }

    override fun trackPanchangCheck(data: GamificationData): GamificationData {
        if (!data.isEnabled) return data
        return data.copy(panchangDaysChecked = data.panchangDaysChecked + 1)
    }

    override fun trackDharmaPath(data: GamificationData, pathId: String): GamificationData {
        if (!data.isEnabled) return data
        return data.copy(dharmaPathsExplored = data.dharmaPathsExplored + pathId)
    }

    override fun trackLanguage(data: GamificationData, language: String): GamificationData {
        if (!data.isEnabled) return data
        return data.copy(languagesUsed = data.languagesUsed + language)
    }

    override fun checkAndAwardBadges(data: GamificationData, streak: StreakData): Pair<GamificationData, List<String>> {
        // Simplified: no badge logic in fake
        return data to emptyList()
    }

    override fun rewardJapaRound(data: GamificationData, japaState: JapaState): GamificationData {
        if (!data.isEnabled) return data
        return data.addPoints(GamificationService.POINTS_JAPA_ROUND)
    }

    override fun rewardDiyaLighting(data: GamificationData, diyaState: DiyaState): GamificationData {
        if (!data.isEnabled) return data
        return data.addPoints(GamificationService.POINTS_DIYA_LIGHTING)
    }

    override fun checkJapaBadges(data: GamificationData, japaState: JapaState): GamificationData {
        // Pass-through: no badge logic in fake
        return data
    }

    override fun checkDiyaBadges(data: GamificationData, diyaState: DiyaState): GamificationData {
        return data
    }

    override fun rewardSanskritLesson(data: GamificationData, points: Int): GamificationData {
        if (!data.isEnabled) return data
        return data.addPoints(points)
    }

    override fun rewardSanskritLetter(data: GamificationData): GamificationData {
        if (!data.isEnabled) return data
        return data.addPoints(GamificationService.POINTS_SANSKRIT_LETTER)
    }

    override fun rewardSanskritModule(data: GamificationData): GamificationData {
        if (!data.isEnabled) return data
        return data.addPoints(GamificationService.POINTS_SANSKRIT_MODULE)
    }

    override fun rewardSanskritVerse(data: GamificationData): GamificationData {
        if (!data.isEnabled) return data
        return data.addPoints(GamificationService.POINTS_SANSKRIT_VERSE)
    }

    override fun checkSanskritBadges(data: GamificationData, progress: SanskritProgress): GamificationData {
        return data
    }

    override fun generateDailyChallenge(): DailyChallenge {
        return DailyChallenge(
            id = "test_challenge",
            type = ChallengeType.VERSE_REFLECTION,
            question = "Test question?",
            options = listOf("A", "B", "C", "D"),
            correctOptionIndex = 0
        )
    }
}
