package dev.gopes.hinducalendar.feature.texts

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import dev.gopes.hinducalendar.R
import dev.gopes.hinducalendar.domain.model.SacredTextType
import dev.gopes.hinducalendar.core.ui.components.*
import dev.gopes.hinducalendar.core.ui.theme.LocalVibrantMode
import dev.gopes.hinducalendar.core.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SacredTextsScreen(
    onTextClick: (SacredTextType) -> Unit = {},
    onBookmarksClick: () -> Unit = {},
    onSanskritClick: () -> Unit = {},
    onBack: () -> Unit = {},
    viewModel: SacredTextsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.sacred_texts_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.common_back))
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.availableTexts.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Filled.MenuBook,
                        contentDescription = stringResource(R.string.cd_no_texts),
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        stringResource(R.string.no_texts_for_path),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        stringResource(R.string.update_path_in_settings),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            val isVibrant = LocalVibrantMode.current
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                // 1. Sanskrit Pathshala card (matches iOS order)
                item(key = "sanskrit") {
                    Box(Modifier.entranceAnimation(0, isVibrant)) {
                        SacredCard(
                            modifier = Modifier.clickable { onSanskritClick() }
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "\uD83D\uDD49",
                                    fontSize = 30.sp,
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(Modifier.width(16.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        stringResource(R.string.sanskrit_title),
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        stringResource(R.string.sanskrit_subtitle),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Icon(
                                    Icons.Filled.ChevronRight,
                                    contentDescription = stringResource(R.string.cd_open_text),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // 2. Continue Reading hero card
                val primaryText = uiState.availableTexts.firstOrNull { it.hasStarted }
                    ?: uiState.availableTexts.firstOrNull()
                if (primaryText != null) {
                    item(key = "continue_reading") {
                        Box(Modifier.entranceAnimation(1, isVibrant)) {
                            ContinueReadingCard(primaryText, onTextClick)
                        }
                    }
                }

                // 3. Bookmarks quick access
                if (uiState.bookmarkCount > 0) {
                    item(key = "bookmarks") {
                        Box(Modifier.entranceAnimation(2, isVibrant)) {
                            SacredCard(
                                modifier = Modifier.clickable { onBookmarksClick() }
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Filled.Favorite,
                                        contentDescription = stringResource(R.string.cd_bookmarks),
                                        modifier = Modifier.size(32.dp),
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                    Spacer(Modifier.width(16.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(
                                            stringResource(R.string.bookmarks_title),
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            stringResource(R.string.bookmarks_saved_count, uiState.bookmarkCount),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Icon(
                                        Icons.Filled.ChevronRight,
                                        contentDescription = stringResource(R.string.cd_open_text),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                // 4. "Your Path" section header — localized path name
                item(key = "path_header") {
                    Box(Modifier.entranceAnimation(3, isVibrant)) {
                        Column {
                            Text(
                                stringResource(R.string.your_path_texts, uiState.dharmaPath.localizedName()),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                stringResource(R.string.daily_readings_desc),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // 4. Path-specific text cards
                items(uiState.availableTexts, key = { "path_${it.textType.name}" }) { textItem ->
                    Box(Modifier.entranceAnimation(4, isVibrant)) {
                        SacredTextCard(textItem, isPathText = true, onTextClick = onTextClick)
                    }
                }

                // 5. "All Sacred Texts" section
                if (uiState.allOtherTexts.isNotEmpty()) {
                    item(key = "all_header") {
                        Box(Modifier.entranceAnimation(5, isVibrant)) {
                            Column {
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    stringResource(R.string.all_sacred_texts),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    items(uiState.allOtherTexts, key = { "other_${it.textType.name}" }) { textItem ->
                        Box(Modifier.entranceAnimation(6, isVibrant)) {
                            SacredTextCard(textItem, isPathText = false, onTextClick = onTextClick)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ContinueReadingCard(
    item: SacredTextItem,
    onTextClick: (SacredTextType) -> Unit
) {
    val localName = item.textType.localizedName()

    SacredHighlightCard(
        modifier = Modifier.clickable { onTextClick(item.textType) }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SacredProgressRing(
                progress = item.progressFraction,
                size = 52.dp,
                strokeWidth = 4.dp
            ) {
                Icon(
                    imageVector = iconForText(item.textType),
                    contentDescription = localName,
                    modifier = Modifier.size(22.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    stringResource(R.string.continue_reading),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    localName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                if (item.totalCount > 0) {
                    Text(
                        stringResource(R.string.texts_position_of, item.currentPosition, item.totalCount),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Icon(
                Icons.Filled.ArrowCircleRight,
                contentDescription = stringResource(R.string.cd_open),
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(Modifier.height(10.dp))

        LinearProgressIndicator(
            progress = { item.progressFraction },
            modifier = Modifier.fillMaxWidth().height(5.dp),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f),
            strokeCap = StrokeCap.Round
        )
    }
}

@Composable
private fun SacredTextCard(
    item: SacredTextItem,
    isPathText: Boolean,
    onTextClick: (SacredTextType) -> Unit
) {
    val isComplete = item.totalCount > 0 && item.currentPosition > item.totalCount
    val iconColor = when {
        isComplete -> MaterialTheme.colorScheme.tertiary
        item.hasStarted -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val localName = item.textType.localizedName()

    SacredCard(
        modifier = Modifier.clickable { onTextClick(item.textType) },
        isHighlighted = isComplete
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SacredProgressRing(
                progress = item.progressFraction,
                color = iconColor
            ) {
                Icon(
                    imageVector = if (isComplete) Icons.Filled.CheckCircle else iconForText(item.textType),
                    contentDescription = localName,
                    modifier = Modifier.size(18.dp),
                    tint = iconColor
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        localName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    if (item.isPrimary) {
                        Spacer(Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                stringResource(R.string.text_primary),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(3.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                    ) {
                        Text(
                            item.textType.localizedThemeTag(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    if (item.totalCount > 0) {
                        Text(
                            "${item.totalCount} ${item.textType.localizedUnitLabel()}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (isPathText || item.hasStarted) {
                    Spacer(Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        LinearProgressIndicator(
                            progress = { item.progressFraction },
                            modifier = Modifier.width(60.dp).height(4.dp),
                            color = if (isComplete) MaterialTheme.colorScheme.tertiary
                            else MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f),
                            strokeCap = StrokeCap.Round
                        )
                        Text(
                            if (isComplete) stringResource(R.string.completed_text)
                            else stringResource(R.string.texts_position_of, item.currentPosition, item.totalCount),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isComplete) MaterialTheme.colorScheme.tertiary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (item.bookmarkCount > 0) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(
                            Icons.Filled.Favorite,
                            contentDescription = stringResource(R.string.cd_bookmark_count),
                            modifier = Modifier.size(9.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Text(
                            "${item.bookmarkCount}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                Spacer(Modifier.width(4.dp))
            }

            Icon(
                Icons.Filled.ChevronRight,
                contentDescription = stringResource(R.string.cd_open),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

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
    val totalCount: Int,
    val bookmarkCount: Int = 0
) {
    val progressFraction: Float
        get() = if (totalCount > 0) (currentPosition.toFloat() / totalCount.toFloat()).coerceIn(0f, 1f) else 0f

    val hasStarted: Boolean
        get() = currentPosition > 1
}
