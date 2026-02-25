package dev.gopes.hinducalendar.ui.sanskrit

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.gopes.hinducalendar.R
import dev.gopes.hinducalendar.data.model.*
import dev.gopes.hinducalendar.ui.components.ConfettiOverlay
import dev.gopes.hinducalendar.ui.components.SacredCard
import dev.gopes.hinducalendar.ui.components.SacredHighlightCard

private data class QueuedExercise(
    val exercise: SanskritExercise,
    var requeueCount: Int = 0
)

private data class AnswerOption(
    val primary: String,
    val character: String? = null,
    val isCorrect: Boolean
)

private enum class LessonPhase {
    CONTEXT_CARD, QUESTION, FEEDBACK, COMPLETE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SanskritLessonScreen(
    lessonId: String,
    onComplete: (correctCount: Int, totalCount: Int, masteredLetterIds: List<String>) -> Unit,
    onSpeak: (String) -> Unit,
    onBack: () -> Unit
) {
    val lesson = remember {
        SanskritData.modules.flatMap { it.lessons }.find { it.id == lessonId }
    }

    if (lesson == null) {
        onBack()
        return
    }

    var queue by remember { mutableStateOf(lesson.exercises.map { QueuedExercise(it) }.toMutableList()) }
    var currentIndex by remember { mutableIntStateOf(0) }
    var phase by remember { mutableStateOf(LessonPhase.CONTEXT_CARD) }
    var correctCount by remember { mutableIntStateOf(0) }
    var consecutiveWrong by remember { mutableIntStateOf(0) }
    var correctLetterIds by remember { mutableStateOf(setOf<String>()) }
    var showConfetti by remember { mutableStateOf(false) }
    var selectedAnswer by remember { mutableStateOf<AnswerOption?>(null) }
    var feedbackCorrect by remember { mutableStateOf(false) }
    var feedbackExplanation by remember { mutableStateOf("") }

    val totalOriginal = lesson.exercises.size
    val currentExercise = queue.getOrNull(currentIndex)
    val progress = if (queue.isNotEmpty()) (currentIndex.toFloat() / queue.size).coerceIn(0f, 1f) else 1f

    fun buildOptions(exercise: SanskritExercise): List<AnswerOption> {
        return when (exercise) {
            is SanskritExercise.LetterToSound -> {
                val correct = AnswerOption(exercise.letter.pronunciation, exercise.letter.transliteration, true)
                val distractors = exercise.distractors.map { AnswerOption(it.pronunciation, it.transliteration, false) }
                (listOf(correct) + distractors).shuffled()
            }
            is SanskritExercise.SoundToLetter -> {
                val correct = AnswerOption(exercise.letter.character, null, true)
                val distractors = exercise.distractors.map { AnswerOption(it.character, null, false) }
                (listOf(correct) + distractors).shuffled()
            }
            is SanskritExercise.WordMeaning -> {
                val correct = AnswerOption(exercise.word.meaning, null, true)
                val distractors = exercise.distractors.map { AnswerOption(it.meaning, null, false) }
                (listOf(correct) + distractors).shuffled()
            }
            is SanskritExercise.SyllableToSound -> {
                val correct = AnswerOption(exercise.syllable.pronunciation, exercise.syllable.transliteration, true)
                val distractors = exercise.distractors.map { AnswerOption(it.pronunciation, it.transliteration, false) }
                (listOf(correct) + distractors).shuffled()
            }
            is SanskritExercise.SoundToSyllable -> {
                val correct = AnswerOption(exercise.syllable.script, null, true)
                val distractors = exercise.distractors.map { AnswerOption(it.script, null, false) }
                (listOf(correct) + distractors).shuffled()
            }
            is SanskritExercise.WordReading -> {
                val correct = AnswerOption(exercise.word.meaning, null, true)
                val distractors = exercise.distractors.map { AnswerOption(it, null, false) }
                (listOf(correct) + distractors).shuffled()
            }
        }
    }

    fun questionText(exercise: SanskritExercise): String {
        return when (exercise) {
            is SanskritExercise.LetterToSound -> "What sound does ${exercise.letter.character} make?"
            is SanskritExercise.SoundToLetter -> "Which letter makes the sound '${exercise.letter.transliteration}'?"
            is SanskritExercise.WordMeaning -> "What does ${exercise.word.sanskrit} mean?"
            is SanskritExercise.SyllableToSound -> "How is ${exercise.syllable.script} pronounced?"
            is SanskritExercise.SoundToSyllable -> "Which syllable is '${exercise.syllable.transliteration}'?"
            is SanskritExercise.WordReading -> "Read ${exercise.word.sanskrit} — what does it mean?"
        }
    }

    fun handleAnswer(option: AnswerOption) {
        selectedAnswer = option
        feedbackCorrect = option.isCorrect
        val item = queue[currentIndex]

        if (option.isCorrect) {
            correctCount++
            consecutiveWrong = 0
            currentExercise?.exercise?.targetLetterId?.let { correctLetterIds = correctLetterIds + it }
            showConfetti = true
            feedbackExplanation = "Correct!"
        } else {
            consecutiveWrong++
            feedbackExplanation = "Not quite — the correct answer was highlighted."
            // Re-queue wrong answers (max 2 times)
            if (item.requeueCount < 2) {
                item.requeueCount++
                val insertAt = (currentIndex + 2).coerceAtMost(queue.size)
                queue.add(insertAt, item.copy())
            }
        }
        phase = LessonPhase.FEEDBACK
    }

    fun advance() {
        selectedAnswer = null
        currentIndex++
        if (currentIndex >= queue.size) {
            phase = LessonPhase.COMPLETE
            onComplete(correctCount, totalOriginal, correctLetterIds.toList())
        } else {
            phase = LessonPhase.QUESTION
            // Auto-speak for sound exercises
            val ex = queue[currentIndex].exercise
            if (ex is SanskritExercise.SoundToLetter) onSpeak(ex.letter.character)
            if (ex is SanskritExercise.SoundToSyllable) onSpeak(ex.syllable.script)
        }
    }

    Box {
        Scaffold { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // Progress bar
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().height(4.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                )

                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.Close, stringResource(R.string.common_close))
                    }
                    Text(
                        lesson.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        "${currentIndex + 1}/${queue.size}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Content
                when (phase) {
                    LessonPhase.CONTEXT_CARD -> {
                        ContextCardView(
                            card = lesson.contextCard,
                            onSpeak = onSpeak,
                            onStart = {
                                phase = LessonPhase.QUESTION
                                val ex = queue.firstOrNull()?.exercise
                                if (ex is SanskritExercise.SoundToLetter) onSpeak(ex.letter.character)
                                if (ex is SanskritExercise.SoundToSyllable) onSpeak(ex.syllable.script)
                            }
                        )
                    }

                    LessonPhase.QUESTION -> {
                        if (currentExercise != null) {
                            val options = remember(currentIndex) { buildOptions(currentExercise.exercise) }
                            QuestionView(
                                question = questionText(currentExercise.exercise),
                                options = options,
                                onAnswer = { handleAnswer(it) }
                            )
                        }
                    }

                    LessonPhase.FEEDBACK -> {
                        if (currentExercise != null) {
                            val options = remember(currentIndex) { buildOptions(currentExercise.exercise) }
                            FeedbackView(
                                isCorrect = feedbackCorrect,
                                explanation = feedbackExplanation,
                                options = options,
                                selectedAnswer = selectedAnswer,
                                onNext = { advance() }
                            )
                        }
                    }

                    LessonPhase.COMPLETE -> {
                        CompletionView(
                            correctCount = correctCount,
                            totalCount = totalOriginal,
                            masteredLetterIds = correctLetterIds,
                            onDone = onBack
                        )
                    }
                }
            }
        }

        ConfettiOverlay(isActive = showConfetti, onFinished = { showConfetti = false })
    }
}

@Composable
private fun ContextCardView(
    card: SanskritContextCard,
    onSpeak: (String) -> Unit,
    onStart: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        SacredHighlightCard {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    card.sanskrit,
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center,
                    lineHeight = 40.sp
                )
                Text(
                    card.transliteration,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                IconButton(onClick = { onSpeak(card.sanskrit) }) {
                    Icon(
                        Icons.Filled.VolumeUp,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                HorizontalDivider()
                Text(
                    card.note,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = onStart,
            modifier = Modifier.fillMaxWidth(0.6f)
        ) {
            Text(stringResource(R.string.sanskrit_start_lesson))
        }
    }
}

@Composable
private fun QuestionView(
    question: String,
    options: List<AnswerOption>,
    onAnswer: (AnswerOption) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            question,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Medium
        )

        Spacer(Modifier.height(32.dp))

        // 2x2 grid
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            for (row in options.chunked(2)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    for (option in row) {
                        OutlinedCard(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onAnswer(option) },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                if (option.character != null) {
                                    Text(
                                        option.character,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(Modifier.height(4.dp))
                                }
                                Text(
                                    option.primary,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                    // Pad if odd number
                    if (row.size < 2) {
                        Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun FeedbackView(
    isCorrect: Boolean,
    explanation: String,
    options: List<AnswerOption>,
    selectedAnswer: AnswerOption?,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            if (isCorrect) Icons.Filled.CheckCircle else Icons.Filled.Cancel,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = if (isCorrect) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
        )
        Spacer(Modifier.height(12.dp))
        Text(
            explanation,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(24.dp))

        // Show options with highlights
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            for (option in options) {
                val bgColor = when {
                    option.isCorrect -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    option == selectedAnswer && !option.isCorrect -> MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                    else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = bgColor
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            option.primary,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                        if (option.isCorrect) {
                            Icon(
                                Icons.Filled.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth(0.6f)
        ) {
            Text(stringResource(R.string.sanskrit_next))
        }
    }
}

@Composable
private fun CompletionView(
    correctCount: Int,
    totalCount: Int,
    masteredLetterIds: Set<String>,
    onDone: () -> Unit
) {
    val isPerfect = correctCount == totalCount
    val points = correctCount * 2 + if (isPerfect) 25 else 15

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(if (isPerfect) "\\uD83C\\uDF1F" else "\\uD83D\\uDC4F", fontSize = 48.sp)
        Spacer(Modifier.height(16.dp))
        Text(
            if (isPerfect) stringResource(R.string.sanskrit_perfect) else stringResource(R.string.sanskrit_well_done),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "$correctCount / $totalCount correct",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))
        Surface(
            shape = RoundedCornerShape(50),
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Text(
                "+$points PP",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        if (masteredLetterIds.isNotEmpty()) {
            Spacer(Modifier.height(24.dp))
            Text(
                stringResource(R.string.sanskrit_letters_learned),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                masteredLetterIds.take(8).forEach { letterId ->
                    val letter = SanskritData.letterById(letterId)
                    if (letter != null) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFFFD700).copy(alpha = 0.15f)
                        ) {
                            Text(
                                letter.character,
                                modifier = Modifier.padding(8.dp),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(32.dp))

        Button(
            onClick = onDone,
            modifier = Modifier.fillMaxWidth(0.6f)
        ) {
            Text(stringResource(R.string.sanskrit_done))
        }
    }
}
