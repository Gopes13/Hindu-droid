package dev.gopes.hinducalendar.ui.sanskrit

import android.speech.tts.TextToSpeech
import androidx.compose.runtime.DisposableEffect
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import dev.gopes.hinducalendar.R
import dev.gopes.hinducalendar.data.model.SanskritModule
import dev.gopes.hinducalendar.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SanskritLearnScreen(
    onLessonClick: (String) -> Unit,
    onVerseClick: () -> Unit = {},
    onBack: () -> Unit,
    viewModel: SanskritViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAlphabet by remember { mutableStateOf(false) }

    // Initialize TTS
    val context = LocalContext.current
    DisposableEffect(Unit) {
        var ttsEngine: TextToSpeech? = null
        ttsEngine = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                ttsEngine?.let { viewModel.initTts(it) }
            }
        }
        onDispose { }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.sanskrit_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back))
                    }
                },
                actions = {
                    IconButton(onClick = { showAlphabet = true }) {
                        Icon(Icons.Filled.GridView, stringResource(R.string.sanskrit_alphabet_ref))
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
            // Progress banner
            item(key = "progress") {
                SacredHighlightCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Text(
                            "\u0950",
                            fontSize = 44.sp,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        )
                        ProgressStat(
                            label = stringResource(R.string.sanskrit_letters_mastered),
                            value = "${uiState.progress.lettersCount}",
                            total = "${uiState.totalLetters}"
                        )
                        ProgressStat(
                            label = stringResource(R.string.sanskrit_lessons_done),
                            value = "${uiState.progress.lessonsCount}",
                            total = "${uiState.totalLessons}"
                        )
                    }
                }
            }

            // Today's lesson CTA
            val nextLesson = viewModel.firstIncompleteLessonId()
            if (nextLesson != null) {
                item(key = "next_lesson") {
                    SacredCard(
                        modifier = Modifier.clickable { onLessonClick(nextLesson) }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.PlayCircle,
                                contentDescription = null,
                                modifier = Modifier.size(32.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    stringResource(R.string.sanskrit_continue_learning),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    stringResource(R.string.sanskrit_next_lesson),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Icon(
                                Icons.Filled.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Module list
            itemsIndexed(uiState.modules, key = { idx, m -> m.id }) { index, module ->
                val isUnlocked = viewModel.isModuleUnlocked(index)
                val isComplete = uiState.progress.isModuleComplete(module.id)

                ModuleCard(
                    module = module,
                    index = index + 1,
                    isUnlocked = isUnlocked,
                    isComplete = isComplete,
                    progress = uiState.progress,
                    onLessonClick = { if (isUnlocked) onLessonClick(it) }
                )
            }

            // Verse reader card (unlock after module 8)
            val verseUnlocked = uiState.progress.isModuleComplete("module8") ||
                    uiState.progress.completedModules.size >= 8
            if (verseUnlocked) {
                item(key = "verses") {
                    Spacer(Modifier.height(8.dp))
                    SacredCard(
                        modifier = Modifier.clickable { onVerseClick() }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.AutoStories,
                                contentDescription = null,
                                modifier = Modifier.size(32.dp),
                                tint = MaterialTheme.colorScheme.tertiary
                            )
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    stringResource(R.string.sanskrit_verse_reader),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    stringResource(R.string.sanskrit_verse_reader_desc),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Icon(
                                Icons.Filled.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAlphabet) {
        SanskritAlphabetSheet(
            progress = uiState.progress,
            onSpeak = { viewModel.speak(it) },
            onDismiss = { showAlphabet = false }
        )
    }
}

@Composable
private fun ProgressStat(label: String, value: String, total: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text(" / $total", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun ModuleCard(
    module: SanskritModule,
    index: Int,
    isUnlocked: Boolean,
    isComplete: Boolean,
    progress: dev.gopes.hinducalendar.data.model.SanskritProgress,
    onLessonClick: (String) -> Unit
) {
    val alpha = if (isUnlocked) 1f else 0.5f

    SacredCard(isHighlighted = isComplete) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Module header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isComplete) MaterialTheme.colorScheme.tertiary
                    else if (isUnlocked) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                ) {
                    Text(
                        "$index",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.surface
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f).then(Modifier.let { if (alpha < 1f) it else it })) {
                    Text(
                        module.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        module.titleSanskrit,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (!isUnlocked) {
                    Icon(
                        Icons.Filled.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else if (isComplete) {
                    Icon(
                        Icons.Filled.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                }
            }

            // Lesson rows (only show if unlocked)
            if (isUnlocked) {
                Spacer(Modifier.height(8.dp))
                module.lessons.forEach { lesson ->
                    val lessonDone = progress.isLessonComplete(lesson.id)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onLessonClick(lesson.id) }
                            .padding(vertical = 6.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (lessonDone) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = if (lessonDone) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            lesson.title,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (lessonDone) MaterialTheme.colorScheme.onSurfaceVariant
                            else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}
