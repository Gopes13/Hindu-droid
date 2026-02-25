package dev.gopes.hinducalendar.ui.kirtans

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.gopes.hinducalendar.data.model.AppLanguage
import dev.gopes.hinducalendar.data.model.Kirtan
import dev.gopes.hinducalendar.data.repository.PreferencesRepository
import dev.gopes.hinducalendar.engine.KirtanService
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class KirtanReaderViewModel @Inject constructor(
    private val kirtanService: KirtanService,
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    var kirtan by mutableStateOf<Kirtan?>(null)
        private set

    var language by mutableStateOf(AppLanguage.ENGLISH)
        private set

    init {
        viewModelScope.launch {
            language = preferencesRepository.preferencesFlow.first().language
        }
    }

    fun loadKirtan(id: String) {
        kirtan = kirtanService.kirtanById(id)
    }
}
