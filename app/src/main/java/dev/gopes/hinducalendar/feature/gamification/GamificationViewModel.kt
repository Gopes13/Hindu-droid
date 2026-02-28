package dev.gopes.hinducalendar.feature.gamification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.gopes.hinducalendar.domain.model.*
import dev.gopes.hinducalendar.domain.repository.PreferencesRepository
import dev.gopes.hinducalendar.domain.service.GamificationService
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface GamificationEvent {
    data class LevelUp(val oldLevel: Int, val newLevel: Int) : GamificationEvent
    data class Milestone(val days: Int) : GamificationEvent
    data class NewBadges(val badgeIds: List<String>) : GamificationEvent
}

@HiltViewModel
class GamificationViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
    private val gamificationService: GamificationService
) : ViewModel() {

    val language: StateFlow<AppLanguage> = preferencesRepository.preferencesFlow
        .map { it.language }
        .stateIn(viewModelScope, SharingStarted.Eagerly, AppLanguage.ENGLISH)

    val gamificationData: StateFlow<GamificationData> = preferencesRepository.preferencesFlow
        .map { it.gamificationData }
        .stateIn(viewModelScope, SharingStarted.Eagerly, GamificationData())

    val streakData: StateFlow<StreakData> = preferencesRepository.preferencesFlow
        .map { it.streakData }
        .stateIn(viewModelScope, SharingStarted.Eagerly, StreakData())

    private val _dailyChallenge = MutableStateFlow<DailyChallenge?>(null)
    val dailyChallenge: StateFlow<DailyChallenge?> = _dailyChallenge.asStateFlow()

    private val _events = Channel<GamificationEvent>(Channel.BUFFERED)
    val events: Flow<GamificationEvent> = _events.receiveAsFlow()

    init {
        viewModelScope.launch {
            val prefs = preferencesRepository.preferencesFlow.first()
            if (prefs.gamificationData.isEnabled) {
                processAppOpen()
                _dailyChallenge.value = gamificationService.generateDailyChallenge(prefs.language.code)
            }
        }
    }

    private suspend fun processAppOpen() {
        val prefs = preferencesRepository.preferencesFlow.first()
        val oldLevel = prefs.gamificationData.currentLevel
        val newMilestones = prefs.streakData.newMilestones()
        val updatedStreak = prefs.streakData.updateForToday()
        var updatedData = gamificationService.rewardAppOpen(prefs.gamificationData, updatedStreak)
        updatedData = gamificationService.trackPanchangCheck(updatedData)
        val (badgeChecked, newBadgeIds) = gamificationService.checkAndAwardBadges(updatedData, updatedStreak)

        preferencesRepository.update { p ->
            p.copy(gamificationData = badgeChecked, streakData = updatedStreak)
        }

        if (badgeChecked.currentLevel > oldLevel) {
            _events.send(GamificationEvent.LevelUp(oldLevel, badgeChecked.currentLevel))
        }
        if (newMilestones.isNotEmpty()) {
            _events.send(GamificationEvent.Milestone(newMilestones.max()))
        }
        if (newBadgeIds.isNotEmpty()) {
            _events.send(GamificationEvent.NewBadges(newBadgeIds))
        }
    }

    fun onChallengeAnswered(correct: Boolean) {
        if (!correct) return
        viewModelScope.launch {
            val data = gamificationData.value
            val streak = streakData.value
            val updated = gamificationService.rewardChallenge(data)
            val (badgeChecked, _) = gamificationService.checkAndAwardBadges(updated, streak)
            preferencesRepository.update { it.copy(gamificationData = badgeChecked) }
        }
    }

    fun recordVerseView() {
        if (!gamificationData.value.isEnabled) return
        viewModelScope.launch {
            val updated = gamificationService.rewardVerseView(gamificationData.value)
            preferencesRepository.update { it.copy(gamificationData = updated) }
        }
    }

    fun recordVerseRead() {
        if (!gamificationData.value.isEnabled) return
        viewModelScope.launch {
            val data = gamificationData.value
            val oldLevel = data.currentLevel
            val updated = gamificationService.rewardVerseRead(data)
            val (badgeChecked, newBadgeIds) = gamificationService.checkAndAwardBadges(updated, streakData.value)
            preferencesRepository.update { it.copy(gamificationData = badgeChecked) }
            if (badgeChecked.currentLevel > oldLevel) {
                _events.send(GamificationEvent.LevelUp(oldLevel, badgeChecked.currentLevel))
            }
            if (newBadgeIds.isNotEmpty()) {
                _events.send(GamificationEvent.NewBadges(newBadgeIds))
            }
        }
    }

    fun recordExplanationView() {
        if (!gamificationData.value.isEnabled) return
        viewModelScope.launch {
            val updated = gamificationService.trackExplanationView(gamificationData.value)
            preferencesRepository.update { it.copy(gamificationData = updated) }
        }
    }

    fun recordFestivalStoryRead(festivalId: String) {
        if (!gamificationData.value.isEnabled) return
        viewModelScope.launch {
            val data = gamificationData.value
            val updated = gamificationService.rewardFestivalStory(data, festivalId)
            val (badgeChecked, newBadgeIds) = gamificationService.checkAndAwardBadges(updated, streakData.value)
            preferencesRepository.update { it.copy(gamificationData = badgeChecked) }
            if (newBadgeIds.isNotEmpty()) {
                _events.send(GamificationEvent.NewBadges(newBadgeIds))
            }
        }
    }

    fun toggleGamification(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.update { prefs ->
                prefs.copy(gamificationData = prefs.gamificationData.copy(isEnabled = enabled))
            }
            if (enabled) {
                val lang = preferencesRepository.preferencesFlow.first().language.code
                _dailyChallenge.value = gamificationService.generateDailyChallenge(lang)
                processAppOpen()
            }
        }
    }
}
