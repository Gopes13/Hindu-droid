package dev.gopes.hinducalendar.engine

import dev.gopes.hinducalendar.data.model.CalendarTradition
import org.junit.Assert.*
import org.junit.Test

/**
 * Diagnostic: validates the evening tithi check approach for festival matching.
 */
class FestivalDateDiagnosticTest {

    private val DELHI_LAT = 28.6139
    private val DELHI_LON = 77.2090

    private data class DayTithis(
        val sunriseTithi: String,
        val sunriseTithiNum: Int,
        val eveningTithi: String,
        val eveningTithiNum: Int,
        val hoursToNextTithi: Double
    )

    private fun tithisForDay(year: Int, month: Int, day: Int): DayTithis {
        val jd = AstronomyEngine.dateToJD(year, month, day)
        val sunriseJD = AstronomyEngine.sunrise(jd, DELHI_LAT, DELHI_LON) ?: return DayTithis("?", 0, "?", 0, -1.0)
        val jdSunriseTT = AstronomyEngine.utToTT(sunriseJD, year.toDouble())

        // Sunrise tithi
        val (tithi, paksha, tithiNum) = PanchangCalculator.calculateTithi(jdSunriseTT)

        // Evening tithi (sunrise + 12h, same as PanchangService)
        val jdEvening = sunriseJD + 0.5
        val jdEveningTT = AstronomyEngine.utToTT(jdEvening, year.toDouble())
        val (eTithi, ePaksha, eTithiNum) = PanchangCalculator.calculateTithi(jdEveningTT)

        // Hours to next tithi
        val nextTithiNum = if (tithiNum >= 30) 1 else tithiNum + 1
        val nextTithiJD = PanchangCalculator.findTithiTransition(sunriseJD, nextTithiNum)
        val hours = (nextTithiJD - sunriseJD) * 24.0

        return DayTithis(
            "$paksha $tithi", tithiNum,
            "$ePaksha $eTithi", eTithiNum,
            hours
        )
    }

    @Test
    fun `evening tithi fixes Diwali 2026 to Nov 8`() {
        val nov8 = tithisForDay(2026, 11, 8)
        val nov9 = tithisForDay(2026, 11, 9)
        System.err.println("Diwali 2026 — Nov 8: sunrise=${nov8.sunriseTithi} evening=${nov8.eveningTithi} | Nov 9: sunrise=${nov9.sunriseTithi} evening=${nov9.eveningTithi}")
        // Nov 8 evening should be Amavasya (tithi 30) → Diwali matches
        assertEquals("Nov 8 evening should be Amavasya", 30, nov8.eveningTithiNum)
        // Nov 9 evening should NOT be Amavasya
        assertNotEquals("Nov 9 evening should not be Amavasya", 30, nov9.eveningTithiNum)
    }

    @Test
    fun `evening tithi fixes Diwali 2025 to Oct 20`() {
        val oct20 = tithisForDay(2025, 10, 20)
        val oct21 = tithisForDay(2025, 10, 21)
        System.err.println("Diwali 2025 — Oct 20: sunrise=${oct20.sunriseTithi} evening=${oct20.eveningTithi} | Oct 21: sunrise=${oct21.sunriseTithi} evening=${oct21.eveningTithi}")
        assertEquals("Oct 20 evening should be Amavasya", 30, oct20.eveningTithiNum)
        assertNotEquals("Oct 21 evening should not be Amavasya", 30, oct21.eveningTithiNum)
    }

    @Test
    fun `evening tithi fixes Maha Shivaratri 2026 to Feb 15`() {
        val feb15 = tithisForDay(2026, 2, 15)
        val feb16 = tithisForDay(2026, 2, 16)
        System.err.println("Shivaratri 2026 — Feb 15: sunrise=${feb15.sunriseTithi} evening=${feb15.eveningTithi} | Feb 16: sunrise=${feb16.sunriseTithi} evening=${feb16.eveningTithi}")
        // Feb 15 evening should be Chaturdashi (tithi 29 = Krishna Chaturdashi)
        assertEquals("Feb 15 evening should be Chaturdashi", 29, feb15.eveningTithiNum)
        assertNotEquals("Feb 16 evening should not be Chaturdashi", 29, feb16.eveningTithiNum)
    }

    @Test
    fun `evening tithi fixes Maha Shivaratri 2025 to Feb 26`() {
        val feb26 = tithisForDay(2025, 2, 26)
        val feb27 = tithisForDay(2025, 2, 27)
        System.err.println("Shivaratri 2025 — Feb 26: sunrise=${feb26.sunriseTithi} evening=${feb26.eveningTithi} | Feb 27: sunrise=${feb27.sunriseTithi} evening=${feb27.eveningTithi}")
        assertEquals("Feb 26 evening should be Chaturdashi", 29, feb26.eveningTithiNum)
        assertNotEquals("Feb 27 evening should not be Chaturdashi", 29, feb27.eveningTithiNum)
    }

    @Test
    fun `evening tithi fixes Dussehra 2026 to Oct 20`() {
        val oct20 = tithisForDay(2026, 10, 20)
        val oct21 = tithisForDay(2026, 10, 21)
        System.err.println("Dussehra 2026 — Oct 20: sunrise=${oct20.sunriseTithi} evening=${oct20.eveningTithi} | Oct 21: sunrise=${oct21.sunriseTithi} evening=${oct21.eveningTithi}")
        // Oct 20 evening should be Dashami (tithi 10)
        assertEquals("Oct 20 evening should be Dashami", 10, oct20.eveningTithiNum)
        assertNotEquals("Oct 21 evening should not be Dashami", 10, oct21.eveningTithiNum)
    }

    @Test
    fun `evening tithi fixes Ganesh Chaturthi 2026 to Sep 14`() {
        val sep14 = tithisForDay(2026, 9, 14)
        val sep15 = tithisForDay(2026, 9, 15)
        System.err.println("Ganesh Chaturthi 2026 — Sep 14: sunrise=${sep14.sunriseTithi} evening=${sep14.eveningTithi} | Sep 15: sunrise=${sep15.sunriseTithi} evening=${sep15.eveningTithi}")
        // Sep 14 evening should be Chaturthi (tithi 4)
        assertEquals("Sep 14 evening should be Chaturthi", 4, sep14.eveningTithiNum)
        assertNotEquals("Sep 15 evening should not be Chaturthi", 4, sep15.eveningTithiNum)
    }

    // ==================== Regression checks ====================
    // These use sunrise tithi (not evening), so should be unaffected

    @Test
    fun `sunrise tithi still correct for Holi 2026 Mar 4`() {
        val mar4 = tithisForDay(2026, 3, 4)
        assertEquals("Mar 4 sunrise should be Pratipada (tithi 16)", 16, mar4.sunriseTithiNum)
    }

    @Test
    fun `sunrise tithi still correct for Ram Navami 2026 Mar 27`() {
        val mar27 = tithisForDay(2026, 3, 27)
        assertEquals("Mar 27 sunrise should be Navami (tithi 9)", 9, mar27.sunriseTithiNum)
    }

    @Test
    fun `sunrise tithi still correct for Raksha Bandhan 2026 Aug 28`() {
        val aug28 = tithisForDay(2026, 8, 28)
        assertEquals("Aug 28 sunrise should be Purnima (tithi 15)", 15, aug28.sunriseTithiNum)
    }

    @Test
    fun `no false positive for Diwali on Nov 7 2026`() {
        val nov7 = tithisForDay(2026, 11, 7)
        assertNotEquals("Nov 7 evening should not be Amavasya", 30, nov7.eveningTithiNum)
    }
}
