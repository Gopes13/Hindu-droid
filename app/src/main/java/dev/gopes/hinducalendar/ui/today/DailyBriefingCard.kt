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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import dev.gopes.hinducalendar.R
import dev.gopes.hinducalendar.engine.DailyVerse
import dev.gopes.hinducalendar.ui.components.*
import dev.gopes.hinducalendar.ui.theme.*

@Composable
fun DailyBriefingCard(viewModel: DailyBriefingViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.primaryVerse == null && uiState.secondaryVerse == null) {
        return
    }

    SacredHighlightCard(accentColor = MaterialTheme.colorScheme.tertiary) {
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
                stringResource(R.string.daily_wisdom),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.tertiary
            )
        }

        Spacer(Modifier.height(12.dp))

        // Primary Verse
        uiState.primaryVerse?.let { verse ->
            VerseSection(verse, isPrimary = true)
        }

        // Divider between primary and secondary
        if (uiState.primaryVerse != null && uiState.secondaryVerse != null) {
            Spacer(Modifier.height(12.dp))
            DecorativeDivider(style = DividerStyle.DOT)
            Spacer(Modifier.height(4.dp))
        }

        // Secondary Verse
        uiState.secondaryVerse?.let { verse ->
            VerseSection(verse, isPrimary = false)
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
                        MaterialTheme.colorScheme.onSurfaceVariant
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
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Subtitle
        Text(
            verse.subtitle,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Sanskrit / original text — using SacredTypography
        verse.sanskrit?.let { sanskrit ->
            Text(
                sanskrit,
                style = SacredTypography.sacredMedium,
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
