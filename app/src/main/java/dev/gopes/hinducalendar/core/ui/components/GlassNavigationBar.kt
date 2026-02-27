package dev.gopes.hinducalendar.core.ui.components

import android.content.res.Configuration
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.ui.tooling.preview.Preview
import kotlin.math.abs
import dev.gopes.hinducalendar.core.ui.theme.HinduCalendarTheme
import dev.gopes.hinducalendar.core.ui.theme.LocalVibrantMode
import dev.gopes.hinducalendar.core.util.DeviceCapabilities

data class GlassNavItem(
    val icon: ImageVector,
    val label: String,
    val selected: Boolean,
    val onClick: () -> Unit
)

@Composable
fun GlassNavigationBar(
    items: List<GlassNavItem>,
    modifier: Modifier = Modifier
) {
    val tier = DeviceCapabilities.renderTier
    val isDark = isSystemInDarkTheme()
    val isVibrant = LocalVibrantMode.current
    val density = LocalDensity.current
    val view = LocalView.current

    // Always-fresh reference to items for use inside pointerInput coroutine
    val currentItems by rememberUpdatedState(items)

    val surfaceColor = MaterialTheme.colorScheme.surface
    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant

    val barShape = RoundedCornerShape(28.dp)

    // ── Liquid Glass Gradient ───────────────────────────────────────────────
    val barBrush = if (tier == DeviceCapabilities.RenderTier.BASIC) {
        Brush.verticalGradient(colors = listOf(surfaceColor, surfaceColor))
    } else if (isDark) {
        Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = if (isVibrant) 0.12f else 0.06f),
                surfaceColor.copy(alpha = 0.35f),
                surfaceColor.copy(alpha = 0.60f)
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = if (isVibrant) 0.60f else 0.45f),
                surfaceColor.copy(alpha = 0.45f),
                surfaceColor.copy(alpha = 0.70f)
            )
        )
    }

    // ── Bar border ──────────────────────────────────────────────────────────
    val barBorderColor = if (tier == DeviceCapabilities.RenderTier.BASIC) {
        Color.Transparent
    } else if (isDark) {
        Color.White.copy(alpha = if (isVibrant) 0.18f else 0.12f)
    } else {
        Color.Black.copy(alpha = 0.08f)
    }

    // ── Tab position tracking ───────────────────────────────────────────────
    val tabOffsets = remember { mutableStateMapOf<Int, Float>() }
    val tabWidths = remember { mutableStateMapOf<Int, Float>() }
    val tabCenters = remember { mutableStateMapOf<Int, Float>() }
    val selectedIndex = items.indexOfFirst { it.selected }.coerceAtLeast(0)

    // ── Drag state ──────────────────────────────────────────────────────────
    var isDragging by remember { mutableStateOf(false) }
    var dragXPx by remember { mutableFloatStateOf(0f) }

    // ── Indicator X: stiff spring during drag (instant), smooth on release ─
    val selectedTargetPx = tabOffsets[selectedIndex] ?: 0f
    val targetPx = if (isDragging) dragXPx else selectedTargetPx
    val targetDp = with(density) { targetPx.toDp() }

    val indicatorX by animateDpAsState(
        targetValue = targetDp,
        animationSpec = if (isDragging) {
            // Near-instant: critically damped, very stiff → tracks finger 1:1
            spring(dampingRatio = 1f, stiffness = 10000f)
        } else {
            // Smooth spring for tab switches and snap-back
            spring(dampingRatio = 0.7f, stiffness = 400f)
        },
        label = "indicatorX"
    )

    // ── Indicator width (animated) ──────────────────────────────────────────
    val targetWidthDp = with(density) { (tabWidths[selectedIndex] ?: 0f).toDp() }
    val indicatorW by animateDpAsState(
        targetValue = targetWidthDp,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f),
        label = "indicatorW"
    )

    // ── Indicator colors (clear glass) ──────────────────────────────────────
    val indicatorFill = if (isDark) {
        Color.White.copy(alpha = 0.12f)
    } else {
        Color.White.copy(alpha = 0.25f)
    }
    val indicatorBorderColor = if (isDark) {
        Color.White.copy(alpha = 0.20f)
    } else {
        Color.Black.copy(alpha = 0.10f)
    }
    val indicatorShape = RoundedCornerShape(22.dp)
    val indicatorPad = 4.dp
    val tabHeight = 64.dp

    // ── Floating capsule bar ────────────────────────────────────────────────
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .clip(barShape)
                .background(barBrush)
                .then(
                    if (barBorderColor != Color.Transparent) {
                        Modifier.border(0.5.dp, barBorderColor, barShape)
                    } else Modifier
                )
                .pointerInput(Unit) {
                    val tapThreshold = 8.dp.toPx()

                    awaitEachGesture {
                        val down = awaitFirstDown()
                        val startX = down.position.x
                        val liveItems = currentItems
                        val startIdx = liveItems.indexOfFirst { it.selected }.coerceAtLeast(0)
                        var hasDragged = false
                        var lastHapticIdx = -1
                        var lastX = startX

                        do {
                            val event = awaitPointerEvent()
                            val x = event.changes.first().position.x

                            if (!hasDragged && abs(x - startX) > tapThreshold) {
                                hasDragged = true
                                isDragging = true
                            }

                            if (hasDragged) {
                                // Continuous tracking: center indicator on finger
                                val tabW = tabWidths[0]
                                    ?: (size.width / liveItems.size.toFloat())
                                val left = (x - tabW / 2f)
                                    .coerceIn(0f, size.width.toFloat() - tabW)
                                dragXPx = left

                                // Haptic tick when crossing a tab center
                                val hoverIdx = tabCenters
                                    .minByOrNull { abs(it.value - x) }?.key ?: -1
                                if (hoverIdx != -1 && hoverIdx != lastHapticIdx) {
                                    lastHapticIdx = hoverIdx
                                    HapticHelper.selection(view)
                                }
                            }

                            lastX = x
                        } while (event.changes.any { it.pressed })

                        if (hasDragged) {
                            // Find which tab finger was released on
                            val closestIdx = tabCenters
                                .minByOrNull { abs(it.value - lastX) }?.key
                                ?: startIdx
                            val center = tabCenters[closestIdx] ?: 0f
                            val w = tabWidths[closestIdx] ?: 0f

                            isDragging = false

                            if (abs(lastX - center) < w * 0.45f
                                && closestIdx != startIdx
                            ) {
                                // Released on a different tab — navigate
                                currentItems[closestIdx].onClick()
                            }
                            // Otherwise: isDragging=false, targetPx reverts to
                            // selectedTargetPx → indicator springs back smoothly
                        } else {
                            // It was a tap — find which tab was tapped
                            val tapIdx = tabCenters
                                .minByOrNull { abs(it.value - startX) }?.key
                                ?: -1
                            if (tapIdx != -1 && !currentItems[tapIdx].selected) {
                                HapticHelper.selection(view)
                                currentItems[tapIdx].onClick()
                            }
                        }
                    }
                }
        ) {
            // Sliding glass indicator (full tab width)
            if (tabOffsets.isNotEmpty() && indicatorW > 0.dp) {
                Box(
                    modifier = Modifier
                        .offset(x = indicatorX + indicatorPad, y = indicatorPad)
                        .width(indicatorW - indicatorPad * 2)
                        .height(tabHeight - indicatorPad * 2)
                        .clip(indicatorShape)
                        .background(indicatorFill)
                        .border(0.5.dp, indicatorBorderColor, indicatorShape)
                )
            }

            // Tab items
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(tabHeight),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEachIndexed { index, item ->
                    GlassTabItem(
                        item = item,
                        primaryColor = primaryColor,
                        unselectedColor = onSurfaceVariantColor,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .onGloballyPositioned { coords ->
                                tabOffsets[index] = coords.positionInParent().x
                                tabWidths[index] = coords.size.width.toFloat()
                                tabCenters[index] = coords.positionInParent().x + coords.size.width / 2f
                            },
                        onTap = {
                            HapticHelper.selection(view)
                            item.onClick()
                        }
                    )
                }
            }
        }

        // Navigation bar inset spacer
        Spacer(Modifier.navigationBarsPadding())
    }
}

@Composable
private fun GlassTabItem(
    item: GlassNavItem,
    primaryColor: Color,
    unselectedColor: Color,
    modifier: Modifier = Modifier,
    onTap: () -> Unit
) {
    val iconColor by animateColorAsState(
        targetValue = if (item.selected) primaryColor else unselectedColor,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessMedium),
        label = "tabIconColor"
    )
    val labelColor by animateColorAsState(
        targetValue = if (item.selected) primaryColor else unselectedColor,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessMedium),
        label = "tabLabelColor"
    )
    val iconScale by animateFloatAsState(
        targetValue = if (item.selected) 1.05f else 1f,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = Spring.StiffnessMediumLow),
        label = "tabIconScale"
    )

    Box(
        modifier = modifier
            .semantics {
                onClick(label = item.label) { onTap(); true }
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.label,
                tint = iconColor,
                modifier = Modifier
                    .size(22.dp)
                    .scale(iconScale)
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = item.label,
                style = MaterialTheme.typography.labelMedium,
                color = labelColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Preview(showBackground = true, name = "Light")
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark")
@Composable
private fun GlassNavigationBarPreview() {
    HinduCalendarTheme {
        GlassNavigationBar(
            items = listOf(
                GlassNavItem(Icons.Filled.WbSunny, "Today", true) {},
                GlassNavItem(Icons.AutoMirrored.Filled.MenuBook, "Texts", false) {},
                GlassNavItem(Icons.Filled.CalendarMonth, "Calendar", false) {},
                GlassNavItem(Icons.Filled.Star, "Festivals", false) {},
                GlassNavItem(Icons.Filled.Settings, "Settings", false) {},
            )
        )
    }
}
