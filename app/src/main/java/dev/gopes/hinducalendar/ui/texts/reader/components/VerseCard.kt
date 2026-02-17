package dev.gopes.hinducalendar.ui.texts.reader.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.gopes.hinducalendar.ui.components.SacredCard

@Composable
fun VerseCard(
    badge: String,
    originalText: String,
    transliteration: String? = null,
    translation: String,
    explanation: String? = null,
    isHighlighted: Boolean = false,
    modifier: Modifier = Modifier
) {
    SacredCard(
        modifier = modifier,
        isHighlighted = isHighlighted
    ) {
        // Reference badge
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
