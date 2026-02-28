package dev.gopes.hinducalendar.data.sanskrit

import dev.gopes.hinducalendar.data.repository.SanskritMigrationHelper
import dev.gopes.hinducalendar.domain.model.SanskritProgress
import org.junit.Assert.*
import org.junit.Test

class SanskritMigrationTest {

    @Test
    fun `fresh user needs no migration`() {
        val progress = SanskritProgress()
        val migrated = SanskritMigrationHelper.migrateIfNeeded(progress)
        assertEquals(progress, migrated)
        assertTrue(migrated.completedKandas.isEmpty())
        assertEquals("kanda_1", migrated.currentKandaId)
    }

    @Test
    fun `already migrated user is unchanged`() {
        val progress = SanskritProgress(
            completedKandas = setOf("kanda_1"),
            currentKandaId = "kanda_2"
        )
        val migrated = SanskritMigrationHelper.migrateIfNeeded(progress)
        assertEquals(progress, migrated)
    }

    @Test
    fun `partial kanda 1 progress does not mark kanda complete`() {
        val progress = SanskritProgress(
            completedModules = setOf("module1", "module2", "module3")
        )
        val migrated = SanskritMigrationHelper.migrateIfNeeded(progress)
        assertTrue(migrated.completedKandas.isEmpty())
        assertEquals("kanda_1", migrated.currentKandaId)
        assertTrue(migrated.earnedMilestones.isEmpty())
    }

    @Test
    fun `all 13 modules complete marks kanda 1 complete`() {
        val allModules = (1..13).map { "module$it" }.toSet()
        val progress = SanskritProgress(completedModules = allModules)
        val migrated = SanskritMigrationHelper.migrateIfNeeded(progress)

        assertTrue("kanda_1" in migrated.completedKandas)
        assertEquals("kanda_2", migrated.currentKandaId)
        assertTrue("aksara_siddhi" in migrated.earnedMilestones)
    }

    @Test
    fun `12 of 13 modules does not mark kanda complete`() {
        val modules = (1..12).map { "module$it" }.toSet()
        val progress = SanskritProgress(completedModules = modules)
        val migrated = SanskritMigrationHelper.migrateIfNeeded(progress)

        assertTrue(migrated.completedKandas.isEmpty())
        assertEquals("kanda_1", migrated.currentKandaId)
    }

    @Test
    fun `migration preserves existing lesson and letter progress`() {
        val allModules = (1..13).map { "module$it" }.toSet()
        val progress = SanskritProgress(
            completedLessons = setOf("module1_l1", "module1_l2"),
            masteredLetters = setOf("a", "aa", "i"),
            completedModules = allModules,
            exploredVerses = setOf("bg_2_47"),
            lastStudyDate = "2026-02-27"
        )
        val migrated = SanskritMigrationHelper.migrateIfNeeded(progress)

        assertEquals(setOf("module1_l1", "module1_l2"), migrated.completedLessons)
        assertEquals(setOf("a", "aa", "i"), migrated.masteredLetters)
        assertEquals(allModules, migrated.completedModules)
        assertEquals(setOf("bg_2_47"), migrated.exploredVerses)
        assertEquals("2026-02-27", migrated.lastStudyDate)
        assertTrue("kanda_1" in migrated.completedKandas)
    }
}
