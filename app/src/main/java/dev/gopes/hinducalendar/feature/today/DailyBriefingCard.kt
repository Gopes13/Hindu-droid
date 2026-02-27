package dev.gopes.hinducalendar.feature.today

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import dev.gopes.hinducalendar.R
import dev.gopes.hinducalendar.domain.model.AppLanguage
import dev.gopes.hinducalendar.domain.model.SacredTextType
import dev.gopes.hinducalendar.domain.model.DailyVerse
import dev.gopes.hinducalendar.core.ui.components.*
import dev.gopes.hinducalendar.core.util.localizedName
import dev.gopes.hinducalendar.feature.gamification.GamificationViewModel
import dev.gopes.hinducalendar.feature.texts.reader.components.RevealableVerseCard
import dev.gopes.hinducalendar.core.ui.theme.*

@Composable
fun DailyBriefingCard(
    viewModel: DailyBriefingViewModel = hiltViewModel(),
    gamificationViewModel: GamificationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showConfetti by remember { mutableStateOf(false) }

    Box {
        SacredHighlightCard(accentColor = MaterialTheme.colorScheme.tertiary) {
            when {
                !uiState.isLoaded -> LoadingView()
                uiState.isTextComplete -> CompletionView(
                    uiState = uiState,
                    onSelectText = { viewModel.selectWisdomText(it) }
                )
                uiState.hasError || uiState.verse == null -> ErrorView(
                    uiState = uiState,
                    onRetry = { viewModel.loadDailyContent() }
                )
                else -> ReadingView(
                    uiState = uiState,
                    verse = uiState.verse!!,
                    onMarkAsRead = {
                        showConfetti = true
                        gamificationViewModel.recordVerseRead()
                        viewModel.markAsRead()
                    },
                    onFullyRevealed = { showConfetti = true },
                    onExplanationReveal = { gamificationViewModel.recordExplanationView() }
                )
            }
        }

        ConfettiOverlay(isActive = showConfetti, onFinished = { showConfetti = false })
    }

    LaunchedEffect(Unit) {
        gamificationViewModel.recordVerseView()
    }
}

@Composable
private fun ReadingView(
    uiState: DailyBriefingUiState,
    verse: DailyVerse,
    onMarkAsRead: () -> Unit,
    onFullyRevealed: () -> Unit,
    onExplanationReveal: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.AutoAwesome,
                        contentDescription = stringResource(R.string.cd_daily_wisdom),
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        stringResource(R.string.daily_wisdom),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
                Text(
                    verse.title,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (uiState.totalPositions > 0) {
                Text(
                    uiState.language.localizedDigits("${uiState.currentPosition}/${uiState.totalPositions}"),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Interactive verse card
        RevealableVerseCard(
            reference = verse.subtitle,
            originalText = verse.sanskrit ?: "",
            transliteration = verse.transliteration,
            translation = verse.translation,
            explanation = verse.commentary,
            onFullyRevealed = onFullyRevealed,
            onExplanationReveal = onExplanationReveal
        )

        // Progress bar
        if (uiState.totalPositions > 0) {
            LinearProgressIndicator(
                progress = { uiState.currentPosition.toFloat() / uiState.totalPositions.toFloat() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f)
            )
        }

        // Mark as Read button
        val gradient = if (uiState.isGamified) {
            Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary))
        } else {
            Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary))
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(gradient)
                .clickable { onMarkAsRead() }
                .padding(vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    if (uiState.isGamified) Icons.Filled.AutoAwesome else Icons.Filled.CheckCircle,
                    contentDescription = stringResource(R.string.cd_mark_verse_read),
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    stringResource(R.string.text_mark_read),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White
                )
                if (uiState.isGamified) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color.White.copy(alpha = 0.25f)
                    ) {
                        Text(
                            uiState.language.localizedDigits(stringResource(R.string.pp_format, 10)),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CompletionView(
    uiState: DailyBriefingUiState,
    onSelectText: (SacredTextType) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                stringResource(R.string.daily_wisdom),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.tertiary
            )
            Icon(
                Icons.Filled.CheckCircle,
                contentDescription = stringResource(R.string.cd_reading_completed),
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }

        // Congratulations
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("\uD83D\uDC4F", fontSize = 36.sp) // clapping hands emoji
            Text(
                stringResource(R.string.briefing_text_completed, uiState.activeText.localizedName()),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                stringResource(R.string.briefing_text_completed_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Text picker
        Text(
            stringResource(R.string.briefing_choose_next),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        val wisdomTexts = SacredTextType.entries.filter { it.isWisdomEligible }
        wisdomTexts.forEach { textType ->
            val isCurrent = textType == uiState.activeText
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surface,
                onClick = { if (!isCurrent) onSelectText(textType) }
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.MenuBook,
                        contentDescription = stringResource(R.string.cd_sacred_text),
                        tint = if (isCurrent) MaterialTheme.colorScheme.onSurfaceVariant
                        else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        textType.localizedName(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isCurrent) MaterialTheme.colorScheme.onSurfaceVariant
                        else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    if (isCurrent) {
                        Icon(
                            Icons.Filled.CheckCircle,
                            contentDescription = stringResource(R.string.cd_text_selected),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ErrorView(
    uiState: DailyBriefingUiState,
    onRetry: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    stringResource(R.string.daily_wisdom),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.tertiary
                )
                Text(
                    uiState.activeText.localizedName(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                Icons.Filled.Warning,
                contentDescription = stringResource(R.string.cd_retry_loading),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
        Text(
            stringResource(R.string.briefing_loading_error),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        TextButton(onClick = onRetry) {
            Icon(Icons.Filled.Refresh, contentDescription = stringResource(R.string.cd_retry_loading), modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(4.dp))
            Text(stringResource(R.string.briefing_load_wisdom))
        }
    }
}

@Composable
private fun LoadingView() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                stringResource(R.string.daily_wisdom),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.tertiary
            )
            Icon(
                Icons.Filled.AutoAwesome,
                contentDescription = stringResource(R.string.cd_loading),
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
        Text(
            stringResource(R.string.briefing_load_wisdom),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
