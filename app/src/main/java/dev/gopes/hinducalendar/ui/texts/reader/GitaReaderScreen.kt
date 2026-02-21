package dev.gopes.hinducalendar.ui.texts.reader

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CropFree
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import dev.gopes.hinducalendar.R
import dev.gopes.hinducalendar.ui.texts.reader.components.VerseCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GitaReaderScreen(
    onBack: () -> Unit,
    viewModel: ReaderViewModel = hiltViewModel()
) {
    val gita = viewModel.gitaData
    val lang = viewModel.language
    val selected = viewModel.selectedChapter
    val studyVerses = remember(viewModel.isLoading, selected) { viewModel.getStudyVerses() }
    var readerMode by remember { mutableStateOf(ReaderMode.NORMAL) }

    if (readerMode == ReaderMode.STUDY && studyVerses.isNotEmpty()) {
        StudyModeScreen(verses = studyVerses, audioPlayerService = viewModel.audioPlayerService, onDismiss = { readerMode = ReaderMode.NORMAL })
        return
    }
    if (readerMode == ReaderMode.FOCUS && studyVerses.isNotEmpty()) {
        FocusedReadingScreen(verses = studyVerses, audioPlayerService = viewModel.audioPlayerService, onDismiss = { readerMode = ReaderMode.NORMAL })
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.text_name_gita)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.cd_go_back))
                    }
                },
                actions = {
                    if (studyVerses.isNotEmpty()) {
                        IconButton(onClick = { readerMode = ReaderMode.STUDY }) {
                            Icon(Icons.Filled.School, stringResource(R.string.study_mode))
                        }
                        IconButton(onClick = { readerMode = ReaderMode.FOCUS }) {
                            Icon(Icons.Filled.CropFree, stringResource(R.string.focus_mode))
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (viewModel.isLoading || gita == null) {
            Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        val chapter = gita.chapters.find { it.chapter == selected } ?: gita.chapters.firstOrNull()

        Column(Modifier.fillMaxSize().padding(padding)) {
            // Chapter picker
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                items(gita.chapters) { ch ->
                    val isSelected = ch.chapter == selected
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { viewModel.selectGitaChapter(ch.chapter) },
                        color = if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                        ) {
                            Text(
                                "Ch ${ch.chapter}",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                        else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                stringResource(R.string.reader_verses_count, ch.verseCount),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                                        else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Chapter header
            chapter?.let { ch ->
                Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text(
                        "${stringResource(R.string.text_chapter)} ${ch.chapter}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    ch.sanskritTitle?.let {
                        Text(
                            it,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary,
                            lineHeight = 28.sp
                        )
                    }
                    ch.title?.let {
                        Text(
                            it,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }

            // Verses
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                chapter?.let { ch ->
                    items(ch.verses) { verse ->
                        val ref = "${ch.chapter}.${verse.verse}"
                        VerseCard(
                            badge = ref,
                            originalText = verse.sanskrit,
                            transliteration = verse.transliteration,
                            translation = verse.translation(lang),
                            isBookmarked = viewModel.isBookmarked(ref),
                            onBookmarkToggle = {
                                viewModel.toggleBookmark(ref, verse.sanskrit, verse.translation(lang))
                            },
                            audioId = "gita_${ch.chapter}_${verse.verse}",
                            audioPlayerService = viewModel.audioPlayerService
                        )
                    }
                }
            }
        }
    }
}
