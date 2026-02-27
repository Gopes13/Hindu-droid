package dev.gopes.hinducalendar.feature.texts.reader.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TextFormat
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.outlined.FormatListBulleted
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.gopes.hinducalendar.R
import dev.gopes.hinducalendar.feature.texts.reader.AudioUiState
import dev.gopes.hinducalendar.core.ui.components.SacredCard

@Composable
fun RevealableVerseCard(
    reference: String,
    originalText: String,
    transliteration: String?,
    translation: String,
    explanation: String?,
    names: List<Pair<String, String>>? = null,
    audioId: String? = null,
    audio: AudioUiState? = null,
    onFullyRevealed: (() -> Unit)? = null,
    onExplanationReveal: (() -> Unit)? = null,
    onShare: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var showTransliteration by remember { mutableStateOf(false) }
    var showTranslation by remember { mutableStateOf(false) }
    var showExplanation by remember { mutableStateOf(false) }
    var showNames by remember { mutableStateOf(false) }

    val sections = mutableListOf<Boolean>()
    if (!transliteration.isNullOrBlank()) sections.add(showTransliteration)
    if (translation.isNotBlank()) sections.add(showTranslation)
    if (!names.isNullOrEmpty()) sections.add(showNames)
    if (!explanation.isNullOrBlank()) sections.add(showExplanation)

    val totalSections = sections.size
    val revealedCount = sections.count { it }
    val fullyRevealed = totalSections > 0 && revealedCount >= totalSections

    LaunchedEffect(fullyRevealed) {
        if (fullyRevealed) onFullyRevealed?.invoke()
    }

    SacredCard(modifier = modifier, isHighlighted = fullyRevealed) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    reference,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(Modifier.weight(1f))
            if (audio != null && audioId != null) {
                VerseAudioButton(
                    audioId = audioId,
                    audio = audio
                )
            }
            if (onShare != null) {
                Icon(
                    Icons.Filled.Share,
                    contentDescription = stringResource(R.string.common_share),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .clickable { onShare() }
                )
                Spacer(Modifier.width(8.dp))
            }
            if (fullyRevealed) {
                Icon(
                    Icons.Filled.CheckCircle,
                    contentDescription = stringResource(R.string.cd_verse_revealed),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Audio progress bar
        if (audio != null) {
            MiniAudioProgressBar(audioId = audioId, audio = audio)
        }

        Spacer(Modifier.height(12.dp))

        // Original text (always visible)
        Text(
            originalText,
            style = MaterialTheme.typography.bodyLarge,
            lineHeight = 28.sp
        )

        // Progress dots
        if (totalSections > 0) {
            Spacer(Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(totalSections) { index ->
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                color = if (index < revealedCount) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                shape = CircleShape
                            )
                    )
                }
                Spacer(Modifier.width(4.dp))
                Text(
                    "$revealedCount/$totalSections",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Transliteration section
        RevealSection(
            label = stringResource(R.string.text_transliteration),
            icon = Icons.Filled.TextFormat,
            content = transliteration,
            revealed = showTransliteration,
            onReveal = { showTransliteration = true }
        )

        // Translation section
        RevealSection(
            label = stringResource(R.string.text_translation),
            icon = Icons.Filled.Translate,
            content = translation.takeIf { it.isNotBlank() },
            revealed = showTranslation,
            onReveal = { showTranslation = true }
        )

        // Names section (Vishnu Sahasranama)
        if (!names.isNullOrEmpty()) {
            RevealSection(
                label = stringResource(R.string.reader_names_meanings),
                icon = Icons.Outlined.FormatListBulleted,
                content = "names",
                revealed = showNames,
                onReveal = { showNames = true }
            ) {
                names.forEach { (name, meaning) ->
                    Row(Modifier.padding(vertical = 2.dp)) {
                        Text(
                            name,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.width(120.dp)
                        )
                        Text(
                            meaning,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Explanation/Commentary section
        RevealSection(
            label = stringResource(R.string.text_commentary),
            icon = Icons.Filled.Lightbulb,
            content = explanation,
            revealed = showExplanation,
            onReveal = {
                showExplanation = true
                onExplanationReveal?.invoke()
            }
        )
    }
}

@Composable
private fun RevealSection(
    label: String,
    icon: ImageVector,
    content: String?,
    revealed: Boolean,
    onReveal: () -> Unit,
    customContent: (@Composable () -> Unit)? = null
) {
    if (content == null) return

    Spacer(Modifier.height(8.dp))

    if (!revealed) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.small)
                .clickable { onReveal() },
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Filled.TouchApp,
                    contentDescription = stringResource(R.string.cd_tap_reveal),
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    stringResource(R.string.tap_to_reveal_format, label),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else {
        AnimatedVisibility(
            visible = true,
            enter = expandVertically() + fadeIn()
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        icon,
                        contentDescription = stringResource(R.string.cd_section_label),
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        label,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(Modifier.height(4.dp))
                if (customContent != null) {
                    customContent()
                } else {
                    Text(
                        content,
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 22.sp
                    )
                }
            }
        }
    }
}
