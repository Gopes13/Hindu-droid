package dev.gopes.hinducalendar.ui.texts.reader

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.gopes.hinducalendar.R
import dev.gopes.hinducalendar.ui.components.SacredHighlightCard

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun StudyModeScreen(
    verses: List<StudyVerse>,
    startIndex: Int = 0,
    onDismiss: () -> Unit,
    onDeepStudy: (() -> Unit)? = null
) {
    if (verses.isEmpty()) {
        onDismiss()
        return
    }

    val pagerState = rememberPagerState(
        initialPage = startIndex.coerceIn(0, verses.size - 1),
        pageCount = { verses.size }
    )

    var showTransliteration by remember { mutableStateOf(false) }
    var showTranslation by remember { mutableStateOf(false) }
    var showExplanation by remember { mutableStateOf(false) }
    var deepStudySeconds by remember { mutableIntStateOf(0) }
    var deepStudyAwarded by remember { mutableStateOf(false) }

    // Reset reveals and timer on page change
    LaunchedEffect(pagerState.currentPage) {
        showTransliteration = false
        showTranslation = false
        showExplanation = false
        deepStudySeconds = 0
        deepStudyAwarded = false
    }

    // Deep study timer — ticks every second
    LaunchedEffect(pagerState.currentPage) {
        while (true) {
            kotlinx.coroutines.delay(1000)
            deepStudySeconds++
            if (deepStudySeconds >= 15 && !deepStudyAwarded) {
                deepStudyAwarded = true
                onDeepStudy?.invoke()
            }
        }
    }

    val currentVerse = verses.getOrNull(pagerState.currentPage)

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Progress bar
            LinearProgressIndicator(
                progress = (pagerState.currentPage + 1f) / verses.size,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
            )

            // Header: Close + counter
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.Close, stringResource(R.string.common_close))
                }
                Text(
                    "${pagerState.currentPage + 1} / ${verses.size}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.width(48.dp))
            }

            // Pager
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                val verse = verses[page]
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Reference badge
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            verse.reference,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Spacer(Modifier.height(24.dp))

                    // Original text (always visible)
                    Text(
                        verse.originalText,
                        style = MaterialTheme.typography.headlineSmall,
                        lineHeight = 36.sp,
                        textAlign = TextAlign.Center
                    )

                    // Transliteration (revealed)
                    AnimatedVisibility(
                        visible = showTransliteration && page == pagerState.currentPage,
                        enter = expandVertically() + fadeIn()
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Spacer(Modifier.height(16.dp))
                            Text(
                                verse.transliteration ?: "",
                                style = MaterialTheme.typography.bodyLarge,
                                fontStyle = FontStyle.Italic,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    // Translation (revealed)
                    AnimatedVisibility(
                        visible = showTranslation && page == pagerState.currentPage,
                        enter = expandVertically() + fadeIn()
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Spacer(Modifier.height(16.dp))
                            Divider()
                            Spacer(Modifier.height(16.dp))
                            Text(
                                verse.translation,
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center,
                                lineHeight = 26.sp
                            )
                        }
                    }

                    // Explanation (revealed)
                    AnimatedVisibility(
                        visible = showExplanation && page == pagerState.currentPage
                                && !verse.explanation.isNullOrBlank(),
                        enter = expandVertically() + fadeIn()
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Spacer(Modifier.height(16.dp))
                            SacredHighlightCard {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Filled.Lightbulb,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        stringResource(R.string.reader_understanding),
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    verse.explanation ?: "",
                                    style = MaterialTheme.typography.bodyMedium,
                                    lineHeight = 22.sp
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(80.dp))
                }
            }

            // Reveal buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
            ) {
                StudyRevealButton(
                    label = stringResource(R.string.text_transliteration),
                    revealed = showTransliteration,
                    enabled = currentVerse?.transliteration != null,
                    onClick = { showTransliteration = true }
                )
                StudyRevealButton(
                    label = stringResource(R.string.text_translation),
                    revealed = showTranslation,
                    enabled = true,
                    onClick = { showTranslation = true }
                )
                if (currentVerse?.explanation != null) {
                    StudyRevealButton(
                        label = stringResource(R.string.study_meaning),
                        revealed = showExplanation,
                        enabled = true,
                        onClick = { showExplanation = true }
                    )
                }
            }
        }
    }
}

@Composable
private fun StudyRevealButton(
    label: String,
    revealed: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    if (revealed) {
        FilledTonalButton(onClick = {}, enabled = false) {
            Text(label, style = MaterialTheme.typography.labelMedium)
        }
    } else {
        OutlinedButton(
            onClick = onClick,
            enabled = enabled
        ) {
            Text(label, style = MaterialTheme.typography.labelMedium)
        }
    }
}
