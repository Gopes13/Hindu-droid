package dev.gopes.hinducalendar.ui.texts.reader

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.gopes.hinducalendar.R
import dev.gopes.hinducalendar.engine.AudioPlayerService
import dev.gopes.hinducalendar.ui.components.ConfettiOverlay
import dev.gopes.hinducalendar.ui.texts.reader.components.RevealableVerseCard

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun FocusedReadingScreen(
    verses: List<StudyVerse>,
    startIndex: Int = 0,
    audioPlayerService: AudioPlayerService? = null,
    onDismiss: () -> Unit
) {
    if (verses.isEmpty()) {
        onDismiss()
        return
    }

    val pagerState = rememberPagerState(
        initialPage = startIndex.coerceIn(0, verses.size - 1),
        pageCount = { verses.size }
    )

    var showConfetti by remember { mutableStateOf(false) }

    // Reset confetti on page change
    LaunchedEffect(pagerState.currentPage) {
        showConfetti = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.focus_mode)) },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, stringResource(R.string.common_close))
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Pager
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) { page ->
                    val verse = verses[page]
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        RevealableVerseCard(
                            reference = verse.reference,
                            originalText = verse.originalText,
                            transliteration = verse.transliteration,
                            translation = verse.translation,
                            explanation = verse.explanation,
                            names = verse.names,
                            audioId = verse.audioId,
                            audioPlayerService = audioPlayerService,
                            onFullyRevealed = { showConfetti = true }
                        )
                    }
                }

                // Progress section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    LinearProgressIndicator(
                        progress = (pagerState.currentPage + 1f) / verses.size,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        stringResource(R.string.focus_verse_counter, pagerState.currentPage + 1, verses.size),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Confetti on full reveal
            ConfettiOverlay(
                isActive = showConfetti,
                onFinished = { showConfetti = false }
            )
        }
    }
}
