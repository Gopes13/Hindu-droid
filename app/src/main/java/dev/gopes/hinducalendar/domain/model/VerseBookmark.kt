package dev.gopes.hinducalendar.domain.model

import java.time.LocalDate
import java.util.UUID

data class VerseBookmark(
    val id: String = UUID.randomUUID().toString(),
    val textType: SacredTextType,
    val verseReference: String,
    val verseText: String,
    val translation: String,
    val note: String? = null,
    val dateBookmarked: String = LocalDate.now().toString()
)

data class BookmarkCollection(
    val bookmarks: List<VerseBookmark> = emptyList()
) {
    fun add(bookmark: VerseBookmark): BookmarkCollection =
        copy(bookmarks = bookmarks + bookmark)

    fun remove(id: String): BookmarkCollection =
        copy(bookmarks = bookmarks.filter { it.id != id })

    fun updateNote(id: String, note: String?): BookmarkCollection =
        copy(bookmarks = bookmarks.map { if (it.id == id) it.copy(note = note) else it })

    fun isBookmarked(textType: SacredTextType, reference: String): Boolean =
        bookmarks.any { it.textType == textType && it.verseReference == reference }

    fun findBookmark(textType: SacredTextType, reference: String): VerseBookmark? =
        bookmarks.find { it.textType == textType && it.verseReference == reference }

    fun forTextType(textType: SacredTextType): List<VerseBookmark> =
        bookmarks.filter { it.textType == textType }

    val groupedByTextType: Map<SacredTextType, List<VerseBookmark>>
        get() = bookmarks.groupBy { it.textType }
}
