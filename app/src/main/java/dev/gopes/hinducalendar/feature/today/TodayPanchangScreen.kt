package dev.gopes.hinducalendar.feature.today

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
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
import dev.gopes.hinducalendar.domain.model.*
import dev.gopes.hinducalendar.core.ui.components.*
import dev.gopes.hinducalendar.core.ui.theme.*
import dev.gopes.hinducalendar.core.util.*
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayPanchangScreen(
    onSadhanaClick: () -> Unit = {},
    onJapaClick: () -> Unit = {},
    onDiyaClick: () -> Unit = {},
    viewModel: TodayPanchangViewModel = hiltViewModel(),
    gamificationViewModel: dev.gopes.hinducalendar.feature.gamification.GamificationViewModel = hiltViewModel()
) {
    val panchang by viewModel.panchang.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val language by viewModel.language.collectAsState()
    val prefs by viewModel.preferences.collectAsState()
    val gamData by gamificationViewModel.gamificationData.collectAsState()
    val gamStreak by gamificationViewModel.streakData.collectAsState()
    val gamChallenge by gamificationViewModel.dailyChallenge.collectAsState()

    // Celebration events (one-shot via Channel)
    var levelUpEvent by remember { mutableStateOf<dev.gopes.hinducalendar.feature.gamification.GamificationEvent.LevelUp?>(null) }
    var milestoneEvent by remember { mutableStateOf<dev.gopes.hinducalendar.feature.gamification.GamificationEvent.Milestone?>(null) }
    LaunchedEffect(Unit) {
        gamificationViewModel.events.collect { event ->
            when (event) {
                is dev.gopes.hinducalendar.feature.gamification.GamificationEvent.LevelUp -> levelUpEvent = event
                is dev.gopes.hinducalendar.feature.gamification.GamificationEvent.Milestone -> milestoneEvent = event
                is dev.gopes.hinducalendar.feature.gamification.GamificationEvent.NewBadges -> { /* not displayed here */ }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.tab_today)) })
        }
    ) { padding ->
        if (isLoading && panchang == null) {
            TodayScreenSkeleton(modifier = Modifier.padding(padding))
        } else if (panchang != null) {
            PullToRefreshBox(
                isRefreshing = isLoading,
                onRefresh = { viewModel.refresh() },
                modifier = Modifier.padding(padding)
            ) {
                PanchangContent(
                    panchang = panchang!!,
                    modifier = Modifier,
                    gamificationData = gamData,
                    streakData = gamStreak,
                    dailyChallenge = gamChallenge,
                    onChallengeAnswered = { gamificationViewModel.onChallengeAnswered(it) },
                    onSadhanaClick = onSadhanaClick,
                    language = language,
                    japaState = prefs?.japaState ?: JapaState(),
                    diyaState = prefs?.diyaState ?: DiyaState(),
                    onJapaClick = onJapaClick,
                    onDiyaClick = onDiyaClick
                )
            }
        }
    }

    // Level up celebration
    levelUpEvent?.let { event ->
        dev.gopes.hinducalendar.feature.gamification.LevelUpCelebration(
            oldLevel = event.oldLevel, newLevel = event.newLevel,
            onDismiss = { levelUpEvent = null },
            language = language
        )
    }
    // Milestone celebration
    milestoneEvent?.let { event ->
        dev.gopes.hinducalendar.feature.gamification.MilestoneCelebration(
            days = event.days,
            onDismiss = { milestoneEvent = null },
            language = language
        )
    }
}

@Composable
private fun PanchangContent(
    panchang: PanchangDay,
    modifier: Modifier,
    gamificationData: GamificationData = GamificationData(),
    streakData: StreakData = StreakData(),
    dailyChallenge: DailyChallenge? = null,
    onChallengeAnswered: (Boolean) -> Unit = {},
    onSadhanaClick: () -> Unit = {},
    language: AppLanguage = AppLanguage.ENGLISH,
    japaState: JapaState = JapaState(),
    diyaState: DiyaState = DiyaState(),
    onJapaClick: () -> Unit = {},
    onDiyaClick: () -> Unit = {}
) {
    val isDark = isSystemInDarkTheme()
    val isVibrant = LocalVibrantMode.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ── Cluster 1: Spiritual Practice ────────────────────────────────

        // 1. Greeting Banner
        Box(Modifier.entranceAnimation(0, isVibrant)) {
            GreetingBanner(
                isDarkTheme = isDark,
                gamificationData = gamificationData,
                streakData = streakData,
                language = language
            )
        }

        // 2. Streak Badge (outside gamification conditional, matching iOS)
        Box(Modifier.entranceAnimation(1, isVibrant)) {
            dev.gopes.hinducalendar.feature.gamification.StreakBadgeView(
                streakData = streakData,
                gamificationData = gamificationData,
                language = language
            )
        }

        // 3-4. Sadhana + Daily Challenge (conditional on gamification)
        if (gamificationData.isEnabled) {
            Box(Modifier.entranceAnimation(2, isVibrant)) {
                dev.gopes.hinducalendar.feature.gamification.SadhanaStatusBar(
                    data = gamificationData,
                    onClick = onSadhanaClick,
                    language = language
                )
            }
            dailyChallenge?.let { challenge ->
                Box(Modifier.entranceAnimation(3, isVibrant)) {
                    dev.gopes.hinducalendar.feature.gamification.DailyChallengeCard(
                        challenge = challenge,
                        gamificationData = gamificationData,
                        onAnswered = { correct -> onChallengeAnswered(correct) },
                        language = language
                    )
                }
            }
        }

        // 5. Japa & Diya card
        Box(Modifier.entranceAnimation(4, isVibrant)) {
            JapaDiyaTodayCard(
                japaState = japaState,
                diyaState = diyaState,
                language = language,
                onJapaClick = onJapaClick,
                onDiyaClick = onDiyaClick
            )
        }

        // 6. Daily Wisdom Briefing
        Box(Modifier.entranceAnimation(5, isVibrant)) {
            DailyBriefingCard()
        }

        // ── Cluster 2: Panchang Details ──────────────────────────────────
        Spacer(Modifier.height(8.dp))
        SectionHeader(stringResource(R.string.group_panchang_details))

        // 7. Hindu Date Header
        Box(Modifier.entranceAnimation(6, isVibrant)) {
            HinduDateHeader(panchang)
        }

        // 8. Sun & Moon Times
        Box(Modifier.entranceAnimation(7, isVibrant)) {
            SunMoonCard(panchang)
        }

        // 10. Panchang Elements
        Box(Modifier.entranceAnimation(8, isVibrant)) {
            PanchangElementsCard(panchang)
        }

        // 11. Inauspicious Periods
        Box(Modifier.entranceAnimation(9, isVibrant)) {
            InauspiciousPeriodsCard(panchang)
        }

        // 12. Auspicious Period
        panchang.abhijitMuhurta?.let {
            Box(Modifier.entranceAnimation(10, isVibrant)) {
                AuspiciousPeriodCard(it)
            }
        }

        // 13. Muhurta
        if (panchang.muhurtas.isNotEmpty()) {
            Box(Modifier.entranceAnimation(11, isVibrant)) {
                MuhurtaCard(panchang.muhurtas)
            }
        }

        // 14. Choghadiya
        if (panchang.choghadiyas.isNotEmpty()) {
            Box(Modifier.entranceAnimation(12, isVibrant)) {
                ChoghadiyaCard(panchang.choghadiyas)
            }
        }

        // 15. Hora
        if (panchang.horas.isNotEmpty()) {
            Box(Modifier.entranceAnimation(13, isVibrant)) {
                HoraCard(panchang.horas)
            }
        }

        // ── Cluster 3: Festivals ─────────────────────────────────────────
        if (panchang.hasFestivals) {
            Spacer(Modifier.height(8.dp))
            Box(Modifier.entranceAnimation(14, isVibrant)) {
                FestivalsCard(panchang.festivals, language)
            }
        }

        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun SectionHeader(title: String) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        )
        Spacer(Modifier.width(12.dp))
        Text(
            title,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
        Spacer(Modifier.width(12.dp))
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        )
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
                "${panchang.hinduDate.localizedDisplayString()}, ${panchang.hinduDate.localizedYearDisplay(panchang.tradition)}",
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
        if (panchang.moonrise != null || panchang.moonset != null) {
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                panchang.moonrise?.let { moonrise ->
                    TimeItem(Icons.Filled.NightsStay, stringResource(R.string.today_moonrise), moonrise.format(timeFmt), MoonriseColor)
                }
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
        Text(time, style = SacredTypography.numericSmall, fontWeight = FontWeight.Medium)
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
        Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                icon,
                contentDescription = stringResource(R.string.cd_panchang_element),
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
            Icon(Icons.Filled.Warning, contentDescription = stringResource(R.string.cd_inauspicious_period), tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
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
            Icon(Icons.Filled.AutoAwesome, contentDescription = stringResource(R.string.cd_auspicious_period), tint = AuspiciousGreen, modifier = Modifier.size(16.dp))
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
            Icon(Icons.Filled.Star, contentDescription = stringResource(R.string.cd_festival_today), tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
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

// ── Expandable Muhurta / Choghadiya / Hora Cards ──────────────────────────

@Composable
private fun MuhurtaCard(muhurtas: List<MuhurtaPeriod>) {
    var expanded by remember { mutableStateOf(false) }
    val timeFmt = DateTimeFormatter.ofPattern("h:mm a")
    val now = java.time.LocalDateTime.now()
    val currentMuhurta = muhurtas.find { now >= it.start && now < it.end }

    SacredCard {
        Row(
            Modifier.fillMaxWidth().clickable { expanded = !expanded },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.section_muhurta), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                currentMuhurta?.let {
                    Spacer(Modifier.width(8.dp))
                    AssistChip(
                        onClick = { expanded = !expanded },
                        label = { Text(it.muhurta.localizedName(), style = MaterialTheme.typography.labelSmall) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = auspiciousColor(it.muhurta.auspiciousness).copy(alpha = 0.15f)
                        )
                    )
                }
            }
            Icon(
                if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        AnimatedVisibility(visible = expanded) {
            Column(Modifier.padding(top = 8.dp)) {
                Text(stringResource(R.string.muhurta_day_header), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(4.dp))
                muhurtas.filter { it.muhurta.isDay }.forEach { period ->
                    MuhurtaRow(period, timeFmt, isCurrent = period == currentMuhurta)
                }
                Spacer(Modifier.height(12.dp))
                Text(stringResource(R.string.muhurta_night_header), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(4.dp))
                muhurtas.filter { !it.muhurta.isDay }.forEach { period ->
                    MuhurtaRow(period, timeFmt, isCurrent = period == currentMuhurta)
                }
            }
        }
    }
}

@Composable
private fun MuhurtaRow(period: MuhurtaPeriod, fmt: DateTimeFormatter, isCurrent: Boolean) {
    Row(
        Modifier
            .fillMaxWidth()
            .then(
                if (isCurrent) Modifier.background(
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                    RoundedCornerShape(8.dp)
                ) else Modifier
            )
            .padding(vertical = 4.dp, horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Canvas(Modifier.size(8.dp)) {
                drawCircle(auspiciousColorStatic(period.muhurta.auspiciousness))
            }
            Spacer(Modifier.width(8.dp))
            Text(period.muhurta.localizedName(), style = MaterialTheme.typography.bodySmall)
            if (isCurrent) {
                Spacer(Modifier.width(4.dp))
                Text(
                    stringResource(R.string.current_period),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Text(
            "${period.start.format(fmt)} - ${period.end.format(fmt)}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ChoghadiyaCard(choghadiyas: List<ChoghadiyaPeriod>) {
    var expanded by remember { mutableStateOf(false) }
    val timeFmt = DateTimeFormatter.ofPattern("h:mm a")
    val now = java.time.LocalDateTime.now()
    val currentChoghadiya = choghadiyas.find { now >= it.start && now < it.end }

    SacredCard {
        Row(
            Modifier.fillMaxWidth().clickable { expanded = !expanded },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.section_choghadiya), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                currentChoghadiya?.let {
                    Spacer(Modifier.width(8.dp))
                    AssistChip(
                        onClick = { expanded = !expanded },
                        label = { Text(it.type.localizedName(), style = MaterialTheme.typography.labelSmall) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = choghadiyaColor(it.type.auspiciousness).copy(alpha = 0.15f)
                        )
                    )
                }
            }
            Icon(
                if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        AnimatedVisibility(visible = expanded) {
            Column(Modifier.padding(top = 8.dp)) {
                Text(stringResource(R.string.choghadiya_day_header), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(4.dp))
                choghadiyas.filter { it.isDay }.forEach { period ->
                    ChoghadiyaRow(period, timeFmt, isCurrent = period == currentChoghadiya)
                }
                Spacer(Modifier.height(12.dp))
                Text(stringResource(R.string.choghadiya_night_header), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(4.dp))
                choghadiyas.filter { !it.isDay }.forEach { period ->
                    ChoghadiyaRow(period, timeFmt, isCurrent = period == currentChoghadiya)
                }
            }
        }
    }
}

@Composable
private fun ChoghadiyaRow(period: ChoghadiyaPeriod, fmt: DateTimeFormatter, isCurrent: Boolean) {
    Row(
        Modifier
            .fillMaxWidth()
            .then(
                if (isCurrent) Modifier.background(
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                    RoundedCornerShape(8.dp)
                ) else Modifier
            )
            .padding(vertical = 4.dp, horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Canvas(Modifier.size(8.dp)) {
                drawCircle(choghadiyaColorStatic(period.type.auspiciousness))
            }
            Spacer(Modifier.width(8.dp))
            Text(period.type.localizedName(), style = MaterialTheme.typography.bodySmall)
            if (isCurrent) {
                Spacer(Modifier.width(4.dp))
                Text(
                    stringResource(R.string.current_period),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Text(
            "${period.start.format(fmt)} - ${period.end.format(fmt)}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun HoraCard(horas: List<HoraPeriod>) {
    var expanded by remember { mutableStateOf(false) }
    val timeFmt = DateTimeFormatter.ofPattern("h:mm a")
    val now = java.time.LocalDateTime.now()
    val currentHora = horas.find { now >= it.start && now < it.end }

    SacredCard {
        Row(
            Modifier.fillMaxWidth().clickable { expanded = !expanded },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.section_hora), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                currentHora?.let {
                    Spacer(Modifier.width(8.dp))
                    AssistChip(
                        onClick = { expanded = !expanded },
                        label = { Text("${it.planet.symbol} ${it.planet.localizedName()}", style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }
            Icon(
                if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        AnimatedVisibility(visible = expanded) {
            Column(Modifier.padding(top = 8.dp)) {
                Text(stringResource(R.string.hora_day_header), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(4.dp))
                horas.filter { it.isDay }.forEach { period ->
                    HoraRow(period, timeFmt, isCurrent = period == currentHora)
                }
                Spacer(Modifier.height(12.dp))
                Text(stringResource(R.string.hora_night_header), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(4.dp))
                horas.filter { !it.isDay }.forEach { period ->
                    HoraRow(period, timeFmt, isCurrent = period == currentHora)
                }
            }
        }
    }
}

@Composable
private fun HoraRow(period: HoraPeriod, fmt: DateTimeFormatter, isCurrent: Boolean) {
    Row(
        Modifier
            .fillMaxWidth()
            .then(
                if (isCurrent) Modifier.background(
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                    RoundedCornerShape(8.dp)
                ) else Modifier
            )
            .padding(vertical = 4.dp, horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(period.planet.symbol, style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.width(8.dp))
            Text(period.planet.localizedName(), style = MaterialTheme.typography.bodySmall)
            if (isCurrent) {
                Spacer(Modifier.width(4.dp))
                Text(
                    stringResource(R.string.current_period),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Text(
            "${period.start.format(fmt)} - ${period.end.format(fmt)}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ── Auspiciousness color helpers ──────────────────────────────────────────

@Composable
private fun auspiciousColor(auspiciousness: MuhurtaAuspiciousness): androidx.compose.ui.graphics.Color = when (auspiciousness) {
    MuhurtaAuspiciousness.SHUBH -> AuspiciousGreen
    MuhurtaAuspiciousness.ATHI_SHUBH -> DivineGold
    MuhurtaAuspiciousness.MADHYAM -> DeepSaffron
    MuhurtaAuspiciousness.ASHUBH -> MaterialTheme.colorScheme.error
}

private fun auspiciousColorStatic(auspiciousness: MuhurtaAuspiciousness): androidx.compose.ui.graphics.Color = when (auspiciousness) {
    MuhurtaAuspiciousness.SHUBH -> AuspiciousGreen
    MuhurtaAuspiciousness.ATHI_SHUBH -> DivineGold
    MuhurtaAuspiciousness.MADHYAM -> DeepSaffron
    MuhurtaAuspiciousness.ASHUBH -> InauspiciousRed
}

@Composable
private fun choghadiyaColor(auspiciousness: ChoghadiyaAuspiciousness): androidx.compose.ui.graphics.Color = when (auspiciousness) {
    ChoghadiyaAuspiciousness.BEST -> DivineGold
    ChoghadiyaAuspiciousness.GOOD -> AuspiciousGreen
    ChoghadiyaAuspiciousness.GAIN -> AuspiciousGreen
    ChoghadiyaAuspiciousness.NEUTRAL -> DeepSaffron
    ChoghadiyaAuspiciousness.BAD -> MaterialTheme.colorScheme.error
}

private fun choghadiyaColorStatic(auspiciousness: ChoghadiyaAuspiciousness): androidx.compose.ui.graphics.Color = when (auspiciousness) {
    ChoghadiyaAuspiciousness.BEST -> DivineGold
    ChoghadiyaAuspiciousness.GOOD -> AuspiciousGreen
    ChoghadiyaAuspiciousness.GAIN -> AuspiciousGreen
    ChoghadiyaAuspiciousness.NEUTRAL -> DeepSaffron
    ChoghadiyaAuspiciousness.BAD -> InauspiciousRed
}
