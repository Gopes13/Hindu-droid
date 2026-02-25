package dev.gopes.hinducalendar.ui.sanskrit

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.gopes.hinducalendar.R
import dev.gopes.hinducalendar.data.model.*
import dev.gopes.hinducalendar.ui.components.SacredCard
import dev.gopes.hinducalendar.ui.components.SacredHighlightCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SanskritVerseScreen(
    progress: SanskritProgress,
    onVerseExplored: (String) -> Unit,
    onSpeak: (String) -> Unit,
    onBack: () -> Unit
) {
    var selectedCategory by remember { mutableStateOf(VerseTextCategory.GITA) }
    var expandedVerseId by remember { mutableStateOf<String?>(null) }

    val shlokas = SanskritData.shlokasByCategory(selectedCategory)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.sanskrit_verse_reader)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back))
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // Category picker
            item(key = "categories") {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(VerseTextCategory.entries) { category ->
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick = { selectedCategory = category },
                            label = { Text(category.displayName) }
                        )
                    }
                }
            }

            // Shloka list
            items(shlokas, key = { it.id }) { shloka ->
                val isExplored = progress.isVerseExplored(shloka.id)
                val isExpanded = expandedVerseId == shloka.id

                val cardContent: @Composable () -> Unit = {
                    Column {
                        // Header
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    expandedVerseId = if (isExpanded) null else shloka.id
                                    if (!isExplored) onVerseExplored(shloka.id)
                                },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Text(
                                        shloka.source,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    shloka.devanagari.take(60) + if (shloka.devanagari.length > 60) "..." else "",
                                    style = MaterialTheme.typography.bodyMedium,
                                    lineHeight = 24.sp
                                )
                            }
                            Spacer(Modifier.width(8.dp))
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                if (isExplored) {
                                    Icon(
                                        Icons.Filled.CheckCircle,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Icon(
                                    if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Expanded content
                        AnimatedVisibility(
                            visible = isExpanded,
                            enter = expandVertically() + fadeIn()
                        ) {
                            Column {
                                Spacer(Modifier.height(12.dp))
                                HorizontalDivider()
                                Spacer(Modifier.height(12.dp))

                                // Full Devanagari
                                Text(
                                    shloka.devanagari,
                                    style = MaterialTheme.typography.bodyLarge,
                                    lineHeight = 28.sp
                                )

                                // TTS button
                                IconButton(onClick = { onSpeak(shloka.devanagari) }) {
                                    Icon(
                                        Icons.Filled.VolumeUp,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }

                                // Transliteration
                                Text(
                                    shloka.transliteration,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 24.sp
                                )

                                Spacer(Modifier.height(12.dp))

                                // Translation
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                                ) {
                                    Text(
                                        shloka.translation,
                                        modifier = Modifier.padding(12.dp),
                                        style = MaterialTheme.typography.bodyMedium,
                                        lineHeight = 22.sp
                                    )
                                }

                                // Word-by-word
                                if (shloka.words.isNotEmpty()) {
                                    Spacer(Modifier.height(12.dp))
                                    Text(
                                        stringResource(R.string.sanskrit_word_by_word),
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    shloka.words.forEach { word ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 2.dp)
                                                .clickable { onSpeak(word.sanskrit) },
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                word.sanskrit,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.SemiBold,
                                                modifier = Modifier.width(100.dp)
                                            )
                                            Text(
                                                word.transliteration,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.width(80.dp)
                                            )
                                            Text(
                                                word.meaning,
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

                if (isExplored) {
                    SacredHighlightCard { cardContent() }
                } else {
                    SacredCard { cardContent() }
                }
            }
        }
    }
}
