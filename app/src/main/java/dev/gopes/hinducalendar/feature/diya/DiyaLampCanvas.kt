package dev.gopes.hinducalendar.feature.diya

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke

/**
 * Canvas drawing of a traditional golden diya (oil lamp).
 * Exact iOS port: bezier bowl, handles, rim highlight, pedestal, and wick.
 */
@Composable
fun DiyaLampCanvas(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        drawLamp()
    }
}

private fun DrawScope.drawLamp() {
    val w = size.width
    val h = size.height
    val cx = w / 2f
    val baseY = h * 0.85f
    val lampWidth = w * 0.6f
    val lampHeight = h * 0.35f

    // Bowl gradient colors (top=bright gold → bottom=dark gold)
    val bowlBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFD8AD40), // bright gold
            Color(0xFFBF8C2D), // medium gold
            Color(0xFF996B1E)  // dark gold
        ),
        startY = baseY - lampHeight * 0.55f,
        endY = baseY + lampHeight * 0.15f
    )

    // Bowl path: left handle → bottom → right handle → rim (closes)
    val bowlPath = Path().apply {
        // Start at left handle top
        moveTo(cx - lampWidth * 0.6f, baseY - lampHeight * 0.3f)
        // Left handle curve
        quadraticBezierTo(
            cx - lampWidth * 0.65f, baseY - lampHeight * 0.15f,
            cx - lampWidth * 0.5f, baseY - lampHeight * 0.1f
        )
        // Bowl bottom curve
        quadraticBezierTo(
            cx, baseY + lampHeight * 0.15f,
            cx + lampWidth * 0.5f, baseY - lampHeight * 0.1f
        )
        // Right handle curve
        quadraticBezierTo(
            cx + lampWidth * 0.65f, baseY - lampHeight * 0.15f,
            cx + lampWidth * 0.6f, baseY - lampHeight * 0.3f
        )
        // Rim curve (closes the path)
        quadraticBezierTo(
            cx, baseY - lampHeight * 0.55f,
            cx - lampWidth * 0.6f, baseY - lampHeight * 0.3f
        )
        close()
    }

    drawPath(bowlPath, bowlBrush)

    // Rim highlight stroke
    val rimPath = Path().apply {
        moveTo(cx - lampWidth * 0.45f, baseY - lampHeight * 0.3f)
        quadraticBezierTo(
            cx, baseY - lampHeight * 0.55f,
            cx + lampWidth * 0.45f, baseY - lampHeight * 0.3f
        )
    }
    drawPath(
        rimPath,
        color = Color(0xFFF2D980).copy(alpha = 0.4f),
        style = Stroke(width = 1.5f)
    )

    // Pedestal base
    val pedestalPath = Path().apply {
        moveTo(cx - lampWidth * 0.25f, baseY)
        quadraticBezierTo(
            cx, baseY + lampHeight * 0.15f,
            cx + lampWidth * 0.25f, baseY
        )
        close()
    }
    drawPath(pedestalPath, color = Color(0xFFA67326))

    // Wick
    val wickWidth = 3f
    val wickHeight = 12f
    val wickX = cx - wickWidth / 2f
    val wickY = baseY - lampHeight * 0.4f - wickHeight
    drawRect(
        color = Color(0xFF4D3319),
        topLeft = Offset(wickX, wickY),
        size = androidx.compose.ui.geometry.Size(wickWidth, wickHeight)
    )
}
