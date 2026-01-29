package dev.gopes.hinducalendar.data.model

data class UserPreferences(
    val tradition: CalendarTradition = CalendarTradition.PURNIMANT,
    val location: HinduLocation = HinduLocation.DELHI,
    val syncToCalendar: Boolean = true,
    val syncOption: CalendarSyncOption = CalendarSyncOption.FESTIVALS_ONLY,
    val notificationsEnabled: Boolean = true,
    val reminderTiming: ReminderTiming = ReminderTiming.DAY_BEFORE,
    val language: AppLanguage = AppLanguage.ENGLISH
)

enum class CalendarSyncOption(val displayName: String) {
    FESTIVALS_ONLY("Festivals Only"),
    FESTIVALS_AND_TITHIS("Festivals + Important Tithis"),
    FULL_PANCHANG("Full Panchang")
}

enum class ReminderTiming(val displayName: String) {
    MORNING_OF("Morning of the event"),
    DAY_BEFORE("1 day before"),
    TWO_DAYS_BEFORE("2 days before")
}

enum class AppLanguage(val displayName: String, val code: String) {
    ENGLISH("English", "en"),
    HINDI("हिन्दी", "hi")
}
