package dev.gopes.hinducalendar.core.ui

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.gopes.hinducalendar.domain.repository.PreferencesRepository
import kotlinx.coroutines.flow.SharedFlow
import javax.inject.Inject

@HiltViewModel
class AppViewModel @Inject constructor(
    preferencesRepository: PreferencesRepository
) : ViewModel() {
    val errors: SharedFlow<String> = preferencesRepository.errors
}
