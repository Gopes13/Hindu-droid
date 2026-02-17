package dev.gopes.hinducalendar.data.model

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for Nakshatra enum — count, numbers, names, and fromSiderealLongitude.
 */
class NakshatraTest {

    @Test
    fun `there are exactly 27 nakshatras`() {
        assertEquals(27, Nakshatra.entries.size)
    }

    @Test
    fun `nakshatra numbers range from 1 to 27`() {
        val numbers = Nakshatra.entries.map { it.number }.sorted()
        assertEquals((1..27).toList(), numbers)
    }

    @Test
    fun `all nakshatras have unique numbers`() {
        val numbers = Nakshatra.entries.map { it.number }
        assertEquals("All nakshatra numbers should be unique",
            numbers.size, numbers.toSet().size)
    }

    @Test
    fun `displayName is non-empty for all nakshatras`() {
        for (nakshatra in Nakshatra.entries) {
            assertTrue("displayName for ${nakshatra.name} should not be empty",
                nakshatra.displayName.isNotEmpty())
        }
    }

    @Test
    fun `hindiName is non-empty for all nakshatras`() {
        for (nakshatra in Nakshatra.entries) {
            assertTrue("hindiName for ${nakshatra.name} should not be empty",
                nakshatra.hindiName.isNotEmpty())
        }
    }

    @Test
    fun `deity is non-empty for all nakshatras`() {
        for (nakshatra in Nakshatra.entries) {
            assertTrue("deity for ${nakshatra.name} should not be empty",
                nakshatra.deity.isNotEmpty())
        }
    }

    @Test
    fun `SPAN_DEGREES is 360 divided by 27`() {
        assertEquals(360.0 / 27.0, Nakshatra.SPAN_DEGREES, 0.0001)
    }

    @Test
    fun `fromSiderealLongitude at 0 degrees returns ASHWINI`() {
        val result = Nakshatra.fromSiderealLongitude(0.0)
        assertEquals(Nakshatra.ASHWINI, result)
    }

    @Test
    fun `fromSiderealLongitude at 13 degrees returns ASHWINI`() {
        // ASHWINI spans 0 to 13.333 degrees
        val result = Nakshatra.fromSiderealLongitude(13.0)
        assertEquals(Nakshatra.ASHWINI, result)
    }

    @Test
    fun `fromSiderealLongitude at 14 degrees returns BHARANI`() {
        // BHARANI starts at 13.333 degrees
        val result = Nakshatra.fromSiderealLongitude(14.0)
        assertEquals(Nakshatra.BHARANI, result)
    }

    @Test
    fun `fromSiderealLongitude at 359 degrees returns REVATI`() {
        // REVATI is the last nakshatra
        val result = Nakshatra.fromSiderealLongitude(359.0)
        assertEquals(Nakshatra.REVATI, result)
    }

    @Test
    fun `fromSiderealLongitude handles negative longitude`() {
        // Negative longitudes should be normalized
        val result = Nakshatra.fromSiderealLongitude(-10.0)
        // -10 normalized to 350, which falls in REVATI (346.67 to 360)
        assertEquals(Nakshatra.REVATI, result)
    }

    @Test
    fun `fromSiderealLongitude handles longitude above 360`() {
        // 370 degrees normalized to 10 degrees -> ASHWINI
        val result = Nakshatra.fromSiderealLongitude(370.0)
        assertEquals(Nakshatra.ASHWINI, result)
    }

    @Test
    fun `ASHWINI is number 1 and REVATI is number 27`() {
        assertEquals(1, Nakshatra.ASHWINI.number)
        assertEquals(27, Nakshatra.REVATI.number)
    }

    @Test
    fun `specific nakshatras have correct names`() {
        assertEquals("Ashwini", Nakshatra.ASHWINI.displayName)
        assertEquals("Rohini", Nakshatra.ROHINI.displayName)
        assertEquals("Pushya", Nakshatra.PUSHYA.displayName)
        assertEquals("Swati", Nakshatra.SWATI.displayName)
        assertEquals("Revati", Nakshatra.REVATI.displayName)
    }
}
