package dev.gopes.hinducalendar.ui.texts.reader.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import dev.gopes.hinducalendar.engine.AudioPlaybackState
import dev.gopes.hinducalendar.engine.AudioPlayerService

@Composable
fun MiniAudioProgressBar(
    audioId: String?,
    audioPlayerService: AudioPlayerService,
    modifier: Modifier = Modifier
) {
    if (audioId == null) return

    val currentId by audioPlayerService.currentlyPlayingId.collectAsState()
    val state by audioPlayerService.state.collectAsState()
    val progress by audioPlayerService.playbackProgress.collectAsState()

    val isThisClip = currentId == audioId
    val clipState = if (isThisClip) state else AudioPlaybackState.IDLE
    val isActive = clipState == AudioPlaybackState.PLAYING || clipState == AudioPlaybackState.PAUSED

    if (!isActive) return

    val animatedProgress by animateFloatAsState(
        targetValue = if (isThisClip) progress.toFloat() else 0f,
        animationSpec = tween(50),
        label = "audioProgress"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(3.dp)
            .clip(RoundedCornerShape(1.5.dp))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(animatedProgress)
                .background(MaterialTheme.colorScheme.primary)
        )
    }
}
