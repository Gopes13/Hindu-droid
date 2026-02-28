package dev.gopes.hinducalendar.core.ui.components

import android.provider.Settings
import androidx.compose.animation.core.*
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.gopes.hinducalendar.core.ui.theme.LocalVibrantMode
import dev.gopes.hinducalendar.core.util.DeviceCapabilities

/**
 * Vibrant animations ported from iOS VibrantAnimations.swift.
 * All effects check reduce-motion and device capability before activating.
 */

// ── Entrance Animation ──────────────────────────────────────────────────────

/**
 * Fade + slide-up + scale on appear, staggered by list index.
 * Vibrant: spring response=0.5, damping=0.7, offset=20dp, scale=0.95
 * Normal:  tween 350ms easeOut, offset=12dp, scale=0.98
 */
fun Modifier.entranceAnimation(
    index: Int = 0,
    isVibrant: Boolean = false,
): Modifier = composed {
    if (reduceMotionEnabled() || DeviceCapabilities.isBasic) return@composed this

    var appeared by remember { mutableStateOf(false) }
    // Logarithmic stagger: front-loads visible items, total cascade ~300ms instead of ~880ms
    val delay = (kotlin.math.ln((index + 1).toDouble()) * 120).toInt().coerceAtMost(500)

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(delay.toLong())
        appeared = true
    }

    val animSpec: AnimationSpec<Float> = if (isVibrant) {
        spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessLow)
    } else {
        tween(350, easing = FastOutSlowInEasing)
    }

    val targetOffset = if (isVibrant) 20f else 12f
    val targetScale = if (isVibrant) 0.95f else 0.98f

    val alphaAnim by animateFloatAsState(
        targetValue = if (appeared) 1f else 0f,
        animationSpec = animSpec,
        label = "entranceAlpha"
    )
    val offsetAnim by animateFloatAsState(
        targetValue = if (appeared) 0f else targetOffset,
        animationSpec = animSpec,
        label = "entranceOffset"
    )
    val scaleAnim by animateFloatAsState(
        targetValue = if (appeared) 1f else targetScale,
        animationSpec = animSpec,
        label = "entranceScale"
    )

    this
        .alpha(alphaAnim)
        .offset { IntOffset(0, offsetAnim.dp.roundToPx()) }
        .scale(scaleAnim)
}

// ── Card Breathing ──────────────────────────────────────────────────────────

/**
 * Subtle 1.005x scale pulse (3s infinite loop) on featured cards.
 * Only active in vibrant mode on FULL/STANDARD devices.
 */
fun Modifier.cardBreathing(): Modifier = composed {
    val isVibrant = LocalVibrantMode.current
    if (!isVibrant || reduceMotionEnabled() || DeviceCapabilities.isBasic) return@composed this

    val transition = rememberInfiniteTransition(label = "breathing")
    val scale by transition.animateFloat(
        initialValue = 1f,
        targetValue = 1.005f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathScale"
    )

    this.scale(scale)
}

// ── Subtle Shimmer ──────────────────────────────────────────────────────────

/**
 * Diagonal light sweep across the surface (4s loop).
 * Colors: [clear, white@0.12, white@0.18, white@0.12, clear]
 * Only active in vibrant mode on FULL devices.
 */
fun Modifier.subtleShimmer(): Modifier = composed {
    val isVibrant = LocalVibrantMode.current
    if (!isVibrant || reduceMotionEnabled() || !DeviceCapabilities.isFull) return@composed this

    val transition = rememberInfiniteTransition(label = "shimmer")
    val shimmerProgress by transition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerOffset"
    )

    this.drawWithContent {
        drawContent()

        val shimmerWidth = size.width * 0.6f
        val offset = shimmerProgress * size.width

        drawRect(
            brush = Brush.linearGradient(
                colors = listOf(
                    Color.Transparent,
                    Color.White.copy(alpha = 0.12f),
                    Color.White.copy(alpha = 0.18f),
                    Color.White.copy(alpha = 0.12f),
                    Color.Transparent,
                ),
                start = Offset(offset - shimmerWidth, 0f),
                end = Offset(offset + shimmerWidth, size.height),
            )
        )
    }
}

// ── Glow Border ─────────────────────────────────────────────────────────────

/**
 * Animated golden border (2.5s loop) on cards when vibrant mode is active.
 * Opacity ranges 0.15 → 0.35. Width 1.5dp.
 * Uses standard Compose .border() for reliable rendering inside clipped surfaces.
 * Only active in vibrant mode on FULL/STANDARD devices.
 */
fun Modifier.glowBorder(
    color: Color = Color.Unspecified,
    cornerRadius: Dp = 16.dp,
): Modifier = composed {
    val isVibrant = LocalVibrantMode.current
    val glowColor = if (color == Color.Unspecified) {
        androidx.compose.material3.MaterialTheme.colorScheme.primary
    } else color

    if (!isVibrant || reduceMotionEnabled() || DeviceCapabilities.isBasic) return@composed this

    val transition = rememberInfiniteTransition(label = "glow")
    val glowPhase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowPhase"
    )

    val borderOpacity = 0.15f + glowPhase * 0.20f

    this.border(
        width = 1.5.dp,
        color = glowColor.copy(alpha = borderOpacity),
        shape = RoundedCornerShape(cornerRadius)
    )
}

// ── Icon Pulse ──────────────────────────────────────────────────────────────

/**
 * Scale 1.0→1.15, opacity 1.0→0.85, 1.5s cycle. Vibrant only.
 */
fun Modifier.iconPulse(): Modifier = composed {
    val isVibrant = LocalVibrantMode.current
    if (!isVibrant || reduceMotionEnabled() || DeviceCapabilities.isBasic) return@composed this

    val transition = rememberInfiniteTransition(label = "iconPulse")
    val pulseScale by transition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    val pulseAlpha by transition.animateFloat(
        initialValue = 1f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    this.scale(pulseScale).alpha(pulseAlpha)
}

// ── Reduce Motion Check ─────────────────────────────────────────────────────

@Composable
private fun reduceMotionEnabled(): Boolean {
    val context = LocalContext.current
    return remember {
        try {
            Settings.Global.getFloat(
                context.contentResolver,
                Settings.Global.ANIMATOR_DURATION_SCALE,
                1f
            ) == 0f
        } catch (_: Exception) {
            false
        }
    }
}
