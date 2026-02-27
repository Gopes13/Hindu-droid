package dev.gopes.hinducalendar.data.engine

import dev.gopes.hinducalendar.domain.model.*
import kotlin.math.abs

/**
 * Core Panchang calculation engine.
 * Computes all five elements for any date and location.
 */
object PanchangCalculator {

    // ==================== Tithi ====================

    data class TithiResult(val tithi: Tithi, val paksha: Paksha, val number: Int)

    fun calculateTithi(jdTT: Double): TithiResult {
        val sunLong = AstronomyEngine.sunTropicalLongitude(jdTT)
        val moonLong = AstronomyEngine.moonTropicalLongitude(jdTT)
        var diff = moonLong - sunLong
        if (diff < 0) diff += 360.0

        val tithiNumber = (diff / 12.0).toInt() + 1
        val paksha = if (tithiNumber <= 15) Paksha.SHUKLA else Paksha.KRISHNA

        val tithiInPaksha = when {
            tithiNumber <= 14 -> tithiNumber
            tithiNumber == 15 -> 15
            tithiNumber <= 29 -> tithiNumber - 15
            else -> 30
        }

        val tithi = when {
            tithiInPaksha == 15 && paksha == Paksha.SHUKLA -> Tithi.PURNIMA
            tithiNumber == 30 -> Tithi.AMAVASYA
            else -> Tithi.fromNumber(tithiInPaksha)
        }

        return TithiResult(tithi, paksha, tithiNumber)
    }

    fun findTithiTransition(jdStart: Double, tithiNumber: Int): Double {
        val targetAngle = (tithiNumber - 1) * 12.0
        return findMoonSunAngle(jdStart, targetAngle)
    }

    // ==================== Nakshatra ====================

    fun calculateNakshatra(jdTT: Double): Nakshatra {
        val moonTropical = AstronomyEngine.moonTropicalLongitude(jdTT)
        val moonSidereal = AstronomyEngine.tropicalToSidereal(moonTropical, jdTT)
        return Nakshatra.fromSiderealLongitude(moonSidereal)
    }

    fun findNakshatraTransition(jdStart: Double, nakshatraNumber: Int): Double {
        val targetLong = (nakshatraNumber - 1) * Nakshatra.SPAN_DEGREES
        return findSiderealMoonLongitude(jdStart, targetLong)
    }

    // ==================== Yoga ====================

    fun calculateYoga(jdTT: Double): Yoga {
        val sunTropical = AstronomyEngine.sunTropicalLongitude(jdTT)
        val moonTropical = AstronomyEngine.moonTropicalLongitude(jdTT)
        val sunSidereal = AstronomyEngine.tropicalToSidereal(sunTropical, jdTT)
        val moonSidereal = AstronomyEngine.tropicalToSidereal(moonTropical, jdTT)
        return Yoga.from(sunSidereal, moonSidereal)
    }

    // ==================== Karana ====================

    fun calculateKarana(jdTT: Double): Karana {
        val (_, _, tithiNumber) = calculateTithi(jdTT)
        val sunLong = AstronomyEngine.sunTropicalLongitude(jdTT)
        val moonLong = AstronomyEngine.moonTropicalLongitude(jdTT)
        var diff = moonLong - sunLong
        if (diff < 0) diff += 360.0
        val posInTithi = diff % 12.0
        return Karana.from(tithiNumber, posInTithi < 6.0)
    }

    // ==================== Rahu Kaal etc. ====================

    data class TimePeriodResult(val name: String, val startMinutes: Double, val endMinutes: Double)

    fun rahuKaal(dayDurationMinutes: Double, weekday: Int): TimePeriodResult {
        val segments = mapOf(1 to 8, 2 to 2, 3 to 7, 4 to 5, 5 to 6, 6 to 4, 7 to 3)
        val seg = segments[weekday] ?: 1
        val segDur = dayDurationMinutes / 8.0
        val start = (seg - 1) * segDur
        val end = start + segDur
        return TimePeriodResult("Rahu Kaal", start, end)
    }

    fun yamaghanda(dayDurationMinutes: Double, weekday: Int): TimePeriodResult {
        val segments = mapOf(1 to 5, 2 to 4, 3 to 3, 4 to 2, 5 to 1, 6 to 7, 7 to 6)
        val seg = segments[weekday] ?: 1
        val segDur = dayDurationMinutes / 8.0
        return TimePeriodResult("Yamaghanda", (seg - 1) * segDur, seg * segDur)
    }

    fun gulikaKaal(dayDurationMinutes: Double, weekday: Int): TimePeriodResult {
        val segments = mapOf(1 to 7, 2 to 6, 3 to 5, 4 to 4, 5 to 3, 6 to 2, 7 to 1)
        val seg = segments[weekday] ?: 1
        val segDur = dayDurationMinutes / 8.0
        return TimePeriodResult("Gulika Kaal", (seg - 1) * segDur, seg * segDur)
    }

    // ==================== Hindu Month (Lunar) ====================

    data class HinduMonthResult(val month: HinduMonth, val isAdhikMaas: Boolean)

    enum class SearchDirection { BACKWARD, FORWARD }

    private const val SYNODIC_MONTH = 29.530588

    /** Cache recent new moon searches to avoid redundant computation. */
    private val newMoonCache = mutableListOf<Triple<Double, Double, Double>>() // (query, backward, forward)

    /**
     * Find the JD of the nearest new moon (Amavasya) in a given direction.
     * Uses Moon-Sun elongation = 0° (conjunction).
     */
    fun findNewMoon(jd: Double, direction: SearchDirection): Double {
        // Check cache — query must fall between the cached backward/forward pair
        for ((_, backward, forward) in newMoonCache) {
            if (backward <= jd && forward >= jd) {
                if (direction == SearchDirection.BACKWARD) return backward
                if (direction == SearchDirection.FORWARD) return forward
            }
        }

        // Estimate using current phase
        val sunLong = AstronomyEngine.sunTropicalLongitude(jd)
        val moonLong = AstronomyEngine.moonTropicalLongitude(jd)
        var elongation = moonLong - sunLong
        if (elongation < 0) elongation += 360.0
        val phaseDay = elongation / 360.0 * SYNODIC_MONTH

        val startJD = if (direction == SearchDirection.BACKWARD) {
            jd - phaseDay - 1
        } else {
            jd + (SYNODIC_MONTH - phaseDay) - 1
        }

        var result = findMoonSunAngle(startJD, 0.0)

        // Validate direction and retry if needed
        if (direction == SearchDirection.BACKWARD && result > jd + 0.5) {
            result = findMoonSunAngle(result - SYNODIC_MONTH, 0.0)
        }
        if (direction == SearchDirection.FORWARD && result < jd - 0.5) {
            result = findMoonSunAngle(result + SYNODIC_MONTH, 0.0)
        }

        // Cache the result
        if (direction == SearchDirection.BACKWARD) {
            val forward = findMoonSunAngle(result + SYNODIC_MONTH - 2, 0.0)
            newMoonCache.add(Triple(jd, result, forward))
        }
        if (newMoonCache.size > 12) newMoonCache.removeAt(0)

        return result
    }

    /**
     * Find the JD of the nearest full moon (Purnima) in a given direction.
     * Uses Moon-Sun elongation = 180° (opposition).
     */
    fun findFullMoon(jd: Double, direction: SearchDirection): Double {
        val sunLong = AstronomyEngine.sunTropicalLongitude(jd)
        val moonLong = AstronomyEngine.moonTropicalLongitude(jd)
        var elongation = moonLong - sunLong
        if (elongation < 0) elongation += 360.0

        val phaseToFull = ((180.0 - elongation + 360.0) % 360.0) / 360.0 * SYNODIC_MONTH

        val startJD = if (direction == SearchDirection.BACKWARD) {
            val est = jd - ((if (elongation > 180) elongation - 180 else elongation + 180) / 360.0 * SYNODIC_MONTH) - 1
            est
        } else {
            jd + phaseToFull - 1
        }

        var result = findMoonSunAngle(startJD, 180.0)

        if (direction == SearchDirection.BACKWARD && result > jd + 0.5) {
            result = findMoonSunAngle(result - SYNODIC_MONTH, 180.0)
        }
        if (direction == SearchDirection.FORWARD && result < jd - 0.5) {
            result = findMoonSunAngle(result + SYNODIC_MONTH, 180.0)
        }

        return result
    }

    /** Map sidereal Rashi index (0-11) to Hindu month. Rashi 0 (Mesha) → Chaitra. */
    private fun rashiToHinduMonth(rashi: Int): HinduMonth {
        val mapping = arrayOf(
            HinduMonth.CHAITRA, HinduMonth.VAISHAKHA, HinduMonth.JYESHTHA, HinduMonth.ASHADHA,
            HinduMonth.SHRAVANA, HinduMonth.BHADRAPADA, HinduMonth.ASHWIN, HinduMonth.KARTIK,
            HinduMonth.MARGASHIRSHA, HinduMonth.PAUSHA, HinduMonth.MAGHA, HinduMonth.PHALGUNA
        )
        return mapping[((rashi % 12) + 12) % 12]
    }

    /** Return the next Hindu month in sequence. */
    private fun nextHinduMonth(month: HinduMonth): HinduMonth = when (month) {
        HinduMonth.CHAITRA -> HinduMonth.VAISHAKHA
        HinduMonth.VAISHAKHA -> HinduMonth.JYESHTHA
        HinduMonth.JYESHTHA -> HinduMonth.ASHADHA
        HinduMonth.ASHADHA -> HinduMonth.SHRAVANA
        HinduMonth.SHRAVANA -> HinduMonth.BHADRAPADA
        HinduMonth.BHADRAPADA -> HinduMonth.ASHWIN
        HinduMonth.ASHWIN -> HinduMonth.KARTIK
        HinduMonth.KARTIK -> HinduMonth.MARGASHIRSHA
        HinduMonth.MARGASHIRSHA -> HinduMonth.PAUSHA
        HinduMonth.PAUSHA -> HinduMonth.MAGHA
        HinduMonth.MAGHA -> HinduMonth.PHALGUNA
        HinduMonth.PHALGUNA -> HinduMonth.CHAITRA
    }

    /**
     * Determine the Hindu month for a given date, dispatching by tradition's month system.
     * Returns the month and whether it's an Adhik Maas (intercalary month).
     */
    fun calculateHinduMonth(jdTT: Double, tradition: CalendarTradition): HinduMonthResult {
        return when (tradition.monthSystem) {
            MonthSystem.AMANT -> calculateAmantMonth(jdTT)
            MonthSystem.PURNIMANT -> calculatePurnimantMonth(jdTT)
        }
    }

    /**
     * Amant system: month spans from one Amavasya to the next.
     * Named after the Sun's sidereal Rashi at the ending Amavasya.
     * Adhik Maas if no Sankranti (Rashi change) occurs within the lunar month.
     */
    private fun calculateAmantMonth(jdTT: Double): HinduMonthResult {
        val lastAmavasya = findNewMoon(jdTT, SearchDirection.BACKWARD)
        val nextAmavasya = findNewMoon(jdTT, SearchDirection.FORWARD)

        val sunSiderealAtStart = AstronomyEngine.tropicalToSidereal(
            AstronomyEngine.sunTropicalLongitude(lastAmavasya), lastAmavasya
        )
        val sunSiderealAtEnd = AstronomyEngine.tropicalToSidereal(
            AstronomyEngine.sunTropicalLongitude(nextAmavasya), nextAmavasya
        )

        val rashiAtStart = (sunSiderealAtStart / 30.0).toInt()
        val rashiAtEnd = (sunSiderealAtEnd / 30.0).toInt()

        // Adhik Maas: no Sankranti within this lunar month (same Rashi at start and end)
        val isAdhikMaas = (rashiAtStart == rashiAtEnd)

        // Month name comes from the Rashi at the ending Amavasya
        val month = rashiToHinduMonth(rashiAtEnd)

        return HinduMonthResult(month, isAdhikMaas)
    }

    /**
     * Purnimant system: month spans from one Purnima to the next.
     * During Shukla paksha: same name as Amant.
     * During Krishna paksha: one month ahead of Amant.
     */
    private fun calculatePurnimantMonth(jdTT: Double): HinduMonthResult {
        val (_, paksha, _) = calculateTithi(jdTT)
        val amantResult = calculateAmantMonth(jdTT)

        return if (paksha == Paksha.SHUKLA) {
            amantResult
        } else {
            // Krishna paksha in Purnimant = next month name
            HinduMonthResult(nextHinduMonth(amantResult.month), amantResult.isAdhikMaas)
        }
    }

    fun vikramSamvatYear(gregorianYear: Int, hinduMonth: HinduMonth): Int {
        return when (hinduMonth) {
            HinduMonth.PAUSHA, HinduMonth.MAGHA, HinduMonth.PHALGUNA -> gregorianYear + 56
            else -> gregorianYear + 57
        }
    }

    fun shakaYear(gregorianYear: Int, hinduMonth: HinduMonth): Int {
        return when (hinduMonth) {
            HinduMonth.PAUSHA, HinduMonth.MAGHA, HinduMonth.PHALGUNA -> gregorianYear - 79
            else -> gregorianYear - 78
        }
    }

    /** Bengali calendar year (Bangabda). New year in Vaishakha. Epoch ~594 CE. */
    fun bangabdaYear(gregorianYear: Int, hinduMonth: HinduMonth): Int {
        return when (hinduMonth) {
            HinduMonth.CHAITRA -> gregorianYear - 594
            else -> gregorianYear - 593
        }
    }

    /** Tamil Thiruvalluvar year. Epoch 31 BCE. */
    fun thiruvalluvarYear(gregorianYear: Int): Int = gregorianYear + 31

    /** Malayalam Kollavarsham year. Epoch 825 CE. New year in Chingam (Shravana). */
    fun kollavarshamYear(gregorianYear: Int, hinduMonth: HinduMonth): Int {
        return when (hinduMonth) {
            HinduMonth.SHRAVANA, HinduMonth.BHADRAPADA, HinduMonth.ASHWIN,
            HinduMonth.KARTIK, HinduMonth.MARGASHIRSHA -> gregorianYear - 824
            else -> gregorianYear - 825
        }
    }

    /** Sikh Nanakshahi year. Epoch 1469 CE (birth of Guru Nanak). */
    fun nanakshahiYear(gregorianYear: Int): Int = gregorianYear - 1469

    /** Jain Vir Nirvana Samvat year. Epoch 527 BCE. */
    fun virNirvanaSamvatYear(gregorianYear: Int): Int = gregorianYear + 527

    // ==================== Helpers ====================

    fun findMoonSunAngle(jdStart: Double, targetAngle: Double): Double {
        var jd = jdStart
        repeat(50) {
            val sunLong = AstronomyEngine.sunTropicalLongitude(jd)
            val moonLong = AstronomyEngine.moonTropicalLongitude(jd)
            var diff = moonLong - sunLong
            if (diff < 0) diff += 360.0
            var delta = targetAngle - diff
            if (delta > 180) delta -= 360.0
            if (delta < -180) delta += 360.0
            if (abs(delta) < 0.0001) return jd

            // Dynamic velocity via numerical derivative
            val h = 0.01 // ~14.4 minutes
            val sunPlus = AstronomyEngine.sunTropicalLongitude(jd + h)
            val moonPlus = AstronomyEngine.moonTropicalLongitude(jd + h)
            var diffPlus = moonPlus - sunPlus
            if (diffPlus < 0) diffPlus += 360.0
            var velocity = (diffPlus - diff) / h
            if (velocity > 180) velocity -= 360.0 / h
            if (velocity < -180) velocity += 360.0 / h
            if (abs(velocity) < 0.1) velocity = 12.2 // fallback

            jd += delta / velocity
        }
        return jd
    }

    private fun findSiderealMoonLongitude(jdStart: Double, targetLong: Double): Double {
        var jd = jdStart
        repeat(50) {
            val moonTropical = AstronomyEngine.moonTropicalLongitude(jd)
            val moonSidereal = AstronomyEngine.tropicalToSidereal(moonTropical, jd)
            var delta = targetLong - moonSidereal
            if (delta > 180) delta -= 360.0
            if (delta < -180) delta += 360.0
            if (abs(delta) < 0.0001) return jd

            // Dynamic velocity via numerical derivative
            val h = 0.01
            val moonTropicalPlus = AstronomyEngine.moonTropicalLongitude(jd + h)
            val moonSiderealPlus = AstronomyEngine.tropicalToSidereal(moonTropicalPlus, jd + h)
            var velocity = (moonSiderealPlus - moonSidereal) / h
            if (velocity > 180) velocity -= 360.0 / h
            if (velocity < -180) velocity += 360.0 / h
            if (abs(velocity) < 0.1) velocity = 13.2 // fallback

            jd += delta / velocity
        }
        return jd
    }
}
