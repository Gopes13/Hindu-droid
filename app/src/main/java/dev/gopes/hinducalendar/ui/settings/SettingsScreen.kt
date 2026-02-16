package dev.gopes.hinducalendar.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.gopes.hinducalendar.data.model.AppLanguage
import dev.gopes.hinducalendar.data.model.ContentPreferences
import dev.gopes.hinducalendar.data.model.DharmaPath
import dev.gopes.hinducalendar.data.model.NotificationTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    var syncEnabled by remember { mutableStateOf(true) }
    var notificationsEnabled by remember { mutableStateOf(true) }

    // Spiritual path state
    var selectedPath by remember { mutableStateOf(DharmaPath.GENERAL) }
    var pathDropdownExpanded by remember { mutableStateOf(false) }

    // Language state
    var selectedLanguage by remember { mutableStateOf(AppLanguage.ENGLISH) }
    var languageDropdownExpanded by remember { mutableStateOf(false) }

    // Content preferences state
    var panchangNotif by remember { mutableStateOf(true) }
    var primaryText by remember { mutableStateOf(true) }
    var festivalStories by remember { mutableStateOf(true) }
    var secondaryText by remember { mutableStateOf(false) }

    // Notification time state
    var notifHour by remember { mutableIntStateOf(7) }
    var notifMinute by remember { mutableIntStateOf(0) }
    var showTimePicker by remember { mutableStateOf(false) }
    val notifTime = NotificationTime(notifHour, notifMinute)

    // Reset confirmation
    var showResetDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Settings") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Spiritual Path
            SettingsSection("Spiritual Path") {
                Box {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { pathDropdownExpanded = true },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                selectedPath.displayName,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                selectedPath.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                        Icon(
                            Icons.Filled.ExpandMore,
                            contentDescription = "Change",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                    DropdownMenu(
                        expanded = pathDropdownExpanded,
                        onDismissRequest = { pathDropdownExpanded = false }
                    ) {
                        DharmaPath.entries.forEach { path ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(
                                            path.displayName,
                                            fontWeight = if (path == selectedPath) FontWeight.Bold else FontWeight.Normal
                                        )
                                        Text(
                                            path.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                        )
                                    }
                                },
                                onClick = {
                                    selectedPath = path
                                    pathDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Language
            SettingsSection("Language") {
                Box {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { languageDropdownExpanded = true },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                "${selectedLanguage.nativeScriptName} (${selectedLanguage.displayName})",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                "Content and UI language",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                        Icon(
                            Icons.Filled.ExpandMore,
                            contentDescription = "Change language",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                    DropdownMenu(
                        expanded = languageDropdownExpanded,
                        onDismissRequest = { languageDropdownExpanded = false }
                    ) {
                        AppLanguage.entries.forEach { lang ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        "${lang.nativeScriptName} (${lang.displayName})",
                                        fontWeight = if (lang == selectedLanguage) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                onClick = {
                                    selectedLanguage = lang
                                    languageDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Content Preferences
            SettingsSection("Daily Content") {
                ContentToggleRow(
                    label = "Panchang Notification",
                    description = "Include Tithi, Nakshatra in notification",
                    checked = panchangNotif,
                    onCheckedChange = { panchangNotif = it }
                )
                ContentToggleRow(
                    label = "Primary Sacred Text",
                    description = "Daily verse from ${selectedPath.displayName} primary text",
                    checked = primaryText,
                    onCheckedChange = { primaryText = it }
                )
                ContentToggleRow(
                    label = "Festival Stories",
                    description = "Stories and significance on festival days",
                    checked = festivalStories,
                    onCheckedChange = { festivalStories = it }
                )
                ContentToggleRow(
                    label = "Secondary Text",
                    description = "Additional reading from secondary text",
                    checked = secondaryText,
                    onCheckedChange = { secondaryText = it }
                )
            }

            // Tradition
            SettingsSection("Calendar Tradition") {
                Text(
                    "North Indian (Purnimant)",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    "Month ends on Purnima (full moon)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            // Location
            SettingsSection("Location") {
                Text("New Delhi", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                Text(
                    "28.6139\u00B0N, 77.2090\u00B0E \u00B7 Asia/Kolkata",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            // Calendar Sync
            SettingsSection("Calendar Sync") {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Sync to Google Calendar", style = MaterialTheme.typography.bodyMedium)
                    Switch(checked = syncEnabled, onCheckedChange = { syncEnabled = it })
                }
                if (syncEnabled) {
                    Text(
                        "Festivals Only",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = { /* sync */ }, modifier = Modifier.fillMaxWidth()) {
                        Text("Sync Now")
                    }
                }
            }

            // Notifications
            SettingsSection("Notifications") {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Festival Reminders", style = MaterialTheme.typography.bodyMedium)
                    Switch(checked = notificationsEnabled, onCheckedChange = { notificationsEnabled = it })
                }
                if (notificationsEnabled) {
                    Text(
                        "1 day before",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }

                Spacer(Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(Modifier.height(12.dp))

                // Notification Time Picker
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable { showTimePicker = true },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Daily Briefing Time", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            "Morning notification with daily wisdom",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                    Text(
                        notifTime.displayString,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Reading Progress
            SettingsSection("Reading Progress") {
                Text(
                    "Your daily reading positions are tracked automatically as you read.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(Modifier.height(12.dp))
                OutlinedButton(
                    onClick = { showResetDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Reset All Reading Progress")
                }
            }

            // About
            SettingsSection("About") {
                SettingsRow("Version", "1.0.0")
                SettingsRow("Calculation Method", "Drik Ganit")
                SettingsRow("Ayanamsa", "Lahiri (Chitrapaksha)")
            }
        }
    }

    // Time Picker Dialog
    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = notifHour,
            initialMinute = notifMinute,
            is24Hour = false
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("Set Daily Briefing Time") },
            text = {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    TimePicker(state = timePickerState)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    notifHour = timePickerState.hour
                    notifMinute = timePickerState.minute
                    showTimePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Reset Confirmation Dialog
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset Reading Progress?") },
            text = {
                Text("This will reset all reading positions back to the beginning. This action cannot be undone.")
            },
            confirmButton = {
                TextButton(onClick = {
                    // In production, reset the persisted reading progress
                    showResetDialog = false
                }) {
                    Text("Reset", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun ContentToggleRow(
    label: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodyMedium)
            Text(
                description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
        Spacer(Modifier.width(8.dp))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(
            title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(8.dp))
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), content = content)
        }
    }
}

@Composable
private fun SettingsRow(label: String, value: String) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}
