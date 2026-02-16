package dev.gopes.hinducalendar.ui.today

import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import dev.gopes.hinducalendar.data.model.*
import dev.gopes.hinducalendar.ui.components.*
import dev.gopes.hinducalendar.ui.theme.*
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
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else if (panchang != null) {
            PanchangContent(panchang!!, Modifier.padding(padding))
        }
    }
}

@Composable
private fun PanchangContent(panchang: PanchangDay, modifier: Modifier) {
    val isDark = isSystemInDarkTheme()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Greeting Banner
        GreetingBanner(isDarkTheme = isDark)

        // Daily Wisdom Briefing
        DailyBriefingCard()

        DecorativeDivider(style = DividerStyle.OM)

        // Hindu Date Header
        HinduDateHeader(panchang)

        // Sun & Moon Times
        SunMoonCard(panchang)

        DecorativeDivider(style = DividerStyle.DIAMOND)

        // Panchang Elements
        PanchangElementsCard(panchang)

        // Inauspicious Periods
        InauspiciousPeriodsCard(panchang)

        // Auspicious Period
        panchang.abhijitMuhurta?.let { AuspiciousPeriodCard(it) }

        DecorativeDivider(style = DividerStyle.LOTUS)

        // Festivals
        if (panchang.hasFestivals) {
            FestivalsCard(panchang.festivals)
        }
    }
}

@Composable
private fun HinduDateHeader(panchang: PanchangDay) {
    SacredHighlightCard {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                panchang.hinduDate.fullDisplayString,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                panchang.date.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            panchang.location.cityName?.let {
                Text(
                    "\uD83D\uDCCD $it",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SunMoonCard(panchang: PanchangDay) {
    val timeFmt = DateTimeFormatter.ofPattern("h:mm a")
    SacredCard {
        Text("Sun & Moon", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            TimeItem(Icons.Filled.WbSunny, "Sunrise", panchang.sunrise.format(timeFmt), SunriseColor)
            TimeItem(Icons.Filled.WbTwilight, "Sunset", panchang.sunset.format(timeFmt), SunsetColor)
        }
        panchang.moonrise?.let { moonrise ->
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                TimeItem(Icons.Filled.NightsStay, "Moonrise", moonrise.format(timeFmt), MoonriseColor)
                panchang.moonset?.let { moonset ->
                    TimeItem(Icons.Filled.Nightlight, "Moonset", moonset.format(timeFmt), MoonsetColor)
                }
            }
        }
    }
}

@Composable
private fun TimeItem(icon: ImageVector, label: String, time: String, tint: androidx.compose.ui.graphics.Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = label, tint = tint, modifier = Modifier.size(28.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(time, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun PanchangElementsCard(panchang: PanchangDay) {
    SacredCard {
        Text("Panchang", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        ElementRow("Tithi", panchang.tithiInfo.name, panchang.tithiInfo.timeRangeString)
        ElementRow("Nakshatra", panchang.nakshatraInfo.name, panchang.nakshatraInfo.timeRangeString)
        ElementRow("Yoga", panchang.yogaInfo.name, null)
        ElementRow("Karana", panchang.karanaInfo.name, null)
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
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
        }
        detail?.takeIf { it.isNotEmpty() }?.let {
            Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun InauspiciousPeriodsCard(panchang: PanchangDay) {
    SacredCard(accentColor = MaterialTheme.colorScheme.error) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Inauspicious Periods", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(8.dp))
            Icon(Icons.Filled.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
        }
        Spacer(Modifier.height(8.dp))
        panchang.rahuKaal?.let { PeriodRow("Rahu Kaal", it.displayString, MaterialTheme.colorScheme.error) }
        panchang.yamaghanda?.let { PeriodRow("Yamaghanda", it.displayString, MaterialTheme.colorScheme.primary) }
        panchang.gulikaKaal?.let { PeriodRow("Gulika Kaal", it.displayString, MaterialTheme.colorScheme.tertiary) }
    }
}

@Composable
private fun PeriodRow(name: String, time: String, color: androidx.compose.ui.graphics.Color) {
    Row(Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(8.dp).padding(end = 4.dp)) {
                Canvas(Modifier.fillMaxSize()) { drawCircle(color) }
            }
            Spacer(Modifier.width(8.dp))
            Text(name, style = MaterialTheme.typography.bodyMedium)
        }
        Text(time, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun AuspiciousPeriodCard(period: TimePeriod) {
    SacredHighlightCard(accentColor = AuspiciousGreen) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = AuspiciousGreen)
                Spacer(Modifier.width(8.dp))
                Text("Abhijit Muhurta", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            }
            Text(period.displayString, style = MaterialTheme.typography.bodyMedium, color = AuspiciousGreen)
        }
    }
}

@Composable
private fun FestivalsCard(festivals: List<FestivalOccurrence>) {
    SacredHighlightCard {
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
                    Text(occurrence.festival.category.displayName, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (occurrence.festival.category == FestivalCategory.MAJOR) {
                    AssistChip(
                        onClick = {},
                        label = { Text("Major", style = MaterialTheme.typography.labelSmall) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            labelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun Canvas(modifier: Modifier, onDraw: androidx.compose.ui.graphics.drawscope.DrawScope.() -> Unit) {
    androidx.compose.foundation.Canvas(modifier = modifier, onDraw = onDraw)
}
