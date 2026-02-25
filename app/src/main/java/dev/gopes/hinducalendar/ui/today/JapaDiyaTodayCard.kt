package dev.gopes.hinducalendar.ui.today

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.gopes.hinducalendar.R
import dev.gopes.hinducalendar.data.model.AppLanguage
import dev.gopes.hinducalendar.data.model.DiyaState
import dev.gopes.hinducalendar.data.model.JapaState
import dev.gopes.hinducalendar.ui.components.GlassSurface
import dev.gopes.hinducalendar.ui.components.SurfaceElevation

/**
 * Compact card on the Today screen showing Japa progress and Diya status.
 * Tapping each half opens the respective full screen.
 */
@Composable
fun JapaDiyaTodayCard(
    japaState: JapaState,
    diyaState: DiyaState,
    language: AppLanguage = AppLanguage.ENGLISH,
    onJapaClick: () -> Unit = {},
    onDiyaClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    GlassSurface(
        modifier = modifier,
        elevation = SurfaceElevation.STANDARD,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Left: Japa summary
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(onClick = onJapaClick)
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Filled.SelfImprovement,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.japa_title),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = language.localizedDigits(
                        stringResource(R.string.japa_bead_progress, japaState.currentBead)
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (japaState.roundsToday > 0) {
                    Text(
                        text = language.localizedDigits(
                            stringResource(R.string.japa_rounds_count, japaState.roundsToday)
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Divider
            VerticalDivider(
                modifier = Modifier.height(80.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )

            // Right: Diya summary
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(onClick = onDiyaClick)
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Filled.LocalFireDepartment,
                    contentDescription = null,
                    tint = if (diyaState.isLitToday) Color(0xFFE88A2D) else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.diya_title),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (diyaState.isLitToday) stringResource(R.string.diya_lit_today)
                    else stringResource(R.string.diya_tap_to_light),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (diyaState.isLitToday) Color(0xFFE88A2D)
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (diyaState.lightingStreak > 0) {
                    Text(
                        text = language.localizedDigits(
                            stringResource(R.string.diya_streak_count, diyaState.lightingStreak)
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
