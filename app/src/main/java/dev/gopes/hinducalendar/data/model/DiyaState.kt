package dev.gopes.hinducalendar.data.model

import java.time.LocalDate

/** Sacred Diya (oil lamp) state — persisted in UserPreferences. */
data class DiyaState(
    val isLitToday: Boolean = false,
    val lastLitDate: String? = null,
    val lightingStreak: Int = 0,
    val longestLightingStreak: Int = 0,
    val totalDaysLit: Int = 0,
    val lastDiyaRewardDate: String? = null
) {
    /** Light the diya for today. */
    fun lightDiya(): DiyaState {
        if (isLitToday) return this
        val today = LocalDate.now().toString()
        val newStreak = lightingStreak + 1
        return copy(
            isLitToday = true,
            lastLitDate = today,
            lightingStreak = newStreak,
            longestLightingStreak = maxOf(longestLightingStreak, newStreak),
            totalDaysLit = totalDaysLit + 1
        )
    }

    /** Reset daily state if the date changed. */
    fun resetDailyIfNeeded(): DiyaState {
        val today = LocalDate.now().toString()
        if (lastLitDate == today) return this
        val daysSince = if (lastLitDate != null) {
            try {
                val last = LocalDate.parse(lastLitDate)
                java.time.temporal.ChronoUnit.DAYS.between(last, LocalDate.now()).toInt()
            } catch (_: Exception) { 999 }
        } else 999
        val newStreak = if (daysSince == 1) lightingStreak else 0
        return copy(
            isLitToday = false,
            lightingStreak = newStreak,
            longestLightingStreak = maxOf(longestLightingStreak, newStreak)
        )
    }
}
