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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.gopes.hinducalendar.R
import dev.gopes.hinducalendar.data.model.AppLanguage
import dev.gopes.hinducalendar.data.model.ContentPreferences
import dev.gopes.hinducalendar.data.model.DharmaPath
import dev.gopes.hinducalendar.data.model.NotificationTime
import dev.gopes.hinducalendar.ui.components.*
import dev.gopes.hinducalendar.ui.theme.*

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
        topBar = { TopAppBar(title = { Text(stringResource(R.string.settings_title)) }) }
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
            SettingsSection(stringResource(R.string.setting_spiritual_path)) {
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
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            Icons.Filled.ExpandMore,
                            contentDescription = stringResource(R.string.setting_change),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
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
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
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
            SettingsSection(stringResource(R.string.settings_language)) {
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
                                stringResource(R.string.setting_content_and_ui_language),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            Icons.Filled.ExpandMore,
                            contentDescription = stringResource(R.string.setting_change_language),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
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
            SettingsSection(stringResource(R.string.setting_daily_content)) {
                ContentToggleRow(
                    label = stringResource(R.string.setting_panchang_notification),
                    description = stringResource(R.string.setting_panchang_notification_desc),
                    checked = panchangNotif,
                    onCheckedChange = { panchangNotif = it }
                )
                ContentToggleRow(
                    label = stringResource(R.string.setting_primary_sacred_text),
                    description = stringResource(R.string.setting_primary_text_desc, selectedPath.displayName),
                    checked = primaryText,
                    onCheckedChange = { primaryText = it }
                )
                ContentToggleRow(
                    label = stringResource(R.string.setting_festival_stories),
                    description = stringResource(R.string.setting_festival_stories_desc),
                    checked = festivalStories,
                    onCheckedChange = { festivalStories = it }
                )
                ContentToggleRow(
                    label = stringResource(R.string.setting_secondary_text),
                    description = stringResource(R.string.setting_secondary_text_desc),
                    checked = secondaryText,
                    onCheckedChange = { secondaryText = it }
                )
            }

            // Tradition
            SettingsSection(stringResource(R.string.setting_calendar_tradition)) {
                Text(
                    stringResource(R.string.setting_north_indian_purnimant),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    stringResource(R.string.setting_month_ends_purnima),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Location
            SettingsSection(stringResource(R.string.settings_location)) {
                Text("New Delhi", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                Text(
                    "28.6139\u00B0N, 77.2090\u00B0E \u00B7 Asia/Kolkata",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Calendar Sync
            SettingsSection(stringResource(R.string.settings_calendar_sync)) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.setting_sync_to_google), style = MaterialTheme.typography.bodyMedium)
                    Switch(
                        checked = syncEnabled,
                        onCheckedChange = { syncEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                            checkedTrackColor = MaterialTheme.colorScheme.primary,
                            uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                }
                if (syncEnabled) {
                    Text(
                        stringResource(R.string.setting_festivals_only),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    SacredButton(text = stringResource(R.string.setting_sync_now), onClick = { /* sync */ })
                }
            }

            // Notifications
            SettingsSection(stringResource(R.string.settings_notifications)) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.setting_festival_reminders), style = MaterialTheme.typography.bodyMedium)
                    Switch(
                        checked = notificationsEnabled,
                        onCheckedChange = { notificationsEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                            checkedTrackColor = MaterialTheme.colorScheme.primary,
                            uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                }
                if (notificationsEnabled) {
                    Text(
                        stringResource(R.string.setting_one_day_before),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(Modifier.height(12.dp))
                Divider(color = MaterialTheme.colorScheme.outline)
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
                        Text(stringResource(R.string.setting_daily_briefing_time), style = MaterialTheme.typography.bodyMedium)
                        Text(
                            stringResource(R.string.setting_morning_notification),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
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
            SettingsSection(stringResource(R.string.setting_reading_progress)) {
                Text(
                    stringResource(R.string.setting_reading_progress_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))
                SacredOutlineButton(
                    text = stringResource(R.string.setting_reset_reading),
                    onClick = { showResetDialog = true }
                )
            }

            // About
            SettingsSection(stringResource(R.string.settings_about)) {
                SettingsRow(stringResource(R.string.settings_version), "1.0.0")
                SettingsRow(stringResource(R.string.setting_calculation_method), stringResource(R.string.setting_drik_ganit))
                SettingsRow(stringResource(R.string.setting_ayanamsa), stringResource(R.string.setting_lahiri))
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
            title = { Text(stringResource(R.string.setting_set_briefing_time)) },
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
                    Text(stringResource(R.string.common_ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        )
    }

    // Reset Confirmation Dialog
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text(stringResource(R.string.setting_reset_dialog_title)) },
            text = {
                Text(stringResource(R.string.setting_reset_dialog_body))
            },
            confirmButton = {
                TextButton(onClick = {
                    // In production, reset the persisted reading progress
                    showResetDialog = false
                }) {
                    Text(stringResource(R.string.setting_reset), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text(stringResource(R.string.common_cancel))
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
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.width(8.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )
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
        SacredCard(content = content)
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
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
