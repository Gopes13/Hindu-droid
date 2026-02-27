package dev.gopes.hinducalendar.feature.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.gopes.hinducalendar.domain.model.*
import dev.gopes.hinducalendar.domain.repository.PreferencesRepository
import dev.gopes.hinducalendar.domain.repository.PanchangRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val panchangRepository: PanchangRepository,
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    val language: StateFlow<AppLanguage> = preferencesRepository.preferencesFlow
        .map { it.language }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppLanguage.ENGLISH)

    private val _displayedMonth = MutableStateFlow(YearMonth.now())
    val displayedMonth: StateFlow<YearMonth> = _displayedMonth

    private val _monthPanchang = MutableStateFlow<List<PanchangDay>>(emptyList())
    val monthPanchang: StateFlow<List<PanchangDay>> = _monthPanchang

    /** Reference-location panchang keyed by day-of-month, for IST detail view. */
    private val _refPanchangByDay = MutableStateFlow<Map<Int, PanchangDay>>(emptyMap())
    val refPanchangByDay: StateFlow<Map<Int, PanchangDay>> = _refPanchangByDay

    /** Festivals from reference location keyed by day-of-month. */
    private val _monthFestivals = MutableStateFlow<Map<Int, List<FestivalOccurrence>>>(emptyMap())
    val monthFestivals: StateFlow<Map<Int, List<FestivalOccurrence>>> = _monthFestivals

    private val _selectedPanchang = MutableStateFlow<PanchangDay?>(null)
    val selectedPanchang: StateFlow<PanchangDay?> = _selectedPanchang

    val festivalDateReference: StateFlow<FestivalDateReference> = preferencesRepository.preferencesFlow
        .map { it.festivalDateReference }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FestivalDateReference.INDIAN_STANDARD)

    init {
        observeChanges()
    }

    private data class PrefsKey(
        val location: HinduLocation,
        val tradition: CalendarTradition,
        val festivalRef: FestivalDateReference
    )

    private fun observeChanges() {
        viewModelScope.launch {
            val prefsKey = preferencesRepository.preferencesFlow
                .map { PrefsKey(it.location, it.tradition, it.festivalDateReference) }
                .distinctUntilChanged()

            combine(prefsKey, _displayedMonth) { key, ym ->
                key to ym
            }.collect { (key, ym) ->
                val (location, tradition, festivalRef) = key
                val refLocation = if (festivalRef == FestivalDateReference.INDIAN_STANDARD)
                    HinduLocation.DELHI else location

                val (userPanchang, refPanchang) = withContext(Dispatchers.Default) {
                    val user = panchangRepository.computeMonthlyPanchang(
                        ym.year, ym.monthValue, location, tradition
                    )
                    val ref = panchangRepository.computeMonthlyPanchang(
                        ym.year, ym.monthValue, refLocation, tradition
                    )
                    user to ref
                }

                _monthPanchang.value = userPanchang

                // Build reference maps keyed by day-of-month
                val festMap = mutableMapOf<Int, List<FestivalOccurrence>>()
                val refMap = mutableMapOf<Int, PanchangDay>()
                for (p in refPanchang) {
                    val day = p.date.dayOfMonth
                    refMap[day] = p
                    if (p.festivals.isNotEmpty()) {
                        festMap[day] = p.festivals
                    }
                }
                _monthFestivals.value = festMap
                _refPanchangByDay.value = refMap

                val today = LocalDate.now()
                if (ym == YearMonth.from(today)) {
                    _selectedPanchang.value = userPanchang.find { it.date == today }
                } else if (_selectedPanchang.value == null || _selectedPanchang.value?.date?.let { YearMonth.from(it) } != ym) {
                    _selectedPanchang.value = userPanchang.firstOrNull()
                }
            }
        }
    }

    fun previousMonth() {
        _displayedMonth.value = _displayedMonth.value.minusMonths(1)
    }

    fun nextMonth() {
        _displayedMonth.value = _displayedMonth.value.plusMonths(1)
    }

    fun selectDate(date: LocalDate) {
        viewModelScope.launch {
            val panchang = _monthPanchang.value.find { it.date == date }
                ?: withContext(Dispatchers.Default) {
                    val prefs = preferencesRepository.preferencesFlow.first()
                    panchangRepository.computePanchang(date, prefs.location, prefs.tradition)
                }
            if (panchang != null) _selectedPanchang.value = panchang
        }
    }
}
