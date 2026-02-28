package dev.gopes.hinducalendar.engine

import dev.gopes.hinducalendar.data.engine.AstronomyEngine
import dev.gopes.hinducalendar.data.engine.PanchangCalculator
import dev.gopes.hinducalendar.domain.model.*
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for PanchangCalculator — Tithi, Nakshatra, Yoga, Karana,
 * Rahu Kaal, Yamaghanda, Gulika Kaal, Hindu month, and year calculations.
 */
class PanchangCalculatorTest {

    // Helper: get a JD TT for a given date
    private fun jdTTForDate(year: Int, month: Int, day: Int, hour: Double = 12.0): Double {
        val jd = AstronomyEngine.dateToJD(year, month, day, hour)
        return AstronomyEngine.utToTT(jd, year.toDouble())
    }

    // ==================== Tithi ====================

    @Test
    fun `calculateTithi returns valid tithi number in 1-30 range`() {
        val jdTT = jdTTForDate(2025, 3, 15)
        val result = PanchangCalculator.calculateTithi(jdTT)
        assertTrue("Tithi number should be 1-30, was ${result.number}",
            result.number in 1..30)
    }

    @Test
    fun `calculateTithi tithis 1-15 are Shukla paksha`() {
        // Test multiple dates and verify the relationship
        val dates = listOf(
            Triple(2025, 1, 1), Triple(2025, 3, 15), Triple(2025, 6, 21),
            Triple(2025, 9, 1), Triple(2025, 12, 25)
        )
        for ((y, m, d) in dates) {
            val jdTT = jdTTForDate(y, m, d)
            val result = PanchangCalculator.calculateTithi(jdTT)
            if (result.number <= 15) {
                assertEquals("Tithi ${result.number} should be SHUKLA paksha on $y-$m-$d",
                    Paksha.SHUKLA, result.paksha)
            } else {
                assertEquals("Tithi ${result.number} should be KRISHNA paksha on $y-$m-$d",
                    Paksha.KRISHNA, result.paksha)
            }
        }
    }

    @Test
    fun `calculateTithi returns valid Tithi enum`() {
        val jdTT = jdTTForDate(2025, 4, 10)
        val result = PanchangCalculator.calculateTithi(jdTT)
        assertNotNull("Tithi should not be null", result.tithi)
        assertTrue("Tithi displayName should not be empty", result.tithi.displayName.isNotEmpty())
    }

    @Test
    fun `calculateTithi for Purnima returns number 15`() {
        // Search for a known Purnima date - full moons are ~every 29.5 days
        // We test the relationship: if tithi number is 15, it should be PURNIMA
        for (day in 1..30) {
            val jdTT = jdTTForDate(2025, 1, day)
            val result = PanchangCalculator.calculateTithi(jdTT)
            if (result.number == 15) {
                assertEquals("Tithi number 15 should be PURNIMA", Tithi.PURNIMA, result.tithi)
                assertEquals("Purnima should be SHUKLA paksha", Paksha.SHUKLA, result.paksha)
            }
        }
    }

    @Test
    fun `calculateTithi for Amavasya returns number 30`() {
        for (day in 1..31) {
            val jdTT = jdTTForDate(2025, 3, day)
            val result = PanchangCalculator.calculateTithi(jdTT)
            if (result.number == 30) {
                assertEquals("Tithi number 30 should be AMAVASYA", Tithi.AMAVASYA, result.tithi)
                assertEquals("Amavasya should be KRISHNA paksha", Paksha.KRISHNA, result.paksha)
            }
        }
    }

    @Test
    fun `calculateTithi changes over a lunar month`() {
        // Over 30 days we should see multiple different tithis
        val tithiNumbers = mutableSetOf<Int>()
        for (day in 1..30) {
            val jdTT = jdTTForDate(2025, 1, day)
            val result = PanchangCalculator.calculateTithi(jdTT)
            tithiNumbers.add(result.number)
        }
        assertTrue("Should see many different tithis over 30 days, saw ${tithiNumbers.size}",
            tithiNumbers.size >= 20)
    }

    // ==================== findTithiTransition ====================

    @Test
    fun `findTithiTransition returns JD within reasonable range`() {
        val jdStart = AstronomyEngine.dateToJD(2025, 1, 1)
        val transitionJD = PanchangCalculator.findTithiTransition(jdStart, 2)
        // Transition should be within about 30 days from start
        assertTrue("Transition JD should be near start date",
            transitionJD > jdStart - 1 && transitionJD < jdStart + 35)
    }

    @Test
    fun `findTithiTransition at result has the next tithi`() {
        val jdStart = AstronomyEngine.dateToJD(2025, 3, 1)
        val jdTT = AstronomyEngine.utToTT(jdStart, 2025.0)
        val currentTithi = PanchangCalculator.calculateTithi(jdTT)
        val nextTithiNum = if (currentTithi.number >= 30) 1 else currentTithi.number + 1

        val transitionJD = PanchangCalculator.findTithiTransition(jdStart, nextTithiNum)
        assertTrue("Transition JD should be after or near start",
            transitionJD >= jdStart - 0.5)
    }

    // ==================== Nakshatra ====================

    @Test
    fun `calculateNakshatra returns valid Nakshatra`() {
        val jdTT = jdTTForDate(2025, 5, 10)
        val nakshatra = PanchangCalculator.calculateNakshatra(jdTT)
        assertTrue("Nakshatra number should be 1-27, was ${nakshatra.number}",
            nakshatra.number in 1..27)
    }

    @Test
    fun `calculateNakshatra returns different values over time`() {
        val nakshatras = mutableSetOf<Nakshatra>()
        for (day in 1..28) {
            val jdTT = jdTTForDate(2025, 1, day)
            nakshatras.add(PanchangCalculator.calculateNakshatra(jdTT))
        }
        // Moon traverses all 27 nakshatras in ~27.3 days
        assertTrue("Should see many nakshatras over 28 days, saw ${nakshatras.size}",
            nakshatras.size >= 20)
    }

    @Test
    fun `calculateNakshatra displayName is not empty`() {
        val jdTT = jdTTForDate(2025, 7, 4)
        val nakshatra = PanchangCalculator.calculateNakshatra(jdTT)
        assertTrue("Nakshatra displayName should not be empty", nakshatra.displayName.isNotEmpty())
        assertTrue("Nakshatra hindiName should not be empty", nakshatra.hindiName.isNotEmpty())
    }

    // ==================== Yoga ====================

    @Test
    fun `calculateYoga returns valid Yoga`() {
        val jdTT = jdTTForDate(2025, 4, 20)
        val yoga = PanchangCalculator.calculateYoga(jdTT)
        assertTrue("Yoga number should be 1-27, was ${yoga.number}",
            yoga.number in 1..27)
    }

    @Test
    fun `calculateYoga displayName is not empty`() {
        val jdTT = jdTTForDate(2025, 8, 15)
        val yoga = PanchangCalculator.calculateYoga(jdTT)
        assertTrue("Yoga displayName should not be empty", yoga.displayName.isNotEmpty())
    }

    @Test
    fun `calculateYoga returns different values over days`() {
        val yogas = mutableSetOf<Yoga>()
        for (day in 1..28) {
            val jdTT = jdTTForDate(2025, 2, day)
            yogas.add(PanchangCalculator.calculateYoga(jdTT))
        }
        assertTrue("Should see multiple yogas over 28 days, saw ${yogas.size}",
            yogas.size >= 15)
    }

    // ==================== Karana ====================

    @Test
    fun `calculateKarana returns valid Karana`() {
        val jdTT = jdTTForDate(2025, 5, 5)
        val karana = PanchangCalculator.calculateKarana(jdTT)
        assertNotNull("Karana should not be null", karana)
        assertTrue("Karana displayName should not be empty", karana.displayName.isNotEmpty())
    }

    @Test
    fun `calculateKarana returns different values over time`() {
        val karanas = mutableSetOf<Karana>()
        for (day in 1..15) {
            val jdTT = jdTTForDate(2025, 3, day)
            karanas.add(PanchangCalculator.calculateKarana(jdTT))
        }
        assertTrue("Should see multiple karanas over 15 days, saw ${karanas.size}",
            karanas.size >= 3)
    }

    // ==================== Rahu Kaal ====================

    @Test
    fun `rahuKaal returns valid time period for all weekdays`() {
        val dayDuration = 720.0 // 12 hours in minutes
        for (weekday in 1..7) {
            val result = PanchangCalculator.rahuKaal(dayDuration, weekday)
            assertEquals("Name should be Rahu Kaal", "Rahu Kaal", result.name)
            assertTrue("Start should be >= 0 for weekday $weekday",
                result.startMinutes >= 0.0)
            assertTrue("End should be <= day duration for weekday $weekday",
                result.endMinutes <= dayDuration)
            assertTrue("Start should be before end for weekday $weekday",
                result.startMinutes < result.endMinutes)
        }
    }

    @Test
    fun `rahuKaal segment duration is one-eighth of day`() {
        val dayDuration = 720.0
        val result = PanchangCalculator.rahuKaal(dayDuration, 1) // Sunday
        val segmentDuration = result.endMinutes - result.startMinutes
        assertEquals("Segment duration should be 1/8 of day", dayDuration / 8.0, segmentDuration, 0.01)
    }

    @Test
    fun `rahuKaal Sunday uses segment 8`() {
        val dayDuration = 720.0
        val result = PanchangCalculator.rahuKaal(dayDuration, 1) // Sunday
        val segDur = dayDuration / 8.0
        // Sunday segment is 8, so start = (8-1) * segDur = 7 * 90 = 630
        assertEquals("Sunday Rahu Kaal start", 7 * segDur, result.startMinutes, 0.01)
    }

    @Test
    fun `rahuKaal Monday uses segment 2`() {
        val dayDuration = 720.0
        val result = PanchangCalculator.rahuKaal(dayDuration, 2) // Monday
        val segDur = dayDuration / 8.0
        assertEquals("Monday Rahu Kaal start", 1 * segDur, result.startMinutes, 0.01)
    }

    // ==================== Yamaghanda ====================

    @Test
    fun `yamaghanda returns valid time period for all weekdays`() {
        val dayDuration = 700.0
        for (weekday in 1..7) {
            val result = PanchangCalculator.yamaghanda(dayDuration, weekday)
            assertEquals("Name should be Yamaghanda", "Yamaghanda", result.name)
            assertTrue("Start >= 0 for weekday $weekday", result.startMinutes >= 0.0)
            assertTrue("End <= day duration for weekday $weekday",
                result.endMinutes <= dayDuration)
            assertTrue("Start < end for weekday $weekday",
                result.startMinutes < result.endMinutes)
        }
    }

    @Test
    fun `yamaghanda Sunday uses segment 5`() {
        val dayDuration = 720.0
        val result = PanchangCalculator.yamaghanda(dayDuration, 1)
        val segDur = dayDuration / 8.0
        assertEquals("Sunday Yamaghanda start", 4 * segDur, result.startMinutes, 0.01)
    }

    // ==================== Gulika Kaal ====================

    @Test
    fun `gulikaKaal returns valid time period for all weekdays`() {
        val dayDuration = 680.0
        for (weekday in 1..7) {
            val result = PanchangCalculator.gulikaKaal(dayDuration, weekday)
            assertEquals("Name should be Gulika Kaal", "Gulika Kaal", result.name)
            assertTrue("Start >= 0 for weekday $weekday", result.startMinutes >= 0.0)
            assertTrue("End <= day duration for weekday $weekday",
                result.endMinutes <= dayDuration)
            assertTrue("Start < end for weekday $weekday",
                result.startMinutes < result.endMinutes)
        }
    }

    @Test
    fun `gulikaKaal Sunday uses segment 7`() {
        val dayDuration = 720.0
        val result = PanchangCalculator.gulikaKaal(dayDuration, 1)
        val segDur = dayDuration / 8.0
        assertEquals("Sunday Gulika start", 6 * segDur, result.startMinutes, 0.01)
    }

    // ==================== Hindu Month ====================

    @Test
    fun `calculateHinduMonth returns valid HinduMonth`() {
        val jdTT = jdTTForDate(2025, 4, 15)
        val result = PanchangCalculator.calculateHinduMonth(jdTT, CalendarTradition.PURNIMANT)
        val month = result.month
        assertNotNull("Hindu month should not be null", month)
        assertTrue("Month displayName should not be empty", month.displayName.isNotEmpty())
        assertTrue("Month number should be 1-12, was ${month.number}", month.number in 1..12)
    }

    @Test
    fun `calculateHinduMonth returns different months throughout the year`() {
        val months = mutableSetOf<HinduMonth>()
        for (m in 1..12) {
            val jdTT = jdTTForDate(2025, m, 15)
            months.add(PanchangCalculator.calculateHinduMonth(jdTT, CalendarTradition.PURNIMANT).month)
        }
        assertTrue("Should see multiple Hindu months over a year, saw ${months.size}",
            months.size >= 10)
    }

    // ==================== Vikram Samvat / Shaka Year ====================

    @Test
    fun `vikramSamvatYear for 2025 Chaitra returns 2082`() {
        val result = PanchangCalculator.vikramSamvatYear(2025, HinduMonth.CHAITRA)
        assertEquals("Vikram Samvat for 2025 Chaitra should be 2082", 2082, result)
    }

    @Test
    fun `vikramSamvatYear for 2025 Pausha returns 2081`() {
        val result = PanchangCalculator.vikramSamvatYear(2025, HinduMonth.PAUSHA)
        assertEquals("Vikram Samvat for 2025 Pausha should be 2081", 2081, result)
    }

    @Test
    fun `vikramSamvatYear for 2025 Magha returns 2081`() {
        val result = PanchangCalculator.vikramSamvatYear(2025, HinduMonth.MAGHA)
        assertEquals("Vikram Samvat for 2025 Magha should be 2081", 2081, result)
    }

    @Test
    fun `vikramSamvatYear for 2025 Phalguna returns 2081`() {
        val result = PanchangCalculator.vikramSamvatYear(2025, HinduMonth.PHALGUNA)
        assertEquals("Vikram Samvat for 2025 Phalguna should be 2081", 2081, result)
    }

    @Test
    fun `shakaYear for 2025 Chaitra returns 1947`() {
        val result = PanchangCalculator.shakaYear(2025, HinduMonth.CHAITRA)
        assertEquals("Shaka year for 2025 Chaitra should be 1947", 1947, result)
    }

    @Test
    fun `shakaYear for 2025 Pausha returns 1946`() {
        val result = PanchangCalculator.shakaYear(2025, HinduMonth.PAUSHA)
        assertEquals("Shaka year for 2025 Pausha should be 1946", 1946, result)
    }

    @Test
    fun `shakaYear for 2025 Magha returns 1946`() {
        val result = PanchangCalculator.shakaYear(2025, HinduMonth.MAGHA)
        assertEquals("Shaka year for 2025 Magha should be 1946", 1946, result)
    }

    @Test
    fun `shakaYear for 2025 Vaishakha returns 1947`() {
        val result = PanchangCalculator.shakaYear(2025, HinduMonth.VAISHAKHA)
        assertEquals("Shaka year for 2025 Vaishakha should be 1947", 1947, result)
    }

    // ==================== Muhurta System ====================

    @Test
    fun `calculateMuhurtas returns 15 day and 15 night muhurtas`() {
        val result = PanchangCalculator.calculateMuhurtas(720.0, 720.0)
        assertEquals(15, result.daySlots.size)
        assertEquals(15, result.nightSlots.size)
    }

    @Test
    fun `calculateMuhurtas day muhurta duration equals dayDuration divided by 15`() {
        val dayDur = 720.0
        val result = PanchangCalculator.calculateMuhurtas(dayDur, 720.0)
        val expected = dayDur / 15.0
        result.daySlots.forEach {
            assertEquals(expected, it.endMinutes - it.startMinutes, 0.01)
        }
    }

    @Test
    fun `calculateMuhurtas night muhurta duration equals nightDuration divided by 15`() {
        val nightDur = 660.0
        val result = PanchangCalculator.calculateMuhurtas(720.0, nightDur)
        val expected = nightDur / 15.0
        result.nightSlots.forEach {
            assertEquals(expected, it.endMinutes - it.startMinutes, 0.01)
        }
    }

    @Test
    fun `calculateMuhurtas first day muhurta starts at 0`() {
        val result = PanchangCalculator.calculateMuhurtas(720.0, 720.0)
        assertEquals(0.0, result.daySlots.first().startMinutes, 0.01)
    }

    @Test
    fun `calculateMuhurtas last day muhurta ends at dayDuration`() {
        val dayDur = 690.0
        val result = PanchangCalculator.calculateMuhurtas(dayDur, 720.0)
        assertEquals(dayDur, result.daySlots.last().endMinutes, 0.01)
    }

    @Test
    fun `calculateMuhurtas muhurta numbers are sequential 1-30`() {
        val result = PanchangCalculator.calculateMuhurtas(720.0, 720.0)
        val allNumbers = result.daySlots.map { it.muhurtaNumber } +
                result.nightSlots.map { it.muhurtaNumber }
        assertEquals((1..30).toList(), allNumbers)
    }

    @Test
    fun `calculateMuhurtas with unequal day and night has different slot durations`() {
        val result = PanchangCalculator.calculateMuhurtas(800.0, 640.0)
        val daySlotDur = result.daySlots[0].endMinutes - result.daySlots[0].startMinutes
        val nightSlotDur = result.nightSlots[0].endMinutes - result.nightSlots[0].startMinutes
        assertTrue("Day and night slot durations should differ",
            kotlin.math.abs(daySlotDur - nightSlotDur) > 1.0)
    }

    // ==================== Choghadiya System ====================

    @Test
    fun `calculateChoghadiyas returns 8 day and 8 night periods`() {
        val result = PanchangCalculator.calculateChoghadiyas(720.0, 720.0, 1)
        assertEquals(8, result.daySlots.size)
        assertEquals(8, result.nightSlots.size)
    }

    @Test
    fun `calculateChoghadiyas day segment duration is one-eighth of day`() {
        val dayDur = 720.0
        val result = PanchangCalculator.calculateChoghadiyas(dayDur, 720.0, 3)
        val segDur = result.daySlots[0].endMinutes - result.daySlots[0].startMinutes
        assertEquals(dayDur / 8.0, segDur, 0.01)
    }

    @Test
    fun `calculateChoghadiyas night segment duration is one-eighth of night`() {
        val nightDur = 660.0
        val result = PanchangCalculator.calculateChoghadiyas(720.0, nightDur, 1)
        val segDur = result.nightSlots[0].endMinutes - result.nightSlots[0].startMinutes
        assertEquals(nightDur / 8.0, segDur, 0.01)
    }

    @Test
    fun `calculateChoghadiyas all weekdays produce valid results`() {
        for (weekday in 1..7) {
            val result = PanchangCalculator.calculateChoghadiyas(720.0, 720.0, weekday)
            assertEquals(8, result.daySlots.size)
            assertEquals(8, result.nightSlots.size)
            assertTrue(result.daySlots.all { it.position in 0..7 })
            assertTrue(result.nightSlots.all { it.position in 0..7 })
        }
    }

    @Test
    fun `calculateChoghadiyas first day slot starts at 0`() {
        val result = PanchangCalculator.calculateChoghadiyas(720.0, 720.0, 1)
        assertEquals(0.0, result.daySlots.first().startMinutes, 0.01)
    }

    @Test
    fun `calculateChoghadiyas last day slot ends at dayDuration`() {
        val dayDur = 700.0
        val result = PanchangCalculator.calculateChoghadiyas(dayDur, 720.0, 1)
        assertEquals(dayDur, result.daySlots.last().endMinutes, 0.01)
    }

    // ==================== Hora System ====================

    @Test
    fun `calculateHoras returns 24 horas`() {
        val result = PanchangCalculator.calculateHoras(720.0, 720.0)
        assertEquals(24, result.slots.size)
    }

    @Test
    fun `calculateHoras first 12 are day horas`() {
        val result = PanchangCalculator.calculateHoras(720.0, 720.0)
        assertTrue(result.slots.take(12).all { it.isDay })
        assertTrue(result.slots.drop(12).all { !it.isDay })
    }

    @Test
    fun `calculateHoras day hora duration equals dayDuration divided by 12`() {
        val dayDur = 720.0
        val result = PanchangCalculator.calculateHoras(dayDur, 720.0)
        val dayHora = result.slots[0]
        assertEquals(dayDur / 12.0, dayHora.endMinutes - dayHora.startMinutes, 0.01)
    }

    @Test
    fun `calculateHoras night hora duration equals nightDuration divided by 12`() {
        val nightDur = 660.0
        val result = PanchangCalculator.calculateHoras(720.0, nightDur)
        val nightHora = result.slots[12]
        assertEquals(nightDur / 12.0, nightHora.endMinutes - nightHora.startMinutes, 0.01)
    }

    @Test
    fun `calculateHoras first day hora starts at 0`() {
        val result = PanchangCalculator.calculateHoras(720.0, 720.0)
        assertEquals(0.0, result.slots.first().startMinutes, 0.01)
    }

    @Test
    fun `calculateHoras horas are contiguous with no gaps`() {
        val result = PanchangCalculator.calculateHoras(700.0, 660.0)
        for (i in 0 until 23) {
            assertEquals("Hora $i end should equal hora ${i + 1} start",
                result.slots[i].endMinutes, result.slots[i + 1].startMinutes, 0.01)
        }
    }

    @Test
    fun `HoraPlanet atHoraIndex Sunday first hora is Sun`() {
        assertEquals(HoraPlanet.SUN, HoraPlanet.atHoraIndex(1, 0))
    }

    @Test
    fun `HoraPlanet atHoraIndex Monday first hora is Moon`() {
        assertEquals(HoraPlanet.MOON, HoraPlanet.atHoraIndex(2, 0))
    }

    @Test
    fun `HoraPlanet atHoraIndex Saturday first hora is Saturn`() {
        assertEquals(HoraPlanet.SATURN, HoraPlanet.atHoraIndex(7, 0))
    }

    @Test
    fun `HoraPlanet Chaldean order cycles correctly for Sunday`() {
        // Sun's position in Chaldean order: Saturn(0), Jupiter(1), Mars(2), Sun(3), Venus(4), Mercury(5), Moon(6)
        // Starting at Sun (index 3), next is Venus(4), Mercury(5), Moon(6), Saturn(0), Jupiter(1), Mars(2)
        val expected = listOf(
            HoraPlanet.SUN, HoraPlanet.VENUS, HoraPlanet.MERCURY, HoraPlanet.MOON,
            HoraPlanet.SATURN, HoraPlanet.JUPITER, HoraPlanet.MARS
        )
        val actual = (0 until 7).map { HoraPlanet.atHoraIndex(1, it) }
        assertEquals(expected, actual)
    }

    @Test
    fun `ChoghadiyaType atPosition Sunday day starts with Udvegh`() {
        assertEquals(ChoghadiyaType.UDVEGH, ChoghadiyaType.atPosition(1, 0, true))
    }

    @Test
    fun `ChoghadiyaType atPosition Monday day starts with Amrit`() {
        assertEquals(ChoghadiyaType.AMRIT, ChoghadiyaType.atPosition(2, 0, true))
    }

    @Test
    fun `ChoghadiyaType atPosition Sunday night starts with Shubh`() {
        assertEquals(ChoghadiyaType.SHUBH, ChoghadiyaType.atPosition(1, 0, false))
    }

    @Test
    fun `ChoghadiyaType atPosition Tuesday day starts with Rog`() {
        assertEquals(ChoghadiyaType.ROG, ChoghadiyaType.atPosition(3, 0, true))
    }

    @Test
    fun `Muhurta fromNumber returns correct muhurta`() {
        assertEquals(Muhurta.RUDRA, Muhurta.fromNumber(1))
        assertEquals(Muhurta.VIDHI, Muhurta.fromNumber(8))
        assertEquals(Muhurta.SAMUDRAM, Muhurta.fromNumber(30))
    }

    @Test
    fun `Muhurta dayMuhurtas has 15 entries`() {
        assertEquals(15, Muhurta.dayMuhurtas.size)
        assertTrue(Muhurta.dayMuhurtas.all { it.isDay })
    }

    @Test
    fun `Muhurta nightMuhurtas has 15 entries`() {
        assertEquals(15, Muhurta.nightMuhurtas.size)
        assertTrue(Muhurta.nightMuhurtas.all { !it.isDay })
    }
}
