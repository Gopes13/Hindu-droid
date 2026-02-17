package dev.gopes.hinducalendar.ui.gamification

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
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
import dev.gopes.hinducalendar.R
import dev.gopes.hinducalendar.data.model.DailyChallenge
import dev.gopes.hinducalendar.data.model.GamificationData
import dev.gopes.hinducalendar.ui.components.SacredCard

@Composable
fun DailyChallengeCard(
    challenge: DailyChallenge,
    gamificationData: GamificationData,
    onAnswered: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val alreadyCompleted = gamificationData.isToday(gamificationData.lastChallengeDate)
            && gamificationData.lastChallengeCompleted

    if (alreadyCompleted) {
        CompletedChallengeCard(modifier)
        return
    }

    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    var hasAnswered by remember { mutableStateOf(false) }
    var showReward by remember { mutableStateOf(false) }

    val letters = listOf("A", "B", "C", "D")

    SacredCard(modifier = modifier) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    stringResource(R.string.daily_challenge),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    stringResource(when (challenge.type.titleKey) {
                        "challenge_panchang" -> R.string.challenge_panchang
                        "challenge_festival" -> R.string.challenge_festival
                        "challenge_verse" -> R.string.challenge_verse
                        else -> R.string.challenge_mantra
                    }),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Surface(
                color = MaterialTheme.colorScheme.tertiaryContainer,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    "+${challenge.xpReward} PP",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // Question with accent bar
        Row(verticalAlignment = Alignment.Top) {
            Box(
                Modifier
                    .width(3.dp)
                    .height(40.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.tertiary
                            )
                        )
                    )
            )
            Spacer(Modifier.width(12.dp))
            Text(
                challenge.question,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(Modifier.height(16.dp))

        // Options
        challenge.options.forEachIndexed { index, option ->
            val isSelected = selectedIndex == index
            val isCorrect = index == challenge.correctOptionIndex
            val showResult = hasAnswered && isSelected

            val containerColor = when {
                showResult && isCorrect -> Color(0xFF2E7D32).copy(alpha = 0.12f)
                showResult && !isCorrect -> Color(0xFFC62828).copy(alpha = 0.12f)
                isSelected && !hasAnswered -> MaterialTheme.colorScheme.primaryContainer
                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            }

            val borderColor = when {
                showResult && isCorrect -> Color(0xFF2E7D32)
                showResult && !isCorrect -> Color(0xFFC62828)
                hasAnswered && isCorrect -> Color(0xFF2E7D32).copy(alpha = 0.5f)
                else -> Color.Transparent
            }

            Surface(
                onClick = {
                    if (!hasAnswered) {
                        selectedIndex = index
                        hasAnswered = true
                        val correct = index == challenge.correctOptionIndex
                        onAnswered(correct)
                        if (correct) showReward = true
                    }
                },
                enabled = !hasAnswered,
                color = containerColor,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Letter badge
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            letters[index],
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Text(
                        option,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )

                    // Result icon
                    if (hasAnswered && (isSelected || isCorrect)) {
                        Icon(
                            if (isCorrect) Icons.Filled.Check else Icons.Filled.Close,
                            contentDescription = null,
                            tint = if (isCorrect) Color(0xFF2E7D32) else Color(0xFFC62828),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // Reward animation
        AnimatedVisibility(
            visible = showReward,
            enter = fadeIn(tween(300, delayMillis = 300)) + scaleIn(tween(400, delayMillis = 300))
        ) {
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "+${challenge.xpReward} ",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    stringResource(R.string.punya_points),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun CompletedChallengeCard(modifier: Modifier = Modifier) {
    SacredCard(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF2E7D32).copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Check,
                    contentDescription = null,
                    tint = Color(0xFF2E7D32),
                    modifier = Modifier.size(20.dp)
                )
            }
            Column {
                Text(
                    stringResource(R.string.challenge_completed),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    stringResource(R.string.challenge_come_back),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
