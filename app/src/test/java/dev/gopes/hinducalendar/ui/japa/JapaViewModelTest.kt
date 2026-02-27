package dev.gopes.hinducalendar.feature.japa

import dev.gopes.hinducalendar.domain.model.GamificationData
import dev.gopes.hinducalendar.domain.model.JapaState
import dev.gopes.hinducalendar.domain.model.MalaMaterial
import dev.gopes.hinducalendar.domain.model.MantraSelection
import dev.gopes.hinducalendar.domain.model.PresetMantra
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
class JapaViewModelTest {

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
    fun `initial state loads japa state from preferences`() = runTest(testDispatcher) {
        val initial = JapaState(currentBead = 5, roundsToday = 2, totalRoundsLifetime = 50)
        val repo = FakePreferencesRepository(UserPreferences(japaState = initial))
        val vm = JapaViewModel(repo, FakeGamificationService())
        advanceUntilIdle()

        assertEquals(5, vm.japaState.value.currentBead)
        assertEquals(50, vm.japaState.value.totalRoundsLifetime)
    }

    @Test
    fun `advanceBead increments current bead by 1`() = runTest(testDispatcher) {
        val repo = FakePreferencesRepository()
        val vm = JapaViewModel(repo, FakeGamificationService())
        advanceUntilIdle()

        vm.advanceBead()
        advanceUntilIdle()

        assertEquals(1, vm.japaState.value.currentBead)
    }

    @Test
    fun `108 bead advances complete one round`() = runTest(testDispatcher) {
        val repo = FakePreferencesRepository()
        val vm = JapaViewModel(repo, FakeGamificationService())
        advanceUntilIdle()

        repeat(108) { vm.advanceBead() }
        advanceUntilIdle()

        assertEquals(0, vm.japaState.value.currentBead)
        assertEquals(1, vm.japaState.value.roundsToday)
        assertEquals(1, vm.japaState.value.totalRoundsLifetime)
        assertTrue(vm.showRoundComplete)
    }

    @Test
    fun `dismissRoundComplete clears the flag`() = runTest(testDispatcher) {
        val repo = FakePreferencesRepository()
        val vm = JapaViewModel(repo, FakeGamificationService())
        advanceUntilIdle()

        repeat(108) { vm.advanceBead() }
        advanceUntilIdle()
        assertTrue(vm.showRoundComplete)

        vm.dismissRoundComplete()
        assertFalse(vm.showRoundComplete)
    }

    @Test
    fun `selectMaterial persists to preferences`() = runTest(testDispatcher) {
        val repo = FakePreferencesRepository()
        val vm = JapaViewModel(repo, FakeGamificationService())
        advanceUntilIdle()

        vm.selectMaterial(MalaMaterial.CRYSTAL)
        advanceUntilIdle()

        assertEquals(MalaMaterial.CRYSTAL, vm.japaState.value.selectedMaterial)
        assertEquals(MalaMaterial.CRYSTAL.name, repo.current.japaState.selectedMaterialName)
    }

    @Test
    fun `selectMantra with preset persists correctly`() = runTest(testDispatcher) {
        val repo = FakePreferencesRepository()
        val vm = JapaViewModel(repo, FakeGamificationService())
        advanceUntilIdle()

        vm.selectMantra(MantraSelection.Preset(PresetMantra.GAYATRI))
        advanceUntilIdle()

        assertEquals(PresetMantra.GAYATRI.name, repo.current.japaState.selectedMantraPresetName)
    }

    @Test
    fun `selectMantra with custom text persists correctly`() = runTest(testDispatcher) {
        val repo = FakePreferencesRepository()
        val vm = JapaViewModel(repo, FakeGamificationService())
        advanceUntilIdle()

        vm.selectMantra(MantraSelection.Custom("Om Sai Ram"))
        advanceUntilIdle()

        assertEquals("Om Sai Ram", repo.current.japaState.customMantraText)
    }

    @Test
    fun `round completion with gamification enabled awards points`() = runTest(testDispatcher) {
        val repo = FakePreferencesRepository(
            UserPreferences(gamificationData = GamificationData(isEnabled = true))
        )
        val vm = JapaViewModel(repo, FakeGamificationService())
        advanceUntilIdle()

        repeat(108) { vm.advanceBead() }
        advanceUntilIdle()

        // 10 PP per round
        assertTrue(repo.current.gamificationData.totalPunyaPoints >= 10)
    }

    @Test
    fun `round completion with gamification disabled does not award points`() = runTest(testDispatcher) {
        val repo = FakePreferencesRepository(
            UserPreferences(gamificationData = GamificationData(isEnabled = false))
        )
        val vm = JapaViewModel(repo, FakeGamificationService())
        advanceUntilIdle()

        repeat(108) { vm.advanceBead() }
        advanceUntilIdle()

        assertEquals(0, repo.current.gamificationData.totalPunyaPoints)
    }

    @Test
    fun `multiple rounds accumulate correctly`() = runTest(testDispatcher) {
        val repo = FakePreferencesRepository()
        val vm = JapaViewModel(repo, FakeGamificationService())
        advanceUntilIdle()

        repeat(108 * 3) { vm.advanceBead() }
        advanceUntilIdle()

        assertEquals(3, vm.japaState.value.roundsToday)
        assertEquals(3, vm.japaState.value.totalRoundsLifetime)
    }
}
