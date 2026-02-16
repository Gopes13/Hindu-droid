package dev.gopes.hinducalendar.ui.today

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import dev.gopes.hinducalendar.data.model.*
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayPanchangScreen(viewModel: TodayPanchangViewModel = hiltViewModel()) {
    val panchang by viewModel.panchang.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Today's Panchang") })
        }
    ) { padding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (panchang != null) {
            PanchangContent(panchang!!, Modifier.padding(padding))
        }
    }
}

@Composable
private fun PanchangContent(panchang: PanchangDay, modifier: Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Daily Wisdom Briefing
        DailyBriefingCard()

        // Hindu Date Header
        HinduDateHeader(panchang)

        // Sun & Moon Times
        SunMoonCard(panchang)

        // Panchang Elements
        PanchangElementsCard(panchang)

        // Inauspicious Periods
        InauspiciousPeriodsCard(panchang)

        // Auspicious Period
        panchang.abhijitMuhurta?.let { AuspiciousPeriodCard(it) }

        // Festivals
        if (panchang.hasFestivals) {
            FestivalsCard(panchang.festivals)
        }
    }
}

@Composable
private fun HinduDateHeader(panchang: PanchangDay) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                panchang.hinduDate.fullDisplayString,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                panchang.date.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )
            panchang.location.cityName?.let {
                Text("📍 $it", style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f))
            }
        }
    }
}

@Composable
private fun SunMoonCard(panchang: PanchangDay) {
    val timeFmt = DateTimeFormatter.ofPattern("h:mm a")
    Card {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Text("Sun & Moon", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                TimeItem(Icons.Filled.WbSunny, "Sunrise", panchang.sunrise.format(timeFmt), Color(0xFFFF9933))
                TimeItem(Icons.Filled.WbTwilight, "Sunset", panchang.sunset.format(timeFmt), Color(0xFFE53935))
            }
            panchang.moonrise?.let { moonrise ->
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    TimeItem(Icons.Filled.NightsStay, "Moonrise", moonrise.format(timeFmt), Color(0xFF5C6BC0))
                    panchang.moonset?.let { moonset ->
                        TimeItem(Icons.Filled.Nightlight, "Moonset", moonset.format(timeFmt), Color(0xFF7E57C2))
                    }
                }
            }
        }
    }
}

@Composable
private fun TimeItem(icon: ImageVector, label: String, time: String, tint: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = label, tint = tint, modifier = Modifier.size(28.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        Text(time, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun PanchangElementsCard(panchang: PanchangDay) {
    Card {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Text("Panchang", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            ElementRow("Tithi", panchang.tithiInfo.name, panchang.tithiInfo.timeRangeString)
            ElementRow("Nakshatra", panchang.nakshatraInfo.name, panchang.nakshatraInfo.timeRangeString)
            ElementRow("Yoga", panchang.yogaInfo.name, null)
            ElementRow("Karana", panchang.karanaInfo.name, null)
        }
    }
}

@Composable
private fun ElementRow(label: String, value: String, detail: String?) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
        }
        detail?.takeIf { it.isNotEmpty() }?.let {
            Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
        }
    }
}

@Composable
private fun InauspiciousPeriodsCard(panchang: PanchangDay) {
    Card {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Inauspicious Periods", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(8.dp))
                Icon(Icons.Filled.Warning, contentDescription = null, tint = Color.Red, modifier = Modifier.size(16.dp))
            }
            Spacer(Modifier.height(8.dp))
            panchang.rahuKaal?.let { PeriodRow("Rahu Kaal", it.displayString, Color.Red) }
            panchang.yamaghanda?.let { PeriodRow("Yamaghanda", it.displayString, Color(0xFFFF9933)) }
            panchang.gulikaKaal?.let { PeriodRow("Gulika Kaal", it.displayString, Color(0xFFFFC107)) }
        }
    }
}

@Composable
private fun PeriodRow(name: String, time: String, color: Color) {
    Row(Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(8.dp).padding(end = 4.dp)) {
                Canvas(Modifier.fillMaxSize()) { drawCircle(color) }
            }
            Spacer(Modifier.width(8.dp))
            Text(name, style = MaterialTheme.typography.bodyMedium)
        }
        Text(time, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
    }
}

@Composable
private fun AuspiciousPeriodCard(period: TimePeriod) {
    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))) {
        Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = Color(0xFF4CAF50))
                Spacer(Modifier.width(8.dp))
                Text("Abhijit Muhurta", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            }
            Text(period.displayString, style = MaterialTheme.typography.bodyMedium, color = Color(0xFF2E7D32))
        }
    }
}

@Composable
private fun FestivalsCard(festivals: List<FestivalOccurrence>) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Today's Festivals", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(8.dp))
                Icon(Icons.Filled.Star, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
            }
            Spacer(Modifier.height(8.dp))
            festivals.forEach { occurrence ->
                Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(occurrence.festival.displayName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                        Text(occurrence.festival.category.displayName, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    }
                    if (occurrence.festival.category == FestivalCategory.MAJOR) {
                        AssistChip(onClick = {}, label = { Text("Major", style = MaterialTheme.typography.labelSmall) })
                    }
                }
            }
        }
    }
}

@Composable
private fun Canvas(modifier: Modifier, onDraw: androidx.compose.ui.graphics.drawscope.DrawScope.() -> Unit) {
    androidx.compose.foundation.Canvas(modifier = modifier, onDraw = onDraw)
}
