package dev.gopes.hinducalendar.feature.japa

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.gopes.hinducalendar.domain.model.*
import dev.gopes.hinducalendar.domain.repository.PreferencesRepository
import dev.gopes.hinducalendar.domain.service.GamificationService
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class JapaViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
    private val gamificationService: GamificationService
) : ViewModel() {

    val japaState: StateFlow<JapaState> = preferencesRepository.preferencesFlow
        .map { it.japaState }
        .stateIn(viewModelScope, SharingStarted.Eagerly, JapaState())

    var showRoundComplete by mutableStateOf(false)
        private set

    init {
        viewModelScope.launch {
            // Reset daily state if needed
            preferencesRepository.update { prefs ->
                prefs.copy(japaState = prefs.japaState.resetDailyIfNeeded())
            }
        }
    }

    fun advanceBead() {
        viewModelScope.launch {
            preferencesRepository.update { prefs ->
                val (newState, roundComplete) = prefs.japaState.advanceBead()
                if (roundComplete) {
                    showRoundComplete = true
                }
                prefs.copy(japaState = newState)
            }
            // Reward after state is persisted (reads latest from repo)
            if (showRoundComplete) {
                rewardRound()
            }
        }
    }

    fun dismissRoundComplete() {
        showRoundComplete = false
    }

    fun selectMaterial(material: MalaMaterial) {
        viewModelScope.launch {
            preferencesRepository.update { prefs ->
                prefs.copy(japaState = prefs.japaState.withMaterial(material))
            }
        }
    }

    fun selectMantra(mantra: MantraSelection) {
        viewModelScope.launch {
            preferencesRepository.update { prefs ->
                prefs.copy(japaState = prefs.japaState.withMantra(mantra))
            }
        }
    }

    private suspend fun rewardRound() {
        preferencesRepository.update { prefs ->
            val data = prefs.gamificationData
            if (!data.isEnabled) return@update prefs
            val state = prefs.japaState
            if (state.roundsRewardedToday >= 10) return@update prefs
            val today = java.time.LocalDate.now().toString()
            val updated = data.addPoints(10)
                .let { gamificationService.checkJapaBadges(it, state) }
            prefs.copy(
                gamificationData = updated,
                japaState = state.copy(
                    roundsRewardedToday = state.roundsRewardedToday + 1,
                    lastJapaRewardDate = today,
                    japaStreak = state.japaStreak + if (state.roundsToday == 1) 1 else 0,
                    longestJapaStreak = maxOf(
                        state.longestJapaStreak,
                        state.japaStreak + if (state.roundsToday == 1) 1 else 0
                    )
                )
            )
        }
    }
}
