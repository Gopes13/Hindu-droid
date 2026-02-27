package dev.gopes.hinducalendar.feature.texts.reader.content

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.gopes.hinducalendar.R
import dev.gopes.hinducalendar.domain.model.AppLanguage
import dev.gopes.hinducalendar.domain.model.ShlokaTextData
import dev.gopes.hinducalendar.feature.texts.reader.AudioUiState
import dev.gopes.hinducalendar.core.ui.components.SacredCard
import dev.gopes.hinducalendar.core.ui.components.SacredHighlightCard
import dev.gopes.hinducalendar.feature.texts.reader.components.VerseCard

@Composable
internal fun ShlokaContent(data: ShlokaTextData, lang: AppLanguage, modifier: Modifier, isBookmarked: (String) -> Boolean, onToggleBookmark: (String, String, String) -> Unit, textFileName: String = "", audio: AudioUiState? = null) {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier.fillMaxSize()
    ) {
        // Dhyana Shloka
        data.dhyanaShloka?.let { ds ->
            item {
                SacredHighlightCard {
                    Text(
                        stringResource(R.string.reader_dhyana_shloka),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(ds.sanskrit, style = MaterialTheme.typography.bodyLarge, lineHeight = 28.sp)
                    ds.transliteration?.let {
                        Spacer(Modifier.height(4.dp))
                        Text(it, style = MaterialTheme.typography.bodySmall,
                            fontStyle = FontStyle.Italic, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(Modifier.height(8.dp))
                    Divider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(Modifier.height(8.dp))
                    Text(ds.translation(lang), style = MaterialTheme.typography.bodyMedium, lineHeight = 22.sp)
                }
            }
        }

        items(data.shlokas, key = { it.shloka }) { shloka ->
            val commentary = shloka.commentary(lang).ifEmpty { null }
                ?: shloka.explanation(lang).ifEmpty { null }
            val ref = "${stringResource(R.string.text_shloka)} ${shloka.shloka}"
            VerseCard(
                badge = ref,
                originalText = shloka.sanskrit,
                transliteration = shloka.transliteration,
                translation = shloka.translation(lang),
                explanation = commentary,
                isBookmarked = isBookmarked(ref),
                onBookmarkToggle = { onToggleBookmark(ref, shloka.sanskrit, shloka.translation(lang)) },
                audioId = "${textFileName}_${shloka.shloka}",
                audio = audio
            )

            // Names (for Vishnu Sahasranama)
            shloka.names?.takeIf { it.isNotEmpty() }?.let { names ->
                Spacer(Modifier.height(4.dp))
                SacredCard {
                    Text(
                        stringResource(R.string.reader_names_meanings),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(8.dp))
                    names.forEach { name ->
                        Row(Modifier.padding(vertical = 2.dp)) {
                            Text(
                                name.name,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.width(120.dp)
                            )
                            Text(
                                name.meaning(lang),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
