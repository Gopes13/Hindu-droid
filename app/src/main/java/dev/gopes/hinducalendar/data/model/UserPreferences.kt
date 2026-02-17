package dev.gopes.hinducalendar.data.model

data class UserPreferences(
    val tradition: CalendarTradition = CalendarTradition.PURNIMANT,
    val location: HinduLocation = HinduLocation.DELHI,
    val syncToCalendar: Boolean = true,
    val syncOption: CalendarSyncOption = CalendarSyncOption.FESTIVALS_ONLY,
    val notificationsEnabled: Boolean = true,
    val reminderTimings: List<ReminderTiming> = listOf(ReminderTiming.DAY_BEFORE),
    val language: AppLanguage = AppLanguage.ENGLISH,
    val dharmaPath: DharmaPath = DharmaPath.GENERAL,
    val activeWisdomText: SacredTextType? = null,
    val contentPreferences: ContentPreferences = ContentPreferences(),
    val notificationTime: NotificationTime = NotificationTime(),
    val readingProgress: ReadingProgress = ReadingProgress(),
    val bookmarks: BookmarkCollection = BookmarkCollection(),
    val gamificationData: GamificationData = GamificationData(),
    val streakData: StreakData = StreakData()
) {
    /** Returns the active wisdom text, falling back to the dharma path's primary text. */
    val effectiveWisdomText: SacredTextType
        get() {
            val active = activeWisdomText
            if (active != null && active.isWisdomEligible) return active
            return SacredTextType.entries.find { it.jsonFileName == dharmaPath.primaryTextId }
                ?: SacredTextType.GITA
        }
}

enum class CalendarSyncOption(val displayName: String) {
    FESTIVALS_ONLY("Festivals Only"),
    FESTIVALS_AND_TITHIS("Festivals + Important Tithis"),
    FULL_PANCHANG("Full Panchang")
}

enum class ReminderTiming(val displayName: String) {
    MORNING_OF("Morning of the event"),
    EVENING_BEFORE("Evening before (6:00 PM)"),
    DAY_BEFORE("1 day before"),
    TWO_DAYS_BEFORE("2 days before")
}

enum class AppLanguage(val displayName: String, val code: String, val nativeScriptName: String) {
    ENGLISH("English", "en", "English"),
    HINDI("Hindi", "hi", "हिन्दी"),
    HINGLISH("Hinglish", "hl", "Hinglish"),
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

    /** Unicode zero character for native numeral scripts, null for Latin-digit languages. */
    val nativeZero: Char?
        get() = when (this) {
            HINDI, MARATHI -> '\u0966'      // Devanagari ०
            GUJARATI -> '\u0AE6'            // Gujarati ૦
            BENGALI, ASSAMESE -> '\u09E6'   // Bengali ০
            TELUGU -> '\u0C66'              // Telugu ౦
            KANNADA -> '\u0CE6'             // Kannada ೦
            PUNJABI -> '\u0A66'             // Gurmukhi ੦
            ODIA -> '\u0B66'               // Odia ୦
            MALAYALAM -> '\u0D66'           // Malayalam ൦
            else -> null                    // English, Hinglish, Tamil use Latin digits
        }

    /** Convert an integer to native numeral string for this language. */
    fun localizedNumber(n: Int): String {
        val zero = nativeZero ?: return n.toString()
        return n.toString().map { ch ->
            if (ch in '0'..'9') (zero + (ch - '0')) else ch
        }.joinToString("")
    }

    /** Convert all ASCII digits in a string to native numerals. */
    fun localizedDigits(s: String): String {
        val zero = nativeZero ?: return s
        return s.map { ch ->
            if (ch in '0'..'9') (zero + (ch - '0')) else ch
        }.joinToString("")
    }
}

/** Extension to get a localized value from a language-keyed map. */
fun Map<String, String>.localized(language: AppLanguage): String {
    return this[language.code] ?: this["en"] ?: ""
}
