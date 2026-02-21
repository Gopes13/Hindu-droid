package dev.gopes.hinducalendar.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.gopes.hinducalendar.R
import dev.gopes.hinducalendar.data.model.AppLanguage
import dev.gopes.hinducalendar.data.model.GamificationData
import dev.gopes.hinducalendar.data.model.SadhanaLevel
import dev.gopes.hinducalendar.data.model.StreakData
import dev.gopes.hinducalendar.ui.theme.*
import java.util.Calendar

@Composable
fun GreetingBanner(
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = false,
    gamificationData: GamificationData? = null,
    streakData: StreakData? = null,
    language: AppLanguage = AppLanguage.ENGLISH
) {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val (sanskrit, english, icon) = when (hour) {
        in 4..11 -> Triple("\u0936\u0941\u092D \u092A\u094D\u0930\u092D\u093E\u0924", stringResource(R.string.greeting_good_morning), Icons.Filled.WbSunny)
        in 12..16 -> Triple("\u0936\u0941\u092D \u0905\u092A\u0930\u093E\u0939\u094D\u0928", stringResource(R.string.greeting_good_afternoon), Icons.Filled.LightMode)
        in 17..20 -> Triple("\u0936\u0941\u092D \u0938\u0902\u0927\u094D\u092F\u093E", stringResource(R.string.greeting_good_evening), Icons.Filled.WbTwilight)
        else -> Triple("\u0928\u092E\u0938\u094D\u0924\u0947", stringResource(R.string.greeting_namaste), Icons.Filled.NightsStay)
    }

    val isGamified = gamificationData?.isEnabled == true

    val gradientColors = if (isGamified) {
        if (isDarkTheme) {
            listOf(Color(0xFF8C40A0), Color(0xFFFF9933), Color(0xFFD4A634), Color(0xFFB34D33))
        } else {
            listOf(Color(0xFFA64DB3), Color(0xFFFF9933), Color(0xFFD4A634), Color(0xFFE66640))
        }
    } else if (isDarkTheme) {
        listOf(Color(0xFF50351A), Color(0xFF453020))
    } else {
        listOf(DeepSaffron, Color(0xFFD9A638))
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.linearGradient(gradientColors))
    ) {
        // Floating sparkles for gamified mode
        if (isGamified) {
            FloatingSparkles(count = 8, color = Color.White, modifier = Modifier.matchParentSize())
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = sanskrit,
                    style = AppTypography.displayMedium,
                    color = Color.White
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = english,
                    style = AppTypography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f)
                )

                // Gamification details
                if (isGamified && gamificationData != null) {
                    Spacer(Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Level badge with pulsing animation
                        GamifiedLevelBadge(gamificationData.currentLevel, language)

                        // Streak capsule
                        val currentStreak = streakData?.currentStreak ?: 0
                        if (currentStreak > 0) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = Color.White.copy(alpha = 0.2f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Text("\uD83D\uDD25", style = MaterialTheme.typography.labelSmall)
                                    Text(
                                        language.localizedNumber(currentStreak),
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White.copy(alpha = 0.9f)
                                    )
                                }
                            }
                        }

                        // PP counter
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text("\u2728", style = MaterialTheme.typography.labelSmall)
                            Text(
                                language.localizedDigits(stringResource(R.string.pp_total_format, gamificationData.totalPunyaPoints)),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }

            // Icon with rotation animation when gamified
            GamifiedIcon(icon = icon, isGamified = isGamified)
        }
    }
}

@Composable
private fun GamifiedLevelBadge(level: Int, language: AppLanguage = AppLanguage.ENGLISH) {
    val infiniteTransition = rememberInfiniteTransition(label = "levelPulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "levelScale"
    )

    val levelData = SadhanaLevel.forLevel(level)

    Surface(
        modifier = Modifier.scale(scale),
        shape = RoundedCornerShape(50),
        color = Color.White.copy(alpha = 0.25f),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                Icons.Filled.AutoAwesome,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(12.dp)
            )
            Text(
                language.localizedDigits(stringResource(R.string.lv_format, level)),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
private fun GamifiedIcon(icon: ImageVector, isGamified: Boolean) {
    if (isGamified) {
        val infiniteTransition = rememberInfiniteTransition(label = "iconRotate")
        val rotation by infiniteTransition.animateFloat(
            initialValue = -15f,
            targetValue = 15f,
            animationSpec = infiniteRepeatable(
                animation = tween(6000, easing = EaseInOut),
                repeatMode = RepeatMode.Reverse
            ),
            label = "iconAngle"
        )
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier
                .size(36.dp)
                .rotate(rotation),
            tint = Color.White.copy(alpha = 0.7f)
        )
    } else {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(36.dp),
            tint = Color.White.copy(alpha = 0.4f)
        )
    }
}
