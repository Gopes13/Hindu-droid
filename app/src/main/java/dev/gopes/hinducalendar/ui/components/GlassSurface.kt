package dev.gopes.hinducalendar.ui.components

import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.gopes.hinducalendar.ui.theme.LocalVibrantMode
import dev.gopes.hinducalendar.ui.util.DeviceCapabilities

/**
 * Glass surface elevation tiers matching iOS GlassSurface.swift exactly.
 */
enum class SurfaceElevation {
    /** Hero content — strongest blur, vibrant tint, deep shadow, inner glow. */
    PROMINENT,
    /** Default cards — moderate blur, subtle tint, standard shadow. */
    STANDARD,
    /** Secondary info — minimal blur, light tint, soft shadow. */
    RECESSED
}

/**
 * A glass-morphism surface composable that provides progressive enhancement:
 * - API 31+: Real blur via RenderEffect + tint overlay + shadows + inner glow
 * - API 26-30: Semi-transparent tinted background + layered shadows
 * - API 21-25: Solid themed background with elevation
 */
@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    elevation: SurfaceElevation = SurfaceElevation.STANDARD,
    cornerRadius: Dp = 16.dp,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    content: @Composable ColumnScope.() -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val isVibrant = LocalVibrantMode.current
    val shape = RoundedCornerShape(cornerRadius)
    val tier = DeviceCapabilities.renderTier

    // Compute visual properties based on elevation + mode
    val props = glassProperties(elevation, isDark, isVibrant)

    Box(
        modifier = modifier
            .fillMaxWidth()
            // Primary shadow (colored)
            .shadow(
                elevation = props.primaryShadowRadius,
                shape = shape,
                ambientColor = accentColor.copy(alpha = props.primaryShadowOpacity),
                spotColor = accentColor.copy(alpha = props.primaryShadowOpacity)
            )
            // Ambient shadow (dark)
            .shadow(
                elevation = props.ambientShadowRadius,
                shape = shape,
                ambientColor = Color.Black.copy(alpha = props.ambientShadowOpacity),
                spotColor = Color.Black.copy(alpha = props.ambientShadowOpacity)
            )
            .clip(shape)
            // Blur effect on API 31+
            .then(
                if (tier == DeviceCapabilities.RenderTier.FULL && Build.VERSION.SDK_INT >= 31) {
                    Modifier.graphicsLayer {
                        renderEffect = RenderEffect
                            .createBlurEffect(
                                props.blurRadius,
                                props.blurRadius,
                                Shader.TileMode.CLAMP
                            )
                            .asComposeRenderEffect()
                    }
                } else {
                    Modifier
                }
            )
            // Background: tinted glass or solid fallback
            .background(glassBackground(elevation, isDark, isVibrant, accentColor, tier))
            // Border
            .border(
                width = props.borderWidth,
                brush = Brush.linearGradient(
                    colors = listOf(
                        accentColor.copy(alpha = props.borderOpacity * 1.2f),
                        accentColor.copy(alpha = props.borderOpacity * 0.6f),
                        accentColor.copy(alpha = props.borderOpacity * 0.3f),
                    )
                ),
                shape = shape
            )
            // Inner glow for PROMINENT
            .then(
                if (elevation == SurfaceElevation.PROMINENT && isDark && tier != DeviceCapabilities.RenderTier.BASIC) {
                    Modifier.drawBehind {
                        drawRoundRect(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.12f),
                                    Color.White.copy(alpha = 0.03f),
                                    Color.Transparent
                                ),
                                start = Offset.Zero,
                                end = Offset(size.width, size.height)
                            ),
                            cornerRadius = CornerRadius(cornerRadius.toPx()),
                            style = Stroke(width = 1.dp.toPx()),
                            size = Size(size.width, size.height)
                        )
                    }
                } else {
                    Modifier
                }
            )
    ) {
        Column(Modifier.padding(16.dp), content = content)
    }
}

// ── Glass Properties per Elevation ──────────────────────────────────────────

private data class GlassProps(
    val blurRadius: Float,
    val primaryShadowRadius: Dp,
    val primaryShadowOpacity: Float,
    val ambientShadowRadius: Dp,
    val ambientShadowOpacity: Float,
    val borderWidth: Dp,
    val borderOpacity: Float,
)

private fun glassProperties(
    elevation: SurfaceElevation,
    isDark: Boolean,
    isVibrant: Boolean
): GlassProps = when (elevation) {
    SurfaceElevation.PROMINENT -> GlassProps(
        blurRadius = 20f,
        primaryShadowRadius = if (isVibrant) 24.dp else 16.dp,
        primaryShadowOpacity = if (isVibrant) 0.22f else 0.12f,
        ambientShadowRadius = if (isDark) 28.dp else 20.dp,
        ambientShadowOpacity = if (isDark) 0.25f else 0.10f,
        borderWidth = if (isVibrant) 1.dp else 0.5.dp,
        borderOpacity = if (isVibrant) 0.35f else 0.20f,
    )
    SurfaceElevation.STANDARD -> GlassProps(
        blurRadius = 12f,
        primaryShadowRadius = if (isVibrant) 14.dp else 8.dp,
        primaryShadowOpacity = if (isVibrant) 0.14f else 0.08f,
        ambientShadowRadius = if (isDark) 18.dp else 12.dp,
        ambientShadowOpacity = if (isDark) 0.15f else 0.06f,
        borderWidth = 0.5.dp,
        borderOpacity = if (isVibrant) 0.20f else 0.12f,
    )
    SurfaceElevation.RECESSED -> GlassProps(
        blurRadius = 6f,
        primaryShadowRadius = 6.dp,
        primaryShadowOpacity = 0.04f,
        ambientShadowRadius = if (isDark) 10.dp else 6.dp,
        ambientShadowOpacity = if (isDark) 0.08f else 0.03f,
        borderWidth = 0.5.dp,
        borderOpacity = 0.06f,
    )
}

// ── Background Color Logic ──────────────────────────────────────────────────

@Composable
private fun glassBackground(
    elevation: SurfaceElevation,
    isDark: Boolean,
    isVibrant: Boolean,
    accentColor: Color,
    tier: DeviceCapabilities.RenderTier
): Color {
    val surfaceColor = MaterialTheme.colorScheme.surface

    // On BASIC tier, return solid surface (no glass effect)
    if (tier == DeviceCapabilities.RenderTier.BASIC) {
        return surfaceColor
    }

    // Compute tint opacity from iOS spec
    val tintOpacity = when (elevation) {
        SurfaceElevation.PROMINENT -> when {
            isVibrant && isDark -> 0.14f
            isVibrant -> 0.10f
            isDark -> 0.08f
            else -> 0.06f
        }
        SurfaceElevation.STANDARD -> when {
            isVibrant && isDark -> 0.08f
            isVibrant -> 0.06f
            isDark -> 0.05f
            else -> 0.03f
        }
        SurfaceElevation.RECESSED -> when {
            isDark -> 0.03f
            else -> 0.02f
        }
    }

    // On STANDARD tier (no real blur), bump opacity to compensate for missing blur
    val adjustedOpacity = if (tier == DeviceCapabilities.RenderTier.STANDARD) {
        tintOpacity + 0.15f
    } else {
        tintOpacity
    }

    // Blend surface with accent tint
    return surfaceColor.copy(alpha = 0.85f - adjustedOpacity)
        .compositeOver(accentColor.copy(alpha = adjustedOpacity))
}

/**
 * Composite this color over another, producing an opaque result.
 */
private fun Color.compositeOver(background: Color): Color {
    val fgA = this.alpha
    val bgA = background.alpha
    val outA = fgA + bgA * (1f - fgA)
    if (outA == 0f) return Color.Transparent
    return Color(
        red = (this.red * fgA + background.red * bgA * (1f - fgA)) / outA,
        green = (this.green * fgA + background.green * bgA * (1f - fgA)) / outA,
        blue = (this.blue * fgA + background.blue * bgA * (1f - fgA)) / outA,
        alpha = outA
    )
}
