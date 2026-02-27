package dev.gopes.hinducalendar.feature.diya

import dev.gopes.hinducalendar.domain.model.AppLanguage
import dev.gopes.hinducalendar.domain.model.DiyaState
import dev.gopes.hinducalendar.domain.model.GamificationData
import dev.gopes.hinducalendar.domain.model.UserPreferences
import dev.gopes.hinducalendar.data.repository.FakePreferencesRepository
import dev.gopes.hinducalendar.engine.FakeGamificationService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DiyaViewModelTest {

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
    fun `initial state resets daily if needed`() = runTest(testDispatcher) {
        val repo = FakePreferencesRepository()
        val vm = DiyaViewModel(repo, FakeGamificationService())
        advanceUntilIdle()

        assertFalse(vm.diyaState.isLitToday)
        assertEquals(0, vm.diyaState.lightingStreak)
    }

    @Test
    fun `lightDiya sets isLitToday and triggers confetti`() = runTest(testDispatcher) {
        val repo = FakePreferencesRepository()
        val vm = DiyaViewModel(repo, FakeGamificationService())
        advanceUntilIdle()

        vm.lightDiya()
        advanceUntilIdle()

        assertTrue(vm.diyaState.isLitToday)
        assertTrue(vm.showConfetti)
        assertEquals(1, vm.diyaState.totalDaysLit)
    }

    @Test
    fun `lightDiya persists state to repository`() = runTest(testDispatcher) {
        val repo = FakePreferencesRepository()
        val vm = DiyaViewModel(repo, FakeGamificationService())
        advanceUntilIdle()

        vm.lightDiya()
        advanceUntilIdle()

        assertTrue(repo.current.diyaState.isLitToday)
        assertEquals(1, repo.current.diyaState.totalDaysLit)
    }

    @Test
    fun `lightDiya awards gamification points`() = runTest(testDispatcher) {
        val prefs = UserPreferences(gamificationData = GamificationData(isEnabled = true))
        val repo = FakePreferencesRepository(prefs)
        val vm = DiyaViewModel(repo, FakeGamificationService())
        advanceUntilIdle()

        vm.lightDiya()
        advanceUntilIdle()

        assertTrue(repo.current.gamificationData.totalPunyaPoints > 0)
    }

    @Test
    fun `lightDiya is idempotent — second call is no-op`() = runTest(testDispatcher) {
        val prefs = UserPreferences(gamificationData = GamificationData(isEnabled = true))
        val repo = FakePreferencesRepository(prefs)
        val vm = DiyaViewModel(repo, FakeGamificationService())
        advanceUntilIdle()

        vm.lightDiya()
        advanceUntilIdle()
        val pointsAfterFirst = repo.current.gamificationData.totalPunyaPoints

        vm.lightDiya()
        advanceUntilIdle()

        assertEquals(pointsAfterFirst, repo.current.gamificationData.totalPunyaPoints)
        assertEquals(1, vm.diyaState.totalDaysLit)
    }

    @Test
    fun `dismissConfetti clears flag`() = runTest(testDispatcher) {
        val repo = FakePreferencesRepository()
        val vm = DiyaViewModel(repo, FakeGamificationService())
        advanceUntilIdle()

        vm.lightDiya()
        advanceUntilIdle()
        assertTrue(vm.showConfetti)

        vm.dismissConfetti()
        assertFalse(vm.showConfetti)
    }

    @Test
    fun `language is reactive from preferences`() = runTest(testDispatcher) {
        val prefs = UserPreferences(language = AppLanguage.HINDI)
        val repo = FakePreferencesRepository(prefs)
        val vm = DiyaViewModel(repo, FakeGamificationService())
        advanceUntilIdle()

        assertEquals(AppLanguage.HINDI, vm.language.value)

        repo.update { it.copy(language = AppLanguage.GUJARATI) }
        advanceUntilIdle()

        assertEquals(AppLanguage.GUJARATI, vm.language.value)
    }
}
