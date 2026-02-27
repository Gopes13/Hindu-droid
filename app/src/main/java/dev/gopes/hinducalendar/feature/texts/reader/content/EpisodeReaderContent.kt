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
import dev.gopes.hinducalendar.domain.model.EpisodeTextData
import dev.gopes.hinducalendar.feature.texts.reader.AudioUiState
import dev.gopes.hinducalendar.core.ui.components.SacredCard
import dev.gopes.hinducalendar.feature.texts.reader.components.CollapsibleExplanation
import dev.gopes.hinducalendar.feature.texts.reader.components.MiniAudioProgressBar
import dev.gopes.hinducalendar.feature.texts.reader.components.VerseAudioButton

@Composable
internal fun EpisodeContent(data: EpisodeTextData, lang: AppLanguage, modifier: Modifier, textFileName: String = "", audio: AudioUiState? = null) {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier.fillMaxSize()
    ) {
        items(data.episodes, key = { it.episode }) { episode ->
            SacredCard {
                // Episode badge
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        "${stringResource(R.string.text_episode)} ${episode.episode}",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(Modifier.height(8.dp))
                Text(
                    episode.title(lang),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(8.dp))
                Text(
                    episode.summary(lang),
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 22.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Audio button
                if (audio != null) {
                    val hasMantra = episode.relatedMantra != null
                    val epAudioId = if (hasMantra) "${textFileName}_ep_${episode.episode}_mantra"
                                    else "${textFileName}_ep_${episode.episode}_verse"
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Spacer(Modifier.weight(1f))
                        VerseAudioButton(audioId = epAudioId, audio = audio)
                    }
                    MiniAudioProgressBar(
                        audioId = if (episode.relatedMantra != null) "${textFileName}_ep_${episode.episode}_mantra"
                                  else "${textFileName}_ep_${episode.episode}_verse",
                        audio = audio
                    )
                }

                // Related verse
                episode.relatedVerse?.let { rv ->
                    if (rv.sanskrit.isNotBlank()) {
                        Spacer(Modifier.height(12.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Spacer(Modifier.height(12.dp))
                        Text(
                            rv.sanskrit,
                            style = MaterialTheme.typography.bodyMedium,
                            lineHeight = 24.sp
                        )
                        rv.transliteration?.let {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                it,
                                style = MaterialTheme.typography.bodySmall,
                                fontStyle = FontStyle.Italic,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            rv.translation(lang),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Key teaching
                val teaching = episode.keyTeaching(lang)
                if (teaching.isNotBlank()) {
                    Spacer(Modifier.height(8.dp))
                    CollapsibleExplanation(text = teaching)
                }
            }
        }
    }
}
