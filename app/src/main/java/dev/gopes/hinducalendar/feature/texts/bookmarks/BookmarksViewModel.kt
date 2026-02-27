package dev.gopes.hinducalendar.feature.texts.bookmarks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.gopes.hinducalendar.domain.model.BookmarkCollection
import dev.gopes.hinducalendar.domain.model.SacredTextType
import dev.gopes.hinducalendar.domain.model.VerseBookmark
import dev.gopes.hinducalendar.domain.repository.PreferencesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookmarksViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    val bookmarks: StateFlow<BookmarkCollection> = preferencesRepository.preferencesFlow
        .map { it.bookmarks }
        .stateIn(viewModelScope, SharingStarted.Eagerly, BookmarkCollection())

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
        }
    }

    fun removeBookmark(id: String) {
        viewModelScope.launch {
            preferencesRepository.update { prefs ->
                prefs.copy(bookmarks = prefs.bookmarks.remove(id))
            }
        }
    }

    fun updateNote(id: String, note: String?) {
        viewModelScope.launch {
            preferencesRepository.update { prefs ->
                prefs.copy(bookmarks = prefs.bookmarks.updateNote(id, note))
            }
        }
    }

    fun isBookmarked(textType: SacredTextType, reference: String): Boolean =
        bookmarks.value.isBookmarked(textType, reference)
}
