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
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import dev.gopes.hinducalendar.R
import dev.gopes.hinducalendar.domain.model.AppLanguage
import dev.gopes.hinducalendar.core.ui.components.SacredHighlightCard
import dev.gopes.hinducalendar.feature.texts.reader.components.MiniAudioProgressBar
import dev.gopes.hinducalendar.feature.texts.reader.components.VerseAudioButton
import dev.gopes.hinducalendar.feature.texts.reader.components.VerseCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JapjiReaderScreen(
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
                title = { Text(stringResource(R.string.text_name_japji_sahib)) },
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
        val japji = (loaded?.content as? TextContent.Japji)?.data
        val lang = loaded?.language ?: AppLanguage.ENGLISH
        if (loaded == null || japji == null) {
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
            // Mool Mantar highlight
            japji.moolMantar?.let { mm ->
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            stringResource(R.string.reader_mool_mantar),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(8.dp))
                        VerseAudioButton(
                            audioId = "japji_moolmantar",
                            audio = audio
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    SacredHighlightCard {
                        MiniAudioProgressBar(
                            audioId = "japji_moolmantar",
                            audio = audio
                        )
                        Text(
                            mm.punjabi,
                            style = MaterialTheme.typography.bodyLarge,
                            lineHeight = 28.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            mm.transliteration,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 22.sp
                        )
                        Spacer(Modifier.height(8.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            mm.translation(lang),
                            style = MaterialTheme.typography.bodyMedium,
                            lineHeight = 22.sp
                        )
                    }
                }
            }

            // Pauris
            item {
                Spacer(Modifier.height(4.dp))
            }
            items(japji.pauris, key = { it.pauri }) { pauri ->
                val ref = "${stringResource(R.string.text_pauri)} ${pauri.pauri}"
                VerseCard(
                    badge = ref,
                    originalText = pauri.punjabi,
                    transliteration = pauri.transliteration,
                    translation = pauri.translation(lang),
                    isBookmarked = viewModel.isBookmarked(ref),
                    onBookmarkToggle = {
                        viewModel.toggleBookmark(ref, pauri.punjabi, pauri.translation(lang))
                    },
                    audioId = "japji_pauri_${pauri.pauri}",
                    audio = audio
                )
            }
        }
    }
}
