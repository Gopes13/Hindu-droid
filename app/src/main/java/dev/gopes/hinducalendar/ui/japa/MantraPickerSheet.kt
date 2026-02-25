package dev.gopes.hinducalendar.ui.japa

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.gopes.hinducalendar.R
import dev.gopes.hinducalendar.data.model.MantraSelection
import dev.gopes.hinducalendar.data.model.PresetMantra
import dev.gopes.hinducalendar.ui.theme.SacredTypography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MantraPickerSheet(
    selected: MantraSelection,
    onSelect: (MantraSelection) -> Unit,
    onDismiss: () -> Unit
) {
    var customText by remember {
        mutableStateOf(
            (selected as? MantraSelection.Custom)?.text ?: ""
        )
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Text(
                text = stringResource(R.string.japa_select_mantra),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.heightIn(max = 400.dp)
            ) {
                // 7 preset mantras
                items(PresetMantra.entries.toList()) { preset ->
                    val isSelected = selected is MantraSelection.Preset &&
                            selected.mantra == preset
                    MantraRow(
                        sanskrit = preset.sanskritKey,
                        displayName = preset.displayKey.replace("mantra_", "")
                            .replace("_", " ")
                            .replaceFirstChar { it.uppercase() },
                        isSelected = isSelected,
                        onClick = {
                            onSelect(MantraSelection.Preset(preset))
                            onDismiss()
                        }
                    )
                }

                // Custom mantra option
                item {
                    HorizontalDivider(Modifier.padding(vertical = 8.dp))

                    Text(
                        text = stringResource(R.string.japa_custom_mantra),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    OutlinedTextField(
                        value = customText,
                        onValueChange = { customText = it },
                        label = { Text(stringResource(R.string.japa_enter_mantra)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(Modifier.height(8.dp))

                    Button(
                        onClick = {
                            if (customText.isNotBlank()) {
                                onSelect(MantraSelection.Custom(customText.trim()))
                                onDismiss()
                            }
                        },
                        enabled = customText.isNotBlank(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.japa_use_custom))
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun MantraRow(
    sanskrit: String,
    displayName: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = sanskrit,
                style = SacredTypography.sacredMedium,
                color = if (isSelected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurface,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
            Text(
                text = displayName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (isSelected) {
            Icon(
                Icons.Filled.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
