package dev.gopes.hinducalendar.ui.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.gopes.hinducalendar.data.model.UserPreferences
import dev.gopes.hinducalendar.engine.DailyVerse
import dev.gopes.hinducalendar.engine.SacredTextService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DailyBriefingUiState(
    val primaryVerse: DailyVerse? = null,
    val secondaryVerse: DailyVerse? = null
)

@HiltViewModel
class DailyBriefingViewModel @Inject constructor(
    private val sacredTextService: SacredTextService
) : ViewModel() {

    private val _uiState = MutableStateFlow(DailyBriefingUiState())
    val uiState: StateFlow<DailyBriefingUiState> = _uiState

    // Default preferences — in production, load from SharedPreferences / DataStore
    private val preferences = UserPreferences()

    init {
        loadDailyContent()
    }

    private fun loadDailyContent() {
        viewModelScope.launch(Dispatchers.IO) {
            val content = sacredTextService.getDailyContent(
                path = preferences.dharmaPath,
                progress = preferences.readingProgress,
                lang = preferences.language
            )
            _uiState.value = DailyBriefingUiState(
                primaryVerse = content.primaryVerse,
                secondaryVerse = content.secondaryVerse
            )
        }
    }
}
