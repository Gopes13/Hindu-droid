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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import dev.gopes.hinducalendar.R
import dev.gopes.hinducalendar.data.model.*
import dev.gopes.hinducalendar.ui.components.*
import dev.gopes.hinducalendar.ui.theme.*
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayPanchangScreen(
    onSadhanaClick: () -> Unit = {},
    viewModel: TodayPanchangViewModel = hiltViewModel(),
    gamificationViewModel: dev.gopes.hinducalendar.ui.gamification.GamificationViewModel = hiltViewModel()
) {
    val panchang by viewModel.panchang.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.today_panchang)) })
        }
    ) { padding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else if (panchang != null) {
            PanchangContent(panchang!!, Modifier.padding(padding), gamificationViewModel, onSadhanaClick)
        }
    }

    // Level up celebration
    gamificationViewModel.levelUpEvent?.let { (old, new) ->
        dev.gopes.hinducalendar.ui.gamification.LevelUpCelebration(
            oldLevel = old, newLevel = new,
            onDismiss = { gamificationViewModel.dismissLevelUp() }
        )
    }
    // Milestone celebration
    gamificationViewModel.milestoneEvent?.let { days ->
        dev.gopes.hinducalendar.ui.gamification.MilestoneCelebration(
            days = days,
            onDismiss = { gamificationViewModel.dismissMilestone() }
        )
    }
}

@Composable
private fun PanchangContent(
    panchang: PanchangDay,
    modifier: Modifier,
    gamificationViewModel: dev.gopes.hinducalendar.ui.gamification.GamificationViewModel? = null,
    onSadhanaClick: () -> Unit = {}
) {
    val isDark = isSystemInDarkTheme()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Sadhana Status Bar (gamification)
        gamificationViewModel?.let { gvm ->
            if (gvm.gamificationData.isEnabled) {
                dev.gopes.hinducalendar.ui.gamification.SadhanaStatusBar(
                    data = gvm.gamificationData,
                    onClick = onSadhanaClick
                )
                dev.gopes.hinducalendar.ui.gamification.StreakBadgeView(
                    streakData = gvm.streakData,
                    gamificationData = gvm.gamificationData
                )
                gvm.dailyChallenge?.let { challenge ->
                    dev.gopes.hinducalendar.ui.gamification.DailyChallengeCard(
                        challenge = challenge,
                        gamificationData = gvm.gamificationData,
                        onAnswered = { correct -> gvm.onChallengeAnswered(correct) }
                    )
                }
            }
        }

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
        Text(stringResource(R.string.section_sun_moon), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, modifier = Modifier.semantics { heading() })
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            TimeItem(Icons.Filled.WbSunny, stringResource(R.string.today_sunrise), panchang.sunrise.format(timeFmt), SunriseColor)
            TimeItem(Icons.Filled.WbTwilight, stringResource(R.string.today_sunset), panchang.sunset.format(timeFmt), SunsetColor)
        }
        panchang.moonrise?.let { moonrise ->
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                TimeItem(Icons.Filled.NightsStay, stringResource(R.string.today_moonrise), moonrise.format(timeFmt), MoonriseColor)
                panchang.moonset?.let { moonset ->
                    TimeItem(Icons.Filled.Nightlight, stringResource(R.string.today_moonset), moonset.format(timeFmt), MoonsetColor)
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
        Text(stringResource(R.string.section_panchang), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, modifier = Modifier.semantics { heading() })
        Spacer(Modifier.height(8.dp))
        ElementRow(stringResource(R.string.panchang_tithi), panchang.tithiInfo.name, panchang.tithiInfo.timeRangeString)
        ElementRow(stringResource(R.string.panchang_nakshatra), panchang.nakshatraInfo.name, panchang.nakshatraInfo.timeRangeString)
        ElementRow(stringResource(R.string.panchang_yoga), panchang.yogaInfo.name, null)
        ElementRow(stringResource(R.string.panchang_karana), panchang.karanaInfo.name, null)
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
            Text(stringResource(R.string.section_inauspicious_periods), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, modifier = Modifier.semantics { heading() })
            Spacer(Modifier.width(8.dp))
            Icon(Icons.Filled.Warning, contentDescription = "Inauspicious periods", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
        }
        Spacer(Modifier.height(8.dp))
        panchang.rahuKaal?.let { PeriodRow(stringResource(R.string.panchang_rahu_kaal), it.displayString, MaterialTheme.colorScheme.error) }
        panchang.yamaghanda?.let { PeriodRow(stringResource(R.string.panchang_yamaghanda), it.displayString, MaterialTheme.colorScheme.primary) }
        panchang.gulikaKaal?.let { PeriodRow(stringResource(R.string.panchang_gulika), it.displayString, MaterialTheme.colorScheme.tertiary) }
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
                Icon(Icons.Filled.AutoAwesome, contentDescription = "Auspicious period", tint = AuspiciousGreen)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.panchang_abhijit), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            }
            Text(period.displayString, style = MaterialTheme.typography.bodyMedium, color = AuspiciousGreen)
        }
    }
}

@Composable
private fun FestivalsCard(festivals: List<FestivalOccurrence>) {
    SacredHighlightCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.section_today_festivals), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, modifier = Modifier.semantics { heading() })
            Spacer(Modifier.width(8.dp))
            Icon(Icons.Filled.Star, contentDescription = "Today's festivals", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
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
                        label = { Text(stringResource(R.string.festival_major), style = MaterialTheme.typography.labelSmall) },
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
