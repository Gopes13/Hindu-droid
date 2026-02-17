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
fun SettingsScreen(
    onSadhanaClick: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel(),
    gamificationViewModel: dev.gopes.hinducalendar.ui.gamification.GamificationViewModel = hiltViewModel()
) {
    val prefs by viewModel.preferences.collectAsState()

    var pathDropdownExpanded by remember { mutableStateOf(false) }
    var languageDropdownExpanded by remember { mutableStateOf(false) }
    var traditionDropdownExpanded by remember { mutableStateOf(false) }
    var syncOptionDropdownExpanded by remember { mutableStateOf(false) }
    var wisdomDropdownExpanded by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showLocationPicker by remember { mutableStateOf(false) }
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
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
                    TextButton(onClick = { showLocationPicker = true }) {
                        Text(
                            stringResource(R.string.setting_change),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Daily Wisdom Text
            SettingsSection(stringResource(R.string.setting_daily_wisdom_text)) {
                Box {
                    val pathTexts = prefs.dharmaPath.availableWisdomTexts
                    val allWisdomTexts = SacredTextType.entries.filter { it.isWisdomEligible }
                    val otherTexts = allWisdomTexts.filter { it !in pathTexts }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { wisdomDropdownExpanded = true },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            prefs.effectiveWisdomText.displayName,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Icon(
                            Icons.Filled.ExpandMore,
                            contentDescription = stringResource(R.string.setting_change),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    DropdownMenu(
                        expanded = wisdomDropdownExpanded,
                        onDismissRequest = { wisdomDropdownExpanded = false }
                    ) {
                        // Path texts first
                        pathTexts.forEach { textType ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        textType.displayName,
                                        fontWeight = if (textType == prefs.effectiveWisdomText) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                onClick = {
                                    viewModel.updateActiveWisdomText(textType)
                                    wisdomDropdownExpanded = false
                                }
                            )
                        }
                        // Divider + other texts
                        if (otherTexts.isNotEmpty()) {
                            HorizontalDivider()
                            Text(
                                stringResource(R.string.setting_wisdom_other_texts),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                            otherTexts.forEach { textType ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            textType.displayName,
                                            fontWeight = if (textType == prefs.effectiveWisdomText) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    onClick = {
                                        viewModel.updateActiveWisdomText(textType)
                                        wisdomDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
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
                    SacredButton(text = stringResource(R.string.setting_sync_now), onClick = { viewModel.syncCalendarNow() })
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
                    // Reminder Timing Toggles (multi-select, matching iOS)
                    ReminderTiming.entries.forEach { timing ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                timing.displayName,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                            Switch(
                                checked = timing in prefs.reminderTimings,
                                onCheckedChange = { enabled ->
                                    viewModel.toggleReminderTiming(timing, enabled)
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
                }

                Spacer(Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline)
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

            // Sadhana Journey (Gamification)
            SettingsSection(stringResource(R.string.sadhana_journey)) {
                ContentToggleRow(
                    label = stringResource(R.string.sadhana_enable),
                    description = stringResource(R.string.sadhana_enable_desc),
                    checked = gamificationViewModel.gamificationData.isEnabled,
                    onCheckedChange = { gamificationViewModel.toggleGamification(it) }
                )
                if (gamificationViewModel.gamificationData.isEnabled) {
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSadhanaClick() },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            val level = dev.gopes.hinducalendar.data.model.SadhanaLevel.forLevel(
                                gamificationViewModel.gamificationData.currentLevel
                            )
                            val titleRes = when (level.titleKey) {
                                "level_1" -> R.string.level_1
                                "level_2" -> R.string.level_2
                                "level_3" -> R.string.level_3
                                "level_4" -> R.string.level_4
                                "level_5" -> R.string.level_5
                                "level_6" -> R.string.level_6
                                "level_7" -> R.string.level_7
                                "level_8" -> R.string.level_8
                                "level_9" -> R.string.level_9
                                "level_10" -> R.string.level_10
                                "level_11" -> R.string.level_11
                                "level_12" -> R.string.level_12
                                "level_13" -> R.string.level_13
                                "level_14" -> R.string.level_14
                                "level_15" -> R.string.level_15
                                "level_16" -> R.string.level_16
                                "level_17" -> R.string.level_17
                                "level_18" -> R.string.level_18
                                "level_19" -> R.string.level_19
                                else -> R.string.level_20
                            }
                            Text(
                                stringResource(R.string.sadhana_level_format, level.level, stringResource(titleRes)),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                stringResource(R.string.punya_points) + ": ${gamificationViewModel.gamificationData.totalPunyaPoints}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            stringResource(R.string.sadhana_continue),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
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

    // Location Picker Bottom Sheet
    if (showLocationPicker) {
        LocationPickerSheet(
            currentLocation = prefs.location,
            onLocationSelected = { viewModel.updateLocation(it) },
            onDismiss = { showLocationPicker = false }
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
