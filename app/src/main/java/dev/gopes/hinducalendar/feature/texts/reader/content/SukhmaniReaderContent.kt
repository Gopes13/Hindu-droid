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
import dev.gopes.hinducalendar.domain.model.SukhmaniData
import dev.gopes.hinducalendar.feature.texts.reader.AudioUiState
import dev.gopes.hinducalendar.core.ui.components.SacredHighlightCard
import dev.gopes.hinducalendar.feature.texts.reader.components.MiniAudioProgressBar
import dev.gopes.hinducalendar.feature.texts.reader.components.VerseAudioButton
import dev.gopes.hinducalendar.feature.texts.reader.components.VerseCard

@Composable
internal fun SukhmaniContent(data: SukhmaniData, lang: AppLanguage, modifier: Modifier, isBookmarked: (String) -> Boolean, onToggleBookmark: (String, String, String) -> Unit, audio: AudioUiState? = null) {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier.fillMaxSize()
    ) {
        items(data.ashtpadis, key = { it.ashtpadi }) { section ->
            // Section header
            Text(
                "${stringResource(R.string.text_ashtpadi)} ${section.ashtpadi}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            val summary = section.summary(lang)
            if (summary.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    summary,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Salok
            section.salok?.let { salok ->
                Spacer(Modifier.height(8.dp))
                val salokAudioId = "sukhmani_${section.ashtpadi}_salok"
                SacredHighlightCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            stringResource(R.string.reader_salok),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (audio != null) {
                            Spacer(Modifier.weight(1f))
                            VerseAudioButton(audioId = salokAudioId, audio = audio)
                        }
                    }
                    if (audio != null) {
                        MiniAudioProgressBar(audioId = salokAudioId, audio = audio)
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(salok.punjabi, style = MaterialTheme.typography.bodyLarge, lineHeight = 28.sp)
                    Spacer(Modifier.height(4.dp))
                    Text(salok.transliteration, style = MaterialTheme.typography.bodySmall,
                        fontStyle = FontStyle.Italic, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(8.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(Modifier.height(8.dp))
                    Text(salok.translation(lang), style = MaterialTheme.typography.bodyMedium, lineHeight = 22.sp)
                }
            }

            // Stanzas
            section.stanzas.forEach { stanza ->
                Spacer(Modifier.height(8.dp))
                val ref = "${section.ashtpadi}.${stanza.stanza}"
                VerseCard(
                    badge = ref,
                    originalText = stanza.punjabi,
                    transliteration = stanza.transliteration,
                    translation = stanza.translation(lang),
                    isBookmarked = isBookmarked(ref),
                    onBookmarkToggle = { onToggleBookmark(ref, stanza.punjabi, stanza.translation(lang)) },
                    audioId = "sukhmani_${section.ashtpadi}_stanza_${stanza.stanza}",
                    audio = audio
                )
            }
        }
    }
}
