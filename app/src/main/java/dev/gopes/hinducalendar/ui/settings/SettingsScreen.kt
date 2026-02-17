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
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import dev.gopes.hinducalendar.R
import dev.gopes.hinducalendar.data.model.*
import dev.gopes.hinducalendar.ui.components.*
import dev.gopes.hinducalendar.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val prefs by viewModel.preferences.collectAsState()

    var pathDropdownExpanded by remember { mutableStateOf(false) }
    var languageDropdownExpanded by remember { mutableStateOf(false) }
    var traditionDropdownExpanded by remember { mutableStateOf(false) }
    var syncOptionDropdownExpanded by remember { mutableStateOf(false) }
    var reminderDropdownExpanded by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
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
                                prefs.dharmaPath.displayName,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                prefs.dharmaPath.description,
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
                                            fontWeight = if (path == prefs.dharmaPath) FontWeight.Bold else FontWeight.Normal
                                        )
                                        Text(
                                            path.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                },
                                onClick = {
                                    viewModel.updateDharmaPath(path)
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
                                "${prefs.language.nativeScriptName} (${prefs.language.displayName})",
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
                                        fontWeight = if (lang == prefs.language) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                onClick = {
                                    viewModel.updateLanguage(lang)
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
                    checked = prefs.contentPreferences.panchangNotification,
                    onCheckedChange = {
                        viewModel.updateContentPreferences(prefs.contentPreferences.copy(panchangNotification = it))
                    }
                )
                ContentToggleRow(
                    label = stringResource(R.string.setting_primary_sacred_text),
                    description = stringResource(R.string.setting_primary_text_desc, prefs.dharmaPath.displayName),
                    checked = prefs.contentPreferences.primaryText,
                    onCheckedChange = {
                        viewModel.updateContentPreferences(prefs.contentPreferences.copy(primaryText = it))
                    }
                )
                ContentToggleRow(
                    label = stringResource(R.string.setting_festival_stories),
                    description = stringResource(R.string.setting_festival_stories_desc),
                    checked = prefs.contentPreferences.festivalStories,
                    onCheckedChange = {
                        viewModel.updateContentPreferences(prefs.contentPreferences.copy(festivalStories = it))
                    }
                )
                ContentToggleRow(
                    label = stringResource(R.string.setting_secondary_text),
                    description = stringResource(R.string.setting_secondary_text_desc),
                    checked = prefs.contentPreferences.secondaryText,
                    onCheckedChange = {
                        viewModel.updateContentPreferences(prefs.contentPreferences.copy(secondaryText = it))
                    }
                )
            }

            // Tradition Picker
            SettingsSection(stringResource(R.string.setting_calendar_tradition)) {
                Box {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { traditionDropdownExpanded = true },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                prefs.tradition.displayName,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                prefs.tradition.description,
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
                        expanded = traditionDropdownExpanded,
                        onDismissRequest = { traditionDropdownExpanded = false }
                    ) {
                        CalendarTradition.entries.forEach { tradition ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(
                                            tradition.displayName,
                                            fontWeight = if (tradition == prefs.tradition) FontWeight.Bold else FontWeight.Normal
                                        )
                                        Text(
                                            tradition.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                },
                                onClick = {
                                    viewModel.updateTradition(tradition)
                                    traditionDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Location
            SettingsSection(stringResource(R.string.settings_location)) {
                Text(
                    prefs.location.cityName ?: "Unknown",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    "%.4f\u00B0N, %.4f\u00B0E \u00B7 ${prefs.location.timeZoneId}".format(
                        prefs.location.latitude, prefs.location.longitude
                    ),
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
                        checked = prefs.syncToCalendar,
                        onCheckedChange = { viewModel.updateSyncEnabled(it) },
                        modifier = Modifier.semantics {
                            stateDescription = if (prefs.syncToCalendar) "Enabled" else "Disabled"
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                            checkedTrackColor = MaterialTheme.colorScheme.primary,
                            uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                }
                if (prefs.syncToCalendar) {
                    // Sync Option Picker
                    Box {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { syncOptionDropdownExpanded = true }
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                prefs.syncOption.displayName,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Icon(
                                Icons.Filled.ExpandMore,
                                contentDescription = stringResource(R.string.setting_change),
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        DropdownMenu(
                            expanded = syncOptionDropdownExpanded,
                            onDismissRequest = { syncOptionDropdownExpanded = false }
                        ) {
                            CalendarSyncOption.entries.forEach { option ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            option.displayName,
                                            fontWeight = if (option == prefs.syncOption) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    onClick = {
                                        viewModel.updateSyncOption(option)
                                        syncOptionDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
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
                        checked = prefs.notificationsEnabled,
                        onCheckedChange = { viewModel.updateNotificationsEnabled(it) },
                        modifier = Modifier.semantics {
                            stateDescription = if (prefs.notificationsEnabled) "Enabled" else "Disabled"
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                            checkedTrackColor = MaterialTheme.colorScheme.primary,
                            uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                }
                if (prefs.notificationsEnabled) {
                    // Reminder Timing Picker
                    Box {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { reminderDropdownExpanded = true }
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                prefs.reminderTiming.displayName,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Icon(
                                Icons.Filled.ExpandMore,
                                contentDescription = stringResource(R.string.setting_change),
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        DropdownMenu(
                            expanded = reminderDropdownExpanded,
                            onDismissRequest = { reminderDropdownExpanded = false }
                        ) {
                            ReminderTiming.entries.forEach { timing ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            timing.displayName,
                                            fontWeight = if (timing == prefs.reminderTiming) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    onClick = {
                                        viewModel.updateReminderTiming(timing)
                                        reminderDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
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
                        prefs.notificationTime.displayString,
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
            initialHour = prefs.notificationTime.hour,
            initialMinute = prefs.notificationTime.minute,
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
                    viewModel.updateNotificationTime(timePickerState.hour, timePickerState.minute)
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
                    viewModel.resetReadingProgress()
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
            modifier = Modifier.semantics {
                stateDescription = if (checked) "Enabled" else "Disabled"
            },
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
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.semantics { heading() }
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
