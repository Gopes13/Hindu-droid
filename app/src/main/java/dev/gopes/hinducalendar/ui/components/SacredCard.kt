package dev.gopes.hinducalendar.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

/**
 * A warm, glowing card with saffron-tinted shadow.
 * Delegates to GlassSurface for glass morphism on capable devices.
 * Same public API as before — all existing usages auto-upgrade.
 */
@Composable
fun SacredCard(
    modifier: Modifier = Modifier,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    isHighlighted: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    GlassSurface(
        modifier = modifier,
        elevation = if (isHighlighted) SurfaceElevation.PROMINENT else SurfaceElevation.STANDARD,
        accentColor = accentColor,
        content = content
    )
}

/**
 * A highlighted card with prominent glass surface treatment.
 * Same public API as before — all existing usages auto-upgrade.
 */
@Composable
fun SacredHighlightCard(
    modifier: Modifier = Modifier,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    content: @Composable ColumnScope.() -> Unit
) {
    GlassSurface(
        modifier = modifier,
        elevation = SurfaceElevation.PROMINENT,
        accentColor = accentColor,
        content = content
    )
}
