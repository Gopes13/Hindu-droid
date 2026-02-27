package dev.gopes.hinducalendar.feature.texts.reader.content

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.gopes.hinducalendar.R
import dev.gopes.hinducalendar.domain.model.AppLanguage
import dev.gopes.hinducalendar.domain.model.SutraTextData
import dev.gopes.hinducalendar.feature.texts.reader.AudioUiState
import dev.gopes.hinducalendar.feature.texts.reader.components.VerseCard

@Composable
internal fun SutraContent(data: SutraTextData, lang: AppLanguage, modifier: Modifier, isBookmarked: (String) -> Boolean, onToggleBookmark: (String, String, String) -> Unit, audio: AudioUiState? = null) {
    var selectedChapter by remember { mutableIntStateOf(0) }

    Column(modifier.fillMaxSize()) {
        // Chapter picker
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            items(data.chapters.size) { index ->
                val ch = data.chapters[index]
                val isSelected = index == selectedChapter
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { selectedChapter = index },
                    color = if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            stringResource(R.string.text_chapter_short, ch.chapter),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Chapter title
        data.chapters.getOrNull(selectedChapter)?.let { ch ->
            Text(
                ch.title(lang),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
        }

        // Sutras
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            data.chapters.getOrNull(selectedChapter)?.sutras?.let { sutras ->
                items(sutras, key = { it.sutra }) { sutra ->
                    val ch = data.chapters[selectedChapter]
                    val ref = "${ch.chapter}.${sutra.sutra}"
                    VerseCard(
                        badge = ref,
                        originalText = sutra.sanskrit,
                        transliteration = sutra.transliteration,
                        translation = sutra.translation(lang),
                        explanation = sutra.commentary(lang).ifEmpty { null },
                        isBookmarked = isBookmarked(ref),
                        onBookmarkToggle = { onToggleBookmark(ref, sutra.sanskrit, sutra.translation(lang)) },
                        audioId = "tattvartha_${ch.chapter}_${sutra.sutra}",
                        audio = audio
                    )
                }
            }
        }
    }
}
