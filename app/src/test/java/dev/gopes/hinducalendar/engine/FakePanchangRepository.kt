package dev.gopes.hinducalendar.engine

import dev.gopes.hinducalendar.domain.repository.PanchangRepository

import dev.gopes.hinducalendar.domain.model.*
import java.time.LocalDate

/**
 * In-memory fake for unit testing ViewModels that depend on [PanchangRepository].
 */
class FakePanchangRepository : PanchangRepository {

    var panchangResult: PanchangDay? = null
    var monthlyResult: List<PanchangDay> = emptyList()
    var festivalsResult: List<FestivalOccurrence> = emptyList()
    var clearCacheCalled = false

    override fun clearCache() {
        clearCacheCalled = true
    }

    override fun computePanchang(
        date: LocalDate,
        location: HinduLocation,
        tradition: CalendarTradition
    ): PanchangDay? = panchangResult

    override fun computeFestivals(
        date: LocalDate,
        location: HinduLocation,
        tradition: CalendarTradition,
        festivalReference: FestivalDateReference
    ): List<FestivalOccurrence> = festivalsResult

    override fun computeMonthlyPanchang(
        year: Int,
        month: Int,
        location: HinduLocation,
        tradition: CalendarTradition
    ): List<PanchangDay> = monthlyResult
}
