package dev.gopes.hinducalendar.feature.diya

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import dev.gopes.hinducalendar.R
import dev.gopes.hinducalendar.core.ui.components.ConfettiOverlay
import dev.gopes.hinducalendar.core.ui.components.GlassSurface
import dev.gopes.hinducalendar.core.ui.components.SurfaceElevation
import dev.gopes.hinducalendar.core.ui.theme.SacredTypography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SacredDiyaScreen(
    onBack: () -> Unit = {},
    viewModel: DiyaViewModel = hiltViewModel()
) {
    val state = viewModel.diyaState
    val language by viewModel.language.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.diya_screen_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(24.dp))

                // Diya visual: lamp + flame stacked
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .clickable(enabled = !state.isLitToday) { viewModel.lightDiya() },
                    contentAlignment = Alignment.Center
                ) {
                    DiyaLampCanvas(
                        modifier = Modifier
                            .width(200.dp)
                            .height(140.dp)
                            .offset(y = 40.dp)
                    )
                    DiyaFlameCanvas(
                        isLit = state.isLitToday,
                        modifier = Modifier
                            .width(120.dp)
                            .height(160.dp)
                    )
                }

                Spacer(Modifier.height(24.dp))

                // Light button or status
                if (!state.isLitToday) {
                    Button(
                        onClick = { viewModel.lightDiya() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFE88A2D)
                        )
                    ) {
                        Icon(
                            Icons.Filled.LocalFireDepartment,
                            contentDescription = stringResource(R.string.cd_light_lamp),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            stringResource(R.string.diya_light_button),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Text(
                        stringResource(R.string.diya_lit_today),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFE88A2D),
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(Modifier.height(32.dp))

                // Stats grid
                GlassSurface(elevation = SurfaceElevation.STANDARD) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterHorizontally)
                    ) {
                        StatColumn(
                            value = language.localizedNumber(state.lightingStreak),
                            label = stringResource(R.string.diya_streak_label)
                        )
                        StatColumn(
                            value = language.localizedNumber(state.totalDaysLit),
                            label = stringResource(R.string.diya_total_days_label)
                        )
                    }
                }
            }

            // Confetti overlay
            ConfettiOverlay(
                isActive = viewModel.showConfetti,
                onFinished = { viewModel.dismissConfetti() }
            )
        }
    }
}

@Composable
private fun StatColumn(value: String, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Text(
            text = value,
            style = SacredTypography.numericMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
