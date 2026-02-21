package dev.gopes.hinducalendar.ui.gamification

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import dev.gopes.hinducalendar.R
import dev.gopes.hinducalendar.data.model.AppLanguage
import dev.gopes.hinducalendar.data.model.SadhanaBadge
import dev.gopes.hinducalendar.data.model.SadhanaLevel
import dev.gopes.hinducalendar.ui.components.SacredCard
import dev.gopes.hinducalendar.ui.util.localizedName

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SadhanaJourneyScreen(
    onBack: () -> Unit,
    viewModel: GamificationViewModel = hiltViewModel()
) {
    val data = viewModel.gamificationData
    val language by viewModel.language.collectAsState()
    val streak = viewModel.streakData
    val level = data.currentLevelData

    var selectedBadge by remember { mutableStateOf<SadhanaBadge?>(null) }

    // Animated progress ring
    val progressAnim by animateFloatAsState(
        targetValue = data.currentLevelProgress.toFloat(),
        animationSpec = tween(800, delayMillis = 200, easing = FastOutSlowInEasing),
        label = "ring"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.sadhana_journey)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.cd_go_back))
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ── Level Header ────────────────────────────────────────────
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Progress ring
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(140.dp)
                    ) {
                        val primary = MaterialTheme.colorScheme.primary
                        val tertiary = MaterialTheme.colorScheme.tertiary
                        val trackColor = MaterialTheme.colorScheme.surfaceVariant

                        Canvas(Modifier.fillMaxSize()) {
                            val strokeWidth = 8.dp.toPx()
                            // Background ring
                            drawArc(
                                color = trackColor,
                                startAngle = -90f,
                                sweepAngle = 360f,
                                useCenter = false,
                                style = Stroke(strokeWidth, cap = StrokeCap.Round)
                            )
                            // Progress arc
                            drawArc(
                                brush = Brush.sweepGradient(listOf(primary, tertiary)),
                                startAngle = -90f,
                                sweepAngle = 360f * progressAnim,
                                useCenter = false,
                                style = Stroke(strokeWidth, cap = StrokeCap.Round)
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                language.localizedDigits(stringResource(R.string.lv_format, level.level)),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                language.localizedDigits("${(data.currentLevelProgress * 100).toInt()}%"),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    Text(
                        language.localizedDigits(stringResource(R.string.sadhana_level_format, level.level, stringResource(
                            when (level.titleKey) {
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
                        ))),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    if (data.punyaPointsToNextLevel > 0) {
                        Text(
                            language.localizedDigits(stringResource(R.string.sadhana_pp_to_next, data.punyaPointsToNextLevel)),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text(
                            stringResource(R.string.sadhana_max_level),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // ── Stats Row ───────────────────────────────────────────────
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatItem(stringResource(R.string.punya_points), language.localizedNumber(data.totalPunyaPoints))
                    StatItem(stringResource(R.string.sadhana_days_active), language.localizedNumber(streak.totalDaysOpened))
                    StatItem(
                        stringResource(R.string.sadhana_badges_label),
                        language.localizedDigits("${data.earnedBadges.size}/${SadhanaBadge.allBadges.size}")
                    )
                }
            }

            // ── Badges ──────────────────────────────────────────────────
            item {
                Divider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Filled.Shield,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        stringResource(R.string.sadhana_badges_section),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Badge categories
            SadhanaBadge.BadgeCategory.entries.forEach { category ->
                val badges = SadhanaBadge.forCategory(category)
                val earnedCount = badges.count { data.hasBadge(it.id) }

                item {
                    Text(
                        language.localizedDigits(stringResource(R.string.badge_category_count, category.localizedName(), earnedCount, badges.size)),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                items(badges.chunked(3)) { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        row.forEach { badge ->
                            BadgeItem(
                                badge = badge,
                                isEarned = data.hasBadge(badge.id),
                                onClick = { selectedBadge = badge },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        // Fill remaining slots
                        repeat(3 - row.size) {
                            Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }

    // Badge detail sheet
    selectedBadge?.let { badge ->
        BadgeDetailSheet(
            badge = badge,
            gamificationData = data,
            onDismiss = { selectedBadge = null }
        )
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun BadgeItem(
    badge: SadhanaBadge,
    isEarned: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .then(
                    if (isEarned) Modifier.background(
                        Brush.radialGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                Color.Transparent
                            )
                        )
                    ) else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (isEarned) Brush.radialGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.tertiary
                            )
                        ) else Brush.radialGradient(
                            listOf(
                                MaterialTheme.colorScheme.surfaceVariant,
                                MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (!isEarned) {
                    Icon(
                        Icons.Filled.Lock,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Text(
                        badge.category.icon.take(2).uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            badge.localizedName(),
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center,
            maxLines = 2,
            color = if (isEarned) MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
    }
}
