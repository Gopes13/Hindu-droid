package dev.gopes.hinducalendar.ui.gamification

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.gopes.hinducalendar.R
import dev.gopes.hinducalendar.data.model.AppLanguage
import dev.gopes.hinducalendar.data.model.GamificationData
import dev.gopes.hinducalendar.data.model.StreakData
import dev.gopes.hinducalendar.ui.components.SacredCard

@Composable
fun StreakBadgeView(
    streakData: StreakData,
    gamificationData: GamificationData? = null,
    modifier: Modifier = Modifier,
    language: AppLanguage = AppLanguage.ENGLISH
) {
    val streak = streakData.currentStreak
    if (streak <= 0) return

    val isGamified = gamificationData?.isEnabled == true

    val flameColor = when {
        streak >= 100 -> Color(0xFF9B59B6) // purple
        streak >= 30 -> Color(0xFFE74C3C)  // red
        streak >= 7 -> Color(0xFFFF8C00)   // orange
        else -> MaterialTheme.colorScheme.primary
    }

    // Flame pulse
    val scale by rememberInfiniteTransition(label = "flame").animateFloat(
        initialValue = 1f,
        targetValue = if (streak >= 30) 1.15f else 1.08f,
        animationSpec = infiniteRepeatable(
            tween(1500, easing = EaseInOut),
            RepeatMode.Reverse
        ),
        label = "flameScale"
    )

    SacredCard(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Flame icon with pulse
            Box(contentAlignment = Alignment.Center) {
                if (isGamified) {
                    Box(
                        Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(flameColor.copy(alpha = 0.15f))
                    )
                }
                Icon(
                    Icons.Filled.LocalFireDepartment,
                    contentDescription = null,
                    tint = flameColor,
                    modifier = Modifier
                        .size(28.dp)
                        .scale(scale)
                )
            }

            // Streak info
            Column(Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        language.localizedNumber(streak),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        stringResource(R.string.streak_day_label),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (isGamified && streakData.streakBonus > 0) {
                        Surface(
                            color = MaterialTheme.colorScheme.tertiaryContainer,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                language.localizedDigits(stringResource(R.string.pp_format, streakData.streakBonus)),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
                }

                // Progress to next milestone
                val nextMilestone = StreakData.milestoneThresholds.firstOrNull { it > streak }
                if (nextMilestone != null && isGamified) {
                    Spacer(Modifier.height(4.dp))
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Box(
                            Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(streak.toFloat() / nextMilestone)
                                .clip(RoundedCornerShape(2.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(MaterialTheme.colorScheme.primary, flameColor)
                                    )
                                )
                        )
                    }
                }
            }

            // Best streak
            if (streakData.longestStreak > streak) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        language.localizedNumber(streakData.longestStreak),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        stringResource(R.string.streak_best),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
