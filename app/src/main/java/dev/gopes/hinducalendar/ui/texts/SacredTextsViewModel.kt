package dev.gopes.hinducalendar.ui.texts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.gopes.hinducalendar.data.model.DharmaPath
import dev.gopes.hinducalendar.data.model.ReadingProgress
import dev.gopes.hinducalendar.data.model.SacredTextType
import dev.gopes.hinducalendar.data.model.UserPreferences
import dev.gopes.hinducalendar.engine.SacredTextService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SacredTextsUiState(
    val dharmaPathName: String = "General Hindu",
    val availableTexts: List<SacredTextItem> = emptyList()
)

@HiltViewModel
class SacredTextsViewModel @Inject constructor(
    private val sacredTextService: SacredTextService
) : ViewModel() {

    private val _uiState = MutableStateFlow(SacredTextsUiState())
    val uiState: StateFlow<SacredTextsUiState> = _uiState

    // Default preferences — in production, load from SharedPreferences / DataStore
    private val preferences = UserPreferences()

    init {
        loadTexts()
    }

    private fun loadTexts() {
        viewModelScope.launch(Dispatchers.IO) {
            val path = preferences.dharmaPath
            val progress = preferences.readingProgress
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
                availableTexts = items
            )
        }
    }
}
