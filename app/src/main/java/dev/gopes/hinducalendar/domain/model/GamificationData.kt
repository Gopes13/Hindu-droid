package dev.gopes.hinducalendar.domain.model

import java.time.LocalDate

data class GamificationData(
    val isEnabled: Boolean = false,
    val totalPunyaPoints: Int = 0,
    val currentLevel: Int = 1,

    // Daily tracking guards (date strings, e.g. "2024-01-15")
    val lastAppOpenRewardDate: String? = null,
    val lastVerseViewRewardDate: String? = null,
    val lastVerseReadRewardDate: String? = null,
    val lastStreakBonusDate: String? = null,
    val lastChallengeDate: String? = null,
    val lastChallengeCompleted: Boolean = false,
    val challengesSolved: Int = 0,

    // Badges
    val earnedBadges: List<String> = emptyList(),
    val badgeEarnedDates: Map<String, String> = emptyMap(),

    // Badge unlock tracking
    val festivalStoriesRead: Set<String> = emptySet(),
    val textsCompleted: Set<String> = emptySet(),
    val dharmaPathsExplored: Set<String> = emptySet(),
    val languagesUsed: Set<String> = emptySet(),
    val panchangDaysChecked: Int = 0,
    val lastPanchangCheckDate: String? = null,

    // Engagement tracking
    val versesExplained: Int = 0,
    val reflectionsWritten: Int = 0,
    val deepStudySessions: Int = 0,
    val lastDeepStudyRewardDate: String? = null,
    val deepStudyPointsToday: Int = 0,

    // In-app review tracking
    val lastReviewPromptDate: String? = null
) {
    val currentLevelData: SadhanaLevel get() = SadhanaLevel.forLevel(currentLevel)

    val nextLevelData: SadhanaLevel? get() = SadhanaLevel.forLevelOrNull(currentLevel + 1)

    val currentLevelProgress: Double
        get() {
            val current = currentLevelData.xpRequired
            val next = nextLevelData?.xpRequired ?: return 1.0
            val range = next - current
            if (range <= 0) return 1.0
            return ((totalPunyaPoints - current).toDouble() / range).coerceIn(0.0, 1.0)
        }

    val punyaPointsToNextLevel: Int
        get() {
            val next = nextLevelData?.xpRequired ?: return 0
            return (next - totalPunyaPoints).coerceAtLeast(0)
        }

    fun isToday(dateString: String?): Boolean {
        return dateString == LocalDate.now().toString()
    }

    fun addPoints(points: Int): GamificationData {
        val newTotal = totalPunyaPoints + points
        val newLevel = SadhanaLevel.levelForPoints(newTotal)
        return copy(totalPunyaPoints = newTotal, currentLevel = newLevel)
    }

    fun hasBadge(badgeId: String): Boolean = badgeId in earnedBadges

    fun awardBadge(badgeId: String): GamificationData {
        if (hasBadge(badgeId)) return this
        return copy(
            earnedBadges = earnedBadges + badgeId,
            badgeEarnedDates = badgeEarnedDates + (badgeId to LocalDate.now().toString())
        )
    }
}
