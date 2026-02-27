package dev.gopes.hinducalendar.domain.model

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for Tithi enum — numbers, names, and fromNumber lookup.
 */
class TithiTest {

    @Test
    fun `all tithis have unique numbers`() {
        val numbers = Tithi.entries.map { it.number }
        assertEquals("All tithi numbers should be unique",
            numbers.size, numbers.toSet().size)
    }

    @Test
    fun `Tithi PURNIMA has number 15`() {
        assertEquals(15, Tithi.PURNIMA.number)
    }

    @Test
    fun `Tithi AMAVASYA has number 30`() {
        assertEquals(30, Tithi.AMAVASYA.number)
    }

    @Test
    fun `Tithi EKADASHI has number 11`() {
        assertEquals(11, Tithi.EKADASHI.number)
    }

    @Test
    fun `Tithi PRATIPADA has number 1`() {
        assertEquals(1, Tithi.PRATIPADA.number)
    }

    @Test
    fun `Tithi CHATURDASHI has number 14`() {
        assertEquals(14, Tithi.CHATURDASHI.number)
    }

    @Test
    fun `displayName is not empty for all tithis`() {
        for (tithi in Tithi.entries) {
            assertTrue("displayName for ${tithi.name} should not be empty",
                tithi.displayName.isNotEmpty())
        }
    }

    @Test
    fun `hindiName is not empty for all tithis`() {
        for (tithi in Tithi.entries) {
            assertTrue("hindiName for ${tithi.name} should not be empty",
                tithi.hindiName.isNotEmpty())
        }
    }

    @Test
    fun `continuousNumber matches number`() {
        for (tithi in Tithi.entries) {
            assertEquals("continuousNumber should match number for ${tithi.name}",
                tithi.number, tithi.continuousNumber)
        }
    }

    @Test
    fun `fromNumber returns correct tithi for valid numbers`() {
        assertEquals(Tithi.PRATIPADA, Tithi.fromNumber(1))
        assertEquals(Tithi.PANCHAMI, Tithi.fromNumber(5))
        assertEquals(Tithi.DASHAMI, Tithi.fromNumber(10))
        assertEquals(Tithi.EKADASHI, Tithi.fromNumber(11))
        assertEquals(Tithi.PURNIMA, Tithi.fromNumber(15))
        assertEquals(Tithi.AMAVASYA, Tithi.fromNumber(30))
    }

    @Test
    fun `fromNumber returns PRATIPADA for unknown number`() {
        val result = Tithi.fromNumber(99)
        assertEquals("fromNumber for unknown number should return PRATIPADA",
            Tithi.PRATIPADA, result)
    }

    @Test
    fun `there are 16 tithi entries`() {
        // 14 regular + Purnima + Amavasya = 16
        assertEquals(16, Tithi.entries.size)
    }

    @Test
    fun `Tithi numbers cover 1 through 15 and 30`() {
        val numbers = Tithi.entries.map { it.number }.toSet()
        for (n in 1..15) {
            assertTrue("Tithi number $n should exist", numbers.contains(n))
        }
        assertTrue("Tithi number 30 (Amavasya) should exist", numbers.contains(30))
    }
}
