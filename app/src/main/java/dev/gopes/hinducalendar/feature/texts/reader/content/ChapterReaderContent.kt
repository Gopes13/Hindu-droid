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
import dev.gopes.hinducalendar.domain.model.ChapterTextData
import dev.gopes.hinducalendar.feature.texts.reader.AudioUiState
import dev.gopes.hinducalendar.core.ui.components.SacredCard
import dev.gopes.hinducalendar.core.ui.components.SacredHighlightCard
import dev.gopes.hinducalendar.feature.texts.reader.components.MiniAudioProgressBar
import dev.gopes.hinducalendar.feature.texts.reader.components.VerseAudioButton

@Composable
internal fun ChapterContent(data: ChapterTextData, lang: AppLanguage, modifier: Modifier, audio: AudioUiState? = null) {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier.fillMaxSize()
    ) {
        // Kavach
        data.kavach?.let { kav ->
            item {
                SacredHighlightCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            stringResource(R.string.reader_kavach),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (audio != null) {
                            Spacer(Modifier.weight(1f))
                            VerseAudioButton(audioId = "devi_mahatmya_kavach", audio = audio)
                        }
                    }
                    if (audio != null) {
                        MiniAudioProgressBar(audioId = "devi_mahatmya_kavach", audio = audio)
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(kav.sanskrit, style = MaterialTheme.typography.bodyLarge, lineHeight = 28.sp)
                    kav.transliteration?.let {
                        Spacer(Modifier.height(4.dp))
                        Text(it, style = MaterialTheme.typography.bodySmall,
                            fontStyle = FontStyle.Italic, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(Modifier.height(8.dp))
                    Divider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(Modifier.height(8.dp))
                    Text(kav.translation(lang), style = MaterialTheme.typography.bodyMedium, lineHeight = 22.sp)
                }
            }
        }

        items(data.chapters, key = { it.chapter }) { chapter ->
            SacredCard {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        "${stringResource(R.string.text_chapter)} ${chapter.chapter}",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    chapter.title(lang),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    chapter.summary(lang),
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 22.sp
                )

                // Key verses
                chapter.keyVerses?.forEachIndexed { idx, kv ->
                    Spacer(Modifier.height(12.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(Modifier.height(12.dp))
                    if (audio != null) {
                        val kvAudioId = "devi_mahatmya_ch_${chapter.chapter}_kv_${idx + 1}"
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Spacer(Modifier.weight(1f))
                            VerseAudioButton(audioId = kvAudioId, audio = audio)
                        }
                        MiniAudioProgressBar(audioId = kvAudioId, audio = audio)
                    }
                    Text(kv.sanskrit, style = MaterialTheme.typography.bodyMedium, lineHeight = 24.sp)
                    kv.transliteration?.let {
                        Spacer(Modifier.height(4.dp))
                        Text(it, style = MaterialTheme.typography.bodySmall,
                            fontStyle = FontStyle.Italic, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(kv.translation(lang), style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
