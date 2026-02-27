package dev.gopes.hinducalendar.core.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import dev.gopes.hinducalendar.core.util.DeviceCapabilities
import java.util.Calendar

/**
 * Time-of-day atmosphere engine matching iOS AtmosphereEngine.swift exactly.
 * 8 day periods with 9-color mesh palettes, accent tints, and ambient opacity.
 */
object AtmosphereEngine {

    enum class DayPeriod {
        BRAHMA_MUHURTA, // 4:00 - 5:30
        DAWN,           // 5:30 - 7:00
        MORNING,        // 7:00 - 11:00
        MIDDAY,         // 11:00 - 14:00
        AFTERNOON,      // 14:00 - 17:00
        GOLDEN_HOUR,    // 17:00 - 18:30
        DUSK,           // 18:30 - 20:00
        NIGHT           // 20:00 - 4:00
    }

    @Stable
    data class DayAtmosphere(
        val period: DayPeriod,
        val progress: Float,
        val meshColors: List<Color>,
        val accentTint: Color,
        val ambientOpacity: Float
    )

    /**
     * Compute current atmosphere from system clock.
     */
    fun computeAtmosphere(): DayAtmosphere {
        val cal = Calendar.getInstance()
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        val minute = cal.get(Calendar.MINUTE)
        val hourDecimal = hour + minute / 60.0
        return computeAtmosphere(hourDecimal)
    }

    /**
     * Compute atmosphere for a given decimal hour (e.g., 6.5 = 6:30 AM).
     */
    fun computeAtmosphere(hourDecimal: Double): DayAtmosphere {
        val (period, progress) = determinePeriod(hourDecimal)
        val colors = meshColors(period)
        val accent = accentTint(period)
        val ambient = ambientOpacity(period)
        return DayAtmosphere(period, progress, colors, accent, ambient)
    }

    private fun determinePeriod(hour: Double): Pair<DayPeriod, Float> = when {
        hour < 4.0  -> DayPeriod.NIGHT to ((hour + 4.0) / 8.0).toFloat()
        hour < 5.5  -> DayPeriod.BRAHMA_MUHURTA to ((hour - 4.0) / 1.5).toFloat()
        hour < 7.0  -> DayPeriod.DAWN to ((hour - 5.5) / 1.5).toFloat()
        hour < 11.0 -> DayPeriod.MORNING to ((hour - 7.0) / 4.0).toFloat()
        hour < 14.0 -> DayPeriod.MIDDAY to ((hour - 11.0) / 3.0).toFloat()
        hour < 17.0 -> DayPeriod.AFTERNOON to ((hour - 14.0) / 3.0).toFloat()
        hour < 18.5 -> DayPeriod.GOLDEN_HOUR to ((hour - 17.0) / 1.5).toFloat()
        hour < 20.0 -> DayPeriod.DUSK to ((hour - 18.5) / 1.5).toFloat()
        else        -> DayPeriod.NIGHT to ((hour - 20.0) / 8.0).toFloat()
    }

    // ── 9-Color Mesh Palettes (3x3 grid: TL, TC, TR, ML, MC, MR, BL, BC, BR) ──

    private fun meshColors(period: DayPeriod): List<Color> = when (period) {
        DayPeriod.BRAHMA_MUHURTA -> listOf(
            Color(0xFF140D2E), // (0.08, 0.05, 0.18)
            Color(0xFF100A30), // (0.06, 0.04, 0.19)
            Color(0xFF1F0D33), // (0.12, 0.05, 0.20)
            Color(0xFF0F0B2B), // (0.06, 0.04, 0.17)
            Color(0xFF170F33), // (0.09, 0.06, 0.20)
            Color(0xFF140D30), // (0.08, 0.05, 0.19)
            Color(0xFF0D081F), // (0.05, 0.03, 0.12)
            Color(0xFF0D0826), // (0.05, 0.03, 0.15)
            Color(0xFF1A0F2E), // (0.10, 0.06, 0.18)
        )
        DayPeriod.DAWN -> listOf(
            Color(0xFF2E1A47), // (0.18, 0.10, 0.28)
            Color(0xFF5C1A33), // (0.36, 0.10, 0.20)
            Color(0xFF8C3340), // (0.55, 0.20, 0.25)
            Color(0xFF6B2E1F), // (0.42, 0.18, 0.12)
            Color(0xFFD97326), // (0.85, 0.45, 0.15)
            Color(0xFFE8852E), // (0.91, 0.52, 0.18)
            Color(0xFFF2993D), // (0.95, 0.60, 0.24)
            Color(0xFFF5A843), // (0.96, 0.66, 0.26)
            Color(0xFFFABF59), // (0.98, 0.75, 0.35)
        )
        DayPeriod.MORNING -> listOf(
            Color(0xFFF2D9B3), // (0.95, 0.85, 0.70)
            Color(0xFFF5E0BF), // (0.96, 0.88, 0.75)
            Color(0xFFF7E6CC), // (0.97, 0.90, 0.80)
            Color(0xFFF5DEB8), // (0.96, 0.87, 0.72)
            Color(0xFFF7E5C4), // (0.97, 0.90, 0.77)
            Color(0xFFFAEDD4), // (0.98, 0.93, 0.83)
            Color(0xFFF7E3BF), // (0.97, 0.89, 0.75)
            Color(0xFFFAEBCC), // (0.98, 0.92, 0.80)
            Color(0xFFFFF0D9), // (1.00, 0.94, 0.85)
        )
        DayPeriod.MIDDAY -> listOf(
            Color(0xFFF7EDD9), // (0.97, 0.93, 0.85)
            Color(0xFFFAF0E0), // (0.98, 0.94, 0.88)
            Color(0xFFFFF5E6), // (1.00, 0.96, 0.90)
            Color(0xFFF7F0E0), // (0.97, 0.94, 0.88)
            Color(0xFFFAF2E3), // (0.98, 0.95, 0.89)
            Color(0xFFFFF7EB), // (1.00, 0.97, 0.92)
            Color(0xFFFAF0E3), // (0.98, 0.94, 0.89)
            Color(0xFFFFF5E8), // (1.00, 0.96, 0.91)
            Color(0xFFFFF8F0), // (1.00, 0.97, 0.94)
        )
        DayPeriod.AFTERNOON -> listOf(
            Color(0xFFFCF2E0), // (0.99, 0.95, 0.88)
            Color(0xFFFAEDD4), // (0.98, 0.93, 0.83)
            Color(0xFFF7E5C4), // (0.97, 0.90, 0.77)
            Color(0xFFFAEDD6), // (0.98, 0.93, 0.84)
            Color(0xFFF5E0B8), // (0.96, 0.88, 0.72)
            Color(0xFFF2D9A8), // (0.95, 0.85, 0.66)
            Color(0xFFF7E3BF), // (0.97, 0.89, 0.75)
            Color(0xFFF2D6A3), // (0.95, 0.84, 0.64)
            Color(0xFFF0CC94), // (0.94, 0.80, 0.58)
        )
        DayPeriod.GOLDEN_HOUR -> listOf(
            Color(0xFFEB8C2E), // (0.92, 0.55, 0.18)
            Color(0xFFD9662E), // (0.85, 0.40, 0.18)
            Color(0xFFC74A2E), // (0.78, 0.29, 0.18)
            Color(0xFFD97026), // (0.85, 0.44, 0.15)
            Color(0xFFC74D2E), // (0.78, 0.30, 0.18)
            Color(0xFFAD3326), // (0.68, 0.20, 0.15)
            Color(0xFFBF5426), // (0.75, 0.33, 0.15)
            Color(0xFFA33326), // (0.64, 0.20, 0.15)
            Color(0xFF8C2E40), // (0.55, 0.18, 0.25)
        )
        DayPeriod.DUSK -> listOf(
            Color(0xFF80334D), // (0.50, 0.20, 0.30)
            Color(0xFF5C2354), // (0.36, 0.14, 0.33)
            Color(0xFF3D1747), // (0.24, 0.09, 0.28)
            Color(0xFF59264D), // (0.35, 0.15, 0.30)
            Color(0xFF401F6B), // (0.25, 0.12, 0.42)
            Color(0xFF2B1452), // (0.17, 0.08, 0.32)
            Color(0xFF33173D), // (0.20, 0.09, 0.24)
            Color(0xFF1F0F40), // (0.12, 0.06, 0.25)
            Color(0xFF1F1247), // (0.12, 0.07, 0.28)
        )
        DayPeriod.NIGHT -> listOf(
            Color(0xFF0F0A17), // (0.06, 0.04, 0.09)
            Color(0xFF0D0814), // (0.05, 0.03, 0.08)
            Color(0xFF0A0812), // (0.04, 0.03, 0.07)
            Color(0xFF0F0A1A), // (0.06, 0.04, 0.10)
            Color(0xFF0D0A1F), // (0.05, 0.04, 0.12)
            Color(0xFF0A0817), // (0.04, 0.03, 0.09)
            Color(0xFF120D1F), // (0.07, 0.05, 0.12)
            Color(0xFF140D24), // (0.08, 0.05, 0.14)
            Color(0xFF0F0A1A), // (0.06, 0.04, 0.10)
        )
    }

    private fun accentTint(period: DayPeriod): Color = when (period) {
        DayPeriod.BRAHMA_MUHURTA -> Color(0xFF5C4DB3) // (0.36, 0.30, 0.70)
        DayPeriod.DAWN           -> Color(0xFFE88033) // (0.91, 0.50, 0.20)
        DayPeriod.MORNING        -> Color(0xFFE89938) // (0.91, 0.60, 0.22)
        DayPeriod.MIDDAY         -> Color(0xFFE0B840) // (0.88, 0.72, 0.25)
        DayPeriod.AFTERNOON      -> Color(0xFFE69433) // (0.90, 0.58, 0.20)
        DayPeriod.GOLDEN_HOUR    -> Color(0xFFD96626) // (0.85, 0.40, 0.15)
        DayPeriod.DUSK           -> Color(0xFF8C408C) // (0.55, 0.25, 0.55)
        DayPeriod.NIGHT          -> Color(0xFF4D408C) // (0.30, 0.25, 0.55)
    }

    private fun ambientOpacity(period: DayPeriod): Float = when (period) {
        DayPeriod.BRAHMA_MUHURTA -> 0.12f
        DayPeriod.DAWN           -> 0.10f
        DayPeriod.MORNING        -> 0.05f
        DayPeriod.MIDDAY         -> 0.03f
        DayPeriod.AFTERNOON      -> 0.05f
        DayPeriod.GOLDEN_HOUR    -> 0.10f
        DayPeriod.DUSK           -> 0.12f
        DayPeriod.NIGHT          -> 0.15f
    }

    // ── Gradient Brush Builders ──────────────────────────────────────────────

    /**
     * Create a multi-layer gradient brush approximating the iOS MeshGradient.
     * Uses 3 overlaid linear gradients at different angles.
     * On BASIC tier, returns a simple 2-color vertical gradient.
     */
    @Composable
    fun atmosphereBrush(atmosphere: DayAtmosphere): Brush {
        val colors = atmosphere.meshColors
        if (colors.size < 9) return Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent))

        return when (DeviceCapabilities.renderTier) {
            DeviceCapabilities.RenderTier.FULL,
            DeviceCapabilities.RenderTier.STANDARD -> {
                // Multi-stop vertical gradient using mesh corner + center colors
                Brush.verticalGradient(
                    0.0f to colors[0],
                    0.15f to colors[1],
                    0.3f to colors[3],
                    0.5f to colors[4],
                    0.7f to colors[5],
                    0.85f to colors[7],
                    1.0f to colors[8],
                )
            }
            DeviceCapabilities.RenderTier.BASIC -> {
                // Simple 2-color for budget devices
                Brush.verticalGradient(listOf(colors[1], colors[7]))
            }
        }
    }

    /**
     * Create a radial accent overlay brush from the atmosphere.
     */
    fun accentOverlayBrush(atmosphere: DayAtmosphere): Brush {
        return Brush.radialGradient(
            colors = listOf(
                atmosphere.accentTint.copy(alpha = atmosphere.ambientOpacity),
                Color.Transparent
            ),
            center = Offset(0.5f, 0.3f),
            radius = 1.2f
        )
    }
}
