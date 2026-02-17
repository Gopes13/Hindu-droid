package dev.gopes.hinducalendar.data.model

import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Unit tests for PanchangDay, PanchangElement, and TimePeriod data classes.
 */
class PanchangDayTest {

    // ==================== Helper factories ====================

    private fun makeFestival(
        id: String = "test_festival",
        category: FestivalCategory = FestivalCategory.MAJOR
    ): Festival = Festival(
        id = id,
        names = mapOf("en" to "Test Festival"),
        description = mapOf("en" to "A test festival"),
        rule = FestivalRule(type = "tithi"),
        traditions = listOf("purnimant"),
        category = category
    )

    private fun makeFestivalOccurrence(
        category: FestivalCategory = FestivalCategory.MAJOR
    ): FestivalOccurrence = FestivalOccurrence(
        festival = makeFestival(category = category),
        date = LocalDate.of(2025, 3, 14)
    )

    private fun makePanchangDay(
        festivals: List<FestivalOccurrence> = emptyList()
    ): PanchangDay {
        val date = LocalDate.of(2025, 3, 14)
        val sunrise = LocalDateTime.of(2025, 3, 14, 6, 30)
        val sunset = LocalDateTime.of(2025, 3, 14, 18, 15)

        return PanchangDay(
            date = date,
            location = HinduLocation(28.6139, 77.2090, "Asia/Kolkata", "New Delhi"),
            tradition = CalendarTradition.PURNIMANT,
            sunrise = sunrise,
            sunset = sunset,
            tithiInfo = PanchangElement(name = "Purnima", hindiName = "पूर्णिमा", number = 15),
            nakshatraInfo = PanchangElement(name = "Hasta", hindiName = "हस्त", number = 13),
            yogaInfo = PanchangElement(name = "Siddhi", number = 16),
            karanaInfo = PanchangElement(name = "Bava", number = 0),
            hinduDate = HinduDate(
                month = HinduMonth.PHALGUNA,
                paksha = Paksha.SHUKLA,
                tithi = Tithi.PURNIMA,
                samvatYear = 2081,
                shakaYear = 1946
            ),
            festivals = festivals
        )
    }

    // ==================== id ====================

    @Test
    fun `id returns ISO date format`() {
        val panchangDay = makePanchangDay()
        assertEquals("2025-03-14", panchangDay.id)
    }

    @Test
    fun `id format is YYYY-MM-DD`() {
        val date = LocalDate.of(2025, 1, 5)
        val panchangDay = makePanchangDay().copy(date = date)
        assertEquals("2025-01-05", panchangDay.id)
    }

    // ==================== hasFestivals ====================

    @Test
    fun `hasFestivals returns false when festivals list is empty`() {
        val panchangDay = makePanchangDay(festivals = emptyList())
        assertFalse("hasFestivals should be false for empty list", panchangDay.hasFestivals)
    }

    @Test
    fun `hasFestivals returns true when festivals exist`() {
        val festivals = listOf(makeFestivalOccurrence())
        val panchangDay = makePanchangDay(festivals = festivals)
        assertTrue("hasFestivals should be true when festivals present", panchangDay.hasFestivals)
    }

    @Test
    fun `hasFestivals returns true for multiple festivals`() {
        val festivals = listOf(
            makeFestivalOccurrence(FestivalCategory.MAJOR),
            makeFestivalOccurrence(FestivalCategory.MODERATE)
        )
        val panchangDay = makePanchangDay(festivals = festivals)
        assertTrue("hasFestivals should be true for multiple festivals", panchangDay.hasFestivals)
    }

    // ==================== hasMajorFestival ====================

    @Test
    fun `hasMajorFestival returns true when MAJOR category festival exists`() {
        val festivals = listOf(makeFestivalOccurrence(FestivalCategory.MAJOR))
        val panchangDay = makePanchangDay(festivals = festivals)
        assertTrue("hasMajorFestival should be true for MAJOR festival", panchangDay.hasMajorFestival)
    }

    @Test
    fun `hasMajorFestival returns false when only MODERATE festivals exist`() {
        val festivals = listOf(makeFestivalOccurrence(FestivalCategory.MODERATE))
        val panchangDay = makePanchangDay(festivals = festivals)
        assertFalse("hasMajorFestival should be false for MODERATE festival", panchangDay.hasMajorFestival)
    }

    @Test
    fun `hasMajorFestival returns false when only RECURRING festivals exist`() {
        val festivals = listOf(makeFestivalOccurrence(FestivalCategory.RECURRING))
        val panchangDay = makePanchangDay(festivals = festivals)
        assertFalse("hasMajorFestival should be false for RECURRING festival", panchangDay.hasMajorFestival)
    }

    @Test
    fun `hasMajorFestival returns false when no festivals`() {
        val panchangDay = makePanchangDay(festivals = emptyList())
        assertFalse("hasMajorFestival should be false when no festivals", panchangDay.hasMajorFestival)
    }

    @Test
    fun `hasMajorFestival returns true when mixed categories including MAJOR`() {
        val festivals = listOf(
            makeFestivalOccurrence(FestivalCategory.VRAT),
            makeFestivalOccurrence(FestivalCategory.MAJOR),
            makeFestivalOccurrence(FestivalCategory.REGIONAL)
        )
        val panchangDay = makePanchangDay(festivals = festivals)
        assertTrue("hasMajorFestival should be true when MAJOR is among mixed categories",
            panchangDay.hasMajorFestival)
    }

    // ==================== PanchangElement ====================

    @Test
    fun `PanchangElement timeRangeString returns empty when times are null`() {
        val element = PanchangElement(name = "Test", number = 1)
        assertEquals("timeRangeString should be empty when times are null", "", element.timeRangeString)
    }

    @Test
    fun `PanchangElement timeRangeString formats correctly when times are set`() {
        val start = LocalDateTime.of(2025, 3, 14, 6, 30)
        val end = LocalDateTime.of(2025, 3, 14, 18, 45)
        val element = PanchangElement(
            name = "Purnima",
            number = 15,
            startTime = start,
            endTime = end
        )
        val result = element.timeRangeString
        assertTrue("timeRangeString should contain start time", result.contains("6:30"))
        assertTrue("timeRangeString should contain end time", result.contains("6:45"))
        assertTrue("timeRangeString should contain AM/PM markers", result.contains("AM") || result.contains("PM"))
    }

    @Test
    fun `PanchangElement timeRangeString returns empty when only startTime set`() {
        val element = PanchangElement(
            name = "Test",
            number = 1,
            startTime = LocalDateTime.of(2025, 3, 14, 6, 30),
            endTime = null
        )
        assertEquals("", element.timeRangeString)
    }

    // ==================== TimePeriod ====================

    @Test
    fun `TimePeriod displayString formats correctly`() {
        val period = TimePeriod(
            name = "Rahu Kaal",
            start = LocalDateTime.of(2025, 3, 14, 9, 0),
            end = LocalDateTime.of(2025, 3, 14, 10, 30)
        )
        val result = period.displayString
        assertTrue("displayString should contain start time", result.contains("9:00"))
        assertTrue("displayString should contain end time", result.contains("10:30"))
        assertTrue("displayString should contain AM", result.contains("AM"))
    }
}
