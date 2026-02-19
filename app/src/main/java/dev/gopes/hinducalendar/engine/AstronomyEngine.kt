package dev.gopes.hinducalendar.engine

import kotlin.math.*

/**
 * Core astronomical calculation engine.
 * Implements Julian Day, solar position, lunar position, and ayanamsa calculations.
 */
object AstronomyEngine {

    // ==================== Julian Day ====================

    fun dateToJD(year: Int, month: Int, day: Int, hour: Double = 0.0): Double {
        var y = year
        var m = month
        if (m <= 2) { y--; m += 12 }
        val a = y / 100
        val b = 2 - a + (a / 4)
        return (365.25 * (y + 4716)).toInt().toDouble() +
                (30.6001 * (m + 1)).toInt().toDouble() +
                day + hour / 24.0 + b - 1524.5
    }

    fun jdToDateTime(jd: Double): Triple<Int, Int, Int> {
        val z = (jd + 0.5).toInt()
        val f = (jd + 0.5) - z
        val a = if (z < 2299161) z else {
            val alpha = ((z - 1867216.25) / 36524.25).toInt()
            z + 1 + alpha - (alpha / 4)
        }
        val b = a + 1524
        val c = ((b - 122.1) / 365.25).toInt()
        val d = (365.25 * c).toInt()
        val e = ((b - d) / 30.6001).toInt()
        val day = b - d - (30.6001 * e).toInt()
        val month = if (e < 14) e - 1 else e - 13
        val year = if (month > 2) c - 4716 else c - 4715
        return Triple(year, month, day)
    }

    private fun centuriesFromJ2000(jd: Double) = (jd - 2451545.0) / 36525.0

    // ==================== Utilities ====================

    fun normalize(degrees: Double): Double {
        var r = degrees % 360.0
        if (r < 0) r += 360.0
        return r
    }

    private fun toRad(deg: Double) = deg * PI / 180.0
    private fun toDeg(rad: Double) = rad * 180.0 / PI

    fun deltaT(year: Double): Double {
        val t = year - 2000.0
        return if (year in 2005.0..2050.0) 62.92 + 0.32217 * t + 0.005589 * t * t
        else 69.184 + 0.3 * t
    }

    fun utToTT(jdUT: Double, year: Double) = jdUT + deltaT(year) / 86400.0

    // ==================== Solar Position ====================

    fun sunTropicalLongitude(jdTT: Double): Double {
        val T = centuriesFromJ2000(jdTT)
        val L0 = normalize(280.46646 + 36000.76983 * T + 0.0003032 * T * T)
        val M = normalize(357.52911 + 35999.05029 * T - 0.0001537 * T * T)
        val Mrad = toRad(M)
        val C = (1.914602 - 0.004817 * T - 0.000014 * T * T) * sin(Mrad) +
                (0.019993 - 0.000101 * T) * sin(2 * Mrad) +
                0.000289 * sin(3 * Mrad)
        val theta = normalize(L0 + C)
        val omega = 125.04 - 1934.136 * T
        return normalize(theta - 0.00569 - 0.00478 * sin(toRad(omega)))
    }

    fun sunEquatorial(jdTT: Double): Pair<Double, Double> {
        val lambda = sunTropicalLongitude(jdTT)
        val T = centuriesFromJ2000(jdTT)
        val epsilon = 23.439291 - 0.0130042 * T
        val lRad = toRad(lambda)
        val eRad = toRad(epsilon)
        val ra = atan2(cos(eRad) * sin(lRad), cos(lRad))
        val dec = asin(sin(eRad) * sin(lRad))
        return Pair(normalize(toDeg(ra)), toDeg(dec))
    }

    fun sunrise(jd: Double, lat: Double, lon: Double): Double? =
        sunRiseSet(jd, lat, lon, isRise = true)

    fun sunset(jd: Double, lat: Double, lon: Double): Double? =
        sunRiseSet(jd, lat, lon, isRise = false)

    private fun sunRiseSet(jd: Double, lat: Double, lon: Double, isRise: Boolean): Double? {
        val altCorr = -0.8333
        val jdNoon = jd.toInt() + 0.5 - lon / 360.0
        var jdResult = jdNoon + if (isRise) -0.25 else 0.25

        repeat(5) {
            val (ra, dec) = sunEquatorial(jdResult)
            val latRad = toRad(lat)
            val decRad = toRad(dec)
            val cosH = (sin(toRad(altCorr)) - sin(latRad) * sin(decRad)) / (cos(latRad) * cos(decRad))
            if (cosH < -1 || cosH > 1) return null
            var H = toDeg(acos(cosH))
            if (isRise) H = -H
            val gmst = greenwichMeanSiderealTime(jdResult)
            val lst = normalize(gmst + lon)
            var hourAngle = lst - ra
            while (hourAngle > 180) hourAngle -= 360
            while (hourAngle < -180) hourAngle += 360
            jdResult += (H - hourAngle) / 360.0
        }
        return jdResult
    }

    private fun greenwichMeanSiderealTime(jdUT: Double): Double {
        val T = centuriesFromJ2000(jdUT)
        return normalize(
            280.46061837 + 360.98564736629 * (jdUT - 2451545.0) +
                    0.000387933 * T * T - T * T * T / 38710000.0
        )
    }

    // ==================== Lunar Position ====================

    fun moonTropicalLongitude(jdTT: Double): Double {
        val T = centuriesFromJ2000(jdTT)
        val Lp = normalize(218.3164477 + 481267.88123421 * T - 0.0015786 * T * T
            + T * T * T / 538841.0 - T * T * T * T / 65194000.0)
        val D = normalize(297.8501921 + 445267.1114034 * T - 0.0018819 * T * T
            + T * T * T / 545868.0 - T * T * T * T / 113065000.0)
        val M = normalize(357.5291092 + 35999.0502909 * T - 0.0001536 * T * T
            + T * T * T / 24490000.0)
        val Mp = normalize(134.9633964 + 477198.8675055 * T + 0.0087414 * T * T
            + T * T * T / 69699.0 - T * T * T * T / 14712000.0)
        val F = normalize(93.2720950 + 483202.0175233 * T - 0.0036539 * T * T
            - T * T * T / 3526000.0 + T * T * T * T / 863310000.0)

        val Drad = toRad(D); val Mrad = toRad(M); val Mprad = toRad(Mp); val Frad = toRad(F)
        val E = 1.0 - 0.002516 * T - 0.0000074 * T * T

        var sigmaL = 0.0
        sigmaL += 6288774 * sin(Mprad)
        sigmaL += 1274027 * sin(2 * Drad - Mprad)
        sigmaL += 658314 * sin(2 * Drad)
        sigmaL += 213618 * sin(2 * Mprad)
        sigmaL += -185116 * E * sin(Mrad)
        sigmaL += -114332 * sin(2 * Frad)
        sigmaL += 58793 * sin(2 * Drad - 2 * Mprad)
        sigmaL += 57066 * E * sin(2 * Drad - Mrad - Mprad)
        sigmaL += 53322 * sin(2 * Drad + Mprad)
        sigmaL += 45758 * E * sin(2 * Drad - Mrad)
        sigmaL += -40923 * E * sin(Mrad - Mprad)
        sigmaL += -34720 * sin(Drad)
        sigmaL += -30383 * E * sin(Mrad + Mprad)
        sigmaL += 15327 * sin(2 * Drad - 2 * Frad)
        sigmaL += -12528 * sin(Mprad + 2 * Frad)
        sigmaL += 10980 * sin(Mprad - 2 * Frad)
        sigmaL += 10675 * sin(4 * Drad - Mprad)
        sigmaL += 10034 * sin(3 * Mprad)
        sigmaL += 8548 * sin(4 * Drad - 2 * Mprad)
        sigmaL += -7888 * E * sin(2 * Drad + Mrad - Mprad)
        sigmaL += -6766 * E * sin(2 * Drad + Mrad)
        sigmaL += -5163 * sin(Drad - Mprad)
        sigmaL += 4987 * E * sin(Drad + Mrad)
        sigmaL += 4036 * E * sin(2 * Drad - Mrad + Mprad)

        val A1 = toRad(normalize(119.75 + 131.849 * T))
        val A2 = toRad(normalize(53.09 + 479264.290 * T))
        sigmaL += 3958 * sin(A1)
        sigmaL += 1962 * sin(toRad(Lp) - Frad)
        sigmaL += 318 * sin(A2)

        return normalize(Lp + sigmaL / 1000000.0)
    }

    fun moonrise(jd: Double, lat: Double, lon: Double): Double? =
        moonRiseSet(jd, lat, lon, isRise = true)

    fun moonset(jd: Double, lat: Double, lon: Double): Double? =
        moonRiseSet(jd, lat, lon, isRise = false)

    private fun moonRiseSet(jd: Double, lat: Double, lon: Double, isRise: Boolean): Double? {
        val altCorr = 0.125
        val jdNoon = jd.toInt() + 0.5 - lon / 360.0
        var jdResult = jdNoon + if (isRise) -0.25 else 0.25

        repeat(10) {
            val moonLong = moonTropicalLongitude(jdResult)
            val T = centuriesFromJ2000(jdResult)
            val epsilon = toRad(23.439291 - 0.0130042 * T)
            val lambda = toRad(moonLong)
            val ra = normalize(toDeg(atan2(sin(lambda) * cos(epsilon), cos(lambda))))
            val dec = toDeg(asin(sin(epsilon) * sin(lambda)))

            val latRad = toRad(lat); val decRad = toRad(dec)
            val cosH = (sin(toRad(altCorr)) - sin(latRad) * sin(decRad)) / (cos(latRad) * cos(decRad))
            if (cosH < -1 || cosH > 1) return null
            var H = toDeg(acos(cosH))
            if (isRise) H = -H
            val gmst = greenwichMeanSiderealTime(jdResult)
            val lst = normalize(gmst + lon)
            var hourAngle = lst - ra
            while (hourAngle > 180) hourAngle -= 360
            while (hourAngle < -180) hourAngle += 360
            jdResult += (H - hourAngle) / 347.8
        }

        val dayStart = jd.toInt() + 0.5 - lon / 360.0 - 0.5
        if (jdResult < dayStart || jdResult > dayStart + 1.5) return null
        return jdResult
    }

    // ==================== Ayanamsa ====================

    fun lahiriAyanamsa(jdTT: Double): Double {
        val T = centuriesFromJ2000(jdTT)
        return 23.853 + (50.29 / 3600.0) * (T * 100)
    }

    fun tropicalToSidereal(tropicalLong: Double, jdTT: Double): Double {
        return normalize(tropicalLong - lahiriAyanamsa(jdTT))
    }
}
