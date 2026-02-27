package dev.gopes.hinducalendar.feature.texts.reader

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CropFree
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import dev.gopes.hinducalendar.R
import dev.gopes.hinducalendar.domain.model.*
import dev.gopes.hinducalendar.core.util.localizedName
import dev.gopes.hinducalendar.feature.texts.reader.content.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenericReaderScreen(
    onBack: () -> Unit,
    viewModel: ReaderViewModel = hiltViewModel()
) {
    val textType = viewModel.textType
    val title = if (textType != null) textType.localizedName() else stringResource(R.string.sacred_texts_title)
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
                title = { Text(title) },
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
        when (val state = uiState) {
            is ReaderUiState.Loading -> {
                Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is ReaderUiState.Error -> {
                Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) {
                    Text(state.message)
                }
            }
            is ReaderUiState.Loaded -> {
                val lang = state.language
                val textFileName = viewModel.textType?.jsonFileName ?: ""

                when (state.content) {
                    is TextContent.Episode ->
                        EpisodeContent(state.content.data, lang, Modifier.padding(padding), textFileName, audio)
                    is TextContent.Shloka ->
                        ShlokaContent(state.content.data, lang, Modifier.padding(padding), viewModel::isBookmarked, viewModel::toggleBookmark, textFileName, audio)
                    is TextContent.Verse ->
                        NumberedVerseContent(state.content.data, lang, Modifier.padding(padding), viewModel::isBookmarked, viewModel::toggleBookmark, audio)
                    is TextContent.Chapter ->
                        ChapterContent(state.content.data, lang, Modifier.padding(padding), audio)
                    is TextContent.Rudram ->
                        RudramContent(state.content.data, lang, Modifier.padding(padding), viewModel::isBookmarked, viewModel::toggleBookmark, audio)
                    is TextContent.Gurbani ->
                        GurbaniContent(state.content.data, lang, Modifier.padding(padding), audio)
                    is TextContent.Sukhmani ->
                        SukhmaniContent(state.content.data, lang, Modifier.padding(padding), viewModel::isBookmarked, viewModel::toggleBookmark, audio)
                    is TextContent.Sutra ->
                        SutraContent(state.content.data, lang, Modifier.padding(padding), viewModel::isBookmarked, viewModel::toggleBookmark, audio)
                    is TextContent.Discourse ->
                        DiscourseContent(state.content.data, lang, Modifier.padding(padding))
                    is TextContent.Jain ->
                        JainContent(state.content.data, lang, Modifier.padding(padding), viewModel::isBookmarked, viewModel::toggleBookmark, audio)
                    else ->
                        Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) {
                            Text(stringResource(R.string.reader_unable_to_load))
                        }
                }
            }
        }
    }
}
