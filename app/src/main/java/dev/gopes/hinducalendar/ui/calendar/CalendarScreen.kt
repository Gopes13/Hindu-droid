package dev.gopes.hinducalendar.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import dev.gopes.hinducalendar.R
import dev.gopes.hinducalendar.data.model.PanchangDay
import dev.gopes.hinducalendar.ui.components.*
import dev.gopes.hinducalendar.ui.theme.*
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(viewModel: CalendarViewModel = hiltViewModel()) {
    val displayedMonth by viewModel.displayedMonth.collectAsState()
    val monthPanchang by viewModel.monthPanchang.collectAsState()
    val selectedPanchang by viewModel.selectedPanchang.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.tab_calendar)) }) }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            // Month navigation
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.previousMonth() }) {
                    Icon(Icons.Filled.ChevronLeft, stringResource(R.string.cd_previous_month), tint = MaterialTheme.colorScheme.primary)
                }
                Text(
                    displayedMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                IconButton(onClick = { viewModel.nextMonth() }) {
                    Icon(Icons.Filled.ChevronRight, stringResource(R.string.cd_next_month), tint = MaterialTheme.colorScheme.primary)
                }
            }

            // Weekday headers
            Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
                DayOfWeek.entries.forEach { dow ->
                    Text(
                        dow.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Calendar grid
            val firstDayOfMonth = displayedMonth.atDay(1)
            val startOffset = (firstDayOfMonth.dayOfWeek.value % 7)
            val daysInMonth = displayedMonth.lengthOfMonth()
            val totalCells = startOffset + daysInMonth

            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                modifier = Modifier.fillMaxWidth().height(280.dp).padding(horizontal = 8.dp),
                contentPadding = PaddingValues(4.dp)
            ) {
                items(totalCells) { index ->
                    if (index < startOffset) {
                        Spacer(Modifier.size(40.dp))
                    } else {
                        val day = index - startOffset + 1
                        val date = displayedMonth.atDay(day)
                        val panchang = monthPanchang.find { it.date == date }
                        val isToday = date == LocalDate.now()
                        val isSelected = selectedPanchang?.date == date

                        DayCell(
                            day = day,
                            isToday = isToday,
                            isSelected = isSelected,
                            hasFestival = panchang?.hasFestivals == true,
                            hasMajorFestival = panchang?.hasMajorFestival == true,
                            onClick = { viewModel.selectDate(date) }
                        )
                    }
                }
            }

            Divider(color = MaterialTheme.colorScheme.outline)

            // Selected day detail
            selectedPanchang?.let { panchang ->
                DayDetailPanel(panchang)
            }
        }
    }
}

@Composable
private fun DayCell(
    day: Int, isToday: Boolean, isSelected: Boolean,
    hasFestival: Boolean, hasMajorFestival: Boolean, onClick: () -> Unit
) {
    val bgColor = when {
        isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
        isToday -> MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
        else -> MaterialTheme.colorScheme.surface
    }

    Column(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .then(
                if (isToday) Modifier.border(
                    1.dp,
                    MaterialTheme.colorScheme.primary,
                    RoundedCornerShape(8.dp)
                ) else Modifier
            )
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "$day",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
            color = if (isToday || isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
        if (hasFestival) {
            Box(
                Modifier
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
    }
}

@Composable
private fun DayDetailPanel(panchang: PanchangDay) {
    Column(
        Modifier.fillMaxWidth().padding(16.dp),
    ) {
        SacredCard {
            Text(
                panchang.hinduDate.displayString,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                panchang.date.format(DateTimeFormatter.ofPattern("EEE, MMM d, yyyy")),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(8.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("${stringResource(R.string.panchang_tithi)}: ${panchang.tithiInfo.name}", style = MaterialTheme.typography.bodySmall)
                Text("${stringResource(R.string.panchang_nakshatra)}: ${panchang.nakshatraInfo.name}", style = MaterialTheme.typography.bodySmall)
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("${stringResource(R.string.panchang_yoga)}: ${panchang.yogaInfo.name}", style = MaterialTheme.typography.bodySmall)
                Text("${stringResource(R.string.panchang_karana)}: ${panchang.karanaInfo.name}", style = MaterialTheme.typography.bodySmall)
            }

            val timeFmt = DateTimeFormatter.ofPattern("h:mm a")
            Text(
                "${stringResource(R.string.today_sunrise)}: ${panchang.sunrise.format(timeFmt)}  |  ${stringResource(R.string.today_sunset)}: ${panchang.sunset.format(timeFmt)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            panchang.festivals.forEach { occurrence ->
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.Star,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        occurrence.festival.displayName,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
