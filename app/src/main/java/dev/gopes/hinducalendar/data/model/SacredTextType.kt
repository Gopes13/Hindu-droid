package dev.gopes.hinducalendar.data.model

enum class SacredTextType(
    val displayName: String,
    val jsonFileName: String,
    val icon: String,
    val themeTag: String,
    val unitLabel: String
) {
    GITA("Bhagavad Gita", "gita", "book", "Philosophy", "verses"),
    HANUMAN_CHALISA("Hanuman Chalisa", "hanuman_chalisa", "music_note", "Devotional Hymn", "verses"),
    JAPJI_SAHIB("Japji Sahib", "japji_sahib", "book_closed", "Morning Prayer", "pauris"),
    BHAGAVATA("Bhagavata Purana", "bhagavata", "books", "Epic Stories", "episodes"),
    VISHNU_SAHASRANAMA("Vishnu Sahasranama", "vishnu_sahasranama", "list", "1000 Names", "shlokas"),
    SHIVA_PURANA("Shiva Purana", "shiva_purana", "books", "Epic Stories", "episodes"),
    RUDRAM("Sri Rudram", "rudram", "waveform", "Vedic Chant", "anuvakas"),
    DEVI_MAHATMYA("Devi Mahatmya", "devi_mahatmya", "flame", "Goddess Glory", "chapters"),
    SOUNDARYA_LAHARI("Soundarya Lahari", "soundarya_lahari", "sparkles", "Devotional Poetry", "verses"),
    SHIKSHAPATRI("Shikshapatri", "shikshapatri", "scroll", "Ethical Guide", "shlokas"),
    VACHANAMRUT("Vachanamrut", "vachanamrut", "text_quote", "Discourses", "discourses"),
    SUKHMANI("Sukhmani Sahib", "sukhmani", "heart", "Meditation", "ashtpadis"),
    GURBANI("Daily Gurbani", "gurbani", "music", "Daily Shabads", "shabads"),
    TATTVARTHA_SUTRA("Tattvartha Sutra", "tattvartha_sutra", "book_text", "Jain Philosophy", "sutras"),
    JAIN_PRAYERS("Jain Prayers", "jain_prayers", "hands", "Prayers & Teachings", "teachings");

    /** Whether this text is suitable for Daily Wisdom sequential reading. */
    val isWisdomEligible: Boolean
        get() = this != HANUMAN_CHALISA && this != GURBANI

    /** Whether this text has audio clips available. */
    val hasAudio: Boolean
        get() = this != VACHANAMRUT
}
