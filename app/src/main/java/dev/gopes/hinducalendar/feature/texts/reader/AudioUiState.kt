package dev.gopes.hinducalendar.feature.texts.reader

import dev.gopes.hinducalendar.domain.repository.AudioPlaybackState

/**
 * UI-layer snapshot of audio playback state and callbacks.
 * Replaces direct [dev.gopes.hinducalendar.domain.repository.AudioPlaybackRepository] references in composables.
 */
class AudioUiState(
    val playbackState: AudioPlaybackState = AudioPlaybackState.IDLE,
    val currentlyPlayingId: String? = null,
    val playbackProgress: Double = 0.0,
    val currentPositionMs: Int = 0,
    val playbackSpeed: Float = 1.0f,
    val onToggle: (audioId: String) -> Unit = {},
    val onSeek: (positionMs: Int) -> Unit = {},
    val onSkipForward: () -> Unit = {},
    val onSkipBackward: () -> Unit = {},
    val onSetSpeed: (Float) -> Unit = {},
    val hasAudio: (String) -> Boolean = { false },
    val duration: (String) -> Int? = { null }
)
