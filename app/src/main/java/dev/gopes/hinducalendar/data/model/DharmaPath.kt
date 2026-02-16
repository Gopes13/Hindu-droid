package dev.gopes.hinducalendar.data.model

enum class DharmaPath(val displayName: String, val description: String, val primaryTextId: String) {
    GENERAL("General Hindu", "Broad Hindu practice including Gita, Hanuman Chalisa, and major festivals.", "gita"),
    VAISHNAV("Vaishnav", "Devotion to Lord Vishnu and his avatars.", "gita"),
    SHAIV("Shaiv", "Devotion to Lord Shiva.", "gita"),
    SHAKTA("Shakta", "Devotion to the Divine Mother.", "devi_mahatmya"),
    SMARTA("Smarta", "Worship of all five deities.", "gita"),
    ISKCON("ISKCON", "International Society for Krishna Consciousness.", "gita"),
    SWAMINARAYAN("Swaminarayan", "Swaminarayan Sampradaya.", "shikshapatri"),
    SIKH("Sikh", "Sikh Dharma founded by Guru Nanak Dev Ji.", "japji_sahib"),
    JAIN("Jain", "Jain Dharma — path of non-violence.", "tattvartha_sutra");

    val availableTextIds: List<String>
        get() = when (this) {
            GENERAL -> listOf("gita", "hanuman_chalisa")
            VAISHNAV -> listOf("gita", "bhagavata", "vishnu_sahasranama", "hanuman_chalisa")
            SHAIV -> listOf("gita", "shiva_purana", "rudram")
            SHAKTA -> listOf("devi_mahatmya", "soundarya_lahari", "gita")
            SMARTA -> listOf("gita", "vishnu_sahasranama", "rudram", "soundarya_lahari")
            ISKCON -> listOf("gita", "bhagavata")
            SWAMINARAYAN -> listOf("shikshapatri", "vachanamrut", "gita")
            SIKH -> listOf("japji_sahib", "sukhmani", "gurbani")
            JAIN -> listOf("tattvartha_sutra", "jain_prayers")
        }
}
