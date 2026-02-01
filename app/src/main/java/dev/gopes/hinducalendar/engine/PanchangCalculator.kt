package dev.gopes.hinducalendar.engine

import dev.gopes.hinducalendar.data.model.*
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

    // ==================== Hindu Month ====================

    fun calculateHinduMonth(jdTT: Double): HinduMonth {
        val sunSidereal = AstronomyEngine.tropicalToSidereal(
            AstronomyEngine.sunTropicalLongitude(jdTT), jdTT
        )
        val solarMonth = (sunSidereal / 30.0).toInt()
        return HinduMonth.fromSolarMonth(solarMonth)
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

    // ==================== Helpers ====================

    private fun findMoonSunAngle(jdStart: Double, targetAngle: Double): Double {
        var jd = jdStart
        repeat(50) {
            val sunLong = AstronomyEngine.sunTropicalLongitude(jd)
            val moonLong = AstronomyEngine.moonTropicalLongitude(jd)
            var diff = moonLong - sunLong
            if (diff < 0) diff += 360.0
            var delta = targetAngle - diff
            if (delta > 180) delta -= 360.0
            if (delta < -180) delta += 360.0
            if (abs(delta) < 0.001) return jd
            jd += delta / 12.2
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
            if (abs(delta) < 0.001) return jd
            jd += delta / 13.2
        }
        return jd
    }
}
