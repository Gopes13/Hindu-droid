package dev.gopes.hinducalendar.feature.diya

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.gopes.hinducalendar.domain.model.AppLanguage
import dev.gopes.hinducalendar.domain.model.DiyaState
import dev.gopes.hinducalendar.domain.repository.PreferencesRepository
import dev.gopes.hinducalendar.domain.service.GamificationService
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DiyaViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
    private val gamificationService: GamificationService
) : ViewModel() {

    var diyaState by mutableStateOf(DiyaState())
        private set

    val language: StateFlow<AppLanguage> = preferencesRepository.preferencesFlow
        .map { it.language }
        .stateIn(viewModelScope, SharingStarted.Eagerly, AppLanguage.ENGLISH)

    var showConfetti by mutableStateOf(false)
        private set

    init {
        viewModelScope.launch {
            val prefs = preferencesRepository.preferencesFlow.first()
            diyaState = prefs.diyaState.resetDailyIfNeeded()
            // Persist the reset
            preferencesRepository.update { it.copy(diyaState = diyaState) }
        }
    }

    fun lightDiya() {
        if (diyaState.isLitToday) return
        val newState = diyaState.lightDiya()
        diyaState = newState
        showConfetti = true

        viewModelScope.launch {
            preferencesRepository.update { it.copy(diyaState = newState) }

            // Gamification: +5 PP, +15 for 7-day streak
            val prefs = preferencesRepository.preferencesFlow.first()
            var data = prefs.gamificationData
            data = gamificationService.rewardDiyaLighting(data, newState)
            data = gamificationService.checkDiyaBadges(data, newState)
            preferencesRepository.update { it.copy(gamificationData = data) }
        }
    }

    fun dismissConfetti() {
        showConfetti = false
    }
}
