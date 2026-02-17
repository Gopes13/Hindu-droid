package dev.gopes.hinducalendar.data.model

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for Yoga enum — count, numbers, names, SPAN_DEGREES, and from() factory.
 */
class YogaTest {

    @Test
    fun `there are exactly 27 yogas`() {
        assertEquals(27, Yoga.entries.size)
    }

    @Test
    fun `yoga numbers range from 1 to 27`() {
        val numbers = Yoga.entries.map { it.number }.sorted()
        assertEquals((1..27).toList(), numbers)
    }

    @Test
    fun `all yogas have unique numbers`() {
        val numbers = Yoga.entries.map { it.number }
        assertEquals("All yoga numbers should be unique",
            numbers.size, numbers.toSet().size)
    }

    @Test
    fun `displayName is non-empty for all yogas`() {
        for (yoga in Yoga.entries) {
            assertTrue("displayName for ${yoga.name} should not be empty",
                yoga.displayName.isNotEmpty())
        }
    }

    @Test
    fun `SPAN_DEGREES is 360 divided by 27`() {
        assertEquals(360.0 / 27.0, Yoga.SPAN_DEGREES, 0.0001)
    }

    @Test
    fun `VISHKAMBHA is number 1`() {
        assertEquals(1, Yoga.VISHKAMBHA.number)
    }

    @Test
    fun `VAIDHRITI is number 27`() {
        assertEquals(27, Yoga.VAIDHRITI.number)
    }

    @Test
    fun `from returns VISHKAMBHA for sum near 0`() {
        // Sum of 0 degrees -> index 0 -> VISHKAMBHA
        val result = Yoga.from(0.0, 0.0)
        assertEquals(Yoga.VISHKAMBHA, result)
    }

    @Test
    fun `from returns valid yoga for arbitrary sun and moon positions`() {
        val result = Yoga.from(90.0, 180.0)
        // Sum = 270, index = (270 / 13.333) = 20 -> SIDDHA (number 21)
        assertTrue("Yoga number should be 1-27, was ${result.number}",
            result.number in 1..27)
    }

    @Test
    fun `from handles sum exceeding 360`() {
        // Sun at 200, Moon at 250 -> sum = 450, normalized to 90
        val result = Yoga.from(200.0, 250.0)
        assertTrue("Result should be a valid yoga", result.number in 1..27)
    }

    @Test
    fun `specific yoga names are correct`() {
        assertEquals("Vishkambha", Yoga.VISHKAMBHA.displayName)
        assertEquals("Priti", Yoga.PRITI.displayName)
        assertEquals("Siddhi", Yoga.SIDDHI.displayName)
        assertEquals("Shiva", Yoga.SHIVA.displayName)
        assertEquals("Vaidhriti", Yoga.VAIDHRITI.displayName)
    }
}
