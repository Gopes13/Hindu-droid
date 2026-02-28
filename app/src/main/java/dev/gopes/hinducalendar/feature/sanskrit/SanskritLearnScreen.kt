package dev.gopes.hinducalendar.feature.sanskrit

import android.speech.tts.TextToSpeech
import androidx.compose.runtime.DisposableEffect
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import dev.gopes.hinducalendar.R
import dev.gopes.hinducalendar.domain.model.SanskritModule
import dev.gopes.hinducalendar.core.ui.components.*
import dev.gopes.hinducalendar.core.ui.theme.SacredTypography

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
                                contentDescription = stringResource(R.string.cd_start_lesson),
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
                                contentDescription = stringResource(R.string.cd_open_text),
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
                val isCurrent = isUnlocked && !isComplete

                ModuleCard(
                    module = module,
                    index = index + 1,
                    isUnlocked = isUnlocked,
                    isComplete = isComplete,
                    isCurrent = isCurrent,
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
                                contentDescription = stringResource(R.string.cd_verse_reader),
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
                                contentDescription = stringResource(R.string.cd_open_text),
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
    isCurrent: Boolean,
    progress: dev.gopes.hinducalendar.domain.model.SanskritProgress,
    onLessonClick: (String) -> Unit
) {
    val completedCount = module.lessons.count { progress.isLessonComplete(it.id) }
    val totalCount = module.lessons.size
    var expanded by remember { mutableStateOf(isCurrent) }

    val badgeColor = when {
        isComplete -> MaterialTheme.colorScheme.tertiary
        isUnlocked -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
    }

    SacredCard(isHighlighted = isComplete) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // ── Header: Emoji circle + Title + Status icon ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Emoji circle with number overlay
                Box(contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(badgeColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            module.emoji,
                            fontSize = 22.sp,
                            color = if (isUnlocked) MaterialTheme.colorScheme.onSurface
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    // Small number badge
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(badgeColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "$index",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.surface
                        )
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
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
                        contentDescription = stringResource(R.string.cd_module_locked),
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else if (isComplete) {
                    Icon(
                        Icons.Filled.CheckCircle,
                        contentDescription = stringResource(R.string.cd_module_complete),
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                }
            }

            // ── Sanskrit preview row ──
            if (isUnlocked && module.lessons.isNotEmpty()) {
                val previewText = module.lessons.first().contextCard.sanskrit
                Spacer(Modifier.height(10.dp))
                Text(
                    text = previewText,
                    style = SacredTypography.sacredLarge,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // ── Progress bar ──
            if (isUnlocked) {
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LinearProgressIndicator(
                        progress = { if (totalCount > 0) completedCount.toFloat() / totalCount else 0f },
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = if (isComplete) MaterialTheme.colorScheme.tertiary
                        else MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "$completedCount/$totalCount",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // ── Expandable lesson list ──
            if (isUnlocked) {
                Spacer(Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expanded = !expanded }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                        contentDescription = if (expanded) "Collapse" else "Expand",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        if (expanded) "Lessons" else "$totalCount lessons",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                AnimatedVisibility(
                    visible = expanded,
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    Column {
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
                                    contentDescription = if (lessonDone) stringResource(R.string.cd_lesson_complete)
                                    else stringResource(R.string.cd_lesson_incomplete),
                                    modifier = Modifier.size(16.dp),
                                    tint = if (lessonDone) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    lesson.title,
                                    modifier = Modifier.weight(1f),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (lessonDone) MaterialTheme.colorScheme.onSurfaceVariant
                                    else MaterialTheme.colorScheme.onSurface
                                )
                                // Sanskrit hint from context card
                                val hint = lesson.contextCard.sanskrit.take(12)
                                Text(
                                    hint,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
