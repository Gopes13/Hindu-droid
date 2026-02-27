package dev.gopes.hinducalendar.feature.gamification

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.gopes.hinducalendar.R
import dev.gopes.hinducalendar.domain.model.AppLanguage
import dev.gopes.hinducalendar.domain.model.GamificationData
import dev.gopes.hinducalendar.core.ui.components.SacredCard

@Composable
fun SadhanaStatusBar(
    data: GamificationData,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    language: AppLanguage = AppLanguage.ENGLISH
) {
    if (!data.isEnabled) return

    val level = data.currentLevelData
    val progress = data.currentLevelProgress

    // Animated progress
    val animatedProgress by animateFloatAsState(
        targetValue = progress.toFloat(),
        animationSpec = tween(800, delayMillis = 200, easing = FastOutSlowInEasing),
        label = "progress"
    )

    // Sparkle rotation
    val sparkleRotation by rememberInfiniteTransition(label = "sparkle").animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(4000, easing = LinearEasing)),
        label = "sparkleRotation"
    )

    SacredCard(modifier = modifier.clickable(onClick = onClick)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Level icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .shadow(4.dp, CircleShape)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.tertiary
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    language.localizedDigits(stringResource(R.string.lv_format, level.level)),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }

            // Level title + progress bar
            Column(Modifier.weight(1f)) {
                Text(
                    language.localizedDigits(stringResource(R.string.sadhana_level_format, level.level, stringResource(
                        when (level.titleKey) {
                            "level_1" -> R.string.level_1
                            "level_2" -> R.string.level_2
                            "level_3" -> R.string.level_3
                            "level_4" -> R.string.level_4
                            "level_5" -> R.string.level_5
                            "level_6" -> R.string.level_6
                            "level_7" -> R.string.level_7
                            "level_8" -> R.string.level_8
                            "level_9" -> R.string.level_9
                            "level_10" -> R.string.level_10
                            "level_11" -> R.string.level_11
                            "level_12" -> R.string.level_12
                            "level_13" -> R.string.level_13
                            "level_14" -> R.string.level_14
                            "level_15" -> R.string.level_15
                            "level_16" -> R.string.level_16
                            "level_17" -> R.string.level_17
                            "level_18" -> R.string.level_18
                            "level_19" -> R.string.level_19
                            else -> R.string.level_20
                        }
                    ))),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(4.dp))
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(
                        Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(animatedProgress)
                            .clip(RoundedCornerShape(3.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.tertiary
                                    )
                                )
                            )
                    )
                }
            }

            // PP badge
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        language.localizedNumber(data.totalPunyaPoints),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        stringResource(R.string.pp_abbreviation),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}
