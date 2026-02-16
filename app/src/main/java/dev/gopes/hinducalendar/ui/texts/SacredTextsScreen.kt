package dev.gopes.hinducalendar.ui.texts

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import dev.gopes.hinducalendar.data.model.SacredTextType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SacredTextsScreen(viewModel: SacredTextsViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Sacred Texts") })
        }
    ) { padding ->
        if (uiState.availableTexts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Filled.MenuBook,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "No texts available for your path.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Update your Spiritual Path in Settings.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                item {
                    Text(
                        "Your ${uiState.dharmaPathName} Texts",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Daily readings based on your spiritual path",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }

                items(uiState.availableTexts) { textItem ->
                    SacredTextCard(textItem)
                }
            }
        }
    }
}

@Composable
private fun SacredTextCard(item: SacredTextItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* Navigate to detail view in future */ },
        colors = if (item.isPrimary) {
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            )
        } else {
            CardDefaults.cardColors()
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Icon(
                imageVector = iconForText(item.textType),
                contentDescription = item.textType.displayName,
                modifier = Modifier.size(40.dp),
                tint = if (item.isPrimary) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                }
            )
            Spacer(Modifier.width(16.dp))

            // Text info
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        item.textType.displayName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (item.isPrimary) {
                        Spacer(Modifier.width(8.dp))
                        AssistChip(
                            onClick = {},
                            label = {
                                Text(
                                    "Primary",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        )
                    }
                }
                Text(
                    item.progressLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                if (item.totalCount > 0) {
                    Spacer(Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { item.currentPosition.toFloat() / item.totalCount.toFloat() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp),
                        trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                    )
                }
            }

            // Arrow
            Spacer(Modifier.width(8.dp))
            Icon(
                Icons.Filled.ChevronRight,
                contentDescription = "Open",
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
            )
        }
    }
}

/**
 * Maps the icon string from SacredTextType to a Material Icon.
 */
private fun iconForText(textType: SacredTextType): ImageVector {
    return when (textType.icon) {
        "book" -> Icons.Filled.MenuBook
        "music_note" -> Icons.Filled.MusicNote
        "book_closed" -> Icons.Filled.Book
        "books" -> Icons.Filled.LibraryBooks
        "list" -> Icons.Filled.FormatListBulleted
        "waveform" -> Icons.Filled.GraphicEq
        "flame" -> Icons.Filled.LocalFireDepartment
        "sparkles" -> Icons.Filled.AutoAwesome
        "scroll" -> Icons.Filled.Description
        "text_quote" -> Icons.Filled.FormatQuote
        "heart" -> Icons.Filled.Favorite
        "music" -> Icons.Filled.MusicNote
        "book_text" -> Icons.Filled.Article
        "hands" -> Icons.Filled.VolunteerActivism
        else -> Icons.Filled.MenuBook
    }
}

data class SacredTextItem(
    val textType: SacredTextType,
    val isPrimary: Boolean,
    val currentPosition: Int,
    val totalCount: Int
) {
    val progressLabel: String
        get() = if (totalCount > 0) {
            "Position $currentPosition of $totalCount"
        } else {
            "Position $currentPosition"
        }
}
