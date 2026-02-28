package dev.gopes.hinducalendar.domain.model

/**
 * A milestone earned upon completing a Kāṇḍa.
 */
data class SanskritMilestone(
    val id: String,
    val kandaNumber: Int,
    val titles: Map<String, String>,
    val descriptions: Map<String, String>,
    val icon: String
)
