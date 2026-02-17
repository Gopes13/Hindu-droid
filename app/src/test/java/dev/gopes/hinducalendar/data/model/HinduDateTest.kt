package dev.gopes.hinducalendar.data.model

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for HinduDate, HinduMonth, and Paksha models.
 */
class HinduDateTest {

    // ==================== HinduDate ====================

    @Test
    fun `displayString contains month paksha and tithi names`() {
        val hinduDate = HinduDate(
            month = HinduMonth.CHAITRA,
            paksha = Paksha.SHUKLA,
            tithi = Tithi.NAVAMI,
            samvatYear = 2082,
            shakaYear = 1947
        )
        val display = hinduDate.displayString
        assertTrue("Should contain month name", display.contains("Chaitra"))
        assertTrue("Should contain paksha name", display.contains("Shukla"))
        assertTrue("Should contain tithi name", display.contains("Navami"))
    }

    @Test
    fun `displayString for adhik maas includes Adhik prefix`() {
        val hinduDate = HinduDate(
            month = HinduMonth.SHRAVANA,
            paksha = Paksha.KRISHNA,
            tithi = Tithi.ASHTAMI,
            samvatYear = 2082,
            shakaYear = 1947,
            isAdhikMaas = true
        )
        assertTrue("Should contain 'Adhik'", hinduDate.displayString.contains("Adhik"))
    }

    @Test
    fun `displayString for regular month does not include Adhik`() {
        val hinduDate = HinduDate(
            month = HinduMonth.KARTIK,
            paksha = Paksha.SHUKLA,
            tithi = Tithi.PURNIMA,
            samvatYear = 2082,
            shakaYear = 1947,
            isAdhikMaas = false
        )
        assertFalse("Should not contain 'Adhik'", hinduDate.displayString.contains("Adhik"))
    }

    @Test
    fun `fullDisplayString includes Vikram Samvat year`() {
        val hinduDate = HinduDate(
            month = HinduMonth.CHAITRA,
            paksha = Paksha.SHUKLA,
            tithi = Tithi.PRATIPADA,
            samvatYear = 2082,
            shakaYear = 1947
        )
        assertTrue("Should contain 'Vikram Samvat'",
            hinduDate.fullDisplayString.contains("Vikram Samvat"))
        assertTrue("Should contain year 2082",
            hinduDate.fullDisplayString.contains("2082"))
    }

    @Test
    fun `isAdhikMaas defaults to false`() {
        val hinduDate = HinduDate(
            month = HinduMonth.CHAITRA,
            paksha = Paksha.SHUKLA,
            tithi = Tithi.PRATIPADA,
            samvatYear = 2082,
            shakaYear = 1947
        )
        assertFalse(hinduDate.isAdhikMaas)
    }

    // ==================== HinduMonth ====================

    @Test
    fun `there are exactly 12 Hindu months`() {
        assertEquals(12, HinduMonth.entries.size)
    }

    @Test
    fun `Hindu month numbers range from 1 to 12`() {
        val numbers = HinduMonth.entries.map { it.number }.sorted()
        assertEquals((1..12).toList(), numbers)
    }

    @Test
    fun `all Hindu months have non-empty displayName`() {
        for (month in HinduMonth.entries) {
            assertTrue("displayName for ${month.name} should not be empty",
                month.displayName.isNotEmpty())
        }
    }

    @Test
    fun `all Hindu months have non-empty hindiName`() {
        for (month in HinduMonth.entries) {
            assertTrue("hindiName for ${month.name} should not be empty",
                month.hindiName.isNotEmpty())
        }
    }

    @Test
    fun `CHAITRA is month 1`() {
        assertEquals(1, HinduMonth.CHAITRA.number)
    }

    @Test
    fun `PHALGUNA is month 12`() {
        assertEquals(12, HinduMonth.PHALGUNA.number)
    }

    @Test
    fun `fromSolarMonth maps correctly`() {
        // solarMonth 0 -> month 1 = CHAITRA
        assertEquals(HinduMonth.CHAITRA, HinduMonth.fromSolarMonth(0))
        // solarMonth 1 -> month 2 = VAISHAKHA
        assertEquals(HinduMonth.VAISHAKHA, HinduMonth.fromSolarMonth(1))
        // solarMonth 11 -> month 12 = PHALGUNA
        assertEquals(HinduMonth.PHALGUNA, HinduMonth.fromSolarMonth(11))
    }

    @Test
    fun `fromSolarMonth returns CHAITRA for out-of-range value`() {
        val result = HinduMonth.fromSolarMonth(99)
        assertEquals(HinduMonth.CHAITRA, result)
    }

    // ==================== Paksha ====================

    @Test
    fun `there are exactly 2 pakshas`() {
        assertEquals(2, Paksha.entries.size)
    }

    @Test
    fun `Paksha SHUKLA displayName is Shukla`() {
        assertEquals("Shukla", Paksha.SHUKLA.displayName)
    }

    @Test
    fun `Paksha KRISHNA displayName is Krishna`() {
        assertEquals("Krishna", Paksha.KRISHNA.displayName)
    }

    @Test
    fun `Paksha key is lowercase name`() {
        assertEquals("shukla", Paksha.SHUKLA.key)
        assertEquals("krishna", Paksha.KRISHNA.key)
    }

    @Test
    fun `Paksha hindiName is non-empty`() {
        for (paksha in Paksha.entries) {
            assertTrue("hindiName for ${paksha.name} should not be empty",
                paksha.hindiName.isNotEmpty())
        }
    }
}
