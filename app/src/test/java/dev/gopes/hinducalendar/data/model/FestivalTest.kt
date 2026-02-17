package dev.gopes.hinducalendar.data.model

import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

/**
 * Unit tests for Festival, FestivalCategory, FestivalOccurrence, and FestivalRule models.
 */
class FestivalTest {

    // ==================== Festival ====================

    @Test
    fun `displayName returns English name from names map`() {
        val festival = Festival(
            id = "holi",
            names = mapOf("en" to "Holi", "hi" to "होली"),
            description = mapOf("en" to "Festival of colors"),
            rule = FestivalRule(type = "tithi"),
            traditions = listOf("purnimant"),
            category = FestivalCategory.MAJOR
        )
        assertEquals("Holi", festival.displayName)
    }

    @Test
    fun `displayName returns empty string when no English name`() {
        val festival = Festival(
            id = "test",
            names = mapOf("hi" to "होली"),
            description = mapOf(),
            rule = FestivalRule(type = "tithi"),
            traditions = listOf("purnimant"),
            category = FestivalCategory.MAJOR
        )
        assertEquals("", festival.displayName)
    }

    @Test
    fun `displayName with language returns localized name`() {
        val festival = Festival(
            id = "diwali",
            names = mapOf("en" to "Diwali", "hi" to "दीवाली", "gu" to "દિવાળી"),
            description = mapOf("en" to "Festival of lights"),
            rule = FestivalRule(type = "tithi"),
            traditions = listOf("purnimant"),
            category = FestivalCategory.MAJOR
        )
        assertEquals("दीवाली", festival.displayName(AppLanguage.HINDI))
        assertEquals("દિવાળી", festival.displayName(AppLanguage.GUJARATI))
        assertEquals("Diwali", festival.displayName(AppLanguage.ENGLISH))
    }

    @Test
    fun `displayName with language falls back to English when locale missing`() {
        val festival = Festival(
            id = "diwali",
            names = mapOf("en" to "Diwali"),
            description = mapOf("en" to "Festival of lights"),
            rule = FestivalRule(type = "tithi"),
            traditions = listOf("purnimant"),
            category = FestivalCategory.MAJOR
        )
        assertEquals("Diwali", festival.displayName(AppLanguage.TAMIL))
    }

    @Test
    fun `descriptionText returns English description`() {
        val festival = Festival(
            id = "test",
            names = mapOf("en" to "Test"),
            description = mapOf("en" to "A festival", "hi" to "एक त्योहार"),
            rule = FestivalRule(type = "tithi"),
            traditions = listOf("purnimant"),
            category = FestivalCategory.MODERATE
        )
        assertEquals("A festival", festival.descriptionText(AppLanguage.ENGLISH))
        assertEquals("एक त्योहार", festival.descriptionText(AppLanguage.HINDI))
    }

    @Test
    fun `durationDays defaults to 1`() {
        val festival = Festival(
            id = "test",
            names = mapOf("en" to "Test"),
            description = mapOf("en" to "Test"),
            rule = FestivalRule(type = "tithi"),
            traditions = listOf("purnimant"),
            category = FestivalCategory.MODERATE
        )
        assertEquals(1, festival.durationDays)
    }

    @Test
    fun `story returns empty string when stories is null`() {
        val festival = Festival(
            id = "test",
            names = mapOf("en" to "Test"),
            description = mapOf("en" to "Test"),
            rule = FestivalRule(type = "tithi"),
            traditions = listOf("purnimant"),
            category = FestivalCategory.MODERATE,
            stories = null
        )
        assertEquals("", festival.story(AppLanguage.ENGLISH))
    }

    // ==================== FestivalCategory ====================

    @Test
    fun `FestivalCategory has expected values`() {
        val categories = FestivalCategory.entries.map { it.name }.toSet()
        assertTrue("Should contain MAJOR", categories.contains("MAJOR"))
        assertTrue("Should contain MODERATE", categories.contains("MODERATE"))
        assertTrue("Should contain RECURRING", categories.contains("RECURRING"))
        assertTrue("Should contain REGIONAL", categories.contains("REGIONAL"))
        assertTrue("Should contain VRAT", categories.contains("VRAT"))
    }

    @Test
    fun `FestivalCategory has exactly 5 values`() {
        assertEquals(5, FestivalCategory.entries.size)
    }

    @Test
    fun `FestivalCategory displayNames are non-empty`() {
        for (category in FestivalCategory.entries) {
            assertTrue("displayName for ${category.name} should not be empty",
                category.displayName.isNotEmpty())
        }
    }

    @Test
    fun `FestivalCategory MAJOR displayName is Major Festival`() {
        assertEquals("Major Festival", FestivalCategory.MAJOR.displayName)
    }

    @Test
    fun `FestivalCategory VRAT displayName contains Vrat`() {
        assertTrue("VRAT displayName should contain 'Vrat'",
            FestivalCategory.VRAT.displayName.contains("Vrat"))
    }

    // ==================== FestivalOccurrence ====================

    @Test
    fun `FestivalOccurrence id format is festivalId_date`() {
        val festival = Festival(
            id = "holi",
            names = mapOf("en" to "Holi"),
            description = mapOf("en" to "Colors"),
            rule = FestivalRule(type = "tithi"),
            traditions = listOf("purnimant"),
            category = FestivalCategory.MAJOR
        )
        val occurrence = FestivalOccurrence(
            festival = festival,
            date = LocalDate.of(2025, 3, 14)
        )
        assertEquals("holi_2025-03-14", occurrence.id)
    }

    @Test
    fun `FestivalOccurrence endDate defaults to null`() {
        val festival = Festival(
            id = "test",
            names = mapOf("en" to "Test"),
            description = mapOf("en" to "Test"),
            rule = FestivalRule(type = "tithi"),
            traditions = listOf("purnimant"),
            category = FestivalCategory.MODERATE
        )
        val occurrence = FestivalOccurrence(festival = festival, date = LocalDate.of(2025, 1, 1))
        assertNull(occurrence.endDate)
    }

    // ==================== FestivalRule ====================

    @Test
    fun `FestivalRule with tithi type has expected fields`() {
        val rule = FestivalRule(
            type = "tithi",
            month = "chaitra",
            paksha = "shukla",
            tithi = 9
        )
        assertEquals("tithi", rule.type)
        assertEquals("chaitra", rule.month)
        assertEquals("shukla", rule.paksha)
        assertEquals(9, rule.tithi)
    }

    @Test
    fun `FestivalRule optional fields default to null`() {
        val rule = FestivalRule(type = "solar")
        assertNull(rule.month)
        assertNull(rule.paksha)
        assertNull(rule.tithi)
        assertNull(rule.solarEvent)
        assertNull(rule.daysAfter)
        assertNull(rule.daysBefore)
    }
}
