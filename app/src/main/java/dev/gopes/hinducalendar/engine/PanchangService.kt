package dev.gopes.hinducalendar.engine

import android.util.LruCache
import dev.gopes.hinducalendar.data.model.*
import timber.log.Timber
import java.time.*
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * High-level service that composes all engines to produce a complete PanchangDay.
 */
@Singleton
class PanchangService @Inject constructor(
    private val festivalEngine: FestivalRulesEngine
) {

    private data class CacheKey(val date: LocalDate, val lat: Double, val lng: Double, val tradition: String)

    private val cache = LruCache<CacheKey, PanchangDay>(120)

    fun clearCache() {
        cache.evictAll()
        Timber.d("Panchang cache cleared")
    }

    fun computePanchang(
        date: LocalDate,
        location: HinduLocation,
        tradition: CalendarTradition
    ): PanchangDay {
        val key = CacheKey(date, location.latitude, location.longitude, tradition.key)
        cache.get(key)?.let { return it }

        Timber.d("Computing panchang for %s at (%.4f, %.4f)", date, location.latitude, location.longitude)
        val year = date.year
        val jdMidnight = AstronomyEngine.dateToJD(year, date.monthValue, date.dayOfMonth)
        val jdTT = AstronomyEngine.utToTT(jdMidnight, year.toDouble())

        // Sunrise / Sunset
        val sunriseJD = AstronomyEngine.sunrise(jdMidnight, location.latitude, location.longitude)
        val sunsetJD = AstronomyEngine.sunset(jdMidnight, location.latitude, location.longitude)
        val sunrise = jdToLocalDateTime(sunriseJD ?: jdMidnight, location.timeZoneId)
        val sunset = jdToLocalDateTime(sunsetJD ?: (jdMidnight + 0.5), location.timeZoneId)

        val jdSunrise = sunriseJD ?: jdTT
        val jdSunriseTT = AstronomyEngine.utToTT(jdSunrise, year.toDouble())

        // Tithi
        val (tithi, paksha, tithiNumber) = PanchangCalculator.calculateTithi(jdSunriseTT)
        val nextTithiJD = PanchangCalculator.findTithiTransition(jdSunrise, tithiNumber + 1)
        val tithiElement = PanchangElement(
            name = tithi.displayName,
            hindiName = tithi.hindiName,
            number = tithi.continuousNumber,
            endTime = jdToLocalDateTime(nextTithiJD, location.timeZoneId)
        )

        // Nakshatra
        val nakshatra = PanchangCalculator.calculateNakshatra(jdSunriseTT)
        val nextNakJD = PanchangCalculator.findNakshatraTransition(
            jdSunrise, (nakshatra.number % 27) + 1
        )
        val nakshatraElement = PanchangElement(
            name = nakshatra.displayName,
            hindiName = nakshatra.hindiName,
            number = nakshatra.number,
            endTime = jdToLocalDateTime(nextNakJD, location.timeZoneId)
        )

        // Yoga
        val yoga = PanchangCalculator.calculateYoga(jdSunriseTT)
        val yogaElement = PanchangElement(name = yoga.displayName, number = yoga.number)

        // Karana
        val karana = PanchangCalculator.calculateKarana(jdSunriseTT)
        val karanaElement = PanchangElement(name = karana.displayName, number = karana.ordinal)

        // Hindu Date (proper lunar month calculation)
        val (hinduMonth, isAdhikMaas) = PanchangCalculator.calculateHinduMonth(jdSunriseTT, tradition)
        val samvatYear = PanchangCalculator.vikramSamvatYear(year, hinduMonth)
        val shakaYear = PanchangCalculator.shakaYear(year, hinduMonth)
        val hinduDate = HinduDate(
            month = hinduMonth,
            paksha = paksha,
            tithi = tithi,
            samvatYear = samvatYear,
            shakaYear = shakaYear,
            isAdhikMaas = isAdhikMaas,
            bangabdaYear = PanchangCalculator.bangabdaYear(year, hinduMonth),
            thiruvalluvarYear = PanchangCalculator.thiruvalluvarYear(year),
            kollavarshamYear = PanchangCalculator.kollavarshamYear(year, hinduMonth),
            nanakshahiYear = PanchangCalculator.nanakshahiYear(year),
            virNirvanaSamvatYear = PanchangCalculator.virNirvanaSamvatYear(year)
        )

        // Day periods
        val weekday = date.dayOfWeek.value % 7 + 1 // 1=Sunday
        val dayDurMin = Duration.between(sunrise, sunset).toMinutes().toDouble()

        val rahuRes = PanchangCalculator.rahuKaal(dayDurMin, weekday)
        val yamaRes = PanchangCalculator.yamaghanda(dayDurMin, weekday)
        val gulikaRes = PanchangCalculator.gulikaKaal(dayDurMin, weekday)

        val rahuKaal = TimePeriod("Rahu Kaal", sunrise.plusMinutes(rahuRes.startMinutes.toLong()), sunrise.plusMinutes(rahuRes.endMinutes.toLong()))
        val yamaghanda = TimePeriod("Yamaghanda", sunrise.plusMinutes(yamaRes.startMinutes.toLong()), sunrise.plusMinutes(yamaRes.endMinutes.toLong()))
        val gulikaKaal = TimePeriod("Gulika Kaal", sunrise.plusMinutes(gulikaRes.startMinutes.toLong()), sunrise.plusMinutes(gulikaRes.endMinutes.toLong()))

        // Abhijit Muhurta
        val muhurtaDur = dayDurMin / 15.0
        val noon = sunrise.plusMinutes((dayDurMin / 2).toLong())
        val abhijit = TimePeriod("Abhijit Muhurta",
            noon.minusMinutes((muhurtaDur / 2).toLong()),
            noon.plusMinutes((muhurtaDur / 2).toLong())
        )

        // Moonrise/Moonset
        val moonriseJD = AstronomyEngine.moonrise(jdMidnight, location.latitude, location.longitude)
        val moonsetJD = AstronomyEngine.moonset(jdMidnight, location.latitude, location.longitude)

        // Festivals
        val festivals = festivalEngine.festivalsFor(date, hinduDate, tradition, jdSunriseTT)

        return PanchangDay(
            date = date,
            location = location,
            tradition = tradition,
            sunrise = sunrise,
            sunset = sunset,
            moonrise = moonriseJD?.let { jdToLocalDateTime(it, location.timeZoneId) },
            moonset = moonsetJD?.let { jdToLocalDateTime(it, location.timeZoneId) },
            tithiInfo = tithiElement,
            nakshatraInfo = nakshatraElement,
            yogaInfo = yogaElement,
            karanaInfo = karanaElement,
            hinduDate = hinduDate,
            rahuKaal = rahuKaal,
            yamaghanda = yamaghanda,
            gulikaKaal = gulikaKaal,
            abhijitMuhurta = abhijit,
            festivals = festivals
        ).also { cache.put(key, it) }
    }

    /**
     * Compute festivals for a date using the given reference location.
     * When [festivalReference] is INDIAN_STANDARD, festivals are computed
     * from Delhi's perspective so diaspora users see the same dates as India.
     */
    fun computeFestivals(
        date: LocalDate,
        location: HinduLocation,
        tradition: CalendarTradition,
        festivalReference: FestivalDateReference
    ): List<FestivalOccurrence> {
        val refLocation = if (festivalReference == FestivalDateReference.INDIAN_STANDARD)
            HinduLocation.DELHI else location
        return computePanchang(date, refLocation, tradition).festivals
    }

    fun computeMonthlyPanchang(
        year: Int,
        month: Int,
        location: HinduLocation,
        tradition: CalendarTradition
    ): List<PanchangDay> {
        val yearMonth = YearMonth.of(year, month)
        return (1..yearMonth.lengthOfMonth()).map { day ->
            computePanchang(LocalDate.of(year, month, day), location, tradition)
        }
    }

    private fun jdToLocalDateTime(jd: Double, timeZoneId: String): LocalDateTime {
        val (y, m, d) = AstronomyEngine.jdToDateTime(jd)
        val dayFrac = (jd + 0.5) - (jd + 0.5).toLong()
        val totalSeconds = (dayFrac * 86400).toLong()
        val utcDateTime = LocalDateTime.of(y, m, d, 0, 0).plusSeconds(totalSeconds)
        val utcInstant = utcDateTime.toInstant(ZoneOffset.UTC)
        return utcInstant.atZone(ZoneId.of(timeZoneId)).toLocalDateTime()
    }
}
