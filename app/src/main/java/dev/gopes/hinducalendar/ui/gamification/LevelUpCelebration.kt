package dev.gopes.hinducalendar.ui.gamification

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.gopes.hinducalendar.R
import dev.gopes.hinducalendar.data.model.SadhanaLevel
import dev.gopes.hinducalendar.ui.components.ConfettiOverlay

@Composable
fun LevelUpCelebration(
    oldLevel: Int,
    newLevel: Int,
    onDismiss: () -> Unit
) {
    val newLevelData = SadhanaLevel.forLevel(newLevel)

    // Animation states
    var appeared by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { appeared = true }

    val bgAlpha by animateFloatAsState(
        if (appeared) 0.7f else 0f,
        tween(300), label = "bg"
    )
    val iconScale by animateFloatAsState(
        if (appeared) 1f else 0.1f,
        spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow),
        label = "icon"
    )
    val textAlpha by animateFloatAsState(
        if (appeared) 1f else 0f,
        tween(500, delayMillis = 600), label = "text"
    )
    val buttonAlpha by animateFloatAsState(
        if (appeared) 1f else 0f,
        tween(400, delayMillis = 1000), label = "button"
    )

    // Ring pulse
    val ringScale by rememberInfiniteTransition(label = "ring").animateFloat(
        initialValue = 1.2f,
        targetValue = 1.4f,
        animationSpec = infiniteRepeatable(tween(2000, easing = EaseInOut), RepeatMode.Reverse),
        label = "ringPulse"
    )

    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = bgAlpha))
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        // Confetti
        ConfettiOverlay(trigger = appeared)

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            // Glowing icon
            Box(contentAlignment = Alignment.Center) {
                // Outer ring pulse
                Box(
                    Modifier
                        .size(120.dp)
                        .scale(ringScale)
                        .alpha(0.3f)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(MaterialTheme.colorScheme.primary, Color.Transparent)
                            )
                        )
                )
                // Inner glow
                Box(
                    Modifier
                        .size(90.dp)
                        .scale(iconScale)
                        .blur(12.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
                )
                // Icon circle
                Box(
                    Modifier
                        .size(80.dp)
                        .scale(iconScale)
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
                        "Lv.${newLevel}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // Level up text
            Text(
                stringResource(R.string.level_up),
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 2.sp
                ),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.alpha(textAlpha)
            )

            Spacer(Modifier.height(12.dp))

            // Level transition
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.alpha(textAlpha)
            ) {
                Text(
                    "Lv.$oldLevel",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White.copy(alpha = 0.5f)
                )
                Text("→", color = Color.White.copy(alpha = 0.7f))
                Text(
                    "Lv.$newLevel",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(Modifier.height(8.dp))

            // New title
            Text(
                stringResource(R.string.sadhana_new_title),
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.6f),
                modifier = Modifier.alpha(textAlpha)
            )

            Spacer(Modifier.height(32.dp))

            // Continue button
            Button(
                onClick = onDismiss,
                modifier = Modifier.alpha(buttonAlpha),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    stringResource(R.string.sadhana_continue),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
        }
    }
}
