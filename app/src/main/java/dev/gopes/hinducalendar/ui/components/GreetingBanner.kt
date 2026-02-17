package dev.gopes.hinducalendar.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.gopes.hinducalendar.R
import dev.gopes.hinducalendar.ui.theme.*
import java.util.Calendar

/**
 * Time-aware greeting banner with Sanskrit text on a warm gradient.
 */
@Composable
fun GreetingBanner(
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = false
) {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val (sanskrit, english, icon) = when (hour) {
        in 4..11 -> Triple("\u0936\u0941\u092D \u092A\u094D\u0930\u092D\u093E\u0924", stringResource(R.string.greeting_good_morning), Icons.Filled.WbSunny)
        in 12..16 -> Triple("\u0936\u0941\u092D \u0905\u092A\u0930\u093E\u0939\u094D\u0928", stringResource(R.string.greeting_good_afternoon), Icons.Filled.LightMode)
        in 17..20 -> Triple("\u0936\u0941\u092D \u0938\u0902\u0927\u094D\u092F\u093E", stringResource(R.string.greeting_good_evening), Icons.Filled.WbTwilight)
        else -> Triple("\u0928\u092E\u0938\u094D\u0924\u0947", stringResource(R.string.greeting_namaste), Icons.Filled.NightsStay)
    }

    val gradientColors = if (isDarkTheme) {
        listOf(Color(0xFF50351A), Color(0xFF453020))
    } else {
        listOf(DeepSaffron, Color(0xFFD9A638))
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.linearGradient(gradientColors))
            .padding(24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = sanskrit,
                style = AppTypography.displayMedium,
                color = Color.White
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = english,
                style = AppTypography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(36.dp),
            tint = Color.White.copy(alpha = 0.4f)
        )
    }
}
