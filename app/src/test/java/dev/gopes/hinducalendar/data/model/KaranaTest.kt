package dev.gopes.hinducalendar.data.model

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for Karana enum — from() factory, auspiciousness, and special karanas.
 */
class KaranaTest {

    @Test
    fun `there are exactly 11 karana entries`() {
        // 4 fixed + 7 recurring = 11
        assertEquals(11, Karana.entries.size)
    }

    @Test
    fun `all karanas have non-empty displayName`() {
        for (karana in Karana.entries) {
            assertTrue("displayName for ${karana.name} should not be empty",
                karana.displayName.isNotEmpty())
        }
    }

    @Test
    fun `KIMSTUGHNA is the first karana of the lunar month`() {
        // Tithi 1, first half -> karana index 0 -> KIMSTUGHNA
        val result = Karana.from(1, true)
        assertEquals(Karana.KIMSTUGHNA, result)
    }

    @Test
    fun `KIMSTUGHNA is auspicious`() {
        assertTrue(Karana.KIMSTUGHNA.isAuspicious)
    }

    @Test
    fun `VISHTI Bhadra is inauspicious`() {
        assertFalse(Karana.VISHTI.isAuspicious)
    }

    @Test
    fun `SHAKUNI is the 58th karana`() {
        // Tithi 30, first half -> karana index 57 -> SHAKUNI
        val result = Karana.from(30, false)
        // karanaIndex = (30-1)*2 + 1 = 59 -> NAGAVA
        // Actually let's compute: karanaIndex for tithi 29, second half = (29-1)*2 + 1 = 57 -> SHAKUNI
        val shakuni = Karana.from(29, false)
        assertEquals(Karana.SHAKUNI, shakuni)
    }

    @Test
    fun `CHATUSHPADA is karana index 58`() {
        // karanaIndex 58 = tithi 30, first half: (30-1)*2 + 0 = 58
        val result = Karana.from(30, true)
        assertEquals(Karana.CHATUSHPADA, result)
    }

    @Test
    fun `NAGAVA is karana index 59`() {
        // karanaIndex 59 = tithi 30, second half: (30-1)*2 + 1 = 59
        val result = Karana.from(30, false)
        assertEquals(Karana.NAGAVA, result)
    }

    @Test
    fun `recurring karanas cycle through 7 values`() {
        // Karanas for tithis 1-29 (excluding index 0 which is KIMSTUGHNA) cycle through
        // BAVA, BALAVA, KAULAVA, TAITILA, GARAJA, VANIJA, VISHTI
        val result = Karana.from(1, false) // index 1 -> RECURRING[(1-1)%7] = BAVA
        assertEquals(Karana.BAVA, result)
    }

    @Test
    fun `karana for tithi 2 first half`() {
        // karanaIndex = (2-1)*2 + 0 = 2 -> RECURRING[(2-1)%7] = RECURRING[1] = BALAVA
        val result = Karana.from(2, true)
        assertEquals(Karana.BALAVA, result)
    }

    @Test
    fun `BAVA BALAVA KAULAVA TAITILA GARAJA VANIJA are auspicious`() {
        assertTrue(Karana.BAVA.isAuspicious)
        assertTrue(Karana.BALAVA.isAuspicious)
        assertTrue(Karana.KAULAVA.isAuspicious)
        assertTrue(Karana.TAITILA.isAuspicious)
        assertTrue(Karana.GARAJA.isAuspicious)
        assertTrue(Karana.VANIJA.isAuspicious)
    }

    @Test
    fun `SHAKUNI CHATUSHPADA NAGAVA are inauspicious`() {
        assertFalse(Karana.SHAKUNI.isAuspicious)
        assertFalse(Karana.CHATUSHPADA.isAuspicious)
        assertFalse(Karana.NAGAVA.isAuspicious)
    }

    @Test
    fun `from returns valid karana for all tithi numbers`() {
        for (tithiNum in 1..30) {
            for (isFirst in listOf(true, false)) {
                val result = Karana.from(tithiNum, isFirst)
                assertNotNull("Karana should not be null for tithi $tithiNum, firstHalf=$isFirst", result)
                assertTrue("Karana displayName should not be empty", result.displayName.isNotEmpty())
            }
        }
    }
}
