package dev.gopes.hinducalendar.navigation

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dev.gopes.hinducalendar.R
import dev.gopes.hinducalendar.data.model.SacredTextType
import dev.gopes.hinducalendar.ui.today.TodayPanchangScreen
import dev.gopes.hinducalendar.ui.calendar.CalendarScreen
import dev.gopes.hinducalendar.ui.festivals.FestivalDetailScreen
import dev.gopes.hinducalendar.ui.festivals.FestivalListScreen
import dev.gopes.hinducalendar.ui.settings.SettingsScreen
import dev.gopes.hinducalendar.ui.texts.SacredTextsScreen
import dev.gopes.hinducalendar.ui.texts.bookmarks.BookmarksScreen
import dev.gopes.hinducalendar.ui.texts.reader.ChalisaReaderScreen
import dev.gopes.hinducalendar.ui.texts.reader.GenericReaderScreen
import dev.gopes.hinducalendar.ui.texts.reader.GitaReaderScreen
import dev.gopes.hinducalendar.ui.texts.reader.JapjiReaderScreen
import dev.gopes.hinducalendar.ui.gamification.SadhanaJourneyScreen
import dev.gopes.hinducalendar.ui.japa.JapaCounterScreen
import dev.gopes.hinducalendar.ui.diya.SacredDiyaScreen
import dev.gopes.hinducalendar.ui.kirtans.KirtanListScreen
import dev.gopes.hinducalendar.ui.kirtans.KirtanReaderScreen

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
fun NavGraph() {
    val navController = rememberNavController()
    val currentEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentEntry?.destination?.route

    val isMainScreen = screens.any { it.route == currentRoute }

    Scaffold(
        bottomBar = {
            if (!isMainScreen) return@Scaffold
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                screens.forEach { screen ->
                    val title = stringResource(screen.titleRes)
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = title) },
                        label = { Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                        selected = currentRoute == screen.route,
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
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
                SacredTextsScreen(
                    onTextClick = { textType ->
                        navController.navigate("reader/${textType.name}")
                    },
                    onBookmarksClick = { navController.navigate("bookmarks") },
                    onKirtansClick = { navController.navigate("kirtans") }
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
    }
}
