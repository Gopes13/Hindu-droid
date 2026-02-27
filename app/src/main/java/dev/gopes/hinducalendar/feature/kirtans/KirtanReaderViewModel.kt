package dev.gopes.hinducalendar.feature.kirtans

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.gopes.hinducalendar.domain.model.AppLanguage
import dev.gopes.hinducalendar.domain.model.Kirtan
import dev.gopes.hinducalendar.domain.repository.PreferencesRepository
import dev.gopes.hinducalendar.domain.repository.AudioPlaybackRepository
import dev.gopes.hinducalendar.domain.repository.KirtanRepository
import dev.gopes.hinducalendar.feature.texts.reader.AudioUiState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class KirtanReaderViewModel @Inject constructor(
    private val kirtanRepository: KirtanRepository,
    private val preferencesRepository: PreferencesRepository,
    private val audioPlaybackRepository: AudioPlaybackRepository
) : ViewModel() {

    var kirtan by mutableStateOf<Kirtan?>(null)
        private set

    val language: StateFlow<AppLanguage> = preferencesRepository.preferencesFlow
        .map { it.language }
        .stateIn(viewModelScope, SharingStarted.Eagerly, AppLanguage.ENGLISH)

    // Stable callback references
    private val audioToggle: (String) -> Unit = { audioPlaybackRepository.toggle(it) }
    private val audioSeek: (Int) -> Unit = { audioPlaybackRepository.seekTo(it) }
    private val audioSkipForward: () -> Unit = { audioPlaybackRepository.skipForward() }
    private val audioSkipBackward: () -> Unit = { audioPlaybackRepository.skipBackward() }
    private val audioSetSpeed: (Float) -> Unit = { audioPlaybackRepository.setSpeed(it) }
    private val audioHasAudio: (String) -> Boolean = { audioPlaybackRepository.hasAudio(it) }
    private val audioDuration: (String) -> Int? = { audioPlaybackRepository.duration(it) }

    val audioUiState: StateFlow<AudioUiState> = combine(
        audioPlaybackRepository.state,
        audioPlaybackRepository.currentlyPlayingId,
        audioPlaybackRepository.playbackProgress,
        audioPlaybackRepository.currentPositionMs,
        audioPlaybackRepository.playbackSpeed
    ) { state, id, progress, position, speed ->
        AudioUiState(
            playbackState = state,
            currentlyPlayingId = id,
            playbackProgress = progress,
            currentPositionMs = position,
            playbackSpeed = speed,
            onToggle = audioToggle,
            onSeek = audioSeek,
            onSkipForward = audioSkipForward,
            onSkipBackward = audioSkipBackward,
            onSetSpeed = audioSetSpeed,
            hasAudio = audioHasAudio,
            duration = audioDuration
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, AudioUiState())

    fun loadKirtan(id: String) {
        kirtan = kirtanRepository.kirtanById(id)
    }

    fun playKirtan() {
        val k = kirtan ?: return
        audioPlaybackRepository.toggle(k.audioId, k.title(language.value))
    }

    override fun onCleared() {
        super.onCleared()
        // Don't stop audio — foreground service manages playback lifecycle
    }
}
