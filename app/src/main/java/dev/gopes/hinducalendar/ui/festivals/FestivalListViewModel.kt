package dev.gopes.hinducalendar.ui.festivals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.gopes.hinducalendar.data.model.*
import dev.gopes.hinducalendar.engine.PanchangService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class FestivalListViewModel @Inject constructor(
    private val panchangService: PanchangService
) : ViewModel() {

    private val _festivals = MutableStateFlow<List<Pair<FestivalOccurrence, PanchangDay>>>(emptyList())
    val festivals: StateFlow<List<Pair<FestivalOccurrence, PanchangDay>>> = _festivals

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val preferences = UserPreferences()

    init {
        loadFestivals()
    }

    private fun loadFestivals() {
        viewModelScope.launch(Dispatchers.Default) {
            _isLoading.value = true
            val results = mutableListOf<Pair<FestivalOccurrence, PanchangDay>>()
            val today = LocalDate.now()

            for (dayOffset in 0 until 90) {
                val date = today.plusDays(dayOffset.toLong())
                val panchang = panchangService.computePanchang(date, preferences.location, preferences.tradition)
                for (festival in panchang.festivals) {
                    if (festival.festival.category == FestivalCategory.MAJOR ||
                        festival.festival.category == FestivalCategory.MODERATE) {
                        results.add(festival to panchang)
                    }
                }
            }

            _festivals.value = results
            _isLoading.value = false
        }
    }
}
