package dev.gopes.hinducalendar.data.model

data class ReadingProgress(
    var gitaChapter: Int = 1,
    var gitaVerse: Int = 1,
    var chalisaVerse: Int = 1,
    var japjiPauri: Int = 1,
    var bhagavataEpisode: Int = 1,
    var vishnuSahasranamaShloka: Int = 1,
    var shivaPuranaEpisode: Int = 1,
    var rudramAnuvaka: Int = 1,
    var deviMahatmyaChapter: Int = 1,
    var soundaryaLahariVerse: Int = 1,
    var shikshapatriShloka: Int = 1,
    var vachanamrutDiscourse: Int = 1,
    var sukhmaniAshtpadi: Int = 1,
    var gurbaniDay: Int = 1,
    var tattvarthaSutraChapter: Int = 1,
    var tattvarthaSutraSutra: Int = 1,
    var jainTeachingEpisode: Int = 1
) {
    fun currentPosition(textType: SacredTextType): Int = when (textType) {
        SacredTextType.GITA -> gitaVerse
        SacredTextType.HANUMAN_CHALISA -> chalisaVerse
        SacredTextType.JAPJI_SAHIB -> japjiPauri
        SacredTextType.BHAGAVATA -> bhagavataEpisode
        SacredTextType.VISHNU_SAHASRANAMA -> vishnuSahasranamaShloka
        SacredTextType.SHIVA_PURANA -> shivaPuranaEpisode
        SacredTextType.RUDRAM -> rudramAnuvaka
        SacredTextType.DEVI_MAHATMYA -> deviMahatmyaChapter
        SacredTextType.SOUNDARYA_LAHARI -> soundaryaLahariVerse
        SacredTextType.SHIKSHAPATRI -> shikshapatriShloka
        SacredTextType.VACHANAMRUT -> vachanamrutDiscourse
        SacredTextType.SUKHMANI -> sukhmaniAshtpadi
        SacredTextType.GURBANI -> gurbaniDay
        SacredTextType.TATTVARTHA_SUTRA -> tattvarthaSutraSutra
        SacredTextType.JAIN_PRAYERS -> jainTeachingEpisode
    }
}

data class ContentPreferences(
    val panchangNotification: Boolean = true,
    val primaryText: Boolean = true,
    val festivalStories: Boolean = true,
    val secondaryText: Boolean = false
)

data class NotificationTime(
    val hour: Int = 7,
    val minute: Int = 0
) {
    val displayString: String
        get() {
            val period = if (hour >= 12) "PM" else "AM"
            val displayHour = if (hour > 12) hour - 12 else if (hour == 0) 12 else hour
            return String.format("%d:%02d %s", displayHour, minute, period)
        }
}
