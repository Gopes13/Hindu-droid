package dev.gopes.hinducalendar.ui.util

import android.os.Build

/**
 * Progressive enhancement tiers for visual effects.
 * FULL:     API 31+ — real blur, full atmosphere, all animations at 60fps
 * STANDARD: API 26-30 — semi-transparent tinted backgrounds, simpler atmosphere, reduced animations
 * BASIC:    API 21-25 — solid themed backgrounds, no blur/atmosphere, basic fade-in only
 */
object DeviceCapabilities {

    enum class RenderTier { FULL, STANDARD, BASIC }

    val renderTier: RenderTier
        get() = when {
            Build.VERSION.SDK_INT >= 31 -> RenderTier.FULL
            Build.VERSION.SDK_INT >= 26 -> RenderTier.STANDARD
            else -> RenderTier.BASIC
        }

    val supportsBlur: Boolean
        get() = Build.VERSION.SDK_INT >= 31

    val supportsRenderEffect: Boolean
        get() = Build.VERSION.SDK_INT >= 31

    val isFull: Boolean get() = renderTier == RenderTier.FULL
    val isStandard: Boolean get() = renderTier == RenderTier.STANDARD
    val isBasic: Boolean get() = renderTier == RenderTier.BASIC
}
