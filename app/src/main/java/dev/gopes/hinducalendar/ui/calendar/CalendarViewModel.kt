package dev.gopes.hinducalendar.ui.calendar

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
import java.time.YearMonth
import javax.inject.Inject

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val panchangService: PanchangService,
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    private val _displayedMonth = MutableStateFlow(YearMonth.now())
    val displayedMonth: StateFlow<YearMonth> = _displayedMonth

    private val _monthPanchang = MutableStateFlow<List<PanchangDay>>(emptyList())
    val monthPanchang: StateFlow<List<PanchangDay>> = _monthPanchang

    private val _selectedPanchang = MutableStateFlow<PanchangDay?>(null)
    val selectedPanchang: StateFlow<PanchangDay?> = _selectedPanchang

    init {
        loadMonth()
    }

    fun previousMonth() {
        _displayedMonth.value = _displayedMonth.value.minusMonths(1)
        loadMonth()
    }

    fun nextMonth() {
        _displayedMonth.value = _displayedMonth.value.plusMonths(1)
        loadMonth()
    }

    fun selectDate(date: LocalDate) {
        viewModelScope.launch(Dispatchers.Default) {
            val prefs = preferencesRepository.preferencesFlow.first()
            val panchang = _monthPanchang.value.find { it.date == date }
                ?: panchangService.computePanchang(date, prefs.location, prefs.tradition)
            _selectedPanchang.value = panchang
        }
    }

    private fun loadMonth() {
        viewModelScope.launch(Dispatchers.Default) {
            val prefs = preferencesRepository.preferencesFlow.first()
            val ym = _displayedMonth.value
            val result = panchangService.computeMonthlyPanchang(
                ym.year, ym.monthValue, prefs.location, prefs.tradition
            )
            _monthPanchang.value = result
            val today = LocalDate.now()
            if (ym == YearMonth.from(today)) {
                _selectedPanchang.value = result.find { it.date == today }
            } else {
                _selectedPanchang.value = result.firstOrNull()
            }
        }
    }
}
