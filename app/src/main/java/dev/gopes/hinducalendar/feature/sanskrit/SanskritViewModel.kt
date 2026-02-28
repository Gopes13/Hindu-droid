package dev.gopes.hinducalendar.feature.sanskrit

import android.speech.tts.TextToSpeech
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.gopes.hinducalendar.domain.model.*
import dev.gopes.hinducalendar.domain.repository.PreferencesRepository
import dev.gopes.hinducalendar.domain.repository.SanskritRepository
import dev.gopes.hinducalendar.domain.service.GamificationService
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.Locale
import javax.inject.Inject

data class SanskritUiState(
    val kandas: List<SanskritKanda> = emptyList(),
    val progress: SanskritProgress = SanskritProgress(),
    val totalLessons: Int = 0,
    val totalLetters: Int = SanskritData.allLetters.size
) {
    /** Backward-compatible: Kāṇḍa 1 modules for the current learn screen. */
    val modules: List<SanskritModule>
        get() = kandas.find { it.id == "kanda_1" }?.modules ?: emptyList()
}

@HiltViewModel
class SanskritViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
    private val gamificationService: GamificationService,
    private val repository: SanskritRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SanskritUiState())
    val uiState: StateFlow<SanskritUiState> = _uiState.asStateFlow()

    private var tts: TextToSpeech? = null
    private var ttsReady = false

    init {
        val kandas = repository.loadKandas()
        _uiState.update {
            it.copy(
                kandas = kandas,
                totalLessons = repository.totalLessons
            )
        }
        viewModelScope.launch {
            preferencesRepository.preferencesFlow.collect { prefs ->
                _uiState.update { it.copy(progress = prefs.sanskritProgress) }
            }
        }
    }

    fun lessonById(lessonId: String): SanskritLesson? = repository.lessonById(lessonId)

    fun initTts(engine: TextToSpeech) {
        tts = engine
        ttsReady = true
        tts?.language = Locale("hi", "IN")
        tts?.setSpeechRate(0.8f)
    }

    fun speak(text: String) {
        if (ttsReady) {
            tts?.stop()
            tts?.setSpeechRate(0.8f)
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, text.hashCode().toString())
        }
    }

    /**
     * Enhanced TTS for teaching: speaks the character clearly at normal speed,
     * pauses, then speaks the example word for context.
     * Like a real teacher: "आ ... आत्मा"
     */
    fun speakForTeaching(character: String, exampleWord: String? = null) {
        if (!ttsReady) return
        tts?.stop()
        tts?.setSpeechRate(1.0f)
        tts?.speak(character, TextToSpeech.QUEUE_FLUSH, null, "teach_char")
        if (exampleWord != null) {
            @Suppress("DEPRECATION")
            tts?.playSilence(500, TextToSpeech.QUEUE_ADD, null)
            tts?.setSpeechRate(0.8f)
            tts?.speak(exampleWord, TextToSpeech.QUEUE_ADD, null, "teach_word")
        }
    }

    fun stopTts() {
        tts?.stop()
    }

    fun isModuleUnlocked(moduleIndex: Int): Boolean {
        val modules = _uiState.value.modules
        if (moduleIndex == 0) return true
        val prevModule = modules.getOrNull(moduleIndex - 1) ?: return false
        return prevModule.lessons.all { _uiState.value.progress.isLessonComplete(it.id) }
    }

    fun firstIncompleteLessonId(): String? {
        val progress = _uiState.value.progress
        val modules = _uiState.value.modules
        for ((idx, module) in modules.withIndex()) {
            if (!isModuleUnlocked(idx)) break
            for (lesson in module.lessons) {
                if (!progress.isLessonComplete(lesson.id)) return lesson.id
            }
        }
        return null
    }

    fun recordLessonCompletion(
        lessonId: String,
        correctCount: Int,
        totalCount: Int,
        masteredLetterIds: List<String>
    ) {
        viewModelScope.launch {
            preferencesRepository.update { prefs ->
                var progress = prefs.sanskritProgress
                var gamData = prefs.gamificationData

                // Mark lesson complete
                progress = progress.copy(
                    completedLessons = progress.completedLessons + lessonId,
                    lastStudyDate = LocalDate.now().toString()
                )

                // Award newly mastered letters (+5 PP each)
                val newLetters = masteredLetterIds.filter { !progress.isLetterMastered(it) }
                if (newLetters.isNotEmpty()) {
                    progress = progress.copy(masteredLetters = progress.masteredLetters + newLetters)
                    for (letter in newLetters) {
                        gamData = gamificationService.rewardSanskritLetter(gamData)
                    }
                }

                // Check module completion (+50 PP)
                val moduleId = lessonId.substringBefore("_l")
                if (!progress.isModuleComplete(moduleId)) {
                    val module = findModuleById(moduleId)
                    val allDone = module?.lessons?.all { progress.isLessonComplete(it.id) } == true
                    if (allDone) {
                        progress = progress.copy(completedModules = progress.completedModules + moduleId)
                        gamData = gamificationService.rewardSanskritModule(gamData)

                        // Check kāṇḍa completion
                        progress = checkKandaCompletion(progress, module.kandaId)
                    }
                }

                // Lesson completion points
                val basePoints = correctCount * 2
                val bonus = if (correctCount == totalCount) 25 else 15
                gamData = gamificationService.rewardSanskritLesson(gamData, basePoints + bonus)

                // Check Sanskrit badges
                gamData = gamificationService.checkSanskritBadges(gamData, progress)

                prefs.copy(sanskritProgress = progress, gamificationData = gamData)
            }
        }
    }

    private fun findModuleById(moduleId: String): SanskritModule? {
        for (kanda in _uiState.value.kandas) {
            val module = kanda.modules.find { it.id == moduleId }
            if (module != null) return module
        }
        return null
    }

    /**
     * After a module is completed, check if all modules in its kāṇḍa are done.
     * If so, mark the kāṇḍa complete, earn its milestone, and advance currentKandaId.
     */
    private fun checkKandaCompletion(progress: SanskritProgress, kandaId: String): SanskritProgress {
        if (kandaId.isBlank() || progress.isKandaComplete(kandaId)) return progress
        val kanda = repository.kandaById(kandaId) ?: return progress
        val allModulesDone = kanda.modules.all { progress.isModuleComplete(it.id) }
        if (!allModulesDone) return progress

        var updated = progress.copy(
            completedKandas = progress.completedKandas + kandaId,
            earnedMilestones = progress.earnedMilestones + kanda.milestoneId
        )

        // Advance to next kāṇḍa
        val nextKanda = repository.loadKandas().find { it.number == kanda.number + 1 }
        if (nextKanda != null) {
            updated = updated.copy(currentKandaId = nextKanda.id)
        }

        return updated
    }

    fun recordVerseExplored(verseId: String) {
        viewModelScope.launch {
            preferencesRepository.update { prefs ->
                val progress = prefs.sanskritProgress
                if (progress.isVerseExplored(verseId)) return@update prefs
                val newProgress = progress.copy(exploredVerses = progress.exploredVerses + verseId)
                val gamData = gamificationService.rewardSanskritVerse(prefs.gamificationData)
                prefs.copy(sanskritProgress = newProgress, gamificationData = gamData)
            }
        }
    }

    override fun onCleared() {
        tts?.stop()
        tts?.shutdown()
        super.onCleared()
    }
}
