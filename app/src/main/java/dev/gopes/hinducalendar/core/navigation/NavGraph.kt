package dev.gopes.hinducalendar.core.navigation

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import dev.gopes.hinducalendar.core.ui.components.GlassNavItem
import dev.gopes.hinducalendar.core.ui.components.GlassNavigationBar
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dev.gopes.hinducalendar.R
import dev.gopes.hinducalendar.core.ui.theme.AtmosphereEngine
import dev.gopes.hinducalendar.core.ui.theme.LocalAtmosphere
import dev.gopes.hinducalendar.domain.model.SacredTextType
import dev.gopes.hinducalendar.feature.today.TodayPanchangScreen
import dev.gopes.hinducalendar.feature.calendar.CalendarScreen
import dev.gopes.hinducalendar.feature.festivals.FestivalDetailScreen
import dev.gopes.hinducalendar.feature.festivals.FestivalListScreen
import dev.gopes.hinducalendar.feature.settings.SettingsScreen
import dev.gopes.hinducalendar.feature.texts.LibraryLandingScreen
import dev.gopes.hinducalendar.feature.texts.SacredTextsScreen
import dev.gopes.hinducalendar.feature.texts.bookmarks.BookmarksScreen
import dev.gopes.hinducalendar.feature.texts.reader.ChalisaReaderScreen
import dev.gopes.hinducalendar.feature.texts.reader.GenericReaderScreen
import dev.gopes.hinducalendar.feature.texts.reader.GitaReaderScreen
import dev.gopes.hinducalendar.feature.texts.reader.JapjiReaderScreen
import dev.gopes.hinducalendar.feature.gamification.SadhanaJourneyScreen
import dev.gopes.hinducalendar.feature.japa.JapaCounterScreen
import dev.gopes.hinducalendar.feature.diya.SacredDiyaScreen
import dev.gopes.hinducalendar.feature.kirtans.KirtanListScreen
import dev.gopes.hinducalendar.feature.kirtans.KirtanReaderScreen
import dev.gopes.hinducalendar.feature.sanskrit.SanskritLearnScreen
import dev.gopes.hinducalendar.feature.sanskrit.SanskritLessonScreen
import dev.gopes.hinducalendar.feature.sanskrit.SanskritVerseScreen
import dev.gopes.hinducalendar.feature.sanskrit.SanskritViewModel
import dev.gopes.hinducalendar.core.ui.AppViewModel
import dev.gopes.hinducalendar.core.ui.components.rememberErrorSnackbarState
import androidx.hilt.navigation.compose.hiltViewModel

sealed class Screen(val route: String, @StringRes val titleRes: Int, val icon: ImageVector) {
    data object Today : Screen("today", R.string.tab_today, Icons.Filled.WbSunny)
    data object Texts : Screen("texts", R.string.tab_texts, Icons.AutoMirrored.Filled.MenuBook)
    data object Calendar : Screen("calendar", R.string.tab_calendar, Icons.Filled.CalendarMonth)
    data object Festivals : Screen("festivals", R.string.tab_festivals, Icons.Filled.Star)
    data object Settings : Screen("settings", R.string.tab_settings, Icons.Filled.Settings)
}

private val screens = listOf(Screen.Today, Screen.Texts, Screen.Calendar, Screen.Festivals, Screen.Settings)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavGraph(windowSizeClass: WindowSizeClass? = null) {
    val navController = rememberNavController()
    val currentEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentEntry?.destination?.route

    val isMainScreen = screens.any { it.route == currentRoute }
        || currentRoute == "sacred_texts" || currentRoute == "kirtans"

    // Derive library section from current route for dynamic tab label/icon
    val librarySection = when {
        currentRoute == "sacred_texts" || currentRoute?.startsWith("reader/") == true
            || currentRoute == "bookmarks" || currentRoute?.startsWith("sanskrit") == true -> "texts"
        currentRoute == "kirtans" || currentRoute?.startsWith("kirtan/") == true -> "kirtans"
        else -> "landing"
    }

    val useNavRail = windowSizeClass?.widthSizeClass == WindowWidthSizeClass.Expanded

    // Atmospheric background behind entire app
    val atmosphere = LocalAtmosphere.current
    val atmosphereBrush = AtmosphereEngine.atmosphereBrush(atmosphere)

    // Shared nav item data used by both bottom bar and rail
    val navItems: List<GlassNavItem> = screens.map { screen ->
        val title = if (screen == Screen.Texts) {
            when (librarySection) {
                "texts" -> stringResource(R.string.tab_text)
                "kirtans" -> stringResource(R.string.tab_kirtan)
                else -> stringResource(R.string.tab_media)
            }
        } else {
            stringResource(screen.titleRes)
        }
        val icon = if (screen == Screen.Texts) {
            when (librarySection) {
                "texts" -> Icons.AutoMirrored.Filled.MenuBook
                "kirtans" -> Icons.Filled.MusicNote
                else -> Icons.Filled.VideoLibrary
            }
        } else {
            screen.icon
        }
        GlassNavItem(
            icon = icon,
            label = title,
            selected = currentRoute == screen.route
                || (screen == Screen.Texts && (currentRoute == "sacred_texts" || currentRoute == "kirtans")),
            onClick = {
                navController.navigate(screen.route) {
                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Layer 1: Atmospheric gradient (15% opacity)
        Box(
            Modifier
                .fillMaxSize()
                .alpha(0.15f)
                .background(atmosphereBrush)
        )
        // Layer 2: Surface color overlay (85% opacity)
        Box(
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background.copy(alpha = 0.85f))
        )
        // Layer 3: Content
        Row(Modifier.fillMaxSize()) {
            // NavigationRail for expanded (tablet/foldable) layouts
            if (useNavRail && isMainScreen) {
                NavigationRail(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
                ) {
                    navItems.forEach { item ->
                        NavigationRailItem(
                            selected = item.selected,
                            onClick = item.onClick,
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) }
                        )
                    }
                }
            }

            val appViewModel: AppViewModel = hiltViewModel()
            val snackbarHostState = rememberErrorSnackbarState(appViewModel.errors)

            Scaffold(
                containerColor = androidx.compose.ui.graphics.Color.Transparent,
                snackbarHost = { SnackbarHost(snackbarHostState) },
                bottomBar = {
                    if (!isMainScreen || useNavRail) return@Scaffold
                    GlassNavigationBar(items = navItems)
                }
            ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Screen.Today.route,
                modifier = Modifier.padding(innerPadding)
            ) {
            composable(Screen.Today.route) {
                TodayPanchangScreen(
                    onSadhanaClick = { navController.navigate("sadhana") },
                    onJapaClick = { navController.navigate("japa") },
                    onDiyaClick = { navController.navigate("diya") }
                )
            }
            composable(Screen.Texts.route) {
                LibraryLandingScreen(
                    onSelectTexts = { navController.navigate("sacred_texts") },
                    onSelectKirtans = { navController.navigate("kirtans") }
                )
            }
            composable("sacred_texts") {
                SacredTextsScreen(
                    onTextClick = { textType ->
                        navController.navigate("reader/${textType.name}")
                    },
                    onBookmarksClick = { navController.navigate("bookmarks") },
                    onSanskritClick = { navController.navigate("sanskrit") },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Calendar.route) { CalendarScreen() }
            composable(Screen.Festivals.route) {
                FestivalListScreen(
                    onFestivalClick = { festivalId ->
                        navController.navigate("festival/$festivalId")
                    }
                )
            }
            composable(Screen.Settings.route) {
                SettingsScreen(onSadhanaClick = { navController.navigate("sadhana") })
            }

            composable("bookmarks") {
                BookmarksScreen(onBack = { navController.popBackStack() })
            }

            composable("sadhana") {
                SadhanaJourneyScreen(onBack = { navController.popBackStack() })
            }

            composable("japa") {
                JapaCounterScreen(onBack = { navController.popBackStack() })
            }

            composable("diya") {
                SacredDiyaScreen(onBack = { navController.popBackStack() })
            }

            composable("kirtans") {
                KirtanListScreen(
                    onKirtanClick = { kirtanId -> navController.navigate("kirtan/$kirtanId") },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                route = "kirtan/{kirtanId}",
                arguments = listOf(navArgument("kirtanId") { type = NavType.StringType })
            ) { backStackEntry ->
                val kirtanId = backStackEntry.arguments?.getString("kirtanId") ?: ""
                KirtanReaderScreen(
                    kirtanId = kirtanId,
                    onBack = { navController.popBackStack() }
                )
            }

            navigation(startDestination = "sanskrit_home", route = "sanskrit") {
                composable("sanskrit_home") { entry ->
                    val parentEntry = remember(entry) {
                        navController.getBackStackEntry("sanskrit")
                    }
                    val sanskritViewModel: SanskritViewModel = hiltViewModel(parentEntry)
                    SanskritLearnScreen(
                        onLessonClick = { lessonId -> navController.navigate("sanskrit_lesson/$lessonId") },
                        onVerseClick = { navController.navigate("sanskrit_verses") },
                        onBack = { navController.popBackStack() },
                        viewModel = sanskritViewModel
                    )
                }

                composable(
                    route = "sanskrit_lesson/{lessonId}",
                    arguments = listOf(navArgument("lessonId") { type = NavType.StringType })
                ) { entry ->
                    val lessonId = entry.arguments?.getString("lessonId") ?: ""
                    val parentEntry = remember(entry) {
                        navController.getBackStackEntry("sanskrit")
                    }
                    val sanskritViewModel: SanskritViewModel = hiltViewModel(parentEntry)
                    SanskritLessonScreen(
                        lessonId = lessonId,
                        onComplete = { correct, total, letters ->
                            sanskritViewModel.recordLessonCompletion(lessonId, correct, total, letters)
                        },
                        onSpeak = { sanskritViewModel.speak(it) },
                        onBack = { navController.popBackStack() }
                    )
                }

                composable("sanskrit_verses") { entry ->
                    val parentEntry = remember(entry) {
                        navController.getBackStackEntry("sanskrit")
                    }
                    val sanskritViewModel: SanskritViewModel = hiltViewModel(parentEntry)
                    val uiState by sanskritViewModel.uiState.collectAsState()
                    SanskritVerseScreen(
                        progress = uiState.progress,
                        onVerseExplored = { sanskritViewModel.recordVerseExplored(it) },
                        onSpeak = { sanskritViewModel.speak(it) },
                        onBack = { navController.popBackStack() }
                    )
                }
            }

            composable(
                route = "festival/{festivalId}",
                arguments = listOf(navArgument("festivalId") { type = NavType.StringType })
            ) { backStackEntry ->
                val festivalId = backStackEntry.arguments?.getString("festivalId") ?: ""
                FestivalDetailScreen(
                    festivalId = festivalId,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                route = "reader/{textType}",
                arguments = listOf(navArgument("textType") { type = NavType.StringType })
            ) { backStackEntry ->
                val textTypeName = backStackEntry.arguments?.getString("textType")
                val textType = textTypeName?.let { name ->
                    SacredTextType.entries.find { it.name == name }
                }
                val onBack: () -> Unit = { navController.popBackStack() }
                when (textType) {
                    SacredTextType.GITA -> GitaReaderScreen(onBack = onBack)
                    SacredTextType.HANUMAN_CHALISA -> ChalisaReaderScreen(onBack = onBack)
                    SacredTextType.JAPJI_SAHIB -> JapjiReaderScreen(onBack = onBack)
                    else -> GenericReaderScreen(onBack = onBack)
                }
            }
        }
        } // Scaffold
        } // Row
    }
}
