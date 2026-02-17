package dev.gopes.hinducalendar.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.gopes.hinducalendar.data.model.*
import dev.gopes.hinducalendar.data.repository.PreferencesRepository
import dev.gopes.hinducalendar.engine.PanchangService
import dev.gopes.hinducalendar.service.CalendarSyncService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
    private val calendarSyncService: CalendarSyncService,
    private val panchangService: PanchangService
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

    fun toggleReminderTiming(timing: ReminderTiming, enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.update { prefs ->
                val current = prefs.reminderTimings.toMutableList()
                if (enabled && timing !in current) current.add(timing)
                if (!enabled) current.remove(timing)
                prefs.copy(reminderTimings = current)
            }
        }
    }

    fun updateActiveWisdomText(textType: SacredTextType?) {
        viewModelScope.launch {
            preferencesRepository.update { it.copy(activeWisdomText = textType) }
        }
    }

    fun updateLocation(location: HinduLocation) {
        viewModelScope.launch {
            preferencesRepository.update { it.copy(location = location) }
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

    fun syncCalendarNow() {
        viewModelScope.launch(Dispatchers.Default) {
            val prefs = preferencesRepository.preferencesFlow.first()
            if (!prefs.syncToCalendar) return@launch

            val today = LocalDate.now()
            val panchangDays = (0 until 90).map { offset ->
                panchangService.computePanchang(
                    today.plusDays(offset.toLong()),
                    prefs.location,
                    prefs.tradition
                )
            }
            calendarSyncService.syncMonth(panchangDays, prefs.syncOption, prefs.reminderTimings)
        }
    }
}
