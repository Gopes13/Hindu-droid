package dev.gopes.hinducalendar.data.model

data class UserPreferences(
    val tradition: CalendarTradition = CalendarTradition.PURNIMANT,
    val location: HinduLocation = HinduLocation.DELHI,
    val syncToCalendar: Boolean = true,
    val syncOption: CalendarSyncOption = CalendarSyncOption.FESTIVALS_ONLY,
    val notificationsEnabled: Boolean = true,
    val reminderTiming: ReminderTiming = ReminderTiming.DAY_BEFORE,
    val language: AppLanguage = AppLanguage.ENGLISH,
    val dharmaPath: DharmaPath = DharmaPath.GENERAL,
    val contentPreferences: ContentPreferences = ContentPreferences(),
    val notificationTime: NotificationTime = NotificationTime(),
    val readingProgress: ReadingProgress = ReadingProgress(),
    val bookmarks: BookmarkCollection = BookmarkCollection()
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

enum class AppLanguage(val displayName: String, val code: String, val nativeScriptName: String) {
    ENGLISH("English", "en", "English"),
    HINDI("Hindi", "hi", "हिन्दी"),
    GUJARATI("Gujarati", "gu", "ગુજરાતી"),
    MARATHI("Marathi", "mr", "मराठी"),
    TAMIL("Tamil", "ta", "தமிழ்"),
    TELUGU("Telugu", "te", "తెలుగు"),
    KANNADA("Kannada", "kn", "ಕನ್ನಡ"),
    MALAYALAM("Malayalam", "ml", "മലയാളം"),
    BENGALI("Bengali", "bn", "বাংলা"),
    PUNJABI("Punjabi", "pa", "ਪੰਜਾਬੀ"),
    ODIA("Odia", "or", "ଓଡ଼ିଆ"),
    ASSAMESE("Assamese", "as", "অসমীয়া");
}

/** Extension to get a localized value from a language-keyed map. */
fun Map<String, String>.localized(language: AppLanguage): String {
    return this[language.code] ?: this["en"] ?: ""
}
