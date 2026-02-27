package dev.gopes.hinducalendar.domain.repository

import kotlinx.coroutines.flow.StateFlow

/**
 * Contract for audio playback state and control.
 * Backed by [AudioPlayerService] in production; fake in tests.
 */
interface AudioPlaybackRepository {
    // State
    val currentlyPlayingId: StateFlow<String?>
    val state: StateFlow<AudioPlaybackState>
    val playbackProgress: StateFlow<Double>
    val currentPositionMs: StateFlow<Int>
    val playbackSpeed: StateFlow<Float>

    // Lifecycle
    fun loadManifest()
    fun hasAudio(audioId: String): Boolean
    fun isPlaying(audioId: String): Boolean
    fun audioState(audioId: String): AudioPlaybackState
    fun duration(audioId: String): Int?

    // Playback control
    fun toggle(audioId: String, displayTitle: String? = null)
    fun play(audioId: String, displayTitle: String? = null)
    fun pause()
    fun resume()
    fun stop()
    fun seekTo(positionMs: Int)
    fun skipForward(ms: Int = 15000)
    fun skipBackward(ms: Int = 15000)
    fun setSpeed(speed: Float)
}
