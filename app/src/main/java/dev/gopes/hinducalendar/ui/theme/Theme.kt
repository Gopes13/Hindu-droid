package dev.gopes.hinducalendar.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = Saffron,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    primaryContainer = SaffronContainer,
    onPrimaryContainer = OnSaffronContainer,
    secondary = Maroon,
    tertiary = Gold,
    background = Surface,
    surface = Surface,
    onBackground = OnSurface,
    onSurface = OnSurface,
)

private val DarkColorScheme = darkColorScheme(
    primary = SaffronLight,
    onPrimary = OnSaffronContainer,
    primaryContainer = SaffronDark,
    onPrimaryContainer = SaffronContainer,
    secondary = Maroon,
    tertiary = Gold,
    background = SurfaceDark,
    surface = SurfaceDark,
    onBackground = OnSurfaceDark,
    onSurface = OnSurfaceDark,
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
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}
