package dev.gopes.hinducalendar.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import kotlin.math.sin
import kotlin.random.Random

/**
 * Canvas-based floating sparkle overlay.
 * Renders circles with sin-wave vertical drift animation.
 */
@Composable
fun FloatingSparkles(
    count: Int = 8,
    color: Color = Color.White,
    modifier: Modifier = Modifier
) {
    val seeds = remember(count) {
        List(count) {
            FloatArray(4).apply {
                val rng = Random(it * 137)
                this[0] = rng.nextFloat()                    // x fraction
                this[1] = rng.nextFloat()                    // y fraction
                this[2] = 1.5f + rng.nextFloat() * 2f       // radius
                this[3] = rng.nextFloat() * 6.28f            // phase offset
            }
        }
    }

    val transition = rememberInfiniteTransition(label = "sparkles")
    val time by transition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sparkleTime"
    )

    Canvas(modifier.fillMaxSize()) {
        seeds.forEach { seed ->
            val x = seed[0] * size.width
            val baseY = seed[1] * size.height
            val radius = seed[2]
            val phase = seed[3]

            val yDrift = sin((time + phase).toDouble()).toFloat() * 12f
            val alpha = 0.3f + 0.4f * ((sin((time * 1.5f + phase).toDouble()).toFloat() + 1f) / 2f)

            drawCircle(
                color = color.copy(alpha = alpha),
                radius = radius,
                center = Offset(x, baseY + yDrift)
            )
        }
    }
}
