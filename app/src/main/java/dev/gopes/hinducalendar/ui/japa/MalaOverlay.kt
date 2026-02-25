package dev.gopes.hinducalendar.ui.japa

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.gopes.hinducalendar.data.model.MantraSelection
import dev.gopes.hinducalendar.ui.components.NumericTransition
import dev.gopes.hinducalendar.ui.theme.SacredTypography

/**
 * Center overlay on the mala ring showing bead count and mantra name.
 */
@Composable
fun MalaOverlay(
    currentBead: Int,
    mantra: MantraSelection,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Bead count (large numeric with slot-machine transition)
        NumericTransition(
            value = currentBead,
            style = MaterialTheme.typography.displayLarge.copy(fontSize = 48.sp),
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
        )

        Text(
            text = "/ 108",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(8.dp))

        // Mantra name
        Text(
            text = mantra.displayText(),
            style = SacredTypography.sacredMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            maxLines = 2,
            modifier = Modifier.widthIn(max = 160.dp)
        )
    }
}
