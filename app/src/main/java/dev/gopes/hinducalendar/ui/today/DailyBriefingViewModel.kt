package dev.gopes.hinducalendar.ui.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.gopes.hinducalendar.data.model.AppLanguage
import dev.gopes.hinducalendar.data.model.SacredTextType
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
    val verse: DailyVerse? = null,
    val activeText: SacredTextType = SacredTextType.GITA,
    val currentPosition: Int = 0,
    val totalPositions: Int = 0,
    val isTextComplete: Boolean = false,
    val isLoaded: Boolean = false,
    val hasError: Boolean = false,
    val isGamified: Boolean = false,
    val language: AppLanguage = AppLanguage.ENGLISH
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

    fun loadDailyContent() {
        viewModelScope.launch(Dispatchers.IO) {
            val prefs = preferencesRepository.preferencesFlow.first()
            val activeText = prefs.effectiveWisdomText
            val progress = prefs.readingProgress
            val lang = prefs.language
            val position = progress.currentPosition(activeText)
            val total = sacredTextService.totalCount(activeText)

            if (total > 0 && position > total) {
                _uiState.value = DailyBriefingUiState(
                    activeText = activeText,
                    currentPosition = total,
                    totalPositions = total,
                    isTextComplete = true,
                    isLoaded = true,
                    isGamified = prefs.gamificationData.isEnabled,
                    language = lang
                )
                return@launch
            }

            val verse = sacredTextService.getVerseForText(activeText, progress, lang)
            _uiState.value = DailyBriefingUiState(
                verse = verse,
                activeText = activeText,
                currentPosition = position,
                totalPositions = total,
                isTextComplete = false,
                isLoaded = true,
                hasError = verse == null,
                isGamified = prefs.gamificationData.isEnabled,
                language = lang
            )
        }
    }

    fun markAsRead() {
        viewModelScope.launch(Dispatchers.IO) {
            val prefs = preferencesRepository.preferencesFlow.first()
            val activeText = prefs.effectiveWisdomText
            val currentPos = prefs.readingProgress.currentPosition(activeText)
            val total = sacredTextService.totalCount(activeText)
            val nextPos = currentPos + 1

            val updatedProgress = prefs.readingProgress.withPosition(activeText, nextPos)
            preferencesRepository.update { it.copy(readingProgress = updatedProgress) }

            val lang = prefs.language
            if (nextPos > total) {
                _uiState.value = _uiState.value.copy(
                    isTextComplete = true,
                    currentPosition = total,
                    verse = null
                )
            } else {
                val verse = sacredTextService.getVerseForText(activeText, updatedProgress, lang)
                _uiState.value = _uiState.value.copy(
                    verse = verse,
                    currentPosition = nextPos,
                    hasError = verse == null
                )
            }
        }
    }

    fun selectWisdomText(textType: SacredTextType) {
        viewModelScope.launch {
            preferencesRepository.update { it.copy(activeWisdomText = textType) }
            loadDailyContent()
        }
    }
}
