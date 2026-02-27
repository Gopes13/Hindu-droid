package dev.gopes.hinducalendar.engine

import dev.gopes.hinducalendar.domain.repository.AudioPlaybackRepository
import dev.gopes.hinducalendar.domain.repository.AudioPlaybackState

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * In-memory fake for unit testing ViewModels that depend on [AudioPlaybackRepository].
 */
class FakeAudioPlaybackRepository : AudioPlaybackRepository {

    private val _currentlyPlayingId = MutableStateFlow<String?>(null)
    override val currentlyPlayingId: StateFlow<String?> = _currentlyPlayingId.asStateFlow()

    private val _state = MutableStateFlow(AudioPlaybackState.IDLE)
    override val state: StateFlow<AudioPlaybackState> = _state.asStateFlow()

    private val _playbackProgress = MutableStateFlow(0.0)
    override val playbackProgress: StateFlow<Double> = _playbackProgress.asStateFlow()

    private val _currentPositionMs = MutableStateFlow(0)
    override val currentPositionMs: StateFlow<Int> = _currentPositionMs.asStateFlow()

    private val _playbackSpeed = MutableStateFlow(1.0f)
    override val playbackSpeed: StateFlow<Float> = _playbackSpeed.asStateFlow()

    var manifestLoaded = false
    private val audioIds = mutableSetOf<String>()
    private val durations = mutableMapOf<String, Int>()

    fun addAudio(audioId: String, durationMs: Int = 60000) {
        audioIds.add(audioId)
        durations[audioId] = durationMs
    }

    override fun loadManifest() {
        manifestLoaded = true
    }

    override fun hasAudio(audioId: String): Boolean = audioId in audioIds

    override fun isPlaying(audioId: String): Boolean =
        _currentlyPlayingId.value == audioId && _state.value == AudioPlaybackState.PLAYING

    override fun audioState(audioId: String): AudioPlaybackState =
        if (_currentlyPlayingId.value == audioId) _state.value else AudioPlaybackState.IDLE

    override fun duration(audioId: String): Int? = durations[audioId]

    override fun toggle(audioId: String, displayTitle: String?) {
        when {
            _currentlyPlayingId.value == audioId && _state.value == AudioPlaybackState.PLAYING -> pause()
            _currentlyPlayingId.value == audioId && _state.value == AudioPlaybackState.PAUSED -> resume()
            else -> play(audioId, displayTitle)
        }
    }

    override fun play(audioId: String, displayTitle: String?) {
        _currentlyPlayingId.value = audioId
        _state.value = AudioPlaybackState.PLAYING
        _playbackProgress.value = 0.0
        _currentPositionMs.value = 0
    }

    override fun pause() {
        _state.value = AudioPlaybackState.PAUSED
    }

    override fun resume() {
        _state.value = AudioPlaybackState.PLAYING
    }

    override fun stop() {
        _currentlyPlayingId.value = null
        _state.value = AudioPlaybackState.IDLE
        _playbackProgress.value = 0.0
        _currentPositionMs.value = 0
        _playbackSpeed.value = 1.0f
    }

    override fun seekTo(positionMs: Int) {
        _currentPositionMs.value = positionMs
    }

    override fun skipForward(ms: Int) {
        _currentPositionMs.value += ms
    }

    override fun skipBackward(ms: Int) {
        _currentPositionMs.value = (_currentPositionMs.value - ms).coerceAtLeast(0)
    }

    override fun setSpeed(speed: Float) {
        _playbackSpeed.value = speed
    }
}
