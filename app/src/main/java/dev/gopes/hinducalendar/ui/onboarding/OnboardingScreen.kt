package dev.gopes.hinducalendar.ui.onboarding

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.gopes.hinducalendar.data.model.CalendarTradition
import dev.gopes.hinducalendar.data.model.DharmaPath
import dev.gopes.hinducalendar.data.model.HinduLocation
import dev.gopes.hinducalendar.ui.components.*
import dev.gopes.hinducalendar.ui.theme.*

@Composable
fun OnboardingScreen(onComplete: (CalendarTradition, HinduLocation, DharmaPath) -> Unit) {
    var step by remember { mutableIntStateOf(0) }
    var selectedTradition by remember { mutableStateOf(CalendarTradition.PURNIMANT) }
    var selectedLocation by remember { mutableStateOf(HinduLocation.DELHI) }
    var selectedDharmaPath by remember { mutableStateOf(DharmaPath.GENERAL) }

    when (step) {
        0 -> WelcomeStep(onNext = { step = 1 })
        1 -> DharmaPathStep(
            selectedPath = selectedDharmaPath,
            onSelect = { selectedDharmaPath = it },
            onNext = { step = 2 }
        )
        2 -> TraditionStep(
            selectedTradition = selectedTradition,
            onSelect = { selectedTradition = it },
            onNext = { step = 3 }
        )
        3 -> LocationStep(
            selectedLocation = selectedLocation,
            onSelect = { selectedLocation = it },
            onComplete = { onComplete(selectedTradition, selectedLocation, selectedDharmaPath) }
        )
    }
}

/**
 * Returns the tradition-specific accent color for each DharmaPath.
 */
private fun dharmaPathAccentColor(path: DharmaPath): Color {
    return when (path) {
        DharmaPath.GENERAL -> TraditionGeneral
        DharmaPath.VAISHNAV -> TraditionVaishnav
        DharmaPath.SHAIV -> TraditionShaiv
        DharmaPath.SHAKTA -> TraditionShakta
        DharmaPath.SMARTA -> TraditionSmarta
        DharmaPath.ISKCON -> TraditionISKCON
        DharmaPath.SWAMINARAYAN -> TraditionSwaminarayan
        DharmaPath.SIKH -> TraditionSikh
        DharmaPath.JAIN -> TraditionJain
    }
}

@Composable
private fun WelcomeStep(onNext: () -> Unit) {
    val gradient = Brush.linearGradient(listOf(DeepSaffron, DivineGold))

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient)
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Om symbol instead of WbSunny icon
        Text(
            "\u0950",
            fontSize = 120.sp,
            fontFamily = FontFamily.Serif,
            color = Color.White
        )
        Spacer(Modifier.height(24.dp))
        Text(
            "Hindu Calendar",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            "\u0939\u093F\u0928\u094D\u0926\u0942 \u092A\u0902\u091A\u093E\u0902\u0917",
            style = MaterialTheme.typography.titleLarge,
            color = Color.White.copy(alpha = 0.8f)
        )

        Spacer(Modifier.height(32.dp))

        FeatureItem(Icons.Filled.CalendarMonth, "Full daily Panchang")
        FeatureItem(Icons.Filled.Star, "Festival reminders")
        FeatureItem(Icons.Filled.Event, "Sync to your calendar")
        FeatureItem(Icons.Filled.LocationOn, "Location-accurate timings")
        FeatureItem(Icons.Filled.MenuBook, "Daily sacred text readings")

        Spacer(Modifier.height(48.dp))

        // White filled button on gradient background
        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = DeepSaffron
            )
        ) {
            Text("Get Started", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun FeatureItem(icon: ImageVector, text: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(16.dp))
        Text(text, style = MaterialTheme.typography.bodyLarge, color = Color.White)
    }
}

@Composable
private fun DharmaPathStep(
    selectedPath: DharmaPath,
    onSelect: (DharmaPath) -> Unit,
    onNext: () -> Unit
) {
    Column(Modifier.fillMaxSize().padding(24.dp)) {
        Text(
            "Your Spiritual Path",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            "Choose your tradition to personalize daily sacred text readings and content.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(16.dp))

        LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(DharmaPath.entries) { path ->
                val accentColor = dharmaPathAccentColor(path)
                SacredCard(
                    modifier = Modifier.selectable(
                        selected = path == selectedPath,
                        onClick = { onSelect(path) }
                    ),
                    accentColor = accentColor,
                    isHighlighted = path == selectedPath
                ) {
                    Text(
                        path.displayName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium,
                        color = if (path == selectedPath) accentColor else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        path.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "${path.availableTextIds.size} sacred texts available",
                        style = MaterialTheme.typography.labelSmall,
                        color = accentColor.copy(alpha = 0.7f)
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        SacredButton(text = "Continue", onClick = onNext)
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
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(16.dp))

        LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(CalendarTradition.entries) { tradition ->
                SacredCard(
                    modifier = Modifier.selectable(
                        selected = tradition == selectedTradition,
                        onClick = { onSelect(tradition) }
                    ),
                    isHighlighted = tradition == selectedTradition
                ) {
                    Text(
                        tradition.displayName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium,
                        color = if (tradition == selectedTradition) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        tradition.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        SacredButton(text = "Continue", onClick = onNext)
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
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(16.dp))

        LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            items(HinduLocation.ALL_PRESETS) { location ->
                SacredCard(
                    modifier = Modifier.selectable(
                        selected = location == selectedLocation,
                        onClick = { onSelect(location) }
                    ),
                    isHighlighted = location == selectedLocation
                ) {
                    Text(
                        location.cityName ?: "Unknown",
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (location == selectedLocation) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        location.timeZoneId,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        SacredButton(text = "Start Using Hindu Calendar", onClick = onComplete)
    }
}
