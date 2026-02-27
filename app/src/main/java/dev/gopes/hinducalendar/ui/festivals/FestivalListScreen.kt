package dev.gopes.hinducalendar.ui.festivals

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import dev.gopes.hinducalendar.R
import dev.gopes.hinducalendar.data.model.AppLanguage
import dev.gopes.hinducalendar.data.model.FestivalCategory
import dev.gopes.hinducalendar.data.model.FestivalOccurrence
import dev.gopes.hinducalendar.data.model.PanchangDay
import dev.gopes.hinducalendar.ui.components.*
import dev.gopes.hinducalendar.ui.theme.LocalVibrantMode
import dev.gopes.hinducalendar.ui.util.localizedName
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FestivalListScreen(
    onFestivalClick: (String) -> Unit = {},
    viewModel: FestivalListViewModel = hiltViewModel()
) {
    val festivals by viewModel.festivals.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val language by viewModel.language.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.festivals_upcoming)) }) }
    ) { padding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else if (festivals.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(
                    stringResource(R.string.no_upcoming_festivals),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            val isVibrant = LocalVibrantMode.current
            LazyColumn(
                modifier = Modifier.padding(padding),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                itemsIndexed(festivals, key = { _, item -> item.first.id }) { index, (occurrence, panchang) ->
                    Box(Modifier.entranceAnimation(index, isVibrant)) {
                        FestivalRow(
                            occurrence = occurrence,
                            panchang = panchang,
                            language = language,
                            onClick = { onFestivalClick(occurrence.festival.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FestivalRow(
    occurrence: FestivalOccurrence,
    panchang: PanchangDay,
    language: AppLanguage,
    onClick: () -> Unit
) {
    val daysAway = ChronoUnit.DAYS.between(LocalDate.now(), occurrence.date).toInt()

    SacredCard(
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            // Date column — localized number
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(50.dp)
            ) {
                Text(
                    language.localizedNumber(occurrence.date.dayOfMonth),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    occurrence.date.format(DateTimeFormatter.ofPattern("MMM")),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                // Hindu date (tithi)
                Text(
                    panchang.tithiInfo.name,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(Modifier.width(16.dp))

            // Festival info — use localized name
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        occurrence.festival.displayName(language),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(Modifier.width(8.dp))
                    CountdownBadge(daysAway, language)
                }
                // Category — localized
                Text(
                    occurrence.festival.category.localizedName(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                // Description preview
                val description = occurrence.festival.descriptionText(language)
                if (description.isNotEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(Modifier.width(4.dp))

            // Chevron
            Icon(
                Icons.Filled.ChevronRight,
                contentDescription = stringResource(R.string.cd_open),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .size(20.dp)
                    .align(Alignment.CenterVertically)
            )
        }
    }
}

@Composable
private fun CountdownBadge(daysAway: Int, language: AppLanguage) {
    val (text, containerColor, contentColor) = when (daysAway) {
        0 -> Triple(
            stringResource(R.string.countdown_today),
            MaterialTheme.colorScheme.tertiary,
            MaterialTheme.colorScheme.onTertiary
        )
        1 -> Triple(
            stringResource(R.string.countdown_tomorrow),
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.onPrimary
        )
        else -> Triple(
            stringResource(R.string.countdown_in_days, daysAway),
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    Surface(
        shape = RoundedCornerShape(50),
        color = containerColor
    ) {
        Text(
            language.localizedDigits(text),
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = contentColor,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 3.dp)
        )
    }
}
