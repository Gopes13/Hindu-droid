package dev.gopes.hinducalendar.feature.sanskrit

import android.speech.tts.TextToSpeech
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.gopes.hinducalendar.domain.model.*
import dev.gopes.hinducalendar.domain.repository.PreferencesRepository
import dev.gopes.hinducalendar.domain.service.GamificationService
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.Locale
import javax.inject.Inject

data class SanskritUiState(
    val modules: List<SanskritModule> = SanskritData.modules,
    val progress: SanskritProgress = SanskritProgress(),
    val totalLessons: Int = SanskritData.totalLessons,
    val totalLetters: Int = SanskritData.allLetters.size
)

@HiltViewModel
class SanskritViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
    private val gamificationService: GamificationService
) : ViewModel() {

    private val _uiState = MutableStateFlow(SanskritUiState())
    val uiState: StateFlow<SanskritUiState> = _uiState.asStateFlow()

    private var tts: TextToSpeech? = null
    private var ttsReady = false

    init {
        viewModelScope.launch {
            preferencesRepository.preferencesFlow.collect { prefs ->
                _uiState.update { it.copy(progress = prefs.sanskritProgress) }
            }
        }
    }

    fun initTts(engine: TextToSpeech) {
        tts = engine
        ttsReady = true
        tts?.language = Locale("hi", "IN")
        tts?.setSpeechRate(0.4f)
    }

    fun speak(text: String) {
        if (ttsReady) {
            tts?.stop()
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, text.hashCode().toString())
        }
    }

    fun stopTts() {
        tts?.stop()
    }

    fun isModuleUnlocked(moduleIndex: Int): Boolean {
        if (moduleIndex == 0) return true
        val prevModule = SanskritData.modules.getOrNull(moduleIndex - 1) ?: return false
        return prevModule.lessons.all { _uiState.value.progress.isLessonComplete(it.id) }
    }

    fun firstIncompleteLessonId(): String? {
        val progress = _uiState.value.progress
        for ((idx, module) in SanskritData.modules.withIndex()) {
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
                    val module = SanskritData.modules.find { it.id == moduleId }
                    val allDone = module?.lessons?.all { progress.isLessonComplete(it.id) } == true
                    if (allDone) {
                        progress = progress.copy(completedModules = progress.completedModules + moduleId)
                        gamData = gamificationService.rewardSanskritModule(gamData)
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
