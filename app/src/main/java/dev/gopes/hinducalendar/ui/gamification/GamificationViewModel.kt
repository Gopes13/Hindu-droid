package dev.gopes.hinducalendar.ui.gamification

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.gopes.hinducalendar.data.model.*
import dev.gopes.hinducalendar.data.repository.PreferencesRepository
import dev.gopes.hinducalendar.engine.GamificationService
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GamificationViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
    private val gamificationService: GamificationService
) : ViewModel() {

    var gamificationData by mutableStateOf(GamificationData())
        private set
    var streakData by mutableStateOf(StreakData())
        private set
    var dailyChallenge by mutableStateOf<DailyChallenge?>(null)
        private set

    // Celebration events
    var levelUpEvent by mutableStateOf<Pair<Int, Int>?>(null) // old level, new level
        private set
    var milestoneEvent by mutableStateOf<Int?>(null) // milestone days
        private set
    var newBadges by mutableStateOf<List<String>>(emptyList())
        private set

    init {
        loadState()
    }

    private fun loadState() {
        viewModelScope.launch {
            val prefs = preferencesRepository.preferencesFlow.first()
            gamificationData = prefs.gamificationData
            streakData = prefs.streakData

            if (gamificationData.isEnabled) {
                processAppOpen()
                dailyChallenge = gamificationService.generateDailyChallenge()
            }
        }
    }

    private fun processAppOpen() {
        viewModelScope.launch {
            val oldLevel = gamificationData.currentLevel
            val newMilestones = streakData.newMilestones()

            // Update streak
            val updatedStreak = streakData.updateForToday()

            // Award app open points + streak bonus
            var updatedData = gamificationService.rewardAppOpen(gamificationData, updatedStreak)

            // Track panchang check
            updatedData = gamificationService.trackPanchangCheck(updatedData)

            // Check badges
            val (badgeChecked, newBadgeIds) = gamificationService.checkAndAwardBadges(updatedData, updatedStreak)
            updatedData = badgeChecked

            // Persist
            preferencesRepository.update { prefs ->
                prefs.copy(gamificationData = updatedData, streakData = updatedStreak)
            }

            gamificationData = updatedData
            streakData = updatedStreak

            // Trigger celebrations
            if (updatedData.currentLevel > oldLevel) {
                levelUpEvent = oldLevel to updatedData.currentLevel
            }
            if (newMilestones.isNotEmpty()) {
                milestoneEvent = newMilestones.max()
            }
            if (newBadgeIds.isNotEmpty()) {
                newBadges = newBadgeIds
            }
        }
    }

    fun onChallengeAnswered(correct: Boolean) {
        if (!correct) return
        viewModelScope.launch {
            val updated = gamificationService.rewardChallenge(gamificationData)
            val (badgeChecked, _) = gamificationService.checkAndAwardBadges(updated, streakData)
            preferencesRepository.update { prefs ->
                prefs.copy(gamificationData = badgeChecked)
            }
            gamificationData = badgeChecked
        }
    }

    fun dismissLevelUp() { levelUpEvent = null }
    fun dismissMilestone() { milestoneEvent = null }
    fun clearNewBadges() { newBadges = emptyList() }

    fun toggleGamification(enabled: Boolean) {
        viewModelScope.launch {
            val updated = gamificationData.copy(isEnabled = enabled)
            preferencesRepository.update { prefs ->
                prefs.copy(gamificationData = updated)
            }
            gamificationData = updated
            if (enabled) {
                dailyChallenge = gamificationService.generateDailyChallenge()
                processAppOpen()
            }
        }
    }
}
