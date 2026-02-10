package dev.gopes.hinducalendar.ui.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.gopes.hinducalendar.data.model.CalendarTradition
import dev.gopes.hinducalendar.data.model.HinduLocation

@Composable
fun OnboardingScreen(onComplete: (CalendarTradition, HinduLocation) -> Unit) {
    var step by remember { mutableIntStateOf(0) }
    var selectedTradition by remember { mutableStateOf(CalendarTradition.PURNIMANT) }
    var selectedLocation by remember { mutableStateOf(HinduLocation.DELHI) }

    when (step) {
        0 -> WelcomeStep(onNext = { step = 1 })
        1 -> TraditionStep(
            selectedTradition = selectedTradition,
            onSelect = { selectedTradition = it },
            onNext = { step = 2 }
        )
        2 -> LocationStep(
            selectedLocation = selectedLocation,
            onSelect = { selectedLocation = it },
            onComplete = { onComplete(selectedTradition, selectedLocation) }
        )
    }
}

@Composable
private fun WelcomeStep(onNext: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Filled.WbSunny,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(24.dp))
        Text("Hindu Calendar", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
        Text("हिन्दू पंचांग", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))

        Spacer(Modifier.height(32.dp))

        FeatureItem(Icons.Filled.CalendarMonth, "Full daily Panchang")
        FeatureItem(Icons.Filled.Star, "Festival reminders")
        FeatureItem(Icons.Filled.Event, "Sync to your calendar")
        FeatureItem(Icons.Filled.LocationOn, "Location-accurate timings")

        Spacer(Modifier.height(48.dp))

        Button(onClick = onNext, modifier = Modifier.fillMaxWidth().height(52.dp)) {
            Text("Get Started", style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun FeatureItem(icon: ImageVector, text: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(16.dp))
        Text(text, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun TraditionStep(
    selectedTradition: CalendarTradition,
    onSelect: (CalendarTradition) -> Unit,
    onNext: () -> Unit
) {
    Column(Modifier.fillMaxSize().padding(24.dp)) {
        Text("Select Your Tradition", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(
            "This determines which calendar system and regional festivals to show.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Spacer(Modifier.height(16.dp))

        LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(CalendarTradition.entries) { tradition ->
                Card(
                    modifier = Modifier.fillMaxWidth().selectable(
                        selected = tradition == selectedTradition,
                        onClick = { onSelect(tradition) }
                    ),
                    colors = if (tradition == selectedTradition)
                        CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    else CardDefaults.cardColors()
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(tradition.displayName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
                        Text(tradition.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        Button(onClick = onNext, modifier = Modifier.fillMaxWidth().height(52.dp)) {
            Text("Continue")
        }
    }
}

@Composable
private fun LocationStep(
    selectedLocation: HinduLocation,
    onSelect: (HinduLocation) -> Unit,
    onComplete: () -> Unit
) {
    Column(Modifier.fillMaxSize().padding(24.dp)) {
        Text("Set Your Location", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(
            "Used for sunrise/sunset times and accurate Panchang calculations.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Spacer(Modifier.height(16.dp))

        LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            items(HinduLocation.ALL_PRESETS) { location ->
                Card(
                    modifier = Modifier.fillMaxWidth().selectable(
                        selected = location == selectedLocation,
                        onClick = { onSelect(location) }
                    ),
                    colors = if (location == selectedLocation)
                        CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    else CardDefaults.cardColors()
                ) {
                    Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text(location.cityName ?: "Unknown", style = MaterialTheme.typography.bodyLarge)
                            Text(location.timeZoneId, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        Button(onClick = onComplete, modifier = Modifier.fillMaxWidth().height(52.dp)) {
            Text("Start Using Hindu Calendar")
        }
    }
}
