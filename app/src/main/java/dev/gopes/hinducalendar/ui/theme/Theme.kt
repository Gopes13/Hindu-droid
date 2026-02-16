package dev.gopes.hinducalendar.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = DeepSaffron,
    onPrimary = OnDeepSaffron,
    primaryContainer = SaffronContainer,
    onPrimaryContainer = OnSaffronContainer,
    secondary = SacredMaroon,
    onSecondary = OnSacredMaroon,
    secondaryContainer = MaroonContainer,
    tertiary = DivineGold,
    tertiaryContainer = GoldContainer,
    background = WarmCream,
    onBackground = WarmNearBlack,
    surface = SoftIvory,
    onSurface = WarmOnSurface,
    surfaceVariant = SoftIvoryElevated,
    onSurfaceVariant = WarmOnSurfaceSecondary,
    outline = LightDivider,
    outlineVariant = LightDivider.copy(alpha = 0.5f),
    error = InauspiciousRed,
    onError = Color.White,
)

private val DarkColorScheme = darkColorScheme(
    primary = WarmGold,
    onPrimary = OnWarmGold,
    primaryContainer = DarkSaffronContainer,
    onPrimaryContainer = DarkOnSaffronContainer,
    secondary = SoftRose,
    onSecondary = Color(0xFF1A0810),
    secondaryContainer = DarkMaroonContainer,
    tertiary = DarkTertiary,
    tertiaryContainer = DarkGoldContainer,
    background = DeepSacred,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = DarkOnSurfaceSecondary,
    outline = DarkDivider,
    outlineVariant = DarkDivider.copy(alpha = 0.5f),
    error = DarkInauspicious,
    onError = Color(0xFF1A0808),
)

private val AppShapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(20.dp),
)

@Composable
fun HinduCalendarTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}
