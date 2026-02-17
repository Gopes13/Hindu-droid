package dev.gopes.hinducalendar.ui.festivals

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
import dev.gopes.hinducalendar.data.model.AppLanguage
import dev.gopes.hinducalendar.data.model.Festival
import dev.gopes.hinducalendar.data.model.FestivalCategory
import dev.gopes.hinducalendar.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FestivalDetailScreen(
    festivalId: String,
    onBack: () -> Unit,
    viewModel: FestivalListViewModel = hiltViewModel()
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
                            else -> Icons.Filled.Celebration
                        },
                        contentDescription = null,
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
                            label = { Text(festival.category.displayName) },
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
                            contentDescription = null,
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

            // Significance section
            val significance = festival.significance(language)
            if (significance.isNotEmpty()) {
                SacredCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.Lightbulb,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            stringResource(R.string.festival_significance),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        significance,
                        style = MaterialTheme.typography.bodyMedium
                    )
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
