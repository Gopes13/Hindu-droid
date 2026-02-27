package dev.gopes.hinducalendar.feature.festivals

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import dev.gopes.hinducalendar.R
import dev.gopes.hinducalendar.domain.model.AppLanguage
import dev.gopes.hinducalendar.domain.model.Festival
import dev.gopes.hinducalendar.domain.model.FestivalCategory
import dev.gopes.hinducalendar.core.ui.components.*
import dev.gopes.hinducalendar.core.util.localizedName

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FestivalDetailScreen(
    festivalId: String,
    onBack: () -> Unit,
    viewModel: FestivalListViewModel = hiltViewModel(),
    gamificationViewModel: dev.gopes.hinducalendar.feature.gamification.GamificationViewModel = hiltViewModel()
) {
    val festivals by viewModel.festivals.collectAsState()
    val language by viewModel.language.collectAsState()
    val festival = festivals.firstOrNull { it.first.festival.id == festivalId }?.first?.festival
    val occurrence = festivals.firstOrNull { it.first.festival.id == festivalId }?.first

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(festival?.displayName(language) ?: "") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.cd_go_back))
                    }
                }
            )
        }
    ) { padding ->
        if (festival == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header card
            SacredHighlightCard {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        when (festival.category) {
                            FestivalCategory.MAJOR -> Icons.Filled.Star
                            FestivalCategory.VRAT -> Icons.Filled.Spa
                            FestivalCategory.RECURRING -> Icons.Filled.Refresh
                            FestivalCategory.REGIONAL -> Icons.Filled.LocationOn
                            else -> Icons.Filled.Celebration
                        },
                        contentDescription = stringResource(R.string.cd_festival_type),
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        festival.displayName(language),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    festival.names["hi"]?.takeIf { language != AppLanguage.HINDI }?.let {
                        Text(
                            it,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AssistChip(
                            onClick = {},
                            label = { Text(festival.category.localizedName()) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                labelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                        if (festival.durationDays > 1) {
                            AssistChip(
                                onClick = {},
                                label = {
                                    Text("${festival.durationDays} ${stringResource(R.string.festival_days)}")
                                },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                    labelColor = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            )
                        }
                    }
                    // Date
                    occurrence?.let {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            it.date.toString(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Calendar Rule
            SacredCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.CalendarMonth,
                        contentDescription = stringResource(R.string.cd_festival_type),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        stringResource(R.string.festival_calendar_rule),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(Modifier.height(8.dp))
                // Show panchang info from the occurrence
                occurrence?.let { occ ->
                    val panchangPair = festivals.firstOrNull { it.first.festival.id == festivalId }
                    panchangPair?.let { (_, panchang) ->
                        Text(
                            "${stringResource(R.string.panchang_tithi)}: ${panchang.tithiInfo.name}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            "${stringResource(R.string.panchang_nakshatra)}: ${panchang.nakshatraInfo.name}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (festival.durationDays > 1) {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "${stringResource(R.string.festival_duration)}: ${festival.durationDays} ${stringResource(R.string.festival_days)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Description
            val description = festival.descriptionText(language)
            if (description.isNotEmpty()) {
                SacredCard {
                    Text(
                        description,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // Story section
            val story = festival.story(language)
            if (story.isNotEmpty()) {
                SacredCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.AutoStories,
                            contentDescription = stringResource(R.string.cd_festival_story),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            stringResource(R.string.festival_story),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        story,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // "Did You Know?" section (was Significance)
            val significance = festival.significance(language)
            if (significance.isNotEmpty()) {
                SacredHighlightCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.Lightbulb,
                            contentDescription = stringResource(R.string.cd_festival_significance),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            stringResource(R.string.festival_did_you_know),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        significance,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                LaunchedEffect(festivalId) {
                    gamificationViewModel.recordFestivalStoryRead(festivalId)
                }
            }

            // Tradition tags
            if (festival.traditions.isNotEmpty()) {
                SacredCard {
                    Text(
                        stringResource(R.string.festival_traditions),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        festival.traditions.forEach { tradition ->
                            AssistChip(
                                onClick = {},
                                label = { Text(tradition) }
                            )
                        }
                    }
                }
            }
        }
    }
}
