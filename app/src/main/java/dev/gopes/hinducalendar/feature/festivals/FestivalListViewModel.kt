package dev.gopes.hinducalendar.feature.festivals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.gopes.hinducalendar.domain.model.*
import dev.gopes.hinducalendar.domain.repository.PreferencesRepository
import dev.gopes.hinducalendar.domain.repository.PanchangRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class FestivalListViewModel @Inject constructor(
    private val panchangRepository: PanchangRepository,
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    private val _festivals = MutableStateFlow<List<Pair<FestivalOccurrence, PanchangDay>>>(emptyList())
    val festivals: StateFlow<List<Pair<FestivalOccurrence, PanchangDay>>> = _festivals

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _language = MutableStateFlow(AppLanguage.ENGLISH)
    val language: StateFlow<AppLanguage> = _language

    init {
        observePreferences()
    }

    private data class PrefsKey(
        val location: HinduLocation,
        val tradition: CalendarTradition,
        val language: AppLanguage,
        val festivalRef: FestivalDateReference
    )

    private fun observePreferences() {
        viewModelScope.launch {
            preferencesRepository.preferencesFlow
                .map { PrefsKey(it.location, it.tradition, it.language, it.festivalDateReference) }
                .distinctUntilChanged()
                .collect { (location, tradition, language, festivalRef) ->
                    _isLoading.value = true
                    _language.value = language

                    val referenceLocation = if (festivalRef == FestivalDateReference.INDIAN_STANDARD)
                        HinduLocation.DELHI else location

                    val results = withContext(Dispatchers.Default) {
                        val list = mutableListOf<Pair<FestivalOccurrence, PanchangDay>>()
                        val today = LocalDate.now()
                        for (dayOffset in 0 until 90) {
                            val date = today.plusDays(dayOffset.toLong())
                            val panchang = panchangRepository.computePanchang(date, referenceLocation, tradition) ?: continue
                            for (festival in panchang.festivals) {
                                if (festival.festival.category == FestivalCategory.MAJOR ||
                                    festival.festival.category == FestivalCategory.MODERATE) {
                                    list.add(festival to panchang)
                                }
                            }
                        }
                        list
                    }

                    _festivals.value = results
                    _isLoading.value = false
                }
        }
    }
}
