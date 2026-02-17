package dev.gopes.hinducalendar.ui.texts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.gopes.hinducalendar.data.model.SacredTextType
import dev.gopes.hinducalendar.data.repository.PreferencesRepository
import dev.gopes.hinducalendar.engine.SacredTextService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SacredTextsUiState(
    val dharmaPathName: String = "General Hindu",
    val availableTexts: List<SacredTextItem> = emptyList(),
    val bookmarkCount: Int = 0
)

@HiltViewModel
class SacredTextsViewModel @Inject constructor(
    private val sacredTextService: SacredTextService,
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SacredTextsUiState())
    val uiState: StateFlow<SacredTextsUiState> = _uiState

    init {
        loadTexts()
    }

    private fun loadTexts() {
        viewModelScope.launch(Dispatchers.IO) {
            val prefs = preferencesRepository.preferencesFlow.first()
            val path = prefs.dharmaPath
            val progress = prefs.readingProgress
            val primaryTextId = path.primaryTextId

            val items = path.availableTextIds.mapNotNull { textId ->
                val textType = sacredTextService.textTypeForId(textId) ?: return@mapNotNull null
                val isPrimary = textId == primaryTextId
                val currentPos = progress.currentPosition(textType)
                val total = sacredTextService.totalCount(textType)

                SacredTextItem(
                    textType = textType,
                    isPrimary = isPrimary,
                    currentPosition = currentPos,
                    totalCount = total
                )
            }

            _uiState.value = SacredTextsUiState(
                dharmaPathName = path.displayName,
                availableTexts = items,
                bookmarkCount = prefs.bookmarks.bookmarks.size
            )
        }
    }
}
