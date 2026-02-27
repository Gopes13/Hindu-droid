package dev.gopes.hinducalendar.feature.diya

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import kotlinx.coroutines.android.awaitFrame
import java.util.Calendar
import kotlin.math.sin

/**
 * Animated diya flame at 30fps with 3 sine-wave oscillators.
 * Exact iOS port: teardrop bezier, outer glow, inner core, dim factor by hour.
 */
@Composable
fun DiyaFlameCanvas(
    isLit: Boolean,
    modifier: Modifier = Modifier
) {
    if (!isLit) return

    var time by remember { mutableFloatStateOf(0f) }
    val dimFactor = remember { computeDimFactor() }

    LaunchedEffect(Unit) {
        var lastFrame = 0L
        while (true) {
            val frameTime = awaitFrame()
            if (lastFrame != 0L) {
                time += (frameTime - lastFrame) / 1_000_000_000f
            }
            lastFrame = frameTime
        }
    }

    Canvas(modifier = modifier) {
        drawFlame(time, dimFactor)
    }
}

private fun DrawScope.drawFlame(time: Float, dimFactor: Float) {
    val w = size.width
    val h = size.height
    val cx = w / 2f
    val baseY = h * 0.7f

    // Three sine oscillators
    val flicker1 = sin(time * 4.5f) * 0.08f
    val flicker2 = sin(time * 7.2f + 1.3f) * 0.05f
    val flicker3 = sin(time * 2.8f + 2.7f) * 0.12f
    val totalFlicker = 1.0f + flicker1 + flicker2 + flicker3

    val flameHeight = h * 0.45f * totalFlicker * dimFactor
    val flameWidth = w * 0.2f * (1.0f + flicker2)
    val sway = sin(time * 3.1f) * flameWidth * 0.15f
    val tipY = baseY - flameHeight

    // Outer glow
    drawOval(
        color = Color(0xFFFF8C00).copy(alpha = 0.2f * dimFactor),
        topLeft = Offset(cx - flameWidth * 2f, baseY - flameHeight * 1.2f),
        size = Size(flameWidth * 4f, flameHeight * 1.5f)
    )

    // Main flame body (teardrop bezier)
    val flamePath = Path().apply {
        moveTo(cx + sway, tipY)
        // Right side
        quadraticBezierTo(
            cx + flameWidth * 0.9f + sway * 0.5f, baseY - flameHeight * 0.35f,
            cx + flameWidth, baseY
        )
        // Bottom
        quadraticBezierTo(
            cx, baseY + flameHeight * 0.08f,
            cx - flameWidth, baseY
        )
        // Left side
        quadraticBezierTo(
            cx - flameWidth * 0.9f + sway * 0.5f, baseY - flameHeight * 0.35f,
            cx + sway, tipY
        )
        close()
    }

    val flameBrush = Brush.verticalGradient(
        colors = listOf(
            Color.White,
            Color.Yellow,
            Color(0xFFFF8C00),
            Color(0xFFFF8C00).copy(alpha = 0.3f)
        ),
        startY = tipY,
        endY = baseY
    )
    drawPath(flamePath, flameBrush, alpha = 0.85f * dimFactor)

    // Inner core (brighter, smaller)
    val innerHeight = flameHeight * 0.55f
    val innerWidth = flameWidth * 0.5f
    val innerTipY = baseY - innerHeight

    val innerPath = Path().apply {
        moveTo(cx + sway * 0.7f, innerTipY)
        quadraticBezierTo(
            cx + innerWidth * 0.8f, baseY - innerHeight * 0.3f,
            cx + innerWidth, baseY
        )
        quadraticBezierTo(
            cx, baseY + innerHeight * 0.05f,
            cx - innerWidth, baseY
        )
        quadraticBezierTo(
            cx - innerWidth * 0.8f, baseY - innerHeight * 0.3f,
            cx + sway * 0.7f, innerTipY
        )
        close()
    }

    val innerBrush = Brush.verticalGradient(
        colors = listOf(
            Color.White,
            Color.White.copy(alpha = 0.8f),
            Color.Yellow
        ),
        startY = innerTipY,
        endY = baseY
    )
    drawPath(innerPath, innerBrush, alpha = 0.9f * dimFactor)
}

/** Brightness factor based on time of day (matches iOS). */
private fun computeDimFactor(): Float {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when (hour) {
        in 5..9 -> 1.0f
        in 10..15 -> 0.85f
        in 16..19 -> 0.7f
        in 20..23 -> 0.5f
        else -> 0.3f
    }
}
