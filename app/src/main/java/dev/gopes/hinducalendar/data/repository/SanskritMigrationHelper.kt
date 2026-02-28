package dev.gopes.hinducalendar.data.repository

import dev.gopes.hinducalendar.domain.model.SanskritProgress

/**
 * Migrates existing [SanskritProgress] from the flat 13-module structure
 * into the 7-Kāṇḍa hierarchy. Runs lazily on first access.
 *
 * Existing module IDs (module1...module13) map to Kāṇḍa 1.
 * If all 13 are complete, Kāṇḍa 1 is marked complete and the user
 * starts at Kāṇḍa 2.
 */
object SanskritMigrationHelper {

    private val KANDA_1_MODULE_IDS = (1..13).map { "module$it" }.toSet()

    fun migrateIfNeeded(progress: SanskritProgress): SanskritProgress {
        if (progress.completedKandas.isNotEmpty()) return progress
        if (progress.completedModules.isEmpty()) return progress

        val kanda1Complete = progress.completedModules.containsAll(KANDA_1_MODULE_IDS)

        return progress.copy(
            completedKandas = if (kanda1Complete) setOf("kanda_1") else emptySet(),
            currentKandaId = if (kanda1Complete) "kanda_2" else "kanda_1",
            earnedMilestones = if (kanda1Complete) setOf("aksara_siddhi") else emptySet()
        )
    }
}
