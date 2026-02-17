package dev.gopes.hinducalendar.ui.festivals

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import dev.gopes.hinducalendar.R
import dev.gopes.hinducalendar.data.model.FestivalCategory
import dev.gopes.hinducalendar.data.model.FestivalOccurrence
import dev.gopes.hinducalendar.ui.components.*
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FestivalListScreen(
    onFestivalClick: (String) -> Unit = {},
    viewModel: FestivalListViewModel = hiltViewModel()
) {
    val festivals by viewModel.festivals.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

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
            LazyColumn(
                modifier = Modifier.padding(padding),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(festivals, key = { it.first.id }) { (occurrence, _) ->
                    FestivalRow(
                        occurrence = occurrence,
                        onClick = { onFestivalClick(occurrence.festival.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun FestivalRow(occurrence: FestivalOccurrence, onClick: () -> Unit) {
    SacredCard(
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Date
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(50.dp)
            ) {
                Text(
                    occurrence.date.dayOfMonth.toString(),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    occurrence.date.format(DateTimeFormatter.ofPattern("MMM")),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.width(16.dp))

            // Festival info
            Column(Modifier.weight(1f)) {
                Text(
                    occurrence.festival.displayName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                occurrence.festival.names["hi"]?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
                Text(
                    occurrence.festival.category.displayName,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (occurrence.festival.category == FestivalCategory.MAJOR) {
                Icon(
                    Icons.Filled.Star,
                    contentDescription = stringResource(R.string.festival_major),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
