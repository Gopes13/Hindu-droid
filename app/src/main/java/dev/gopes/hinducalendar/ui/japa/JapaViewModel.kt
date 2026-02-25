package dev.gopes.hinducalendar.ui.japa

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
class JapaViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
    private val gamificationService: GamificationService
) : ViewModel() {

    var japaState by mutableStateOf(JapaState())
        private set

    var showRoundComplete by mutableStateOf(false)
        private set

    init {
        viewModelScope.launch {
            val prefs = preferencesRepository.preferencesFlow.first()
            japaState = prefs.japaState.resetDailyIfNeeded()
            // Save the daily-reset state
            preferencesRepository.update { it.copy(japaState = japaState) }
        }
    }

    fun advanceBead() {
        val (newState, roundComplete) = japaState.advanceBead()
        japaState = newState
        if (roundComplete) {
            showRoundComplete = true
            rewardRound()
        }
        save()
    }

    fun dismissRoundComplete() {
        showRoundComplete = false
    }

    fun selectMaterial(material: MalaMaterial) {
        japaState = japaState.withMaterial(material)
        save()
    }

    fun selectMantra(mantra: MantraSelection) {
        japaState = japaState.withMantra(mantra)
        save()
    }

    private fun rewardRound() {
        viewModelScope.launch {
            preferencesRepository.update { prefs ->
                val data = prefs.gamificationData
                if (!data.isEnabled) return@update prefs
                // Cap at 10 rewarded rounds per day
                val state = prefs.japaState
                if (state.roundsRewardedToday >= 10) return@update prefs
                val today = java.time.LocalDate.now().toString()
                val updated = data.addPoints(10) // 10 PP per round
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

    private fun save() {
        viewModelScope.launch {
            preferencesRepository.update { it.copy(japaState = japaState) }
        }
    }
}
