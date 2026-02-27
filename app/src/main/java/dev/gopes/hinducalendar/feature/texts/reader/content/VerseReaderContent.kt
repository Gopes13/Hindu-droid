package dev.gopes.hinducalendar.feature.texts.reader.content

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.gopes.hinducalendar.R
import dev.gopes.hinducalendar.domain.model.AppLanguage
import dev.gopes.hinducalendar.domain.model.VerseTextData
import dev.gopes.hinducalendar.feature.texts.reader.AudioUiState
import dev.gopes.hinducalendar.feature.texts.reader.components.VerseCard

@Composable
internal fun NumberedVerseContent(data: VerseTextData, lang: AppLanguage, modifier: Modifier, isBookmarked: (String) -> Boolean, onToggleBookmark: (String, String, String) -> Unit, audio: AudioUiState? = null) {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier.fillMaxSize()
    ) {
        items(data.verses, key = { it.verse }) { verse ->
            val theme = verse.theme(lang).ifEmpty { null }
            val ref = "${stringResource(R.string.text_verse)} ${verse.verse}"
            VerseCard(
                badge = ref,
                originalText = verse.sanskrit,
                transliteration = verse.transliteration,
                translation = verse.translation(lang),
                explanation = theme,
                isBookmarked = isBookmarked(ref),
                onBookmarkToggle = { onToggleBookmark(ref, verse.sanskrit, verse.translation(lang)) },
                audioId = "soundarya_${verse.verse}",
                audio = audio
            )
        }
    }
}
