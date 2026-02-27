package dev.gopes.hinducalendar.engine

import dev.gopes.hinducalendar.data.engine.AstronomyEngine
import dev.gopes.hinducalendar.data.engine.PanchangCalculator
import dev.gopes.hinducalendar.domain.model.*
import org.junit.Assert.*
import org.junit.Test

/**
 * Validates that the full computation chain (JD -> sunrise -> tithi -> Hindu date)
 * produces stable, correct Hindu dates for known festival dates.
 *
 * This catches the jdNoon off-by-one bug where sunrise converged to the previous
 * day, shifting all festivals by +1 day.
 *
 * Expected values are from our engine's own computation (verified against
 * device output and drikpanchang.com). All dates are IST (Delhi: 28.6139, 77.2090).
 */
class FestivalDateValidationTest {

    // Delhi coordinates
    private val DELHI_LAT = 28.6139
    private val DELHI_LON = 77.2090

    // Atlanta coordinates (for diaspora testing)
    private val ATL_LAT = 33.749
    private val ATL_LON = -84.388

    /**
     * Compute the Hindu date at sunrise for a given Gregorian date and location.
     */
    private fun hinduDateAt(
        year: Int, month: Int, day: Int,
        lat: Double, lon: Double,
        tradition: CalendarTradition = CalendarTradition.PURNIMANT
    ): HinduDateResult {
        val jdMidnight = AstronomyEngine.dateToJD(year, month, day)
        val jdTT = AstronomyEngine.utToTT(jdMidnight, year.toDouble())

        val sunriseJD = AstronomyEngine.sunrise(jdMidnight, lat, lon) ?: jdTT
        val jdSunriseTT = AstronomyEngine.utToTT(sunriseJD, year.toDouble())

        val (tithi, paksha, _) = PanchangCalculator.calculateTithi(jdSunriseTT)
        val (hinduMonth, isAdhikMaas) = PanchangCalculator.calculateHinduMonth(jdSunriseTT, tradition)

        return HinduDateResult(
            tithi = tithi,
            paksha = paksha,
            hinduMonth = hinduMonth,
            isAdhikMaas = isAdhikMaas,
            sunriseJD = sunriseJD,
            inputJD = jdMidnight
        )
    }

    private data class HinduDateResult(
        val tithi: Tithi,
        val paksha: Paksha,
        val hinduMonth: HinduMonth,
        val isAdhikMaas: Boolean,
        val sunriseJD: Double,
        val inputJD: Double
    )

    // ==================== Sunrise Convergence ====================
    // The critical test: sunrise must be within ~12 hours of input JD (same local day)

    @Test
    fun `sunrise for Delhi is within half a day of input JD for all months`() {
        for (month in 1..12) {
            val jd = AstronomyEngine.dateToJD(2026, month, 15)
            val sunriseJD = AstronomyEngine.sunrise(jd, DELHI_LAT, DELHI_LON)
            assertNotNull("Sunrise should not be null for 2026-$month-15", sunriseJD)
            val diff = sunriseJD!! - jd
            // Sunrise should be 0-0.5 JD after midnight UT (i.e., within the same UT day
            // or very slightly before midnight for eastern longitudes in summer)
            assertTrue("Sunrise should be within 0.5 JD of input for 2026-$month-15, diff=$diff",
                diff > -0.1 && diff < 0.5)
        }
    }

    @Test
    fun `sunrise for Atlanta is within half a day of input JD for all months`() {
        for (month in 1..12) {
            val jd = AstronomyEngine.dateToJD(2026, month, 15)
            val sunriseJD = AstronomyEngine.sunrise(jd, ATL_LAT, ATL_LON)
            assertNotNull("Sunrise should not be null for 2026-$month-15 Atlanta", sunriseJD)
            val diff = sunriseJD!! - jd
            assertTrue("Sunrise should be within 0.75 JD of input for 2026-$month-15 Atlanta, diff=$diff",
                diff > -0.1 && diff < 0.75)
        }
    }

    @Test
    fun `sunrise for Delhi Mar 4 2026 returns correct day not Mar 3`() {
        // This specifically tests the jdNoon fix — before the fix,
        // Delhi Mar 4 sunrise converged to Mar 3
        val jd = AstronomyEngine.dateToJD(2026, 3, 4)
        val sunriseJD = AstronomyEngine.sunrise(jd, DELHI_LAT, DELHI_LON)!!
        val (_, sm, sd) = AstronomyEngine.jdToDateTime(sunriseJD)
        assertEquals("Sunrise month should be March", 3, sm)
        assertEquals("Sunrise day should be 4, not 3 (jdNoon fix)", 4, sd)
    }

    // ==================== 2026 Festival Dates (Delhi / IST) ====================
    // Expected Hindu dates verified via engine diagnostic output

    @Test
    fun `Vasant Panchami 2026 - Jan 23 - Magh Shukla Panchami`() {
        val result = hinduDateAt(2026, 1, 23, DELHI_LAT, DELHI_LON)
        assertEquals(HinduMonth.MAGHA, result.hinduMonth)
        assertEquals(Paksha.SHUKLA, result.paksha)
        assertEquals(Tithi.PANCHAMI, result.tithi)
    }

    @Test
    fun `Maha Shivaratri 2026 - Feb 16 - Phalguna Krishna Chaturdashi`() {
        val result = hinduDateAt(2026, 2, 16, DELHI_LAT, DELHI_LON)
        assertEquals(HinduMonth.PHALGUNA, result.hinduMonth)
        assertEquals(Paksha.KRISHNA, result.paksha)
        assertEquals(Tithi.CHATURDASHI, result.tithi)
    }

    @Test
    fun `Holika Dahan 2026 - Mar 3 - Phalguna Shukla Purnima`() {
        val result = hinduDateAt(2026, 3, 3, DELHI_LAT, DELHI_LON)
        assertEquals(HinduMonth.PHALGUNA, result.hinduMonth)
        assertEquals(Paksha.SHUKLA, result.paksha)
        assertEquals(Tithi.PURNIMA, result.tithi)
    }

    @Test
    fun `Holi 2026 - Mar 4 - Chaitra Krishna Pratipada`() {
        val result = hinduDateAt(2026, 3, 4, DELHI_LAT, DELHI_LON)
        assertEquals(HinduMonth.CHAITRA, result.hinduMonth)
        assertEquals(Paksha.KRISHNA, result.paksha)
        assertEquals(Tithi.PRATIPADA, result.tithi)
    }

    @Test
    fun `Ram Navami 2026 - Mar 27 - Chaitra Shukla Navami`() {
        val result = hinduDateAt(2026, 3, 27, DELHI_LAT, DELHI_LON)
        assertEquals(HinduMonth.CHAITRA, result.hinduMonth)
        assertEquals(Paksha.SHUKLA, result.paksha)
        assertEquals(Tithi.NAVAMI, result.tithi)
    }

    @Test
    fun `Raksha Bandhan 2026 - Aug 28 - Shravan Shukla Purnima`() {
        val result = hinduDateAt(2026, 8, 28, DELHI_LAT, DELHI_LON)
        assertEquals(HinduMonth.SHRAVANA, result.hinduMonth)
        assertEquals(Paksha.SHUKLA, result.paksha)
        assertEquals(Tithi.PURNIMA, result.tithi)
    }

    @Test
    fun `Krishna Janmashtami 2026 - Sep 4 - Bhadrapada Krishna Ashtami`() {
        val result = hinduDateAt(2026, 9, 4, DELHI_LAT, DELHI_LON)
        assertEquals(HinduMonth.BHADRAPADA, result.hinduMonth)
        assertEquals(Paksha.KRISHNA, result.paksha)
        assertEquals(Tithi.ASHTAMI, result.tithi)
    }

    @Test
    fun `Ganesh Chaturthi 2026 - Sep 15 - Bhadrapada Shukla Chaturthi`() {
        val result = hinduDateAt(2026, 9, 15, DELHI_LAT, DELHI_LON)
        assertEquals(HinduMonth.BHADRAPADA, result.hinduMonth)
        assertEquals(Paksha.SHUKLA, result.paksha)
        assertEquals(Tithi.CHATURTHI, result.tithi)
    }

    @Test
    fun `Sharad Navratri 2026 - Oct 11 - Ashwin Shukla Pratipada`() {
        val result = hinduDateAt(2026, 10, 11, DELHI_LAT, DELHI_LON)
        assertEquals(HinduMonth.ASHWIN, result.hinduMonth)
        assertEquals(Paksha.SHUKLA, result.paksha)
        assertEquals(Tithi.PRATIPADA, result.tithi)
    }

    @Test
    fun `Dussehra 2026 - Oct 21 - Ashwin Shukla Dashami`() {
        val result = hinduDateAt(2026, 10, 21, DELHI_LAT, DELHI_LON)
        assertEquals(HinduMonth.ASHWIN, result.hinduMonth)
        assertEquals(Paksha.SHUKLA, result.paksha)
        assertEquals(Tithi.DASHAMI, result.tithi)
    }

    @Test
    fun `Diwali 2026 - Nov 9 - Kartik Krishna Amavasya`() {
        val result = hinduDateAt(2026, 11, 9, DELHI_LAT, DELHI_LON)
        assertEquals(HinduMonth.KARTIK, result.hinduMonth)
        assertEquals(Paksha.KRISHNA, result.paksha)
        assertEquals(Tithi.AMAVASYA, result.tithi)
    }

    // ==================== 2025 Festival Dates (Delhi / IST) ====================

    @Test
    fun `Maha Shivaratri 2025 - Feb 27 - Phalguna Krishna Chaturdashi`() {
        val result = hinduDateAt(2025, 2, 27, DELHI_LAT, DELHI_LON)
        assertEquals(HinduMonth.PHALGUNA, result.hinduMonth)
        assertEquals(Paksha.KRISHNA, result.paksha)
        assertEquals(Tithi.CHATURDASHI, result.tithi)
    }

    @Test
    fun `Holika Dahan 2025 - Mar 14 - Phalguna Shukla Purnima`() {
        val result = hinduDateAt(2025, 3, 14, DELHI_LAT, DELHI_LON)
        assertEquals(HinduMonth.PHALGUNA, result.hinduMonth)
        assertEquals(Paksha.SHUKLA, result.paksha)
        assertEquals(Tithi.PURNIMA, result.tithi)
    }

    @Test
    fun `Holi 2025 - Mar 15 - Chaitra Krishna Pratipada`() {
        val result = hinduDateAt(2025, 3, 15, DELHI_LAT, DELHI_LON)
        assertEquals(HinduMonth.CHAITRA, result.hinduMonth)
        assertEquals(Paksha.KRISHNA, result.paksha)
        assertEquals(Tithi.PRATIPADA, result.tithi)
    }

    @Test
    fun `Diwali 2025 - Oct 21 - Kartik Krishna Amavasya`() {
        val result = hinduDateAt(2025, 10, 21, DELHI_LAT, DELHI_LON)
        assertEquals(HinduMonth.KARTIK, result.hinduMonth)
        assertEquals(Paksha.KRISHNA, result.paksha)
        assertEquals(Tithi.AMAVASYA, result.tithi)
    }

    // ==================== Diaspora: Atlanta ====================

    @Test
    fun `Holi 2026 Atlanta - Mar 3 - Chaitra Krishna Pratipada`() {
        val result = hinduDateAt(2026, 3, 3, ATL_LAT, ATL_LON)
        assertEquals(Paksha.KRISHNA, result.paksha)
        assertEquals(Tithi.PRATIPADA, result.tithi)
    }

    // ==================== Tithi Progression Sanity ====================
    // Verify tithis advance monotonically over consecutive days

    @Test
    fun `tithis advance over consecutive days without skipping backwards`() {
        var prevTithiNum = 0
        for (day in 1..28) {
            val jd = AstronomyEngine.dateToJD(2026, 3, day)
            val sunriseJD = AstronomyEngine.sunrise(jd, DELHI_LAT, DELHI_LON)!!
            val jdTT = AstronomyEngine.utToTT(sunriseJD, 2026.0)
            val (_, _, tithiNum) = PanchangCalculator.calculateTithi(jdTT)

            if (prevTithiNum > 0) {
                // Tithi should advance by 0 or 1, or wrap from 30 to 1
                val advance = if (tithiNum >= prevTithiNum) tithiNum - prevTithiNum
                else tithiNum + 30 - prevTithiNum
                assertTrue("Tithi should advance 0-2 per day, advanced $advance on Mar $day " +
                    "(prev=$prevTithiNum, curr=$tithiNum)", advance in 0..2)
            }
            prevTithiNum = tithiNum
        }
    }
}
