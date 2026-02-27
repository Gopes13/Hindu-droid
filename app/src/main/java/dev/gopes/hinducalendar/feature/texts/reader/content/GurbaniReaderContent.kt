package dev.gopes.hinducalendar.feature.texts.reader.content

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import dev.gopes.hinducalendar.domain.model.GurbaniData
import dev.gopes.hinducalendar.feature.texts.reader.AudioUiState
import dev.gopes.hinducalendar.core.ui.components.SacredCard
import dev.gopes.hinducalendar.feature.texts.reader.components.CollapsibleExplanation
import dev.gopes.hinducalendar.feature.texts.reader.components.MiniAudioProgressBar
import dev.gopes.hinducalendar.feature.texts.reader.components.VerseAudioButton

@Composable
internal fun GurbaniContent(data: GurbaniData, lang: AppLanguage, modifier: Modifier, audio: AudioUiState? = null) {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier.fillMaxSize()
    ) {
        items(data.shabads.size) { index ->
            val shabad = data.shabads[index]
            val shabadAudioId = "gurbani_day_${index + 1}"
            SacredCard {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            "${stringResource(R.string.text_shabad)} ${shabad.day}",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    if (shabad.author.isNotBlank()) {
                        Text(
                            shabad.author,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                    Spacer(Modifier.weight(1f))
                    if (audio != null) {
                        VerseAudioButton(audioId = shabadAudioId, audio = audio)
                    }
                }

                if (audio != null) {
                    MiniAudioProgressBar(audioId = shabadAudioId, audio = audio)
                }

                Spacer(Modifier.height(12.dp))
                Text(
                    shabad.punjabi,
                    style = MaterialTheme.typography.bodyLarge,
                    lineHeight = 28.sp
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    shabad.transliteration,
                    style = MaterialTheme.typography.bodyMedium,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 22.sp
                )
                Spacer(Modifier.height(12.dp))
                Divider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(Modifier.height(12.dp))
                Text(
                    shabad.translation(lang),
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 22.sp
                )

                val theme = shabad.theme(lang)
                if (theme.isNotBlank()) {
                    Spacer(Modifier.height(8.dp))
                    CollapsibleExplanation(text = theme)
                }
            }
        }
    }
}
