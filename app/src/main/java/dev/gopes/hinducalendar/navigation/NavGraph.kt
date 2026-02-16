package dev.gopes.hinducalendar.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dev.gopes.hinducalendar.ui.today.TodayPanchangScreen
import dev.gopes.hinducalendar.ui.calendar.CalendarScreen
import dev.gopes.hinducalendar.ui.festivals.FestivalListScreen
import dev.gopes.hinducalendar.ui.settings.SettingsScreen
import dev.gopes.hinducalendar.ui.texts.SacredTextsScreen

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    data object Today : Screen("today", "Today", Icons.Filled.WbSunny)
    data object Texts : Screen("texts", "Texts", Icons.Filled.MenuBook)
    data object Calendar : Screen("calendar", "Calendar", Icons.Filled.CalendarMonth)
    data object Festivals : Screen("festivals", "Festivals", Icons.Filled.Star)
    data object Settings : Screen("settings", "Settings", Icons.Filled.Settings)
}

private val screens = listOf(Screen.Today, Screen.Texts, Screen.Calendar, Screen.Festivals, Screen.Settings)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavGraph() {
    val navController = rememberNavController()
    val currentEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                screens.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
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
            composable(Screen.Today.route) { TodayPanchangScreen() }
            composable(Screen.Texts.route) { SacredTextsScreen() }
            composable(Screen.Calendar.route) { CalendarScreen() }
            composable(Screen.Festivals.route) { FestivalListScreen() }
            composable(Screen.Settings.route) { SettingsScreen() }
        }
    }
}
