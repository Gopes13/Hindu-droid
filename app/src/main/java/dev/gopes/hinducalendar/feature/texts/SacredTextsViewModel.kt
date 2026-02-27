package dev.gopes.hinducalendar.feature.texts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.gopes.hinducalendar.domain.model.DharmaPath
import dev.gopes.hinducalendar.domain.model.SacredTextType
import dev.gopes.hinducalendar.domain.repository.PreferencesRepository
import dev.gopes.hinducalendar.domain.repository.SacredTextRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class SacredTextsUiState(
    val dharmaPath: DharmaPath = DharmaPath.GENERAL,
    val availableTexts: List<SacredTextItem> = emptyList(),
    val allOtherTexts: List<SacredTextItem> = emptyList(),
    val bookmarkCount: Int = 0,
    val isLoading: Boolean = true
)

@HiltViewModel
class SacredTextsViewModel @Inject constructor(
    private val sacredTextRepository: SacredTextRepository,
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SacredTextsUiState())
    val uiState: StateFlow<SacredTextsUiState> = _uiState

    init {
        observePreferences()
    }

    private fun observePreferences() {
        viewModelScope.launch {
            preferencesRepository.preferencesFlow.collect { prefs ->
                val path = prefs.dharmaPath
                val progress = prefs.readingProgress
                val primaryTextId = path.primaryTextId
                val pathTextIds = path.availableTextIds.toSet()

                val (pathItems, otherItems) = withContext(Dispatchers.IO) {
                    val pathTexts = path.availableTextIds.mapNotNull { textId ->
                        val textType = sacredTextRepository.textTypeForId(textId) ?: return@mapNotNull null
                        val isPrimary = textId == primaryTextId
                        val currentPos = progress.currentPosition(textType)
                        val total = sacredTextRepository.totalCount(textType)
                        val bmCount = prefs.bookmarks.bookmarks.count { it.textType == textType }

                        SacredTextItem(
                            textType = textType,
                            isPrimary = isPrimary,
                            currentPosition = currentPos,
                            totalCount = total,
                            bookmarkCount = bmCount
                        )
                    }

                    val otherTexts = SacredTextType.entries
                        .filter { it.jsonFileName !in pathTextIds }
                        .map { textType ->
                            val currentPos = progress.currentPosition(textType)
                            val total = sacredTextRepository.totalCount(textType)
                            val bmCount = prefs.bookmarks.bookmarks.count { it.textType == textType }

                            SacredTextItem(
                                textType = textType,
                                isPrimary = false,
                                currentPosition = currentPos,
                                totalCount = total,
                                bookmarkCount = bmCount
                            )
                        }

                    pathTexts to otherTexts
                }

                _uiState.value = SacredTextsUiState(
                    dharmaPath = path,
                    availableTexts = pathItems,
                    allOtherTexts = otherItems,
                    bookmarkCount = prefs.bookmarks.bookmarks.size,
                    isLoading = false
                )
            }
        }
    }
}
