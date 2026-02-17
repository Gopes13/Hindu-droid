package dev.gopes.hinducalendar.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.gopes.hinducalendar.data.model.*
import dev.gopes.hinducalendar.data.repository.PreferencesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    val preferences: StateFlow<UserPreferences> = preferencesRepository.preferencesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserPreferences())

    fun updateDharmaPath(path: DharmaPath) {
        viewModelScope.launch {
            preferencesRepository.update { it.copy(dharmaPath = path) }
        }
    }

    fun updateLanguage(language: AppLanguage) {
        viewModelScope.launch {
            preferencesRepository.update { it.copy(language = language) }
        }
    }

    fun updateTradition(tradition: CalendarTradition) {
        viewModelScope.launch {
            preferencesRepository.update { it.copy(tradition = tradition) }
        }
    }

    fun updateSyncEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.update { it.copy(syncToCalendar = enabled) }
        }
    }

    fun updateSyncOption(option: CalendarSyncOption) {
        viewModelScope.launch {
            preferencesRepository.update { it.copy(syncOption = option) }
        }
    }

    fun updateNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.update { it.copy(notificationsEnabled = enabled) }
        }
    }

    fun updateReminderTiming(timing: ReminderTiming) {
        viewModelScope.launch {
            preferencesRepository.update { it.copy(reminderTiming = timing) }
        }
    }

    fun updateNotificationTime(hour: Int, minute: Int) {
        viewModelScope.launch {
            preferencesRepository.update {
                it.copy(notificationTime = NotificationTime(hour, minute))
            }
        }
    }

    fun updateContentPreferences(content: ContentPreferences) {
        viewModelScope.launch {
            preferencesRepository.update { it.copy(contentPreferences = content) }
        }
    }

    fun resetReadingProgress() {
        viewModelScope.launch {
            preferencesRepository.update { it.copy(readingProgress = ReadingProgress()) }
        }
    }
}
