package dev.gopes.hinducalendar.ui.calendar

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import dev.gopes.hinducalendar.R
import dev.gopes.hinducalendar.data.model.*
import dev.gopes.hinducalendar.ui.util.*
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(viewModel: CalendarViewModel = hiltViewModel()) {
    val displayedMonth by viewModel.displayedMonth.collectAsState()
    val selectedPanchang by viewModel.selectedPanchang.collectAsState()
    val language by viewModel.language.collectAsState()
    val monthFestivals by viewModel.monthFestivals.collectAsState()
    val refPanchangByDay by viewModel.refPanchangByDay.collectAsState()
    val festivalRef by viewModel.festivalDateReference.collectAsState()

    var dragDirection by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.tab_calendar)) }) }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            // ── PINNED: month nav + weekdays + grid ──
            Column {
                // Month navigation
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
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
                val firstDay = displayedMonth.atDay(1)
                val offset = firstDay.dayOfWeek.value % 7
                val days = displayedMonth.lengthOfMonth()
                val total = offset + days
                val rows = (total + 6) / 7

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height((rows * 44).dp)
                        .pointerInput(displayedMonth) {
                            detectHorizontalDragGestures(
                                onDragEnd = {
                                    if (dragDirection < 0) viewModel.previousMonth()
                                    else if (dragDirection > 0) viewModel.nextMonth()
                                    dragDirection = 0
                                },
                                onHorizontalDrag = { _, dragAmount ->
                                    dragDirection = if (dragAmount < 0) 1 else -1
                                }
                            )
                        }
                ) {
                    Crossfade(
                        targetState = displayedMonth,
                        label = "month_transition"
                    ) { month ->
                        val mFirstDay = month.atDay(1)
                        val mOffset = mFirstDay.dayOfWeek.value % 7
                        val mDays = month.lengthOfMonth()
                        val mTotal = mOffset + mDays
                        val mRows = (mTotal + 6) / 7

                        LazyVerticalGrid(
                            columns = GridCells.Fixed(7),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height((mRows * 44).dp)
                                .padding(horizontal = 8.dp),
                            contentPadding = PaddingValues(2.dp)
                        ) {
                            items(mTotal) { index ->
                                if (index < mOffset) {
                                    Spacer(Modifier.size(44.dp))
                                } else {
                                    val day = index - mOffset + 1
                                    val date = month.atDay(day)
                                    val isToday = date == LocalDate.now()
                                    val isSelected = selectedPanchang?.date == date
                                    val refFests = monthFestivals[day] ?: emptyList()

                                    DayCell(
                                        day = day,
                                        language = language,
                                        isToday = isToday,
                                        isSelected = isSelected,
                                        hasFestival = refFests.isNotEmpty(),
                                        hasMajorFestival = refFests.any { it.festival.category == FestivalCategory.MAJOR },
                                        onClick = { viewModel.selectDate(date) }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // ── DETAIL: fills remaining space, content top-aligned ──
            // When IST reference is selected, show Delhi panchang for consistency
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.TopStart) {
                selectedPanchang?.let { panchang ->
                    val detailPanchang = if (festivalRef == FestivalDateReference.INDIAN_STANDARD) {
                        refPanchangByDay[panchang.date.dayOfMonth] ?: panchang
                    } else panchang
                    DayDetailPanel(detailPanchang, language)
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    day: Int, language: AppLanguage, isToday: Boolean, isSelected: Boolean,
    hasFestival: Boolean, hasMajorFestival: Boolean, onClick: () -> Unit
) {
    val bgColor = when {
        isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
        isToday -> MaterialTheme.colorScheme.primary.copy(alpha = 0.06f)
        else -> Color.Transparent
    }

    Column(
        modifier = Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .then(
                if (isToday) Modifier.border(
                    1.dp,
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                    RoundedCornerShape(8.dp)
                ) else Modifier
            )
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            language.localizedNumber(day),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
            color = if (isToday || isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
        if (hasFestival) {
            Box(
                Modifier
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(
                        if (hasMajorFestival) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
            )
        } else {
            Spacer(Modifier.size(4.dp))
        }
    }
}

@Composable
private fun DayDetailPanel(panchang: PanchangDay, language: AppLanguage) {
    val timeFmt = DateTimeFormatter.ofPattern("h:mm a")

    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // ── Hindu date header: name (left) | year (right) ──
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    panchang.hinduDate.displayString,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    language.localizedDigits(panchang.date.format(DateTimeFormatter.ofPattern("EEE, MMM d, yyyy"))),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    panchang.hinduDate.yearDisplayForTradition(panchang.tradition),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        // ── Panchang table: label | value | time range ──
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            ElementRow(
                stringResource(R.string.panchang_tithi),
                localizedTithiName(panchang.tithiInfo.number),
                language.localizedDigits(panchang.tithiInfo.timeRangeString)
            )
            ElementRow(
                stringResource(R.string.panchang_nakshatra),
                localizedNakshatraName(panchang.nakshatraInfo.number),
                language.localizedDigits(panchang.nakshatraInfo.timeRangeString)
            )
            ElementRow(
                stringResource(R.string.panchang_yoga),
                localizedYogaName(panchang.yogaInfo.number),
                null
            )
            ElementRow(
                stringResource(R.string.panchang_karana),
                localizedKaranaName(panchang.karanaInfo.number),
                null
            )
        }

        // ── Sunrise / Sunset with icons ──
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Filled.WbSunny,
                contentDescription = stringResource(R.string.today_sunrise),
                tint = Color(0xFFE8A317),
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text(
                language.localizedDigits(panchang.sunrise.format(timeFmt)),
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFFE8A317)
            )
            Spacer(Modifier.width(32.dp))
            Icon(
                Icons.Filled.WbTwilight,
                contentDescription = stringResource(R.string.today_sunset),
                tint = Color(0xFFE86017),
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text(
                language.localizedDigits(panchang.sunset.format(timeFmt)),
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFFE86017)
            )
        }

        // ── Festivals ──
        if (panchang.festivals.isNotEmpty()) {
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            panchang.festivals.forEach { occurrence ->
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.Star,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        occurrence.festival.displayName(language),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun ElementRow(label: String, value: String, detail: String?) {
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(80.dp)
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        detail?.takeIf { it.isNotEmpty() }?.let {
            Text(
                it,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
