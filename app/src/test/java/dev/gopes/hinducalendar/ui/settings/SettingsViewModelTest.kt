package dev.gopes.hinducalendar.feature.settings

import dev.gopes.hinducalendar.domain.model.*
import dev.gopes.hinducalendar.data.repository.FakePreferencesRepository
import dev.gopes.hinducalendar.domain.repository.PanchangRepository
import dev.gopes.hinducalendar.data.service.CalendarSyncService
import dev.gopes.hinducalendar.data.service.NotificationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val mockCalendarSync: CalendarSyncService = mock()
    private val mockPanchang: PanchangRepository = mock()
    private val mockNotificationHelper: NotificationHelper = mock()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createVm(
        prefs: UserPreferences = UserPreferences()
    ): Pair<SettingsViewModel, FakePreferencesRepository> {
        val repo = FakePreferencesRepository(prefs)
        val vm = SettingsViewModel(repo, mockCalendarSync, mockPanchang, mockNotificationHelper)
        return vm to repo
    }

    @Test
    fun `updateDharmaPath persists to preferences`() = runTest(testDispatcher) {
        val (vm, repo) = createVm()
        advanceUntilIdle()

        vm.updateDharmaPath(DharmaPath.SHAIV)
        advanceUntilIdle()

        assertEquals(DharmaPath.SHAIV, repo.current.dharmaPath)
    }

    @Test
    fun `updateTradition persists to preferences`() = runTest(testDispatcher) {
        val (vm, repo) = createVm()
        advanceUntilIdle()

        vm.updateTradition(CalendarTradition.AMANT)
        advanceUntilIdle()

        assertEquals(CalendarTradition.AMANT, repo.current.tradition)
    }

    @Test
    fun `updateSyncEnabled persists to preferences`() = runTest(testDispatcher) {
        val (vm, repo) = createVm()
        advanceUntilIdle()

        vm.updateSyncEnabled(false)
        advanceUntilIdle()

        assertFalse(repo.current.syncToCalendar)
    }

    @Test
    fun `updateSyncOption persists to preferences`() = runTest(testDispatcher) {
        val (vm, repo) = createVm()
        advanceUntilIdle()

        vm.updateSyncOption(CalendarSyncOption.FULL_PANCHANG)
        advanceUntilIdle()

        assertEquals(CalendarSyncOption.FULL_PANCHANG, repo.current.syncOption)
    }

    @Test
    fun `updateNotificationsEnabled persists to preferences`() = runTest(testDispatcher) {
        val (vm, repo) = createVm()
        advanceUntilIdle()

        vm.updateNotificationsEnabled(false)
        advanceUntilIdle()

        assertFalse(repo.current.notificationsEnabled)
    }

    @Test
    fun `toggleReminderTiming adds timing`() = runTest(testDispatcher) {
        val (vm, repo) = createVm(UserPreferences(reminderTimings = emptyList()))
        advanceUntilIdle()

        vm.toggleReminderTiming(ReminderTiming.MORNING_OF, true)
        advanceUntilIdle()

        assertTrue(ReminderTiming.MORNING_OF in repo.current.reminderTimings)
    }

    @Test
    fun `toggleReminderTiming removes timing`() = runTest(testDispatcher) {
        val (vm, repo) = createVm(
            UserPreferences(reminderTimings = listOf(ReminderTiming.DAY_BEFORE, ReminderTiming.MORNING_OF))
        )
        advanceUntilIdle()

        vm.toggleReminderTiming(ReminderTiming.MORNING_OF, false)
        advanceUntilIdle()

        assertFalse(ReminderTiming.MORNING_OF in repo.current.reminderTimings)
        assertTrue(ReminderTiming.DAY_BEFORE in repo.current.reminderTimings)
    }

    @Test
    fun `updateActiveWisdomText persists to preferences`() = runTest(testDispatcher) {
        val (vm, repo) = createVm()
        advanceUntilIdle()

        vm.updateActiveWisdomText(SacredTextType.GITA)
        advanceUntilIdle()

        assertEquals(SacredTextType.GITA, repo.current.activeWisdomText)
    }

    @Test
    fun `updateActiveWisdomText with null clears selection`() = runTest(testDispatcher) {
        val (vm, repo) = createVm(UserPreferences(activeWisdomText = SacredTextType.GITA))
        advanceUntilIdle()

        vm.updateActiveWisdomText(null)
        advanceUntilIdle()

        assertNull(repo.current.activeWisdomText)
    }

    @Test
    fun `updateLocation persists to preferences`() = runTest(testDispatcher) {
        val (vm, repo) = createVm()
        advanceUntilIdle()

        vm.updateLocation(HinduLocation.MUMBAI)
        advanceUntilIdle()

        assertEquals(HinduLocation.MUMBAI, repo.current.location)
    }

    @Test
    fun `updateFestivalDateReference persists to preferences`() = runTest(testDispatcher) {
        val (vm, repo) = createVm()
        advanceUntilIdle()

        vm.updateFestivalDateReference(FestivalDateReference.USER_LOCATION)
        advanceUntilIdle()

        assertEquals(FestivalDateReference.USER_LOCATION, repo.current.festivalDateReference)
    }

    @Test
    fun `updateNotificationTime persists to preferences`() = runTest(testDispatcher) {
        val (vm, repo) = createVm()
        advanceUntilIdle()

        vm.updateNotificationTime(8, 30)
        advanceUntilIdle()

        assertEquals(8, repo.current.notificationTime.hour)
        assertEquals(30, repo.current.notificationTime.minute)
    }

    @Test
    fun `resetReadingProgress clears progress`() = runTest(testDispatcher) {
        val (vm, repo) = createVm(
            UserPreferences(readingProgress = ReadingProgress(gitaChapter = 5))
        )
        advanceUntilIdle()

        vm.resetReadingProgress()
        advanceUntilIdle()

        assertEquals(ReadingProgress(), repo.current.readingProgress)
    }

    @Test
    fun `preferences flow exposes current state`() = runTest(testDispatcher) {
        val (vm, _) = createVm(UserPreferences(language = AppLanguage.HINDI))

        // Subscribe to trigger WhileSubscribed sharing
        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            vm.preferences.collect {}
        }
        advanceUntilIdle()

        assertEquals(AppLanguage.HINDI, vm.preferences.value.language)
        job.cancel()
    }
}
