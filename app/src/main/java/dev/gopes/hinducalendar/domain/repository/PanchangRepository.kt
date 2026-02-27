package dev.gopes.hinducalendar.domain.repository

import dev.gopes.hinducalendar.domain.model.*
import java.time.LocalDate

/**
 * Contract for panchang computation and festival queries.
 * Backed by [PanchangService] in production; fake in tests.
 */
interface PanchangRepository {
    fun clearCache()
    fun computePanchang(date: LocalDate, location: HinduLocation, tradition: CalendarTradition): PanchangDay?
    fun computeFestivals(date: LocalDate, location: HinduLocation, tradition: CalendarTradition, festivalReference: FestivalDateReference): List<FestivalOccurrence>
    fun computeMonthlyPanchang(year: Int, month: Int, location: HinduLocation, tradition: CalendarTradition): List<PanchangDay>
}
