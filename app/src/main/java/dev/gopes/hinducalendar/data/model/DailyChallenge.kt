package dev.gopes.hinducalendar.data.model

data class DailyChallenge(
    val id: String,
    val type: ChallengeType,
    val question: String,
    val options: List<String>,
    val correctOptionIndex: Int,
    val xpReward: Int = 15
)

enum class ChallengeType(val titleKey: String, val icon: String) {
    PANCHANG_EXPLORER("challenge_panchang", "nightlight"),
    FESTIVAL_KNOWLEDGE("challenge_festival", "star"),
    VERSE_REFLECTION("challenge_verse", "menu_book"),
    MANTRA_MATCH("challenge_mantra", "graphic_eq")
}
