package dev.gopes.hinducalendar.ui.gamification

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.gopes.hinducalendar.R
import dev.gopes.hinducalendar.data.model.GamificationData
import dev.gopes.hinducalendar.data.model.SadhanaBadge
import dev.gopes.hinducalendar.ui.util.localizedName

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BadgeDetailSheet(
    badge: SadhanaBadge,
    gamificationData: GamificationData,
    onDismiss: () -> Unit
) {
    val isEarned = gamificationData.hasBadge(badge.id)
    val earnedDate = gamificationData.badgeEarnedDates[badge.id]

    var appeared by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { appeared = true }

    val iconScale by animateFloatAsState(
        if (appeared) 1f else 0.5f,
        spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow),
        label = "icon"
    )
    val contentAlpha by animateFloatAsState(
        if (appeared) 1f else 0f,
        tween(400, delayMillis = 200), label = "content"
    )

    // Glow pulse for earned badges
    val glowAlpha by rememberInfiniteTransition(label = "glow").animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(tween(1500, easing = EaseInOut), RepeatMode.Reverse),
        label = "glowPulse"
    )

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Badge icon
            Box(contentAlignment = Alignment.Center) {
                if (isEarned) {
                    Box(
                        Modifier
                            .size(100.dp)
                            .alpha(glowAlpha)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )
                }

                Box(
                    Modifier
                        .size(72.dp)
                        .scale(iconScale)
                        .clip(CircleShape)
                        .background(
                            if (isEarned) Brush.radialGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.tertiary
                                )
                            ) else Brush.radialGradient(
                                listOf(
                                    MaterialTheme.colorScheme.surfaceVariant,
                                    MaterialTheme.colorScheme.surfaceVariant
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (!isEarned) {
                        Icon(
                            Icons.Filled.Lock,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(28.dp)
                        )
                    } else {
                        Text(
                            badge.category.icon.take(2).uppercase(),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Badge title
            Text(
                badge.localizedName(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.alpha(contentAlpha)
            )

            Spacer(Modifier.height(8.dp))

            // Badge description
            Text(
                stringResource(R.string.badge_type_label, badge.category.localizedName()),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(contentAlpha)
            )

            Spacer(Modifier.height(16.dp))

            // Status
            if (isEarned) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.alpha(contentAlpha)
                ) {
                    Icon(
                        Icons.Filled.Check,
                        contentDescription = null,
                        tint = Color(0xFF2E7D32),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        stringResource(R.string.badge_earned_on, earnedDate ?: ""),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF2E7D32)
                    )
                }
            } else {
                Text(
                    stringResource(R.string.badge_keep_practicing),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.alpha(contentAlpha)
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}
