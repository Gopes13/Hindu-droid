package dev.gopes.hinducalendar.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private val confettiColors = listOf(
    Color(0xFFFF9933), // Saffron
    Color(0xFFE34234), // Vermillion
    Color(0xFFFFD700), // Gold
    Color(0xFF228B22), // Green
    Color(0xFF9B59B6), // Purple
    Color(0xFFFF69B4), // Pink
    Color(0xFFFF8C00), // Orange
    Color(0xFFE74C3C), // Red
    Color(0xFF3498DB)  // Blue
)

private data class Particle(
    val color: Color,
    val startAngle: Float,
    val speed: Float,
    val size: Float,
    val rotation: Float,
    val shape: Int // 0=circle, 1=rect, 2=capsule
)

@Composable
fun ConfettiOverlay(isActive: Boolean, onFinished: () -> Unit = {}) {
    if (!isActive) return

    val particles = remember {
        List(35) {
            Particle(
                color = confettiColors.random(),
                startAngle = Random.nextFloat() * 360f,
                speed = 200f + Random.nextFloat() * 400f,
                size = 4f + Random.nextFloat() * 6f,
                rotation = Random.nextFloat() * 720f,
                shape = Random.nextInt(3)
            )
        }
    }

    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(isActive) {
        if (isActive) {
            animProgress.snapTo(0f)
            animProgress.animateTo(1f, tween(2000, easing = LinearOutSlowInEasing))
            onFinished()
        }
    }

    Canvas(Modifier.fillMaxSize()) {
        val centerX = size.width / 2
        val centerY = size.height / 2
        val t = animProgress.value

        particles.forEach { particle ->
            val angle = Math.toRadians(particle.startAngle.toDouble())
            val distance = particle.speed * t
            val gravity = 200f * t * t
            val x = centerX + (cos(angle) * distance).toFloat()
            val y = centerY + (sin(angle) * distance).toFloat() + gravity
            val alpha = (1f - t).coerceIn(0f, 1f)
            val scale = 1f - (t * 0.6f)

            if (alpha > 0.01f && y < size.height + 50) {
                rotate(particle.rotation * t, pivot = Offset(x, y)) {
                    val s = particle.size * scale
                    when (particle.shape) {
                        0 -> drawCircle(
                            color = particle.color.copy(alpha = alpha),
                            radius = s,
                            center = Offset(x, y)
                        )
                        1 -> drawRect(
                            color = particle.color.copy(alpha = alpha),
                            topLeft = Offset(x - s, y - s / 2),
                            size = Size(s * 2, s)
                        )
                        else -> drawOval(
                            color = particle.color.copy(alpha = alpha),
                            topLeft = Offset(x - s, y - s / 3),
                            size = Size(s * 2, s * 0.66f)
                        )
                    }
                }
            }
        }
    }
}
