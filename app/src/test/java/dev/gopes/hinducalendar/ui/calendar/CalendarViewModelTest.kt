package dev.gopes.hinducalendar.feature.calendar

import androidx.lifecycle.viewModelScope
import dev.gopes.hinducalendar.domain.model.PanchangDay
import dev.gopes.hinducalendar.data.repository.FakePreferencesRepository
import dev.gopes.hinducalendar.domain.repository.PanchangRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.YearMonth

@OptIn(ExperimentalCoroutinesApi::class)
class CalendarViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var mockPanchangRepository: PanchangRepository
    private val mockPanchangDay: PanchangDay = mock()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mockPanchangRepository = mock()
        whenever(mockPanchangRepository.computeMonthlyPanchang(any(), any(), any(), any()))
            .thenReturn(emptyList())
        whenever(mockPanchangRepository.computePanchang(any(), any(), any()))
            .thenReturn(mockPanchangDay)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial displayedMonth is current month`() = runTest(testDispatcher) {
        val repo = FakePreferencesRepository()
        val vm = CalendarViewModel(mockPanchangRepository, repo)

        assertEquals(YearMonth.now(), vm.displayedMonth.value)
        vm.viewModelScope.cancel()
    }

    @Test
    fun `previousMonth decrements by one month`() = runTest(testDispatcher) {
        val repo = FakePreferencesRepository()
        val vm = CalendarViewModel(mockPanchangRepository, repo)

        val expected = YearMonth.now().minusMonths(1)
        vm.previousMonth()

        assertEquals(expected, vm.displayedMonth.value)
        vm.viewModelScope.cancel()
    }

    @Test
    fun `nextMonth increments by one month`() = runTest(testDispatcher) {
        val repo = FakePreferencesRepository()
        val vm = CalendarViewModel(mockPanchangRepository, repo)

        val expected = YearMonth.now().plusMonths(1)
        vm.nextMonth()

        assertEquals(expected, vm.displayedMonth.value)
        vm.viewModelScope.cancel()
    }

    @Test
    fun `multiple month navigations accumulate correctly`() = runTest(testDispatcher) {
        val repo = FakePreferencesRepository()
        val vm = CalendarViewModel(mockPanchangRepository, repo)

        vm.nextMonth()
        vm.nextMonth()
        vm.previousMonth()

        assertEquals(YearMonth.now().plusMonths(1), vm.displayedMonth.value)
        vm.viewModelScope.cancel()
    }

    @Test
    fun `language defaults to ENGLISH`() = runTest(testDispatcher) {
        val repo = FakePreferencesRepository()
        val vm = CalendarViewModel(mockPanchangRepository, repo)

        assertEquals(dev.gopes.hinducalendar.domain.model.AppLanguage.ENGLISH, vm.language.value)
        vm.viewModelScope.cancel()
    }
}
