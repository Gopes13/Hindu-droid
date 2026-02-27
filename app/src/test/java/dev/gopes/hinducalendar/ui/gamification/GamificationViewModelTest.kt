package dev.gopes.hinducalendar.feature.gamification

import dev.gopes.hinducalendar.domain.model.GamificationData
import dev.gopes.hinducalendar.domain.model.StreakData
import dev.gopes.hinducalendar.domain.model.UserPreferences
import dev.gopes.hinducalendar.data.repository.FakePreferencesRepository
import dev.gopes.hinducalendar.engine.FakeGamificationService
import dev.gopes.hinducalendar.domain.service.GamificationService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GamificationViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state with gamification disabled does not award points`() = runTest(testDispatcher) {
        val repo = FakePreferencesRepository(
            UserPreferences(gamificationData = GamificationData(isEnabled = false))
        )
        val vm = GamificationViewModel(repo, FakeGamificationService())
        advanceUntilIdle()

        assertEquals(0, vm.gamificationData.value.totalPunyaPoints)
        assertNull(vm.dailyChallenge.value)
    }

    @Test
    fun `initial state with gamification enabled awards app open points`() = runTest(testDispatcher) {
        val repo = FakePreferencesRepository(
            UserPreferences(gamificationData = GamificationData(isEnabled = true))
        )
        val vm = GamificationViewModel(repo, FakeGamificationService())
        advanceUntilIdle()

        assertTrue(vm.gamificationData.value.totalPunyaPoints >= GamificationService.POINTS_APP_OPEN)
        assertNotNull(vm.dailyChallenge.value)
    }

    @Test
    fun `onChallengeAnswered correct awards challenge points`() = runTest(testDispatcher) {
        val repo = FakePreferencesRepository(
            UserPreferences(gamificationData = GamificationData(isEnabled = true))
        )
        val vm = GamificationViewModel(repo, FakeGamificationService())
        advanceUntilIdle()
        val pointsBefore = vm.gamificationData.value.totalPunyaPoints

        vm.onChallengeAnswered(true)
        advanceUntilIdle()

        assertEquals(pointsBefore + GamificationService.POINTS_CHALLENGE, vm.gamificationData.value.totalPunyaPoints)
    }

    @Test
    fun `onChallengeAnswered incorrect does not change points`() = runTest(testDispatcher) {
        val repo = FakePreferencesRepository(
            UserPreferences(gamificationData = GamificationData(isEnabled = true))
        )
        val vm = GamificationViewModel(repo, FakeGamificationService())
        advanceUntilIdle()
        val pointsBefore = vm.gamificationData.value.totalPunyaPoints

        vm.onChallengeAnswered(false)
        advanceUntilIdle()

        assertEquals(pointsBefore, vm.gamificationData.value.totalPunyaPoints)
    }

    @Test
    fun `recordVerseView awards verse view points`() = runTest(testDispatcher) {
        val repo = FakePreferencesRepository(
            UserPreferences(gamificationData = GamificationData(isEnabled = true))
        )
        val vm = GamificationViewModel(repo, FakeGamificationService())
        advanceUntilIdle()
        val pointsBefore = vm.gamificationData.value.totalPunyaPoints

        vm.recordVerseView()
        advanceUntilIdle()

        assertEquals(pointsBefore + GamificationService.POINTS_VERSE_VIEW, vm.gamificationData.value.totalPunyaPoints)
    }

    @Test
    fun `recordVerseRead awards verse read points`() = runTest(testDispatcher) {
        val repo = FakePreferencesRepository(
            UserPreferences(gamificationData = GamificationData(isEnabled = true))
        )
        val vm = GamificationViewModel(repo, FakeGamificationService())
        advanceUntilIdle()
        val pointsBefore = vm.gamificationData.value.totalPunyaPoints

        vm.recordVerseRead()
        advanceUntilIdle()

        assertEquals(pointsBefore + GamificationService.POINTS_VERSE_READ, vm.gamificationData.value.totalPunyaPoints)
    }

    @Test
    fun `recordExplanationView increments explanation count`() = runTest(testDispatcher) {
        val repo = FakePreferencesRepository(
            UserPreferences(gamificationData = GamificationData(isEnabled = true))
        )
        val vm = GamificationViewModel(repo, FakeGamificationService())
        advanceUntilIdle()

        vm.recordExplanationView()
        advanceUntilIdle()

        assertEquals(1, vm.gamificationData.value.versesExplained)
    }

    @Test
    fun `recordFestivalStoryRead tracks festival ID`() = runTest(testDispatcher) {
        val repo = FakePreferencesRepository(
            UserPreferences(gamificationData = GamificationData(isEnabled = true))
        )
        val vm = GamificationViewModel(repo, FakeGamificationService())
        advanceUntilIdle()

        vm.recordFestivalStoryRead("diwali")
        advanceUntilIdle()

        assertTrue("diwali" in vm.gamificationData.value.festivalStoriesRead)
    }

    @Test
    fun `toggleGamification enables and processes app open`() = runTest(testDispatcher) {
        val repo = FakePreferencesRepository(
            UserPreferences(gamificationData = GamificationData(isEnabled = false))
        )
        val vm = GamificationViewModel(repo, FakeGamificationService())
        advanceUntilIdle()
        assertEquals(0, vm.gamificationData.value.totalPunyaPoints)

        vm.toggleGamification(true)
        advanceUntilIdle()

        assertTrue(vm.gamificationData.value.isEnabled)
        assertTrue(vm.gamificationData.value.totalPunyaPoints > 0)
        assertNotNull(vm.dailyChallenge.value)
    }

    @Test
    fun `toggleGamification disables gamification`() = runTest(testDispatcher) {
        val repo = FakePreferencesRepository(
            UserPreferences(gamificationData = GamificationData(isEnabled = true))
        )
        val vm = GamificationViewModel(repo, FakeGamificationService())
        advanceUntilIdle()

        vm.toggleGamification(false)
        advanceUntilIdle()

        assertEquals(false, vm.gamificationData.value.isEnabled)
    }

    @Test
    fun `gamification data persists to repository`() = runTest(testDispatcher) {
        val repo = FakePreferencesRepository(
            UserPreferences(gamificationData = GamificationData(isEnabled = true))
        )
        val vm = GamificationViewModel(repo, FakeGamificationService())
        advanceUntilIdle()

        vm.recordVerseView()
        advanceUntilIdle()

        // Verify data was persisted
        assertTrue(repo.current.gamificationData.totalPunyaPoints > 0)
    }
}
