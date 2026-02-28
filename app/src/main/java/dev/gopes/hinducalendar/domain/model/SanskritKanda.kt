package dev.gopes.hinducalendar.domain.model

/**
 * Top-level curriculum unit — a Kāṇḍa (Book) in the Sanskrit Pathshala.
 * 7 Kāṇḍas form the complete learning journey from script to independent reading.
 */
data class SanskritKanda(
    val id: String,
    val number: Int,
    val titleSanskrit: String,
    val titleTranslit: String,
    val titles: Map<String, String>,
    val descriptions: Map<String, String>,
    val icon: String,
    val modules: List<SanskritModule>,
    val milestoneId: String,
    val milestoneTitles: Map<String, String>,
    val unlockRequirement: KandaUnlockRequirement
)

data class KandaUnlockRequirement(
    val type: String = "none",
    val kandaId: String? = null
) {
    companion object {
        val NONE = KandaUnlockRequirement("none")
        fun kandaComplete(kandaId: String) = KandaUnlockRequirement("kanda_complete", kandaId)
    }
}
