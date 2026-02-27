package dev.gopes.hinducalendar.feature.texts.reader

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CropFree
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import dev.gopes.hinducalendar.R
import dev.gopes.hinducalendar.domain.model.AppLanguage
import dev.gopes.hinducalendar.feature.texts.reader.components.VerseCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChalisaReaderScreen(
    onBack: () -> Unit,
    viewModel: ReaderViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val audio by viewModel.audioUiState.collectAsState()
    val studyVerses = remember(uiState) { viewModel.getStudyVerses() }
    var readerMode by remember { mutableStateOf(ReaderMode.NORMAL) }

    if (readerMode == ReaderMode.STUDY && studyVerses.isNotEmpty()) {
        StudyModeScreen(verses = studyVerses, audio = audio, onDismiss = { readerMode = ReaderMode.NORMAL })
        return
    }
    if (readerMode == ReaderMode.FOCUS && studyVerses.isNotEmpty()) {
        FocusedReadingScreen(verses = studyVerses, audio = audio, onDismiss = { readerMode = ReaderMode.NORMAL })
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.text_name_hanuman_chalisa)) },
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
        val loaded = uiState as? ReaderUiState.Loaded
        val chalisa = (loaded?.content as? TextContent.Chalisa)?.data
        val lang = loaded?.language ?: AppLanguage.ENGLISH
        if (loaded == null || chalisa == null) {
            Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize().padding(padding)
        ) {
            // Dohas
            if (chalisa.dohas.isNotEmpty()) {
                item {
                    Text(
                        stringResource(R.string.reader_doha),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                items(chalisa.dohas, key = { it.verse }) { verse ->
                    val ref = "${stringResource(R.string.reader_doha)} ${verse.verse}"
                    VerseCard(
                        badge = ref,
                        originalText = verse.sanskrit,
                        transliteration = verse.transliteration,
                        translation = verse.translation(lang),
                        isHighlighted = true,
                        isBookmarked = viewModel.isBookmarked(ref),
                        onBookmarkToggle = {
                            viewModel.toggleBookmark(ref, verse.sanskrit, verse.translation(lang))
                        },
                        audioId = "chalisa_${verse.type ?: "verse"}_${verse.verse}",
                        audio = audio
                    )
                }
            }

            // Chaupais
            if (chalisa.chaupais.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        stringResource(R.string.reader_chaupai),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                items(chalisa.chaupais, key = { it.verse }) { verse ->
                    val ref = "${stringResource(R.string.reader_chaupai)} ${verse.verse}"
                    VerseCard(
                        badge = ref,
                        originalText = verse.sanskrit,
                        transliteration = verse.transliteration,
                        translation = verse.translation(lang),
                        isBookmarked = viewModel.isBookmarked(ref),
                        onBookmarkToggle = {
                            viewModel.toggleBookmark(ref, verse.sanskrit, verse.translation(lang))
                        },
                        audioId = "chalisa_${verse.type ?: "verse"}_${verse.verse}",
                        audio = audio
                    )
                }
            }

            // Closing Doha
            chalisa.closingDoha?.let { closing ->
                item {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        stringResource(R.string.reader_closing_doha),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                item {
                    val ref = stringResource(R.string.reader_closing_doha)
                    VerseCard(
                        badge = ref,
                        originalText = closing.sanskrit,
                        transliteration = closing.transliteration,
                        translation = closing.translation(lang),
                        isHighlighted = true,
                        isBookmarked = viewModel.isBookmarked(ref),
                        onBookmarkToggle = {
                            viewModel.toggleBookmark(ref, closing.sanskrit, closing.translation(lang))
                        },
                        audioId = "chalisa_${closing.type ?: "doha_closing"}_${closing.verse}",
                        audio = audio
                    )
                }
            }
        }
    }
}
