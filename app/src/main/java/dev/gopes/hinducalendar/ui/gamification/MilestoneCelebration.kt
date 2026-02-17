package dev.gopes.hinducalendar.ui.gamification

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.gopes.hinducalendar.R
import dev.gopes.hinducalendar.ui.components.ConfettiOverlay

@Composable
fun MilestoneCelebration(
    days: Int,
    onDismiss: () -> Unit
) {
    var appeared by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { appeared = true }

    val iconScale by animateFloatAsState(
        if (appeared) 1f else 0.3f,
        spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow),
        label = "icon"
    )
    val contentAlpha by animateFloatAsState(
        if (appeared) 1f else 0f,
        tween(400, delayMillis = 300), label = "content"
    )

    val milestoneIcon = when {
        days >= 365 -> Icons.Filled.AutoAwesome
        days >= 100 -> Icons.Filled.EmojiEvents
        days >= 60 -> Icons.Filled.WorkspacePremium
        days >= 30 -> Icons.Filled.Star
        days >= 14 -> Icons.Filled.Star
        else -> Icons.Filled.LocalFireDepartment
    }
    val milestoneColor = when {
        days >= 100 -> Color(0xFFFFD700) // gold
        days >= 60 -> Color(0xFF3498DB)  // blue
        days >= 30 -> Color(0xFF9B59B6)  // purple
        days >= 14 -> Color(0xFFFFD700)  // yellow
        else -> Color(0xFFFF8C00)        // orange
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        ConfettiOverlay(isActive = appeared)

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Spacer(Modifier.height(48.dp))

            Icon(
                milestoneIcon,
                contentDescription = null,
                tint = milestoneColor,
                modifier = Modifier
                    .size(80.dp)
                    .scale(iconScale)
            )

            Spacer(Modifier.height(24.dp))

            Text(
                stringResource(R.string.milestone_title, days),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.alpha(contentAlpha)
            )

            Spacer(Modifier.height(8.dp))

            Text(
                stringResource(R.string.milestone_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.alpha(contentAlpha)
            )

            Spacer(Modifier.height(32.dp))

            Button(
                onClick = onDismiss,
                modifier = Modifier.alpha(contentAlpha),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    stringResource(R.string.common_done),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
        }
    }
}
