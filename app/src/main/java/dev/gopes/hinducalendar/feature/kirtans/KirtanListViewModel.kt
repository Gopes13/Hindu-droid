package dev.gopes.hinducalendar.feature.kirtans

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.gopes.hinducalendar.domain.model.AppLanguage
import dev.gopes.hinducalendar.domain.model.Kirtan
import dev.gopes.hinducalendar.domain.model.KirtanCategory
import dev.gopes.hinducalendar.domain.repository.PreferencesRepository
import dev.gopes.hinducalendar.domain.repository.KirtanRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class KirtanListViewModel @Inject constructor(
    private val kirtanRepository: KirtanRepository,
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    var aartis by mutableStateOf<List<Kirtan>>(emptyList())
        private set

    var bhajans by mutableStateOf<List<Kirtan>>(emptyList())
        private set

    val language: StateFlow<AppLanguage> = preferencesRepository.preferencesFlow
        .map { it.language }
        .stateIn(viewModelScope, SharingStarted.Eagerly, AppLanguage.ENGLISH)

    init {
        viewModelScope.launch {
            aartis = kirtanRepository.kirtansByCategory(KirtanCategory.AARTI)
            bhajans = kirtanRepository.kirtansByCategory(KirtanCategory.BHAJAN)
        }
    }
}
