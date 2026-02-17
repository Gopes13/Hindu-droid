package dev.gopes.hinducalendar.ui.texts.reader

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.gopes.hinducalendar.R
import dev.gopes.hinducalendar.ui.components.ConfettiOverlay
import dev.gopes.hinducalendar.ui.theme.DeepSaffron
import dev.gopes.hinducalendar.ui.theme.DivineGold

data class ChapterCompletionEvent(
    val textName: String,
    val chapterName: String,
    val isTextComplete: Boolean = false,
    val ppAwarded: Int = 0
)

@Composable
fun ChapterCompletionOverlay(
    event: ChapterCompletionEvent,
    onContinue: () -> Unit,
    onReviewChapter: (() -> Unit)? = null
) {
    var showConfetti by remember { mutableStateOf(false) }

    // Entrance animation
    val scaleAnim = remember { Animatable(0.8f) }
    LaunchedEffect(Unit) {
        scaleAnim.animateTo(
            1f,
            spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessLow)
        )
    }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(300)
        showConfetti = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.65f)),
        contentAlignment = Alignment.Center
    ) {
        // Confetti
        ConfettiOverlay(
            isActive = showConfetti,
            onFinished = { showConfetti = false }
        )

        // Card
        Card(
            modifier = Modifier
                .padding(32.dp)
                .scale(scaleAnim.value),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Concentric circles + icon
                Box(contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                                CircleShape
                            )
                    )
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .background(
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                CircleShape
                            )
                    )
                    Icon(
                        if (event.isTextComplete) Icons.Filled.EmojiEvents
                        else Icons.Filled.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(36.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(Modifier.height(16.dp))

                // Title
                Text(
                    if (event.isTextComplete) stringResource(R.string.completion_text_complete)
                    else stringResource(R.string.completion_chapter_complete),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(8.dp))

                // Chapter/Text name
                Text(
                    event.chapterName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
                Text(
                    event.textName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                // PP awarded
                if (event.ppAwarded > 0) {
                    Spacer(Modifier.height(12.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            "\u2728 +${event.ppAwarded} PP",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                // Continue button
                Button(
                    onClick = onContinue,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent
                    ),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(listOf(DeepSaffron, DivineGold)),
                                RoundedCornerShape(24.dp)
                            )
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            if (event.isTextComplete) stringResource(R.string.completion_wonderful)
                            else stringResource(R.string.completion_continue_reading),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 16.sp
                        )
                    }
                }

                // Review button
                if (!event.isTextComplete && onReviewChapter != null) {
                    Spacer(Modifier.height(8.dp))
                    TextButton(onClick = onReviewChapter) {
                        Text(
                            stringResource(R.string.completion_review_chapter),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}
