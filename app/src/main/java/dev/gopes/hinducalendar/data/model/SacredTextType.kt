package dev.gopes.hinducalendar.data.model

enum class SacredTextType(val displayName: String, val jsonFileName: String, val icon: String) {
    GITA("Bhagavad Gita", "gita", "book"),
    HANUMAN_CHALISA("Hanuman Chalisa", "hanuman_chalisa", "music_note"),
    JAPJI_SAHIB("Japji Sahib", "japji_sahib", "book_closed"),
    BHAGAVATA("Bhagavata Purana", "bhagavata", "books"),
    VISHNU_SAHASRANAMA("Vishnu Sahasranama", "vishnu_sahasranama", "list"),
    SHIVA_PURANA("Shiva Purana", "shiva_purana", "books"),
    RUDRAM("Sri Rudram", "rudram", "waveform"),
    DEVI_MAHATMYA("Devi Mahatmya", "devi_mahatmya", "flame"),
    SOUNDARYA_LAHARI("Soundarya Lahari", "soundarya_lahari", "sparkles"),
    SHIKSHAPATRI("Shikshapatri", "shikshapatri", "scroll"),
    VACHANAMRUT("Vachanamrut", "vachanamrut", "text_quote"),
    SUKHMANI("Sukhmani Sahib", "sukhmani", "heart"),
    GURBANI("Daily Gurbani", "gurbani", "music"),
    TATTVARTHA_SUTRA("Tattvartha Sutra", "tattvartha_sutra", "book_text"),
    JAIN_PRAYERS("Jain Prayers", "jain_prayers", "hands");

    /** Whether this text is suitable for Daily Wisdom sequential reading. */
    val isWisdomEligible: Boolean
        get() = this != HANUMAN_CHALISA && this != GURBANI
}
