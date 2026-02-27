package dev.gopes.hinducalendar.domain.service.gamification

import dev.gopes.hinducalendar.domain.model.*

internal object BadgeChecker {

    fun checkAndAwardBadges(data: GamificationData, streak: StreakData): Pair<GamificationData, List<String>> {
        if (!data.isEnabled) return data to emptyList()
        val newBadges = mutableListOf<String>()
        var updated = data
        for (badge in SadhanaBadge.allBadges) {
            if (!updated.hasBadge(badge.id) && badge.requirement(updated, streak)) {
                updated = updated.awardBadge(badge.id)
                newBadges.add(badge.id)
            }
        }
        return updated to newBadges
    }

    fun checkJapaBadges(data: GamificationData, japaState: JapaState): GamificationData {
        var updated = data
        val rounds = japaState.totalRoundsLifetime
        if (rounds >= 10 && !updated.hasBadge("badge_japa_10")) updated = updated.awardBadge("badge_japa_10")
        if (rounds >= 108 && !updated.hasBadge("badge_japa_108")) updated = updated.awardBadge("badge_japa_108")
        if (rounds >= 1008 && !updated.hasBadge("badge_japa_1008")) updated = updated.awardBadge("badge_japa_1008")
        val streak = japaState.japaStreak
        if (streak >= 7 && !updated.hasBadge("badge_japa_streak_7")) updated = updated.awardBadge("badge_japa_streak_7")
        if (streak >= 30 && !updated.hasBadge("badge_japa_streak_30")) updated = updated.awardBadge("badge_japa_streak_30")
        return updated
    }

    fun checkDiyaBadges(data: GamificationData, diyaState: DiyaState): GamificationData {
        var updated = data
        val days = diyaState.totalDaysLit
        if (days >= 7 && !updated.hasBadge("badge_diya_7")) updated = updated.awardBadge("badge_diya_7")
        if (days >= 30 && !updated.hasBadge("badge_diya_30")) updated = updated.awardBadge("badge_diya_30")
        if (days >= 108 && !updated.hasBadge("badge_diya_108")) updated = updated.awardBadge("badge_diya_108")
        val streak = diyaState.lightingStreak
        if (streak >= 7 && !updated.hasBadge("badge_diya_streak_7")) updated = updated.awardBadge("badge_diya_streak_7")
        return updated
    }

    fun checkSanskritBadges(data: GamificationData, progress: SanskritProgress): GamificationData {
        var updated = data
        if (progress.isModuleComplete("module1") && !updated.hasBadge("badge_sanskrit_first_letters")) {
            updated = updated.awardBadge("badge_sanskrit_first_letters")
        }
        if (progress.lessonsCount >= 10 && !updated.hasBadge("badge_sanskrit_student")) {
            updated = updated.awardBadge("badge_sanskrit_student")
        }
        if (progress.lettersCount >= 20 && !updated.hasBadge("badge_sanskrit_scholar")) {
            updated = updated.awardBadge("badge_sanskrit_scholar")
        }
        if (progress.isModuleComplete("module5") && !updated.hasBadge("badge_sanskrit_mantra_reader")) {
            updated = updated.awardBadge("badge_sanskrit_mantra_reader")
        }
        return updated
    }
}
