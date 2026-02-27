package dev.gopes.hinducalendar.domain.model

import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class StreakData(
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val lastOpenDate: String? = null,
    val totalDaysOpened: Int = 0,
    val milestonesReached: List<Int> = emptyList()
) {
    val streakBonus: Int get() = currentStreak * 2

    fun updateForToday(): StreakData {
        val today = LocalDate.now().toString()
        if (lastOpenDate == today) return this

        val yesterday = LocalDate.now().minusDays(1).toString()
        val newStreak = if (lastOpenDate == yesterday) currentStreak + 1 else 1
        val newLongest = maxOf(longestStreak, newStreak)
        val newMilestones = milestoneThresholds
            .filter { it <= newStreak && it !in milestonesReached }
            .let { milestonesReached + it }

        return copy(
            currentStreak = newStreak,
            longestStreak = newLongest,
            lastOpenDate = today,
            totalDaysOpened = totalDaysOpened + 1,
            milestonesReached = newMilestones
        )
    }

    fun newMilestones(): List<Int> {
        val today = LocalDate.now().toString()
        if (lastOpenDate == today) return emptyList()
        val yesterday = LocalDate.now().minusDays(1).toString()
        val projectedStreak = if (lastOpenDate == yesterday) currentStreak + 1 else 1
        return milestoneThresholds.filter { it == projectedStreak && it !in milestonesReached }
    }

    companion object {
        val milestoneThresholds = listOf(3, 7, 14, 30, 60, 100, 200, 365)

        data class MilestoneInfo(
            val days: Int,
            val icon: String,
            val color: String
        )

        val milestoneDetails = listOf(
            MilestoneInfo(3, "local_fire_department", "orange"),
            MilestoneInfo(7, "local_fire_department", "orange"),
            MilestoneInfo(14, "star", "yellow"),
            MilestoneInfo(30, "star", "purple"),
            MilestoneInfo(60, "workspace_premium", "blue"),
            MilestoneInfo(100, "emoji_events", "gold"),
            MilestoneInfo(200, "emoji_events", "gold"),
            MilestoneInfo(365, "auto_awesome", "gold")
        )
    }
}
