package dev.gopes.hinducalendar.ui.texts

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.gopes.hinducalendar.R
import dev.gopes.hinducalendar.ui.components.SacredHighlightCard
import dev.gopes.hinducalendar.ui.components.entranceAnimation
import dev.gopes.hinducalendar.ui.theme.LocalVibrantMode

/**
 * Landing page for the Texts tab — matches iOS LibraryLandingView.
 * Two prominent cards: Sacred Texts and Kirtans.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryLandingScreen(
    onSelectTexts: () -> Unit = {},
    onSelectKirtans: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.tab_media)) })
        }
    ) { padding ->
        val isVibrant = LocalVibrantMode.current
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Sacred Texts card
            Box(Modifier.entranceAnimation(0, isVibrant)) {
            SacredHighlightCard(
                modifier = Modifier.clickable { onSelectTexts() }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.MenuBook,
                            contentDescription = null,
                            modifier = Modifier.size(26.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    Column(Modifier.weight(1f)) {
                        Text(
                            stringResource(R.string.sacred_texts_title),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            stringResource(R.string.sacred_texts_landing_subtitle),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2
                        )
                    }

                    Icon(
                        Icons.Filled.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            }

            // Kirtans card
            Box(Modifier.entranceAnimation(1, isVibrant)) {
            SacredHighlightCard(
                modifier = Modifier.clickable { onSelectKirtans() },
                accentColor = MaterialTheme.colorScheme.tertiary
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(
                                MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.MusicNote,
                            contentDescription = null,
                            modifier = Modifier.size(26.dp),
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                    }

                    Column(Modifier.weight(1f)) {
                        Text(
                            stringResource(R.string.kirtans_title),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            stringResource(R.string.kirtans_subtitle),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2
                        )
                    }

                    Icon(
                        Icons.Filled.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
            }
        }
    }
}
