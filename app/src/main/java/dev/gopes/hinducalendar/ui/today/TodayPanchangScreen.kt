package dev.gopes.hinducalendar.ui.today

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import dev.gopes.hinducalendar.R
import dev.gopes.hinducalendar.data.model.*
import dev.gopes.hinducalendar.ui.components.*
import dev.gopes.hinducalendar.ui.theme.*
import dev.gopes.hinducalendar.ui.util.*
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayPanchangScreen(
    onSadhanaClick: () -> Unit = {},
    onJapaClick: () -> Unit = {},
    onDiyaClick: () -> Unit = {},
    viewModel: TodayPanchangViewModel = hiltViewModel(),
    gamificationViewModel: dev.gopes.hinducalendar.ui.gamification.GamificationViewModel = hiltViewModel()
) {
    val panchang by viewModel.panchang.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val language by viewModel.language.collectAsState()
    val prefs by viewModel.preferences.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.tab_today)) })
        }
    ) { padding ->
        if (isLoading && panchang == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else if (panchang != null) {
            PanchangContent(
                panchang = panchang!!,
                modifier = Modifier.padding(padding),
                gamificationViewModel = gamificationViewModel,
                onSadhanaClick = onSadhanaClick,
                language = language,
                japaState = prefs?.japaState ?: JapaState(),
                diyaState = prefs?.diyaState ?: DiyaState(),
                onJapaClick = onJapaClick,
                onDiyaClick = onDiyaClick
            )
        }
    }

    // Level up celebration
    gamificationViewModel.levelUpEvent?.let { (old, new) ->
        dev.gopes.hinducalendar.ui.gamification.LevelUpCelebration(
            oldLevel = old, newLevel = new,
            onDismiss = { gamificationViewModel.dismissLevelUp() },
            language = language
        )
    }
    // Milestone celebration
    gamificationViewModel.milestoneEvent?.let { days ->
        dev.gopes.hinducalendar.ui.gamification.MilestoneCelebration(
            days = days,
            onDismiss = { gamificationViewModel.dismissMilestone() },
            language = language
        )
    }
}

@Composable
private fun PanchangContent(
    panchang: PanchangDay,
    modifier: Modifier,
    gamificationViewModel: dev.gopes.hinducalendar.ui.gamification.GamificationViewModel? = null,
    onSadhanaClick: () -> Unit = {},
    language: AppLanguage = AppLanguage.ENGLISH,
    japaState: JapaState = JapaState(),
    diyaState: DiyaState = DiyaState(),
    onJapaClick: () -> Unit = {},
    onDiyaClick: () -> Unit = {}
) {
    val isDark = isSystemInDarkTheme()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Greeting Banner (first, matching iOS)
        GreetingBanner(
            isDarkTheme = isDark,
            gamificationData = gamificationViewModel?.gamificationData,
            streakData = gamificationViewModel?.streakData,
            language = language
        )

        // 2. Streak Badge (outside gamification conditional, matching iOS)
        gamificationViewModel?.let { gvm ->
            dev.gopes.hinducalendar.ui.gamification.StreakBadgeView(
                streakData = gvm.streakData,
                gamificationData = gvm.gamificationData,
                language = language
            )

            // 3-4. Sadhana + Daily Challenge (conditional on gamification)
            if (gvm.gamificationData.isEnabled) {
                dev.gopes.hinducalendar.ui.gamification.SadhanaStatusBar(
                    data = gvm.gamificationData,
                    onClick = onSadhanaClick,
                    language = language
                )
                gvm.dailyChallenge?.let { challenge ->
                    dev.gopes.hinducalendar.ui.gamification.DailyChallengeCard(
                        challenge = challenge,
                        gamificationData = gvm.gamificationData,
                        onAnswered = { correct -> gvm.onChallengeAnswered(correct) },
                        language = language
                    )
                }
            }
        }

        // 5. Japa & Diya card
        JapaDiyaTodayCard(
            japaState = japaState,
            diyaState = diyaState,
            language = language,
            onJapaClick = onJapaClick,
            onDiyaClick = onDiyaClick
        )

        // 6. Daily Wisdom Briefing
        DailyBriefingCard()

        DecorativeDivider(style = DividerStyle.OM)

        // 7. Hindu Date Header
        HinduDateHeader(panchang)

        // 8. Sun & Moon Times
        SunMoonCard(panchang)

        DecorativeDivider(style = DividerStyle.LOTUS)

        // 10. Panchang Elements
        PanchangElementsCard(panchang)

        // 11. Inauspicious Periods
        InauspiciousPeriodsCard(panchang)

        // 12. Auspicious Period
        panchang.abhijitMuhurta?.let { AuspiciousPeriodCard(it) }

        DecorativeDivider(style = DividerStyle.DIAMOND)

        // 14. Festivals
        if (panchang.hasFestivals) {
            FestivalsCard(panchang.festivals, language)
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
                "${panchang.hinduDate.displayString}, ${panchang.hinduDate.yearDisplayForTradition(panchang.tradition)}",
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
        ElementRow(Icons.Filled.DarkMode, stringResource(R.string.panchang_tithi), localizedTithiName(panchang.tithiInfo.number), panchang.tithiInfo.timeRangeString)
        ElementRow(Icons.Filled.Star, stringResource(R.string.panchang_nakshatra), localizedNakshatraName(panchang.nakshatraInfo.number), panchang.nakshatraInfo.timeRangeString)
        ElementRow(Icons.Filled.Grain, stringResource(R.string.panchang_yoga), localizedYogaName(panchang.yogaInfo.number), null)
        ElementRow(Icons.Filled.Square, stringResource(R.string.panchang_karana), localizedKaranaName(panchang.karanaInfo.number), null, iconRotation = 45f)
    }
}

@Composable
private fun ElementRow(icon: ImageVector, label: String, value: String, detail: String?, iconRotation: Float = 0f) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(28.dp)
                    .then(if (iconRotation != 0f) Modifier.rotate(iconRotation) else Modifier)
            )
            Column {
                Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            }
        }
        detail?.takeIf { it.isNotEmpty() }?.let {
            Text(it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun InauspiciousPeriodsCard(panchang: PanchangDay) {
    SacredCard(accentColor = MaterialTheme.colorScheme.error) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.section_inauspicious_periods), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, modifier = Modifier.semantics { heading() })
            Spacer(Modifier.width(8.dp))
            Icon(Icons.Filled.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
        }
        Spacer(Modifier.height(8.dp))
        panchang.rahuKaal?.let { PeriodRow(stringResource(R.string.panchang_rahu_kaal), it.displayString, MaterialTheme.colorScheme.error) }
        panchang.yamaghanda?.let { PeriodRow(stringResource(R.string.panchang_yamaghanda), it.displayString, MaterialTheme.colorScheme.primary) }
        panchang.gulikaKaal?.let { PeriodRow(stringResource(R.string.panchang_gulika), it.displayString, MaterialTheme.colorScheme.tertiary) }
    }
}

@Composable
private fun PeriodRow(name: String, time: String, color: androidx.compose.ui.graphics.Color) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Canvas(Modifier.size(8.dp)) { drawCircle(color) }
            Spacer(Modifier.width(8.dp))
            Text(name, style = MaterialTheme.typography.bodyMedium)
        }
        Text(time, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun AuspiciousPeriodCard(period: TimePeriod) {
    SacredHighlightCard(accentColor = AuspiciousGreen) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.section_auspicious_period), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(8.dp))
            Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = AuspiciousGreen, modifier = Modifier.size(16.dp))
        }
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(stringResource(R.string.panchang_abhijit), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Text(period.displayString, style = MaterialTheme.typography.bodyMedium, color = AuspiciousGreen)
        }
    }
}

@Composable
private fun FestivalsCard(festivals: List<FestivalOccurrence>, language: AppLanguage) {
    SacredHighlightCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.section_today_festivals), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, modifier = Modifier.semantics { heading() })
            Spacer(Modifier.width(8.dp))
            Icon(Icons.Filled.Star, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
        }
        Spacer(Modifier.height(8.dp))
        festivals.forEach { occurrence ->
            Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(occurrence.festival.displayName(language), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                    Text(occurrence.festival.category.localizedName(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
