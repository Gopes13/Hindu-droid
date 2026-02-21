package dev.gopes.hinducalendar.ui.onboarding

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.gopes.hinducalendar.R
import dev.gopes.hinducalendar.data.model.AppLanguage
import dev.gopes.hinducalendar.data.model.CalendarTradition
import dev.gopes.hinducalendar.data.model.DharmaPath
import dev.gopes.hinducalendar.data.model.HinduLocation
import dev.gopes.hinducalendar.ui.components.*
import dev.gopes.hinducalendar.ui.theme.*
import dev.gopes.hinducalendar.ui.util.*

@Composable
fun OnboardingScreen(onComplete: (CalendarTradition, HinduLocation, DharmaPath, AppLanguage) -> Unit) {
    var step by remember { mutableIntStateOf(0) }
    var selectedLanguage by remember { mutableStateOf(AppLanguage.ENGLISH) }
    var selectedTradition by remember { mutableStateOf(CalendarTradition.PURNIMANT) }
    var selectedLocation by remember { mutableStateOf(HinduLocation.DELHI) }
    var selectedDharmaPath by remember { mutableStateOf(DharmaPath.GENERAL) }

    Column(Modifier.fillMaxSize()) {
        // Back button + Progress dots (hidden on Welcome step)
        if (step != 1) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (step > 0) {
                    IconButton(onClick = { step-- }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_go_back)
                        )
                    }
                } else {
                    Spacer(Modifier.size(48.dp))
                }

                Spacer(Modifier.weight(1f))
                StepProgressDots(currentStep = step, totalSteps = 5)
                Spacer(Modifier.weight(1f))
                Spacer(Modifier.size(48.dp))
            }
        }

        // Animated step content
        AnimatedContent(
            targetState = step,
            transitionSpec = {
                if (targetState > initialState) {
                    slideInHorizontally { it } + fadeIn() togetherWith
                            slideOutHorizontally { -it } + fadeOut()
                } else {
                    slideInHorizontally { -it } + fadeIn() togetherWith
                            slideOutHorizontally { it } + fadeOut()
                }
            },
            modifier = Modifier.weight(1f),
            label = "onboarding_step"
        ) { currentStep ->
            when (currentStep) {
                0 -> LanguageStep(
                    selectedLanguage = selectedLanguage,
                    onSelect = { selectedLanguage = it },
                    onNext = { step = 1 }
                )
                1 -> WelcomeStep(onNext = { step = 2 })
                2 -> DharmaPathStep(
                    selectedPath = selectedDharmaPath,
                    onSelect = { selectedDharmaPath = it },
                    onNext = { step = 3 }
                )
                3 -> TraditionStep(
                    selectedTradition = selectedTradition,
                    onSelect = { selectedTradition = it },
                    onNext = { step = 4 }
                )
                4 -> LocationStep(
                    selectedLocation = selectedLocation,
                    onSelect = { selectedLocation = it },
                    onComplete = { onComplete(selectedTradition, selectedLocation, selectedDharmaPath, selectedLanguage) }
                )
            }
        }
    }
}

@Composable
private fun StepProgressDots(currentStep: Int, totalSteps: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        repeat(totalSteps) { index ->
            Box(
                modifier = Modifier
                    .size(if (index == currentStep) 10.dp else 8.dp)
                    .clip(CircleShape)
                    .background(
                        if (index <= currentStep) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                    )
            )
        }
    }
}

/**
 * Returns the tradition-specific accent color for each DharmaPath.
 */
private fun dharmaPathAccentColor(path: DharmaPath): Color {
    return when (path) {
        DharmaPath.GENERAL -> TraditionGeneral
        DharmaPath.VAISHNAV -> TraditionVaishnav
        DharmaPath.SHAIV -> TraditionShaiv
        DharmaPath.SHAKTA -> TraditionShakta
        DharmaPath.SMARTA -> TraditionSmarta
        DharmaPath.ISKCON -> TraditionISKCON
        DharmaPath.SWAMINARAYAN -> TraditionSwaminarayan
        DharmaPath.SIKH -> TraditionSikh
        DharmaPath.JAIN -> TraditionJain
    }
}

@Composable
private fun LanguageStep(
    selectedLanguage: AppLanguage,
    onSelect: (AppLanguage) -> Unit,
    onNext: () -> Unit
) {
    Column(Modifier.fillMaxSize().padding(24.dp)) {
        Text(
            stringResource(R.string.onboarding_choose_language),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            stringResource(R.string.onboarding_language_desc),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(16.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(AppLanguage.entries.toList()) { lang ->
                val isSelected = lang == selectedLanguage
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .selectable(
                            selected = isSelected,
                            onClick = { onSelect(lang) }
                        )
                        .then(
                            if (isSelected) Modifier.border(
                                2.dp,
                                MaterialTheme.colorScheme.primary,
                                RoundedCornerShape(12.dp)
                            ) else Modifier.border(
                                1.dp,
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                RoundedCornerShape(12.dp)
                            )
                        ),
                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 14.dp)
                    ) {
                        Text(
                            lang.nativeScriptName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            lang.displayName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        SacredButton(text = stringResource(R.string.onboarding_continue), onClick = onNext)
    }
}

@Composable
private fun WelcomeStep(onNext: () -> Unit) {
    val gradient = Brush.linearGradient(listOf(DeepSaffron, DivineGold))

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient)
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Om symbol instead of WbSunny icon
        Text(
            "\u0950",
            fontSize = 120.sp,
            fontFamily = FontFamily.Serif,
            color = Color.White
        )
        Spacer(Modifier.height(24.dp))
        Text(
            stringResource(R.string.app_name_display),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            "\u0939\u093F\u0928\u094D\u0926\u0942 \u092A\u0902\u091A\u093E\u0902\u0917",
            style = MaterialTheme.typography.titleLarge,
            color = Color.White.copy(alpha = 0.8f)
        )

        Spacer(Modifier.height(32.dp))

        FeatureItem(Icons.Filled.CalendarMonth, stringResource(R.string.onboarding_full_panchang))
        FeatureItem(Icons.Filled.Star, stringResource(R.string.onboarding_festival_reminders))
        FeatureItem(Icons.Filled.Event, stringResource(R.string.onboarding_sync_calendar))
        FeatureItem(Icons.Filled.LocationOn, stringResource(R.string.onboarding_location_timings))
        FeatureItem(Icons.Filled.MenuBook, stringResource(R.string.onboarding_daily_readings))

        Spacer(Modifier.height(48.dp))

        // White filled button on gradient background
        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = DeepSaffron
            )
        ) {
            Text(stringResource(R.string.onboarding_get_started), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun FeatureItem(icon: ImageVector, text: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(16.dp))
        Text(text, style = MaterialTheme.typography.bodyLarge, color = Color.White)
    }
}

@Composable
private fun DharmaPathStep(
    selectedPath: DharmaPath,
    onSelect: (DharmaPath) -> Unit,
    onNext: () -> Unit
) {
    Column(Modifier.fillMaxSize().padding(24.dp)) {
        Text(
            stringResource(R.string.onboarding_spiritual_path),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            stringResource(R.string.onboarding_path_desc),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(16.dp))

        LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(DharmaPath.entries) { path ->
                val accentColor = dharmaPathAccentColor(path)
                SacredCard(
                    modifier = Modifier.selectable(
                        selected = path == selectedPath,
                        onClick = { onSelect(path) }
                    ),
                    accentColor = accentColor,
                    isHighlighted = path == selectedPath
                ) {
                    Text(
                        path.localizedName(),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium,
                        color = if (path == selectedPath) accentColor else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        path.localizedDescription(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        stringResource(R.string.sacred_texts_available, path.availableTextIds.size),
                        style = MaterialTheme.typography.labelSmall,
                        color = accentColor.copy(alpha = 0.7f)
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        SacredButton(text = stringResource(R.string.onboarding_continue), onClick = onNext)
    }
}

@Composable
private fun TraditionStep(
    selectedTradition: CalendarTradition,
    onSelect: (CalendarTradition) -> Unit,
    onNext: () -> Unit
) {
    Column(Modifier.fillMaxSize().padding(24.dp)) {
        Text(stringResource(R.string.onboarding_select_tradition), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(
            stringResource(R.string.onboarding_tradition_desc),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(16.dp))

        LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(CalendarTradition.entries) { tradition ->
                SacredCard(
                    modifier = Modifier.selectable(
                        selected = tradition == selectedTradition,
                        onClick = { onSelect(tradition) }
                    ),
                    isHighlighted = tradition == selectedTradition
                ) {
                    Text(
                        tradition.localizedName(),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium,
                        color = if (tradition == selectedTradition) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        tradition.localizedDescription(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        SacredButton(text = stringResource(R.string.onboarding_continue), onClick = onNext)
    }
}

@Composable
private fun LocationStep(
    selectedLocation: HinduLocation,
    onSelect: (HinduLocation) -> Unit,
    onComplete: () -> Unit
) {
    Column(Modifier.fillMaxSize().padding(24.dp)) {
        Text(stringResource(R.string.onboarding_set_location), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(
            stringResource(R.string.onboarding_location_desc),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(16.dp))

        LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            items(HinduLocation.ALL_PRESETS) { location ->
                SacredCard(
                    modifier = Modifier.selectable(
                        selected = location == selectedLocation,
                        onClick = { onSelect(location) }
                    ),
                    isHighlighted = location == selectedLocation
                ) {
                    Text(
                        location.cityName ?: stringResource(R.string.onboarding_unknown_location),
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (location == selectedLocation) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        location.timeZoneId,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        SacredButton(text = stringResource(R.string.onboarding_start_using), onClick = onComplete)
    }
}
