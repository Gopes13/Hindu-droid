package dev.gopes.hinducalendar.feature.sanskrit

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.gopes.hinducalendar.R
import dev.gopes.hinducalendar.domain.model.*
import dev.gopes.hinducalendar.core.ui.components.ConfettiOverlay
import dev.gopes.hinducalendar.core.ui.components.SacredCard
import dev.gopes.hinducalendar.core.ui.components.SacredHighlightCard
import kotlinx.coroutines.delay

// ── Data models ──────────────────────────────────────────────────────────────

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
    CONTEXT_CARD, TEACH, PRACTICE, QUIZ, QUIZ_FEEDBACK, COMPLETE
}

/** Normalized teaching item — wraps letter, word, or syllable for the teach UI. */
private sealed class TeachItem {
    abstract val displayChar: String
    abstract val transliteration: String
    abstract val speakText: String
    /** Example word for contextual TTS (letter → exampleWord, others null). */
    abstract val exampleWord: String?

    data class LetterItem(val letter: SanskritLetter) : TeachItem() {
        override val displayChar = letter.character
        override val transliteration = letter.transliteration
        override val speakText = letter.character
        override val exampleWord = letter.exampleWord
    }
    data class WordItem(val word: SanskritWord) : TeachItem() {
        override val displayChar = word.sanskrit
        override val transliteration = word.transliteration
        override val speakText = word.sanskrit
        override val exampleWord: String? = null
    }
    data class SyllableItem(val syllable: SanskritSyllable) : TeachItem() {
        override val displayChar = syllable.script
        override val transliteration = syllable.transliteration
        override val speakText = syllable.script
        override val exampleWord: String? = null
    }
}

/** A step in the teach sequence — either showing an item or a quick check. */
private sealed class TeachStep {
    data class ShowItem(val item: TeachItem, val itemIndex: Int, val totalItems: Int) : TeachStep()
    data class QuickCheck(val target: TeachItem, val distractor: TeachItem) : TeachStep()
}

/** A 2-choice practice question. */
private data class PracticeQuestion(
    val questionText: String,
    val option1: String,
    val option2: String,
    val correctIndex: Int  // 0 or 1
)

// ── Helper functions ─────────────────────────────────────────────────────────

/** Extract unique teachable items from exercises. */
private fun extractTeachItems(exercises: List<SanskritExercise>): List<TeachItem> {
    val seen = mutableSetOf<String>()
    return exercises.mapNotNull { ex ->
        when (ex) {
            is SanskritExercise.LetterToSound -> TeachItem.LetterItem(ex.letter)
            is SanskritExercise.SoundToLetter -> TeachItem.LetterItem(ex.letter)
            is SanskritExercise.WordMeaning -> TeachItem.WordItem(ex.word)
            is SanskritExercise.WordReading -> TeachItem.WordItem(ex.word)
            is SanskritExercise.SyllableToSound -> TeachItem.SyllableItem(ex.syllable)
            is SanskritExercise.SoundToSyllable -> TeachItem.SyllableItem(ex.syllable)
            else -> null
        }
    }.filter { seen.add(it.displayChar) }
}

/** Build teach sequence: show items one at a time, with quick checks after every 2. */
private fun buildTeachSequence(items: List<TeachItem>): List<TeachStep> {
    val steps = mutableListOf<TeachStep>()
    items.forEachIndexed { i, item ->
        steps.add(TeachStep.ShowItem(item, i, items.size))
        // After every 2nd item, add a quick check
        if (i % 2 == 1 && i >= 1) {
            steps.add(TeachStep.QuickCheck(target = items[i - 1], distractor = items[i]))
        }
    }
    return steps
}

/** Generate 2-choice practice questions from teach items. */
private fun buildPracticeQuestions(items: List<TeachItem>): List<PracticeQuestion> {
    if (items.size < 2) return emptyList()
    val questions = mutableListOf<PracticeQuestion>()

    fun addPair(a: TeachItem, b: TeachItem) {
        // Direction 1: show transliteration, pick character
        val q1Correct = if ((0..1).random() == 0) 0 else 1
        questions.add(PracticeQuestion(
            questionText = "Which is '${a.transliteration}'?",
            option1 = if (q1Correct == 0) a.displayChar else b.displayChar,
            option2 = if (q1Correct == 0) b.displayChar else a.displayChar,
            correctIndex = q1Correct
        ))
        // Direction 2: show character, pick transliteration
        val q2Correct = if ((0..1).random() == 0) 0 else 1
        questions.add(PracticeQuestion(
            questionText = "What is ${b.displayChar}?",
            option1 = if (q2Correct == 0) b.transliteration else a.transliteration,
            option2 = if (q2Correct == 0) a.transliteration else b.transliteration,
            correctIndex = q2Correct
        ))
    }

    // Adjacent pairs: (0,1), (2,3), (4,5)...
    for (i in 0 until items.size - 1 step 2) {
        addPair(items[i], items[i + 1])
    }
    // Cross pairs for more mixing: (0,2), (1,3)...
    for (i in 0 until items.size - 2 step 2) {
        if (i + 2 < items.size) addPair(items[i], items[i + 2])
    }

    return questions.shuffled()
}

// ── Main Screen ──────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SanskritLessonScreen(
    lessonId: String,
    onComplete: (correctCount: Int, totalCount: Int, masteredLetterIds: List<String>) -> Unit,
    onSpeak: (String) -> Unit,
    onSpeakForTeaching: (character: String, exampleWord: String?) -> Unit,
    onBack: () -> Unit,
    lessonResolver: (String) -> SanskritLesson? = { id ->
        SanskritData.modules.flatMap { it.lessons }.find { it.id == id }
    }
) {
    val lesson = remember { lessonResolver(lessonId) }

    if (lesson == null) {
        onBack()
        return
    }

    // ── Teach phase state ──
    val teachItems = remember { extractTeachItems(lesson.exercises) }
    val teachSteps = remember { buildTeachSequence(teachItems) }
    var teachIndex by remember { mutableIntStateOf(0) }

    // ── Practice phase state ──
    val practiceQuestions = remember { buildPracticeQuestions(teachItems) }
    var practiceIndex by remember { mutableIntStateOf(0) }
    var practiceAnswer by remember { mutableStateOf<Int?>(null) }

    // ── Quiz phase state ──
    var queue by remember { mutableStateOf(lesson.exercises.map { QueuedExercise(it) }.toMutableList()) }
    var quizIndex by remember { mutableIntStateOf(0) }
    var phase by remember { mutableStateOf(LessonPhase.CONTEXT_CARD) }
    var correctCount by remember { mutableIntStateOf(0) }
    var correctLetterIds by remember { mutableStateOf(setOf<String>()) }
    var showConfetti by remember { mutableStateOf(false) }
    var selectedAnswer by remember { mutableStateOf<AnswerOption?>(null) }
    var feedbackCorrect by remember { mutableStateOf(false) }
    var feedbackExplanation by remember { mutableStateOf("") }

    val totalOriginal = lesson.exercises.size
    val currentExercise = queue.getOrNull(quizIndex)

    // Overall progress across all phases
    val totalSteps = teachSteps.size + practiceQuestions.size + queue.size
    val currentStep = when (phase) {
        LessonPhase.CONTEXT_CARD -> 0
        LessonPhase.TEACH -> teachIndex
        LessonPhase.PRACTICE -> teachSteps.size + practiceIndex
        LessonPhase.QUIZ, LessonPhase.QUIZ_FEEDBACK -> teachSteps.size + practiceQuestions.size + quizIndex
        LessonPhase.COMPLETE -> totalSteps
    }
    val overallProgress = if (totalSteps > 0) (currentStep.toFloat() / totalSteps).coerceIn(0f, 1f) else 0f

    // Phase label for header
    val phaseLabel = when (phase) {
        LessonPhase.CONTEXT_CARD -> "Learn"
        LessonPhase.TEACH -> "Learn"
        LessonPhase.PRACTICE -> "Practice"
        LessonPhase.QUIZ, LessonPhase.QUIZ_FEEDBACK -> "Quiz"
        LessonPhase.COMPLETE -> "Done"
    }

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
            else -> emptyList()
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
            else -> ""
        }
    }

    fun handleQuizAnswer(option: AnswerOption) {
        selectedAnswer = option
        feedbackCorrect = option.isCorrect
        val item = queue[quizIndex]

        if (option.isCorrect) {
            correctCount++
            currentExercise?.exercise?.targetLetterId?.let { correctLetterIds = correctLetterIds + it }
            showConfetti = true
            feedbackExplanation = "Correct!"
        } else {
            feedbackExplanation = "Not quite — the correct answer is highlighted."
            if (item.requeueCount < 2) {
                item.requeueCount++
                val insertAt = (quizIndex + 2).coerceAtMost(queue.size)
                queue.add(insertAt, item.copy())
            }
        }
        phase = LessonPhase.QUIZ_FEEDBACK
    }

    fun advanceQuiz() {
        selectedAnswer = null
        quizIndex++
        if (quizIndex >= queue.size) {
            phase = LessonPhase.COMPLETE
            onComplete(correctCount, totalOriginal, correctLetterIds.toList())
        } else {
            phase = LessonPhase.QUIZ
            val ex = queue[quizIndex].exercise
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
                    progress = { overallProgress },
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
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            lesson.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            phaseLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Text(
                        "${currentStep + 1}/$totalSteps",
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
                                if (teachSteps.isNotEmpty()) {
                                    phase = LessonPhase.TEACH
                                    // TeachCardView's LaunchedEffect handles auto-speak
                                } else {
                                    phase = LessonPhase.QUIZ
                                }
                            }
                        )
                    }

                    LessonPhase.TEACH -> {
                        val step = teachSteps.getOrNull(teachIndex)
                        if (step != null) {
                            when (step) {
                                is TeachStep.ShowItem -> {
                                    TeachCardView(
                                        item = step.item,
                                        itemIndex = step.itemIndex,
                                        totalItems = step.totalItems,
                                        onSpeakForTeaching = onSpeakForTeaching,
                                        onContinue = {
                                            teachIndex++
                                            if (teachIndex >= teachSteps.size) {
                                                // Move to practice
                                                if (practiceQuestions.isNotEmpty()) {
                                                    phase = LessonPhase.PRACTICE
                                                } else {
                                                    phase = LessonPhase.QUIZ
                                                }
                                            }
                                            // TeachCardView's LaunchedEffect handles auto-speak
                                        }
                                    )
                                }
                                is TeachStep.QuickCheck -> {
                                    QuickCheckView(
                                        target = step.target,
                                        distractor = step.distractor,
                                        onSpeak = onSpeak,
                                        onDone = {
                                            teachIndex++
                                            if (teachIndex >= teachSteps.size) {
                                                if (practiceQuestions.isNotEmpty()) {
                                                    phase = LessonPhase.PRACTICE
                                                } else {
                                                    phase = LessonPhase.QUIZ
                                                }
                                            }
                                            // TeachCardView's LaunchedEffect handles auto-speak
                                        }
                                    )
                                }
                            }
                        }
                    }

                    LessonPhase.PRACTICE -> {
                        val question = practiceQuestions.getOrNull(practiceIndex)
                        if (question != null) {
                            PracticeView(
                                question = question,
                                questionIndex = practiceIndex,
                                totalQuestions = practiceQuestions.size,
                                selectedAnswer = practiceAnswer,
                                onAnswer = { chosen ->
                                    practiceAnswer = chosen
                                },
                                onNext = {
                                    practiceAnswer = null
                                    practiceIndex++
                                    if (practiceIndex >= practiceQuestions.size) {
                                        phase = LessonPhase.QUIZ
                                        val ex = queue.firstOrNull()?.exercise
                                        if (ex is SanskritExercise.SoundToLetter) onSpeak(ex.letter.character)
                                        if (ex is SanskritExercise.SoundToSyllable) onSpeak(ex.syllable.script)
                                    }
                                }
                            )
                        }
                    }

                    LessonPhase.QUIZ -> {
                        if (currentExercise != null) {
                            val options = remember(quizIndex) { buildOptions(currentExercise.exercise) }
                            QuestionView(
                                question = questionText(currentExercise.exercise),
                                options = options,
                                onAnswer = { handleQuizAnswer(it) }
                            )
                        }
                    }

                    LessonPhase.QUIZ_FEEDBACK -> {
                        if (currentExercise != null) {
                            val options = remember(quizIndex) { buildOptions(currentExercise.exercise) }
                            FeedbackView(
                                isCorrect = feedbackCorrect,
                                explanation = feedbackExplanation,
                                options = options,
                                selectedAnswer = selectedAnswer,
                                onNext = { advanceQuiz() }
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

// ── Context Card (existing — overview) ───────────────────────────────────────

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
                        contentDescription = stringResource(R.string.cd_pronounce),
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

// ── Teach Card — introduces one item at a time ──────────────────────────────

@Composable
private fun TeachCardView(
    item: TeachItem,
    itemIndex: Int,
    totalItems: Int,
    onSpeakForTeaching: (character: String, exampleWord: String?) -> Unit,
    onContinue: () -> Unit
) {
    // Auto-play TTS when this card appears — speaks character, then example word
    LaunchedEffect(item.speakText) {
        delay(300)
        onSpeakForTeaching(item.speakText, item.exampleWord)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Large character
        Text(
            item.displayChar,
            fontSize = 72.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(Modifier.height(8.dp))

        // Transliteration
        Text(
            item.transliteration,
            fontSize = 24.sp,
            fontStyle = FontStyle.Italic,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(16.dp))

        // Type-specific details
        when (item) {
            is TeachItem.LetterItem -> {
                // Pronunciation guide
                Text(
                    item.letter.pronunciation,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(16.dp))

                // Example word card
                SacredCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            item.letter.exampleWord,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                item.letter.exampleTranslit,
                                style = MaterialTheme.typography.bodyMedium,
                                fontStyle = FontStyle.Italic,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                item.letter.exampleMeaning,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
            is TeachItem.WordItem -> {
                // Word meaning
                Text(
                    "\"${item.word.meaning}\"",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    fontStyle = FontStyle.Italic
                )
            }
            is TeachItem.SyllableItem -> {
                // Base + mark breakdown
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        item.syllable.base,
                        fontSize = 28.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "  +  ",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        item.syllable.markDisplay,
                        fontSize = 28.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "  (${item.syllable.markName})",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // Listen button — speaks character then example word
        OutlinedButton(onClick = { onSpeakForTeaching(item.speakText, item.exampleWord) }) {
            Icon(
                Icons.Filled.VolumeUp,
                contentDescription = stringResource(R.string.cd_pronounce),
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text("Listen")
        }

        Spacer(Modifier.height(24.dp))

        // Counter
        Text(
            "${itemIndex + 1} of $totalItems",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(12.dp))

        // Continue button
        Button(
            onClick = onContinue,
            modifier = Modifier.fillMaxWidth(0.6f)
        ) {
            Text("Continue")
            Spacer(Modifier.width(4.dp))
            Icon(Icons.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
        }
    }
}

// ── Quick Check — 2-choice inline after teaching a pair ──────────────────────

@Composable
private fun QuickCheckView(
    target: TeachItem,
    distractor: TeachItem,
    onSpeak: (String) -> Unit,
    onDone: () -> Unit
) {
    var answered by remember { mutableStateOf(false) }
    var selectedCorrectly by remember { mutableStateOf(false) }
    val options = remember { listOf(target, distractor).shuffled() }
    val correctIndex = options.indexOf(target)

    // Auto-advance after feedback
    LaunchedEffect(answered) {
        if (answered) {
            delay(1200)
            onDone()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Phase label
        Surface(
            shape = RoundedCornerShape(50),
            color = MaterialTheme.colorScheme.secondaryContainer
        ) {
            Text(
                "Quick Check",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }

        Spacer(Modifier.height(24.dp))

        Text(
            "Which is '${target.transliteration}'?",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Medium
        )

        Spacer(Modifier.height(32.dp))

        // Two large option buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            options.forEachIndexed { idx, option ->
                val isCorrectOption = idx == correctIndex
                val bgColor = when {
                    !answered -> MaterialTheme.colorScheme.surfaceVariant
                    isCorrectOption -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                    !isCorrectOption && !selectedCorrectly -> MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                    else -> MaterialTheme.colorScheme.surfaceVariant
                }
                val borderColor = when {
                    !answered -> Color.Transparent
                    isCorrectOption -> MaterialTheme.colorScheme.primary
                    else -> Color.Transparent
                }

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .then(
                            if (!answered) Modifier.clickable {
                                selectedCorrectly = isCorrectOption
                                answered = true
                                if (isCorrectOption) onSpeak(option.speakText)
                            } else Modifier
                        )
                        .border(2.dp, borderColor, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    color = bgColor
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            option.displayChar,
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Feedback
        if (answered) {
            Spacer(Modifier.height(16.dp))
            Text(
                if (selectedCorrectly) "Yes!" else "It's ${target.displayChar}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = if (selectedCorrectly) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.error
            )
        }
    }
}

// ── Practice View — 2-choice reinforcement questions ─────────────────────────

@Composable
private fun PracticeView(
    question: PracticeQuestion,
    questionIndex: Int,
    totalQuestions: Int,
    selectedAnswer: Int?,
    onAnswer: (Int) -> Unit,
    onNext: () -> Unit
) {
    val isAnswered = selectedAnswer != null
    val isCorrect = selectedAnswer == question.correctIndex

    // Auto-advance after feedback
    LaunchedEffect(isAnswered) {
        if (isAnswered) {
            delay(1200)
            onNext()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Phase label
        Surface(
            shape = RoundedCornerShape(50),
            color = MaterialTheme.colorScheme.tertiaryContainer
        ) {
            Text(
                "Practice ${questionIndex + 1}/$totalQuestions",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
        }

        Spacer(Modifier.height(24.dp))

        Text(
            question.questionText,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Medium
        )

        Spacer(Modifier.height(32.dp))

        // Two option buttons
        val options = listOf(question.option1, question.option2)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            options.forEachIndexed { idx, optionText ->
                val isCorrectOption = idx == question.correctIndex
                val isSelected = selectedAnswer == idx
                val bgColor = when {
                    !isAnswered -> MaterialTheme.colorScheme.surfaceVariant
                    isCorrectOption -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                    isSelected -> MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                    else -> MaterialTheme.colorScheme.surfaceVariant
                }
                val borderColor = when {
                    !isAnswered -> Color.Transparent
                    isCorrectOption -> MaterialTheme.colorScheme.primary
                    else -> Color.Transparent
                }

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .then(
                            if (!isAnswered) Modifier.clickable { onAnswer(idx) }
                            else Modifier
                        )
                        .border(2.dp, borderColor, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    color = bgColor
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            optionText,
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // Feedback
        if (isAnswered) {
            Spacer(Modifier.height(16.dp))
            Text(
                if (isCorrect) "Yes!" else "The answer is ${options[question.correctIndex]}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = if (isCorrect) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.error
            )
        }
    }
}

// ── Quiz View — existing 4-choice test ───────────────────────────────────────

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
        // Quiz phase label
        Surface(
            shape = RoundedCornerShape(50),
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Text(
                "Quiz",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        Spacer(Modifier.height(24.dp))

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
                    if (row.size < 2) {
                        Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

// ── Feedback View — shows correct/incorrect after quiz answer ────────────────

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
            contentDescription = stringResource(R.string.cd_answer_correct),
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
                                contentDescription = stringResource(R.string.cd_answer_correct),
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

// ── Completion View — results ────────────────────────────────────────────────

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
        Text(if (isPerfect) "\uD83C\uDF1F" else "\uD83D\uDC4F", fontSize = 48.sp)
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
