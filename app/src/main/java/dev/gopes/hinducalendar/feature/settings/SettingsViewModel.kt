package dev.gopes.hinducalendar.feature.settings

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.gopes.hinducalendar.domain.model.*
import dev.gopes.hinducalendar.domain.repository.PreferencesRepository
import dev.gopes.hinducalendar.domain.repository.PanchangRepository
import dev.gopes.hinducalendar.data.service.CalendarSyncService
import dev.gopes.hinducalendar.data.service.NotificationHelper
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
    private val panchangRepository: PanchangRepository,
    private val notificationHelper: NotificationHelper
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
            // Switch Android locale so string resources update
            val tag = when (language) {
                AppLanguage.ENGLISH, AppLanguage.HINGLISH -> "en"
                else -> language.code
            }
            AppCompatDelegate.setApplicationLocales(
                LocaleListCompat.forLanguageTags(tag)
            )
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
            if (enabled) {
                notificationHelper.scheduleFestivalWorker()
            } else {
                notificationHelper.cancelFestivalWorker()
            }
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

    fun updateFestivalDateReference(ref: FestivalDateReference) {
        viewModelScope.launch {
            preferencesRepository.update { it.copy(festivalDateReference = ref) }
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
            val panchangDays = (0 until 90).mapNotNull { offset ->
                panchangRepository.computePanchang(
                    today.plusDays(offset.toLong()),
                    prefs.location,
                    prefs.tradition
                )
            }
            calendarSyncService.syncMonth(panchangDays, prefs.syncOption, prefs.reminderTimings, prefs.language)
        }
    }
}
