package dev.gopes.hinducalendar.ui.kirtans

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import dev.gopes.hinducalendar.R
import dev.gopes.hinducalendar.data.model.AppLanguage
import dev.gopes.hinducalendar.data.model.Kirtan
import dev.gopes.hinducalendar.data.model.KirtanStanza
import dev.gopes.hinducalendar.engine.AudioPlaybackState
import dev.gopes.hinducalendar.engine.AudioPlayerService
import dev.gopes.hinducalendar.ui.components.SacredCard
import dev.gopes.hinducalendar.ui.components.SacredHighlightCard
import dev.gopes.hinducalendar.ui.util.localizedOriginLanguage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KirtanReaderScreen(
    kirtanId: String,
    onBack: () -> Unit = {},
    viewModel: KirtanReaderViewModel = hiltViewModel()
) {
    LaunchedEffect(kirtanId) { viewModel.loadKirtan(kirtanId) }

    val kirtan = viewModel.kirtan
    val language = viewModel.language

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(kirtan?.title(language) ?: "") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { padding ->
        if (kirtan == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                // Header
                item(key = "header") {
                    KirtanHeader(kirtan, language)
                }

                // Full Kirtan Player
                val audioService = viewModel.audioPlayerService
                if (audioService.hasAudio(kirtan.audioId)) {
                    item(key = "player") {
                        KirtanPlayer(
                            kirtan = kirtan,
                            language = language,
                            audioService = audioService,
                            onToggle = { viewModel.playKirtan() }
                        )
                    }
                }

                // Stanza cards
                itemsIndexed(kirtan.stanzas, key = { _, s -> "stanza_${s.stanza}" }) { _, stanza ->
                    StanzaCard(stanza, language)
                }
            }
        }
    }
}

@Composable
private fun KirtanHeader(kirtan: Kirtan, language: AppLanguage) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            kirtan.title(language),
            style = MaterialTheme.typography.headlineSmall.copy(
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold
            ),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )

        val localTitle = kirtan.title(language)
        if (kirtan.titleSanskrit != localTitle) {
            Spacer(Modifier.height(4.dp))
            Text(
                kirtan.titleSanskrit,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }

        Spacer(Modifier.height(8.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            kirtan.author?.let {
                Text(
                    it,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Surface(
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f)
            ) {
                Text(
                    localizedOriginLanguage(kirtan.originLanguage),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }

        Spacer(Modifier.height(12.dp))
    }
}

private val SPEED_OPTIONS = listOf(0.75f, 1.0f, 1.25f, 1.5f, 2.0f)

@Composable
private fun KirtanPlayer(
    kirtan: Kirtan,
    language: AppLanguage,
    audioService: AudioPlayerService,
    onToggle: () -> Unit
) {
    val currentId by audioService.currentlyPlayingId.collectAsState()
    val state by audioService.state.collectAsState()
    val positionMs by audioService.currentPositionMs.collectAsState()
    val speed by audioService.playbackSpeed.collectAsState()

    val audioId = kirtan.audioId
    val isThisKirtan = currentId == audioId
    val kirtanState = if (isThisKirtan) state else AudioPlaybackState.IDLE
    val durationMs = audioService.duration(audioId) ?: 0
    val isActive = kirtanState == AudioPlaybackState.PLAYING || kirtanState == AudioPlaybackState.PAUSED

    SacredHighlightCard {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Title
            Text(
                kirtan.title(language),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            if (kirtanState == AudioPlaybackState.ERROR) {
                // Error state
                Spacer(Modifier.height(12.dp))
                Icon(
                    Icons.Filled.ErrorOutline,
                    contentDescription = stringResource(R.string.cd_audio_error),
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(Modifier.height(4.dp))
                TextButton(onClick = onToggle) {
                    Icon(Icons.Filled.Replay, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Retry")
                }
            } else if (!isActive && kirtanState != AudioPlaybackState.LOADING) {
                // Idle state — show play button with stanza count
                Spacer(Modifier.height(12.dp))
                FilledIconButton(
                    onClick = onToggle,
                    modifier = Modifier.size(56.dp),
                    shape = CircleShape,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        Icons.Filled.PlayArrow,
                        contentDescription = stringResource(R.string.cd_play_audio),
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    stringResource(R.string.kirtan_stanzas_count, kirtan.stanzas.size),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (durationMs > 0) {
                    Text(
                        formatTime(durationMs, language),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else if (kirtanState == AudioPlaybackState.LOADING) {
                // Loading state
                Spacer(Modifier.height(16.dp))
                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp),
                    strokeWidth = 3.dp,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(8.dp))
            } else {
                // Active player — seek bar + transport + speed
                Spacer(Modifier.height(8.dp))

                // Seek bar
                var isSeeking by remember { mutableStateOf(false) }
                var seekPosition by remember { mutableFloatStateOf(0f) }
                val sliderValue = if (isSeeking) seekPosition
                    else if (durationMs > 0) positionMs.toFloat() / durationMs else 0f

                Slider(
                    value = sliderValue.coerceIn(0f, 1f),
                    onValueChange = { value ->
                        isSeeking = true
                        seekPosition = value
                    },
                    onValueChangeFinished = {
                        audioService.seekTo((seekPosition * durationMs).toInt())
                        isSeeking = false
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary,
                        inactiveTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                    )
                )

                // Time labels
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val displayPosition = if (isSeeking) (seekPosition * durationMs).toInt() else positionMs
                    Text(
                        formatTime(displayPosition, language),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        formatTime(durationMs, language),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(Modifier.height(4.dp))

                // Transport controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Skip backward 15s
                    IconButton(onClick = { audioService.skipBackward() }) {
                        Icon(
                            Icons.Filled.Replay10,
                            contentDescription = "Skip back 15s",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(Modifier.width(16.dp))

                    // Play/Pause
                    FilledIconButton(
                        onClick = onToggle,
                        modifier = Modifier.size(56.dp),
                        shape = CircleShape,
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            if (kirtanState == AudioPlaybackState.PLAYING)
                                Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = if (kirtanState == AudioPlaybackState.PLAYING)
                                stringResource(R.string.cd_pause_audio) else stringResource(R.string.cd_play_audio),
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(Modifier.width(16.dp))

                    // Skip forward 15s
                    IconButton(onClick = { audioService.skipForward() }) {
                        Icon(
                            Icons.Filled.Forward10,
                            contentDescription = "Skip forward 15s",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Spacer(Modifier.height(4.dp))

                // Speed selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    SPEED_OPTIONS.forEach { option ->
                        val isSelected = speed == option
                        FilterChip(
                            selected = isSelected,
                            onClick = { audioService.setSpeed(option) },
                            label = {
                                Text(
                                    if (option == option.toInt().toFloat()) "${option.toInt()}x" else "${option}x",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            modifier = Modifier.padding(horizontal = 2.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                selectedLabelColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }
            }
        }
    }
}

private fun formatTime(ms: Int, language: AppLanguage): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    val formatted = "%d:%02d".format(minutes, seconds)
    return language.localizedDigits(formatted)
}

@Composable
private fun StanzaCard(stanza: KirtanStanza, language: AppLanguage) {
    SacredCard {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Stanza number
            Text(
                "${stringResource(R.string.kirtan_stanza)} ${language.localizedNumber(stanza.stanza)}",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            // Lyrics (serif for Devanagari scripts)
            Text(
                stanza.lyrics,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = FontFamily.Serif,
                    lineHeight = 28.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            // Translation
            val meaning = stanza.translation(language)
            if (meaning.isNotEmpty()) {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 2.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )

                Text(
                    stringResource(R.string.kirtan_meaning),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    meaning,
                    style = MaterialTheme.typography.bodySmall.copy(lineHeight = 20.sp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                )
            }
        }
    }
}
