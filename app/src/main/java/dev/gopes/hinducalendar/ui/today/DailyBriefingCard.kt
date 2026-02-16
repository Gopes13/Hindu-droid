package dev.gopes.hinducalendar.ui.today

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import dev.gopes.hinducalendar.engine.DailyVerse

@Composable
fun DailyBriefingCard(viewModel: DailyBriefingViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.primaryVerse == null && uiState.secondaryVerse == null) {
        return
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.AutoAwesome,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Daily Wisdom",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }

            // Primary Verse
            uiState.primaryVerse?.let { verse ->
                VerseSection(verse, isPrimary = true)
            }

            // Divider between primary and secondary
            if (uiState.primaryVerse != null && uiState.secondaryVerse != null) {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.15f)
                )
            }

            // Secondary Verse
            uiState.secondaryVerse?.let { verse ->
                VerseSection(verse, isPrimary = false)
            }
        }
    }
}

@Composable
private fun VerseSection(verse: DailyVerse, isPrimary: Boolean) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        // Title and position
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.MenuBook,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = if (isPrimary) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    }
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    verse.title,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isPrimary) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    }
                )
            }
            Text(
                "${verse.position}/${verse.totalCount}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
        }

        // Subtitle
        Text(
            verse.subtitle,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )

        // Sanskrit / original text
        verse.sanskrit?.let { sanskrit ->
            Text(
                sanskrit,
                style = MaterialTheme.typography.bodyMedium,
                fontStyle = FontStyle.Italic,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Translation
        Text(
            verse.translation,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            maxLines = 4,
            overflow = TextOverflow.Ellipsis
        )
    }
}
