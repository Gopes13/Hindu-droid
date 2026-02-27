package dev.gopes.hinducalendar.core.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.gopes.hinducalendar.core.ui.theme.HinduCalendarTheme

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

@Preview(showBackground = true, name = "Light")
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark")
@Composable
private fun SacredCardPreview() {
    HinduCalendarTheme {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SacredCard {
                Text("Standard Card", style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(4.dp))
                Text("Sample content inside a SacredCard.", style = MaterialTheme.typography.bodyMedium)
            }
            SacredHighlightCard {
                Text("Highlighted Card", style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(4.dp))
                Text("Sample content inside a SacredHighlightCard.", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
