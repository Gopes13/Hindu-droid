package dev.gopes.hinducalendar.feature.texts.reader.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.gopes.hinducalendar.R
import dev.gopes.hinducalendar.domain.repository.AudioPlaybackState
import dev.gopes.hinducalendar.feature.texts.reader.AudioUiState

@Composable
fun VerseAudioButton(
    audioId: String?,
    audio: AudioUiState,
    modifier: Modifier = Modifier
) {
    if (audioId == null) return

    val isThisClip = audio.currentlyPlayingId == audioId
    val clipState = if (isThisClip) audio.playbackState else AudioPlaybackState.IDLE

    IconButton(
        onClick = { audio.onToggle(audioId) },
        modifier = modifier.size(36.dp)
    ) {
        when (clipState) {
            AudioPlaybackState.IDLE -> Icon(
                Icons.Filled.PlayCircle,
                contentDescription = stringResource(R.string.cd_play_audio),
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
            AudioPlaybackState.LOADING -> CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.primary
            )
            AudioPlaybackState.PLAYING -> Icon(
                Icons.Filled.PauseCircle,
                contentDescription = stringResource(R.string.cd_pause_audio),
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
            AudioPlaybackState.PAUSED -> Icon(
                Icons.Filled.PlayCircle,
                contentDescription = stringResource(R.string.cd_resume_audio),
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
            AudioPlaybackState.ERROR -> Icon(
                Icons.Filled.ErrorOutline,
                contentDescription = stringResource(R.string.cd_audio_error),
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}
