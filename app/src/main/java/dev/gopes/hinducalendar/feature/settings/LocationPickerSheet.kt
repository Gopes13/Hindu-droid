package dev.gopes.hinducalendar.feature.settings

import android.Manifest
import android.annotation.SuppressLint
import android.location.Geocoder
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.android.gms.location.LocationServices
import dev.gopes.hinducalendar.R
import dev.gopes.hinducalendar.domain.model.HinduLocation
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationPickerSheet(
    currentLocation: HinduLocation,
    onLocationSelected: (HinduLocation) -> Unit,
    onDismiss: () -> Unit
) {
    var searchText by remember { mutableStateOf("") }
    var isLocating by remember { mutableStateOf(false) }
    var locationError by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    val filteredLocations = remember(searchText) {
        if (searchText.isBlank()) {
            HinduLocation.ALL_PRESETS
        } else {
            HinduLocation.ALL_PRESETS.filter {
                (it.cityName ?: "").contains(searchText, ignoreCase = true)
            }
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            isLocating = true
            locationError = null
            fetchCurrentLocation(context, onLocationSelected, onDismiss) { error ->
                locationError = error
                isLocating = false
            }
        } else {
            locationError = context.getString(R.string.setting_location_permission_denied)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                stringResource(R.string.setting_select_location),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            Text(
                stringResource(R.string.setting_location_desc),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            // Use My Location button
            Button(
                onClick = {
                    isLocating = true
                    locationError = null
                    locationPermissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                shape = RoundedCornerShape(12.dp),
                enabled = !isLocating
            ) {
                if (isLocating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.setting_detecting_location))
                } else {
                    Icon(Icons.Filled.MyLocation, contentDescription = stringResource(R.string.cd_use_location), modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.setting_use_my_location), fontWeight = FontWeight.SemiBold)
                }
            }

            if (locationError != null) {
                Text(
                    locationError!!,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            // Search field
            OutlinedTextField(
                value = searchText,
                onValueChange = { searchText = it },
                placeholder = { Text(stringResource(R.string.setting_search_city)) },
                leadingIcon = {
                    Icon(Icons.Filled.Search, contentDescription = stringResource(R.string.cd_search_locations))
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            // City list
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .heightIn(max = 400.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(filteredLocations, key = { it.cityName ?: "" }) { location ->
                    val isSelected = location == currentLocation
                    LocationRow(
                        location = location,
                        isSelected = isSelected,
                        onClick = {
                            onLocationSelected(location)
                            onDismiss()
                        }
                    )
                }
            }
        }
    }
}

@SuppressLint("MissingPermission")
private fun fetchCurrentLocation(
    context: android.content.Context,
    onLocationSelected: (HinduLocation) -> Unit,
    onDismiss: () -> Unit,
    onError: (String) -> Unit
) {
    try {
        val fusedClient = LocationServices.getFusedLocationProviderClient(context)
        fusedClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val cityName = try {
                    @Suppress("DEPRECATION")
                    val geocoder = Geocoder(context, Locale.getDefault())
                    val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                    addresses?.firstOrNull()?.locality ?: addresses?.firstOrNull()?.subAdminArea ?: context.getString(R.string.setting_use_my_location)
                } catch (_: Exception) {
                    context.getString(R.string.setting_use_my_location)
                }
                val timeZoneId = TimeZone.getDefault().id
                val hinduLocation = HinduLocation(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    timeZoneId = timeZoneId,
                    cityName = cityName
                )
                onLocationSelected(hinduLocation)
                onDismiss()
            } else {
                onError(context.getString(R.string.setting_location_not_found))
            }
        }.addOnFailureListener { _ ->
            onError(context.getString(R.string.setting_location_not_found))
        }
    } catch (_: Exception) {
        onError(context.getString(R.string.setting_location_not_found))
    }
}

@Composable
private fun LocationRow(
    location: HinduLocation,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
        else MaterialTheme.colorScheme.surface,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    Icons.Filled.LocationOn,
                    contentDescription = stringResource(R.string.cd_location_marker),
                    tint = if (isSelected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
                Column {
                    Text(
                        location.cityName ?: "Unknown",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                    )
                    Text(
                        location.timeZoneId,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (isSelected) {
                Icon(
                    Icons.Filled.CheckCircle,
                    contentDescription = stringResource(R.string.common_done),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
