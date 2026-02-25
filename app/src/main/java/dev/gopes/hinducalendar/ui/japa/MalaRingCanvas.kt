package dev.gopes.hinducalendar.ui.japa

import android.graphics.BlurMaskFilter
import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import dev.gopes.hinducalendar.data.model.MalaMaterial
import kotlin.math.*

/**
 * Compose Canvas rendering of a 108-bead mala ring.
 * Ported 1:1 from iOS MalaRingView.swift with exact geometry.
 */
@Composable
fun MalaRingCanvas(
    currentBead: Int,
    material: MalaMaterial,
    modifier: Modifier = Modifier
) {
    val beadColors = remember(material) { material.beadGradient }
    val sumeruColor = remember(material) { material.sumeruColor }
    val isRudraksha = material == MalaMaterial.RUDRAKSHA

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // Ellipse center & radii (matching iOS exactly)
        val cx = w / 2f
        val cy = h * 0.55f
        val hw = minOf(w * 0.38f, 150f)
        val hh = minOf(h * 0.35f, 200f)

        // Bead sizing via perimeter approximation
        val perimeter = PI.toFloat() * (3f * (hw + hh) - sqrt((3f * hw + hh) * (hw + 3f * hh)))
        val beadDia = maxOf(5f, minOf(8f, perimeter / 145f))
        val sumeruDia = beadDia * 2f

        // Sumeru position & gap
        val sumeruAngle = -PI.toFloat() / 2f
        val sumeruGap = (2f * PI.toFloat()) / 80f

        // Precompute 108 bead positions
        val beadPositions = (0 until 108).map { i ->
            val fraction = i.toFloat() / 108f
            val angle = sumeruAngle + sumeruGap / 2f + fraction * (2f * PI.toFloat() - sumeruGap)
            malaPoint(cx, cy, hw, hh, angle)
        }

        // 1. Draw thread
        drawThread(cx, cy, hw, hh, sumeruAngle, sumeruGap, beadDia)

        // 2. Draw tassel below sumeru
        val sumeruPos = malaPoint(cx, cy, hw, hh, sumeruAngle)
        drawTassel(sumeruPos, sumeruDia, h)

        // 3. Draw all 108 beads
        beadPositions.forEachIndexed { i, pos ->
            val state = when {
                i < currentBead -> BeadState.COMPLETED
                i == currentBead -> BeadState.ACTIVE
                else -> BeadState.FUTURE
            }
            drawBead(pos, beadDia, beadColors, state, isRudraksha)
        }

        // 4. Draw sumeru bead
        drawSumeruBead(sumeruPos, sumeruDia, sumeruColor)
    }
}

// ── Bead Position Formula (exact iOS port) ──────────────────────────────────

private fun malaPoint(cx: Float, cy: Float, hw: Float, hh: Float, angle: Float): Offset {
    val s = sin(angle)
    val narrowing = if (s > 0) 0.12f * s * s else 0.45f * s * s
    return Offset(
        cx + hw * (1f - narrowing) * cos(angle),
        cy + hh * s
    )
}

// ── Bead Rendering ──────────────────────────────────────────────────────────

private enum class BeadState { COMPLETED, ACTIVE, FUTURE }

private fun DrawScope.drawBead(
    pos: Offset,
    dia: Float,
    colors: List<Color>,
    state: BeadState,
    isRudraksha: Boolean
) {
    val alpha = when (state) {
        BeadState.COMPLETED -> 1f
        BeadState.ACTIVE -> 1f
        BeadState.FUTURE -> 0.3f
    }
    val radius = if (state == BeadState.ACTIVE) dia / 2f + 2f else dia / 2f

    // Glow for active bead
    if (state == BeadState.ACTIVE) {
        drawIntoCanvas { canvas ->
            val paint = Paint().apply {
                isAntiAlias = true
                color = colors[0].copy(alpha = 0.5f).toArgb()
                maskFilter = BlurMaskFilter(5f, BlurMaskFilter.Blur.NORMAL)
            }
            canvas.nativeCanvas.drawCircle(pos.x, pos.y, radius + 3f, paint)
        }
    }

    // Bead body with radial gradient
    val gradientCenter = Offset(pos.x - dia * 0.15f, pos.y - dia * 0.15f)
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(colors[0].copy(alpha = alpha), colors[1].copy(alpha = alpha)),
            center = gradientCenter,
            radius = dia * 0.7f
        ),
        radius = radius,
        center = pos,
    )

    // Rudraksha ridge texture
    if (isRudraksha && state != BeadState.FUTURE) {
        drawLine(
            color = Color.Black.copy(alpha = 0.3f),
            start = Offset(pos.x, pos.y - dia * 0.3f),
            end = Offset(pos.x, pos.y + dia * 0.3f),
            strokeWidth = 0.5f
        )
    }
}

// ── Sumeru Bead ─────────────────────────────────────────────────────────────

private fun DrawScope.drawSumeruBead(pos: Offset, dia: Float, color: Color) {
    // Glow
    drawIntoCanvas { canvas ->
        val paint = Paint().apply {
            isAntiAlias = true
            this.color = color.copy(alpha = 0.4f).toArgb()
            maskFilter = BlurMaskFilter(6f, BlurMaskFilter.Blur.NORMAL)
        }
        canvas.nativeCanvas.drawCircle(pos.x, pos.y, dia / 2f + 3f, paint)
    }
    // Body
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(color, color.copy(alpha = 0.7f)),
            center = Offset(pos.x - dia * 0.1f, pos.y - dia * 0.1f),
            radius = dia * 0.8f
        ),
        radius = dia / 2f,
        center = pos,
    )
    // Border
    drawCircle(
        color = color.copy(alpha = 0.6f),
        radius = dia / 2f,
        center = pos,
        style = Stroke(width = 1f)
    )
}

// ── Thread ──────────────────────────────────────────────────────────────────

private fun DrawScope.drawThread(
    cx: Float, cy: Float, hw: Float, hh: Float,
    sumeruAngle: Float, sumeruGap: Float, beadDia: Float
) {
    val path = Path()
    val segments = 200
    val startAngle = sumeruAngle + sumeruGap / 2f
    val totalArc = 2f * PI.toFloat() - sumeruGap

    for (i in 0..segments) {
        val fraction = i.toFloat() / segments
        val angle = startAngle + fraction * totalArc
        val pt = malaPoint(cx, cy, hw, hh, angle)
        if (i == 0) path.moveTo(pt.x, pt.y) else path.lineTo(pt.x, pt.y)
    }

    drawPath(
        path = path,
        color = Color(0xFFBF9940).copy(alpha = 0.5f),
        style = Stroke(width = 1.5f, cap = StrokeCap.Round)
    )
}

// ── Tassel ──────────────────────────────────────────────────────────────────

private fun DrawScope.drawTassel(sumeruPos: Offset, sumeruDia: Float, canvasHeight: Float) {
    val cx = sumeruPos.x
    val topY = sumeruPos.y + sumeruDia / 2f + 2f

    // 1. Red connector bead
    val connectorSize = sumeruDia * 0.45f
    drawCircle(
        color = Color(0xFFB31A1A),
        radius = connectorSize / 2f,
        center = Offset(cx, topY + connectorSize / 2f)
    )

    // 2. Turquoise binding (trapezoid)
    val bindingTop = topY + connectorSize
    val bindingTopW = sumeruDia * 0.45f
    val bindingBotW = sumeruDia * 0.65f
    val bindingH = sumeruDia * 0.55f

    val bindingPath = Path().apply {
        moveTo(cx - bindingTopW / 2f, bindingTop)
        lineTo(cx + bindingTopW / 2f, bindingTop)
        lineTo(cx + bindingBotW / 2f, bindingTop + bindingH)
        lineTo(cx - bindingBotW / 2f, bindingTop + bindingH)
        close()
    }
    drawPath(bindingPath, color = Color(0xFF33999A))

    // 3. Gold bands (3 horizontal lines)
    for (j in 0..2) {
        val bandY = bindingTop + bindingH * (0.2f + j * 0.3f)
        val bandHalfW = bindingTopW / 2f + (bindingBotW - bindingTopW) / 2f * ((0.2f + j * 0.3f))
        drawLine(
            color = Color(0xFFD4A046).copy(alpha = 0.6f),
            start = Offset(cx - bandHalfW, bandY),
            end = Offset(cx + bandHalfW, bandY),
            strokeWidth = 1f
        )
    }

    // 4. Red tassel threads (14 curved threads)
    val threadStartY = topY + connectorSize / 2f
    val spreadWidth = sumeruDia * 1.6f
    val threadLength = minOf(40f, canvasHeight - threadStartY - 8f)
    if (threadLength <= 0f) return

    for (i in 0 until 14) {
        val t = i.toFloat() / 13f
        val endX = cx - spreadWidth / 2f + t * spreadWidth
        val endY = threadStartY + threadLength

        val path = Path().apply {
            moveTo(cx, threadStartY)
            quadraticBezierTo(
                (cx + endX) / 2f,
                threadStartY + threadLength * 0.6f,
                endX,
                endY
            )
        }
        drawPath(
            path = path,
            color = Color(0xFFA61414).copy(alpha = 0.75f),
            style = Stroke(width = 1.5f, cap = StrokeCap.Round)
        )
    }
}
