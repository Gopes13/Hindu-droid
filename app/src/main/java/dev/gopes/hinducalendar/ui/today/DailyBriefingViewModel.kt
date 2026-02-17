package dev.gopes.hinducalendar.ui.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.gopes.hinducalendar.data.repository.PreferencesRepository
import dev.gopes.hinducalendar.engine.DailyVerse
import dev.gopes.hinducalendar.engine.SacredTextService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DailyBriefingUiState(
    val primaryVerse: DailyVerse? = null,
    val secondaryVerse: DailyVerse? = null
)

@HiltViewModel
class DailyBriefingViewModel @Inject constructor(
    private val sacredTextService: SacredTextService,
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DailyBriefingUiState())
    val uiState: StateFlow<DailyBriefingUiState> = _uiState

    init {
        loadDailyContent()
    }

    private fun loadDailyContent() {
        viewModelScope.launch(Dispatchers.IO) {
            val prefs = preferencesRepository.preferencesFlow.first()
            val content = sacredTextService.getDailyContent(
                path = prefs.dharmaPath,
                progress = prefs.readingProgress,
                lang = prefs.language
            )
            _uiState.value = DailyBriefingUiState(
                primaryVerse = content.primaryVerse,
                secondaryVerse = content.secondaryVerse
            )
        }
    }
}
