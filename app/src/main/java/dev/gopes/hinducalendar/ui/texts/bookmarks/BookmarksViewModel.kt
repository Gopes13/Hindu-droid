package dev.gopes.hinducalendar.ui.texts.bookmarks

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.gopes.hinducalendar.data.model.BookmarkCollection
import dev.gopes.hinducalendar.data.model.SacredTextType
import dev.gopes.hinducalendar.data.model.VerseBookmark
import dev.gopes.hinducalendar.data.repository.PreferencesRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookmarksViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    var bookmarks by mutableStateOf(BookmarkCollection())
        private set

    init {
        loadBookmarks()
    }

    private fun loadBookmarks() {
        viewModelScope.launch {
            val prefs = preferencesRepository.preferencesFlow.first()
            bookmarks = prefs.bookmarks
        }
    }

    fun toggleBookmark(
        textType: SacredTextType,
        reference: String,
        verseText: String,
        translation: String
    ) {
        viewModelScope.launch {
            preferencesRepository.update { prefs ->
                val existing = prefs.bookmarks.findBookmark(textType, reference)
                val updated = if (existing != null) {
                    prefs.bookmarks.remove(existing.id)
                } else {
                    prefs.bookmarks.add(
                        VerseBookmark(
                            textType = textType,
                            verseReference = reference,
                            verseText = verseText,
                            translation = translation
                        )
                    )
                }
                prefs.copy(bookmarks = updated)
            }
            loadBookmarks()
        }
    }

    fun removeBookmark(id: String) {
        viewModelScope.launch {
            preferencesRepository.update { prefs ->
                prefs.copy(bookmarks = prefs.bookmarks.remove(id))
            }
            loadBookmarks()
        }
    }

    fun updateNote(id: String, note: String?) {
        viewModelScope.launch {
            preferencesRepository.update { prefs ->
                prefs.copy(bookmarks = prefs.bookmarks.updateNote(id, note))
            }
            loadBookmarks()
        }
    }

    fun isBookmarked(textType: SacredTextType, reference: String): Boolean =
        bookmarks.isBookmarked(textType, reference)
}
