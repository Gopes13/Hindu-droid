package dev.gopes.hinducalendar.data.model

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class PanchangDay(
    val date: LocalDate,
    val location: HinduLocation,
    val tradition: CalendarTradition,

    // Astronomical
    val sunrise: LocalDateTime,
    val sunset: LocalDateTime,
    val moonrise: LocalDateTime? = null,
    val moonset: LocalDateTime? = null,

    // Panchang elements
    val tithiInfo: PanchangElement,
    val nakshatraInfo: PanchangElement,
    val yogaInfo: PanchangElement,
    val karanaInfo: PanchangElement,

    // Hindu date
    val hinduDate: HinduDate,

    // Inauspicious periods
    val rahuKaal: TimePeriod? = null,
    val yamaghanda: TimePeriod? = null,
    val gulikaKaal: TimePeriod? = null,

    // Auspicious
    val abhijitMuhurta: TimePeriod? = null,

    // Festivals
    val festivals: List<FestivalOccurrence> = emptyList()
) {
    val id: String get() = date.format(DateTimeFormatter.ISO_LOCAL_DATE)

    val hasFestivals: Boolean get() = festivals.isNotEmpty()

    val hasMajorFestival: Boolean
        get() = festivals.any { it.festival.category == FestivalCategory.MAJOR }
}

data class PanchangElement(
    val name: String,
    val hindiName: String? = null,
    val number: Int,
    val startTime: LocalDateTime? = null,
    val endTime: LocalDateTime? = null
) {
    val timeRangeString: String
        get() {
            if (startTime == null || endTime == null) return ""
            val fmt = DateTimeFormatter.ofPattern("h:mm a")
            return "${startTime.format(fmt)} – ${endTime.format(fmt)}"
        }
}

data class TimePeriod(
    val name: String,
    val start: LocalDateTime,
    val end: LocalDateTime
) {
    val displayString: String
        get() {
            val fmt = DateTimeFormatter.ofPattern("h:mm a")
            return "${start.format(fmt)} – ${end.format(fmt)}"
        }
}
