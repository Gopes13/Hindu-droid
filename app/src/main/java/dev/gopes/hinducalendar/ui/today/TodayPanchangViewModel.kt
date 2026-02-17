package dev.gopes.hinducalendar.ui.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.gopes.hinducalendar.data.model.*
import dev.gopes.hinducalendar.data.repository.PreferencesRepository
import dev.gopes.hinducalendar.engine.PanchangService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class TodayPanchangViewModel @Inject constructor(
    private val panchangService: PanchangService,
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    private val _panchang = MutableStateFlow<PanchangDay?>(null)
    val panchang: StateFlow<PanchangDay?> = _panchang

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        loadTodayPanchang()
    }

    fun loadTodayPanchang() {
        viewModelScope.launch(Dispatchers.Default) {
            _isLoading.value = true
            val prefs = preferencesRepository.preferencesFlow.first()
            val result = panchangService.computePanchang(
                LocalDate.now(),
                prefs.location,
                prefs.tradition
            )
            _panchang.value = result
            _isLoading.value = false
        }
    }
}
