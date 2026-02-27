package dev.gopes.hinducalendar.feature.texts.bookmarks

import dev.gopes.hinducalendar.domain.model.SacredTextType
import dev.gopes.hinducalendar.domain.model.UserPreferences
import dev.gopes.hinducalendar.domain.model.VerseBookmark
import dev.gopes.hinducalendar.data.repository.FakePreferencesRepository
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
class BookmarksViewModelTest {

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
    fun `initial state loads bookmarks from preferences`() = runTest(testDispatcher) {
        val bookmark = VerseBookmark(
            textType = SacredTextType.GITA,
            verseReference = "1.1",
            verseText = "dharma-kshetre",
            translation = "In the holy field"
        )
        val prefs = UserPreferences(
            bookmarks = dev.gopes.hinducalendar.domain.model.BookmarkCollection().add(bookmark)
        )
        val repo = FakePreferencesRepository(prefs)
        val vm = BookmarksViewModel(repo)
        advanceUntilIdle()

        assertEquals(1, vm.bookmarks.value.bookmarks.size)
    }

    @Test
    fun `toggleBookmark adds new bookmark`() = runTest(testDispatcher) {
        val repo = FakePreferencesRepository()
        val vm = BookmarksViewModel(repo)
        advanceUntilIdle()

        vm.toggleBookmark(SacredTextType.GITA, "2.47", "karmany evadhikaras te", "You have a right to action")
        advanceUntilIdle()

        assertEquals(1, vm.bookmarks.value.bookmarks.size)
        assertTrue(vm.isBookmarked(SacredTextType.GITA, "2.47"))
    }

    @Test
    fun `toggleBookmark removes existing bookmark`() = runTest(testDispatcher) {
        val repo = FakePreferencesRepository()
        val vm = BookmarksViewModel(repo)
        advanceUntilIdle()

        vm.toggleBookmark(SacredTextType.GITA, "2.47", "karmany evadhikaras te", "You have a right to action")
        advanceUntilIdle()
        assertTrue(vm.isBookmarked(SacredTextType.GITA, "2.47"))

        vm.toggleBookmark(SacredTextType.GITA, "2.47", "karmany evadhikaras te", "You have a right to action")
        advanceUntilIdle()
        assertFalse(vm.isBookmarked(SacredTextType.GITA, "2.47"))
    }

    @Test
    fun `removeBookmark removes by ID`() = runTest(testDispatcher) {
        val repo = FakePreferencesRepository()
        val vm = BookmarksViewModel(repo)
        advanceUntilIdle()

        vm.toggleBookmark(SacredTextType.GITA, "1.1", "dharma-kshetre", "In the holy field")
        advanceUntilIdle()
        val id = vm.bookmarks.value.bookmarks.first().id

        vm.removeBookmark(id)
        advanceUntilIdle()
        assertEquals(0, vm.bookmarks.value.bookmarks.size)
    }

    @Test
    fun `updateNote persists note`() = runTest(testDispatcher) {
        val repo = FakePreferencesRepository()
        val vm = BookmarksViewModel(repo)
        advanceUntilIdle()

        vm.toggleBookmark(SacredTextType.GITA, "1.1", "dharma-kshetre", "In the holy field")
        advanceUntilIdle()
        val id = vm.bookmarks.value.bookmarks.first().id

        vm.updateNote(id, "My reflection")
        advanceUntilIdle()

        assertEquals("My reflection", vm.bookmarks.value.bookmarks.first().note)
        assertEquals("My reflection", repo.current.bookmarks.bookmarks.first().note)
    }

    @Test
    fun `bookmarks are reactive — changes persist to repository`() = runTest(testDispatcher) {
        val repo = FakePreferencesRepository()
        val vm = BookmarksViewModel(repo)
        advanceUntilIdle()

        vm.toggleBookmark(SacredTextType.HANUMAN_CHALISA, "3", "Sanskrit text", "Translation")
        advanceUntilIdle()

        assertEquals(1, repo.current.bookmarks.bookmarks.size)
        assertEquals(SacredTextType.HANUMAN_CHALISA, repo.current.bookmarks.bookmarks.first().textType)
    }
}
