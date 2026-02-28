package dev.gopes.hinducalendar.feature.texts.reader.content

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.gopes.hinducalendar.R
import dev.gopes.hinducalendar.domain.model.AppLanguage
import dev.gopes.hinducalendar.domain.model.JainPrayersData
import dev.gopes.hinducalendar.feature.texts.reader.AudioUiState
import dev.gopes.hinducalendar.core.ui.components.SacredCard
import dev.gopes.hinducalendar.core.ui.components.SacredHighlightCard
import dev.gopes.hinducalendar.feature.texts.reader.components.CollapsibleExplanation
import dev.gopes.hinducalendar.feature.texts.reader.components.MiniAudioProgressBar
import dev.gopes.hinducalendar.feature.texts.reader.components.VerseAudioButton
import dev.gopes.hinducalendar.feature.texts.reader.components.VerseCard

@Composable
internal fun JainContent(data: JainPrayersData, lang: AppLanguage, modifier: Modifier, isBookmarked: (String) -> Boolean, onToggleBookmark: (String, String, String) -> Unit, audio: AudioUiState? = null) {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier.fillMaxSize()
    ) {
        // Namokar Mantra
        data.namokarMantra?.let { nm ->
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        stringResource(R.string.reader_namokar_mantra),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (audio != null) {
                        Spacer(Modifier.width(8.dp))
                        VerseAudioButton(audioId = "jain_namokar", audio = audio)
                    }
                }
                Spacer(Modifier.height(8.dp))
                SacredHighlightCard {
                    if (audio != null) {
                        MiniAudioProgressBar(audioId = "jain_namokar", audio = audio)
                    }
                    Text(nm.sanskrit, style = MaterialTheme.typography.bodyLarge, lineHeight = 28.sp)
                    nm.transliteration?.let {
                        Spacer(Modifier.height(4.dp))
                        Text(it, style = MaterialTheme.typography.bodySmall,
                            fontStyle = FontStyle.Italic, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(Modifier.height(8.dp))
                    Divider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(Modifier.height(8.dp))
                    Text(nm.translation(lang), style = MaterialTheme.typography.bodyMedium, lineHeight = 22.sp)

                    val desc = nm.description(lang)
                    if (desc.isNotBlank()) {
                        Spacer(Modifier.height(8.dp))
                        CollapsibleExplanation(text = desc)
                    }
                }
            }

            // Line-by-line breakdown
            if (nm.lineByLine.isNotEmpty()) {
                items(nm.lineByLine, key = { "namokar_${it.line}" }) { line ->
                    val ref = "${stringResource(R.string.text_line)} ${line.line}"
                    VerseCard(
                        badge = ref,
                        originalText = line.sanskrit,
                        transliteration = line.transliteration,
                        translation = line.translation(lang),
                        explanation = line.significance(lang).ifEmpty { null },
                        isBookmarked = isBookmarked(ref),
                        onBookmarkToggle = { onToggleBookmark(ref, line.sanskrit, line.translation(lang)) }
                    )
                }
            }
        }

        // Mahavira Teachings
        if (data.mahaviraTeachings.isNotEmpty()) {
            item {
                Spacer(Modifier.height(8.dp))
                Text(
                    stringResource(R.string.reader_teachings),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            items(data.mahaviraTeachings, key = { "teaching_${it.episode}" }) { teaching ->
                SacredCard {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            "${stringResource(R.string.text_episode)} ${teaching.episode}",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        teaching.title(lang),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        teaching.content(lang),
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 22.sp
                    )

                    val quote = teaching.keyQuote(lang)
                    if (quote.isNotBlank()) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "\u201C$quote\u201D",
                            style = MaterialTheme.typography.bodySmall,
                            fontStyle = FontStyle.Italic,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }

                    val lesson = teaching.lesson(lang)
                    if (lesson.isNotBlank()) {
                        Spacer(Modifier.height(8.dp))
                        CollapsibleExplanation(text = lesson)
                    }
                }
            }
        }
    }
}
