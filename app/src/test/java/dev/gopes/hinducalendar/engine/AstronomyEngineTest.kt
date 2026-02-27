package dev.gopes.hinducalendar.engine

import dev.gopes.hinducalendar.data.engine.AstronomyEngine
import org.junit.Assert.*
import org.junit.Test
import kotlin.math.abs

/**
 * Unit tests for AstronomyEngine — Julian Day conversions, solar/lunar positions,
 * sunrise/sunset, ayanamsa, and related astronomical calculations.
 */
class AstronomyEngineTest {

    private val DELTA_JD = 0.0001  // ~8.6 seconds tolerance for JD comparisons
    private val DELTA_DEG = 0.01   // degree tolerance for angular comparisons

    // ==================== dateToJD ====================

    @Test
    fun `dateToJD for J2000 epoch returns 2451545`() {
        // J2000.0 epoch = January 1.5, 2000 (noon UT) = JD 2451545.0
        val jd = AstronomyEngine.dateToJD(2000, 1, 1, 12.0)
        assertEquals(2451545.0, jd, DELTA_JD)
    }

    @Test
    fun `dateToJD for January 1 2000 midnight returns 2451544_5`() {
        val jd = AstronomyEngine.dateToJD(2000, 1, 1, 0.0)
        assertEquals(2451544.5, jd, DELTA_JD)
    }

    @Test
    fun `dateToJD for Unix epoch Jan 1 1970 returns correct JD`() {
        // Unix epoch: Jan 1, 1970 00:00 UT = JD 2440587.5
        val jd = AstronomyEngine.dateToJD(1970, 1, 1, 0.0)
        assertEquals(2440587.5, jd, DELTA_JD)
    }

    @Test
    fun `dateToJD for known historical date`() {
        // July 4, 1776 = JD 2369916.5 (at midnight)
        val jd = AstronomyEngine.dateToJD(1776, 7, 4, 0.0)
        assertEquals(2369916.5, jd, DELTA_JD)
    }

    @Test
    fun `dateToJD default hour is zero`() {
        val jdWithHour = AstronomyEngine.dateToJD(2025, 3, 15, 0.0)
        val jdDefault = AstronomyEngine.dateToJD(2025, 3, 15)
        assertEquals(jdWithHour, jdDefault, 0.0)
    }

    @Test
    fun `dateToJD for leap year Feb 29`() {
        // Feb 29, 2024 should produce a valid JD (2024 is a leap year)
        val jd = AstronomyEngine.dateToJD(2024, 2, 29)
        assertTrue("JD for Feb 29 2024 should be reasonable", jd > 2460000.0)
    }

    // ==================== jdToDateTime ====================

    @Test
    fun `jdToDateTime for J2000 epoch returns Jan 1 2000`() {
        val (year, month, day) = AstronomyEngine.jdToDateTime(2451545.0)
        assertEquals(2000, year)
        assertEquals(1, month)
        assertEquals(1, day)
    }

    @Test
    fun `jdToDateTime for Unix epoch returns Jan 1 1970`() {
        val (year, month, day) = AstronomyEngine.jdToDateTime(2440587.5)
        assertEquals(1970, year)
        assertEquals(1, month)
        assertEquals(1, day)
    }

    @Test
    fun `dateToJD and jdToDateTime round trip`() {
        val testCases = listOf(
            Triple(2025, 1, 1),
            Triple(2025, 6, 15),
            Triple(2024, 2, 29),
            Triple(1999, 12, 31),
            Triple(2000, 3, 20)
        )
        for ((y, m, d) in testCases) {
            val jd = AstronomyEngine.dateToJD(y, m, d)
            val (ry, rm, rd) = AstronomyEngine.jdToDateTime(jd)
            assertEquals("Year mismatch for $y-$m-$d", y, ry)
            assertEquals("Month mismatch for $y-$m-$d", m, rm)
            assertEquals("Day mismatch for $y-$m-$d", d, rd)
        }
    }

    @Test
    fun `dateToJD and jdToDateTime round trip for multiple years`() {
        for (year in 1900..2100 step 25) {
            val jd = AstronomyEngine.dateToJD(year, 7, 1)
            val (ry, rm, rd) = AstronomyEngine.jdToDateTime(jd)
            assertEquals(year, ry)
            assertEquals(7, rm)
            assertEquals(1, rd)
        }
    }

    // ==================== normalize ====================

    @Test
    fun `normalize keeps value in 0-360 range`() {
        assertEquals(0.0, AstronomyEngine.normalize(0.0), DELTA_DEG)
        assertEquals(180.0, AstronomyEngine.normalize(180.0), DELTA_DEG)
        assertEquals(0.0, AstronomyEngine.normalize(360.0), DELTA_DEG)
        assertEquals(90.0, AstronomyEngine.normalize(450.0), DELTA_DEG)
    }

    @Test
    fun `normalize handles negative degrees`() {
        assertEquals(270.0, AstronomyEngine.normalize(-90.0), DELTA_DEG)
        assertEquals(180.0, AstronomyEngine.normalize(-180.0), DELTA_DEG)
        assertEquals(1.0, AstronomyEngine.normalize(-359.0), DELTA_DEG)
    }

    // ==================== sunTropicalLongitude ====================

    @Test
    fun `sunTropicalLongitude returns value in 0-360 range`() {
        val jd = AstronomyEngine.dateToJD(2025, 6, 21, 12.0)
        val jdTT = AstronomyEngine.utToTT(jd, 2025.0)
        val longitude = AstronomyEngine.sunTropicalLongitude(jdTT)
        assertTrue("Solar longitude should be >= 0, was $longitude", longitude >= 0.0)
        assertTrue("Solar longitude should be < 360, was $longitude", longitude < 360.0)
    }

    @Test
    fun `sunTropicalLongitude near vernal equinox is about 0 degrees`() {
        // March equinox 2025 is around March 20. Sun longitude ~ 0 degrees
        val jd = AstronomyEngine.dateToJD(2025, 3, 20, 12.0)
        val jdTT = AstronomyEngine.utToTT(jd, 2025.0)
        val longitude = AstronomyEngine.sunTropicalLongitude(jdTT)
        // Should be close to 0 (or 360) within a few degrees
        val distFrom0 = if (longitude > 180) 360.0 - longitude else longitude
        assertTrue("Sun near equinox should be close to 0 degrees, was $longitude", distFrom0 < 3.0)
    }

    @Test
    fun `sunTropicalLongitude near summer solstice is about 90 degrees`() {
        // June solstice 2025 ~ June 21. Sun longitude ~ 90 degrees
        val jd = AstronomyEngine.dateToJD(2025, 6, 21, 12.0)
        val jdTT = AstronomyEngine.utToTT(jd, 2025.0)
        val longitude = AstronomyEngine.sunTropicalLongitude(jdTT)
        assertEquals("Sun near summer solstice should be ~90 degrees", 90.0, longitude, 3.0)
    }

    @Test
    fun `sunTropicalLongitude changes over time`() {
        val jd1 = AstronomyEngine.dateToJD(2025, 1, 1, 12.0)
        val jd2 = AstronomyEngine.dateToJD(2025, 7, 1, 12.0)
        val jdTT1 = AstronomyEngine.utToTT(jd1, 2025.0)
        val jdTT2 = AstronomyEngine.utToTT(jd2, 2025.0)
        val lon1 = AstronomyEngine.sunTropicalLongitude(jdTT1)
        val lon2 = AstronomyEngine.sunTropicalLongitude(jdTT2)
        assertNotEquals("Sun longitude should differ by ~180 degrees over 6 months", lon1, lon2, 10.0)
    }

    // ==================== deltaT / utToTT ====================

    @Test
    fun `deltaT is positive for modern era`() {
        val dt = AstronomyEngine.deltaT(2025.0)
        assertTrue("Delta T should be positive for 2025, was $dt", dt > 0.0)
    }

    @Test
    fun `deltaT for year 2000 is approximately 63-64 seconds`() {
        val dt = AstronomyEngine.deltaT(2000.0)
        // Known value: ~63.8 seconds in 2000
        assertTrue("Delta T for 2000 should be around 62-65, was $dt", dt in 60.0..68.0)
    }

    @Test
    fun `utToTT adds positive correction`() {
        val jdUT = AstronomyEngine.dateToJD(2025, 1, 1, 12.0)
        val jdTT = AstronomyEngine.utToTT(jdUT, 2025.0)
        assertTrue("TT should be slightly ahead of UT", jdTT > jdUT)
    }

    // ==================== sunrise / sunset ====================

    @Test
    fun `sunrise for Delhi returns non-null`() {
        val jd = AstronomyEngine.dateToJD(2025, 3, 21)
        val sunrise = AstronomyEngine.sunrise(jd, 28.6139, 77.2090)
        assertNotNull("Sunrise should not be null for Delhi", sunrise)
    }

    @Test
    fun `sunset for Delhi returns non-null`() {
        val jd = AstronomyEngine.dateToJD(2025, 3, 21)
        val sunset = AstronomyEngine.sunset(jd, 28.6139, 77.2090)
        assertNotNull("Sunset should not be null for Delhi", sunset)
    }

    @Test
    fun `sunrise occurs before sunset for Delhi`() {
        val jd = AstronomyEngine.dateToJD(2025, 6, 15)
        val sunrise = AstronomyEngine.sunrise(jd, 28.6139, 77.2090)!!
        val sunset = AstronomyEngine.sunset(jd, 28.6139, 77.2090)!!
        assertTrue("Sunrise ($sunrise) should be before sunset ($sunset)", sunrise < sunset)
    }

    @Test
    fun `sunrise and sunset are within same day`() {
        val jd = AstronomyEngine.dateToJD(2025, 1, 15)
        val sunrise = AstronomyEngine.sunrise(jd, 28.6139, 77.2090)!!
        val sunset = AstronomyEngine.sunset(jd, 28.6139, 77.2090)!!
        val dayLength = sunset - sunrise
        // Day length should be between 8 and 16 hours (0.33 - 0.67 JD)
        assertTrue("Day length should be reasonable (0.33-0.67 JD), was $dayLength",
            dayLength in 0.33..0.67)
    }

    // ==================== moonrise / moonset ====================

    @Test
    fun `moonrise for Delhi returns non-null on normal date`() {
        val jd = AstronomyEngine.dateToJD(2025, 6, 15)
        val moonrise = AstronomyEngine.moonrise(jd, 28.6139, 77.2090)
        assertNotNull("Moonrise should not be null for a normal Shukla Paksha date", moonrise)
    }

    @Test
    fun `moonset for Delhi returns non-null on normal date`() {
        val jd = AstronomyEngine.dateToJD(2025, 6, 15)
        val moonset = AstronomyEngine.moonset(jd, 28.6139, 77.2090)
        assertNotNull("Moonset should not be null for a normal Shukla Paksha date", moonset)
    }

    @Test
    fun `moonrise for Delhi during Krishna Paksha Ekadashi`() {
        val jd = AstronomyEngine.dateToJD(2026, 2, 26)
        val moonrise = AstronomyEngine.moonrise(jd, 28.6139, 77.2090)
        assertNotNull("Moonrise should not be null during Krishna Paksha", moonrise)
    }

    @Test
    fun `moonset for Delhi during Krishna Paksha Ekadashi`() {
        val jd = AstronomyEngine.dateToJD(2026, 2, 26)
        val moonset = AstronomyEngine.moonset(jd, 28.6139, 77.2090)
        assertNotNull("Moonset should not be null during Krishna Paksha", moonset)
    }

    @Test
    fun `moonrise exists for most days in a month`() {
        var count = 0
        for (day in 1..28) {
            val jd = AstronomyEngine.dateToJD(2026, 2, day)
            if (AstronomyEngine.moonrise(jd, 28.6139, 77.2090) != null) count++
        }
        assertTrue("At least 23 of 28 days should have moonrise, got $count", count >= 23)
    }

    // ==================== tropicalToSidereal / lahiriAyanamsa ====================

    @Test
    fun `lahiriAyanamsa is approximately 24 degrees for 2025`() {
        val jd = AstronomyEngine.dateToJD(2025, 1, 1, 12.0)
        val jdTT = AstronomyEngine.utToTT(jd, 2025.0)
        val ayanamsa = AstronomyEngine.lahiriAyanamsa(jdTT)
        // Lahiri ayanamsa for 2025 is approximately 24.2 degrees
        assertTrue("Ayanamsa should be around 24 degrees for 2025, was $ayanamsa",
            ayanamsa in 23.0..26.0)
    }

    @Test
    fun `tropicalToSidereal subtracts ayanamsa`() {
        val jd = AstronomyEngine.dateToJD(2025, 1, 1, 12.0)
        val jdTT = AstronomyEngine.utToTT(jd, 2025.0)
        val tropical = 90.0
        val sidereal = AstronomyEngine.tropicalToSidereal(tropical, jdTT)
        val ayanamsa = AstronomyEngine.lahiriAyanamsa(jdTT)
        val expected = AstronomyEngine.normalize(tropical - ayanamsa)
        assertEquals("Sidereal should be tropical minus ayanamsa", expected, sidereal, DELTA_DEG)
    }

    @Test
    fun `tropicalToSidereal handles wraparound near 0 degrees`() {
        val jd = AstronomyEngine.dateToJD(2025, 1, 1, 12.0)
        val jdTT = AstronomyEngine.utToTT(jd, 2025.0)
        // Tropical longitude of 10 degrees minus ~24 degrees ayanamsa should wrap to ~346
        val sidereal = AstronomyEngine.tropicalToSidereal(10.0, jdTT)
        assertTrue("Sidereal should be in 0-360 range, was $sidereal",
            sidereal in 0.0..360.0)
    }

    // ==================== moonTropicalLongitude ====================

    @Test
    fun `moonTropicalLongitude returns value in 0-360 range`() {
        val jd = AstronomyEngine.dateToJD(2025, 1, 15, 12.0)
        val jdTT = AstronomyEngine.utToTT(jd, 2025.0)
        val moonLong = AstronomyEngine.moonTropicalLongitude(jdTT)
        assertTrue("Moon longitude should be in 0-360, was $moonLong",
            moonLong >= 0.0 && moonLong < 360.0)
    }

    @Test
    fun `moonTropicalLongitude changes significantly in one day`() {
        val jd1 = AstronomyEngine.dateToJD(2025, 1, 15, 0.0)
        val jd2 = AstronomyEngine.dateToJD(2025, 1, 16, 0.0)
        val jdTT1 = AstronomyEngine.utToTT(jd1, 2025.0)
        val jdTT2 = AstronomyEngine.utToTT(jd2, 2025.0)
        val moon1 = AstronomyEngine.moonTropicalLongitude(jdTT1)
        val moon2 = AstronomyEngine.moonTropicalLongitude(jdTT2)
        // Moon moves ~13 degrees per day
        var diff = abs(moon2 - moon1)
        if (diff > 180.0) diff = 360.0 - diff
        assertTrue("Moon should move ~10-15 degrees per day, moved $diff", diff in 8.0..18.0)
    }

    // ==================== sunEquatorial ====================

    @Test
    fun `sunEquatorial returns RA in 0-360 and Dec in valid range`() {
        val jd = AstronomyEngine.dateToJD(2025, 6, 21, 12.0)
        val jdTT = AstronomyEngine.utToTT(jd, 2025.0)
        val (ra, dec) = AstronomyEngine.sunEquatorial(jdTT)
        assertTrue("RA should be in 0-360, was $ra", ra in 0.0..360.0)
        assertTrue("Dec should be in -23.5 to 23.5, was $dec", abs(dec) <= 24.0)
    }
}
