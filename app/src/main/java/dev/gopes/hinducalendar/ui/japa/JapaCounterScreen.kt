package dev.gopes.hinducalendar.ui.japa

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import dev.gopes.hinducalendar.R
import dev.gopes.hinducalendar.ui.components.ConfettiOverlay
import dev.gopes.hinducalendar.ui.components.NumericTransition
import dev.gopes.hinducalendar.ui.components.sacredPress
import dev.gopes.hinducalendar.ui.theme.SacredTypography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JapaCounterScreen(
    onBack: () -> Unit = {},
    viewModel: JapaViewModel = hiltViewModel()
) {
    val state = viewModel.japaState
    var showMaterialPicker by remember { mutableStateOf(false) }
    var showMantraPicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.japa_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Mantra header
                Text(
                    text = state.selectedMantra.displayText(),
                    style = SacredTypography.sacredLarge,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Mala ring with overlay (tappable area)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .sacredPress { viewModel.advanceBead() },
                    contentAlignment = Alignment.Center
                ) {
                    MalaRingCanvas(
                        currentBead = state.currentBead,
                        material = state.selectedMaterial,
                        modifier = Modifier.fillMaxSize()
                    )

                    MalaOverlay(
                        currentBead = state.currentBead,
                        mantra = state.selectedMantra
                    )
                }

                Spacer(Modifier.height(16.dp))

                // Stats row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatColumn(
                        label = stringResource(R.string.japa_beads_left),
                        value = 108 - state.currentBead
                    )
                    StatColumn(
                        label = stringResource(R.string.japa_rounds_today),
                        value = state.roundsToday
                    )
                    StatColumn(
                        label = stringResource(R.string.japa_total_rounds),
                        value = state.totalRoundsLifetime
                    )
                }

                Spacer(Modifier.height(16.dp))

                // Controls row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Material picker button
                    OutlinedButton(
                        onClick = { showMaterialPicker = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Filled.Circle, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            state.selectedMaterial.displayKey
                                .replaceFirstChar { it.uppercase() }
                                .replace("_", " ")
                        )
                    }

                    // Mantra picker button
                    OutlinedButton(
                        onClick = { showMantraPicker = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Filled.MusicNote, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(stringResource(R.string.japa_mantra))
                    }
                }
            }

            // Round completion confetti
            ConfettiOverlay(
                isActive = viewModel.showRoundComplete,
                onFinished = { viewModel.dismissRoundComplete() }
            )
        }
    }

    // Material picker sheet
    if (showMaterialPicker) {
        MalaMaterialPicker(
            selected = state.selectedMaterial,
            onSelect = { viewModel.selectMaterial(it) },
            onDismiss = { showMaterialPicker = false }
        )
    }

    // Mantra picker sheet
    if (showMantraPicker) {
        MantraPickerSheet(
            selected = state.selectedMantra,
            onSelect = { viewModel.selectMantra(it) },
            onDismiss = { showMantraPicker = false }
        )
    }
}

@Composable
private fun StatColumn(label: String, value: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        NumericTransition(
            value = value,
            style = SacredTypography.numericMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
