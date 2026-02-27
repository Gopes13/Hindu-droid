package dev.gopes.hinducalendar.feature.texts.reader.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.gopes.hinducalendar.R
import dev.gopes.hinducalendar.feature.texts.reader.AudioUiState
import dev.gopes.hinducalendar.core.ui.components.SacredCard
import dev.gopes.hinducalendar.core.ui.theme.HinduCalendarTheme

@Composable
fun VerseCard(
    badge: String,
    originalText: String,
    transliteration: String? = null,
    translation: String,
    explanation: String? = null,
    isHighlighted: Boolean = false,
    isBookmarked: Boolean = false,
    onBookmarkToggle: (() -> Unit)? = null,
    audioId: String? = null,
    audio: AudioUiState? = null,
    modifier: Modifier = Modifier
) {
    SacredCard(
        modifier = modifier,
        isHighlighted = isHighlighted
    ) {
        // Header: badge + audio + bookmark
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    text = badge,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(Modifier.weight(1f))
            if (audio != null && audioId != null) {
                VerseAudioButton(
                    audioId = audioId,
                    audio = audio
                )
            }
            onBookmarkToggle?.let { toggle ->
                IconButton(onClick = toggle, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = if (isBookmarked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = if (isBookmarked) stringResource(R.string.cd_remove_bookmark) else stringResource(R.string.cd_add_bookmark),
                        tint = if (isBookmarked) MaterialTheme.colorScheme.error
                               else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Audio progress bar
        if (audio != null) {
            MiniAudioProgressBar(audioId = audioId, audio = audio)
        }

        Spacer(Modifier.height(12.dp))

        // Original text (Sanskrit / Punjabi)
        Text(
            text = originalText,
            style = MaterialTheme.typography.bodyLarge,
            lineHeight = 28.sp,
            color = MaterialTheme.colorScheme.onSurface
        )

        // Transliteration
        if (!transliteration.isNullOrBlank()) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = transliteration,
                style = MaterialTheme.typography.bodyMedium,
                fontStyle = FontStyle.Italic,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 22.sp
            )
        }

        Spacer(Modifier.height(12.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Spacer(Modifier.height(12.dp))

        // Translation
        Text(
            text = translation,
            style = MaterialTheme.typography.bodyMedium,
            lineHeight = 22.sp,
            color = MaterialTheme.colorScheme.onSurface
        )

        // Collapsible explanation
        if (!explanation.isNullOrBlank()) {
            Spacer(Modifier.height(8.dp))
            CollapsibleExplanation(text = explanation)
        }
    }
}

@Preview(showBackground = true, name = "Light")
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark")
@Composable
private fun VerseCardPreview() {
    HinduCalendarTheme {
        VerseCard(
            badge = "Verse 2.47",
            originalText = "\u0915\u0930\u094d\u092e\u0923\u094d\u092f\u0947\u0935\u093e\u0927\u093f\u0915\u093e\u0930\u0938\u094d\u0924\u0947 \u092e\u093e \u092b\u0932\u0947\u0937\u0941 \u0915\u0926\u093e\u091a\u0928",
            transliteration = "karmany evadhikaras te ma phaleshu kadachana",
            translation = "You have the right to perform your duty, but you are not entitled to the fruits of your actions.",
            explanation = "This verse teaches the principle of selfless action.",
            isBookmarked = true,
            onBookmarkToggle = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}
