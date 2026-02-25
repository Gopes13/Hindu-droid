package dev.gopes.hinducalendar.ui.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.gopes.hinducalendar.data.model.*
import dev.gopes.hinducalendar.data.repository.PreferencesRepository
import dev.gopes.hinducalendar.engine.PanchangService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class TodayPanchangViewModel @Inject constructor(
    private val panchangService: PanchangService,
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    val language: StateFlow<AppLanguage> = preferencesRepository.preferencesFlow
        .map { it.language }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppLanguage.ENGLISH)

    val preferences: StateFlow<UserPreferences?> = preferencesRepository.preferencesFlow
        .map { it }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _panchang = MutableStateFlow<PanchangDay?>(null)
    val panchang: StateFlow<PanchangDay?> = _panchang

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        observePreferences()
    }

    private fun observePreferences() {
        viewModelScope.launch {
            preferencesRepository.preferencesFlow
                .map { prefs -> prefs.location to prefs.tradition }
                .distinctUntilChanged()
                .collect { (location, tradition) ->
                    _isLoading.value = true
                    val result = withContext(Dispatchers.Default) {
                        panchangService.computePanchang(LocalDate.now(), location, tradition)
                    }
                    _panchang.value = result
                    _isLoading.value = false
                }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _isLoading.value = true
            val prefs = preferencesRepository.preferencesFlow.first()
            val result = withContext(Dispatchers.Default) {
                panchangService.computePanchang(LocalDate.now(), prefs.location, prefs.tradition)
            }
            _panchang.value = result
            _isLoading.value = false
        }
    }
}
