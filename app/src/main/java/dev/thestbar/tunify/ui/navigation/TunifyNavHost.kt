package dev.thestbar.tunify.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dev.thestbar.tunify.data.viewmodels.ThemeViewModel
import dev.thestbar.tunify.data.viewmodels.TunerViewModel
import dev.thestbar.tunify.data.viewmodels.TuningViewModel
import dev.thestbar.tunify.ui.screens.InfoScreen
import dev.thestbar.tunify.ui.screens.SettingsScreen
import dev.thestbar.tunify.ui.screens.TunerScreen
import dev.thestbar.tunify.ui.screens.TuningsScreen

private sealed class Screen(val route: String, val label: String) {
    object Tuner    : Screen("tuner",    "Tuner")
    object Tunings  : Screen("tunings",  "Tunings")
    object Settings : Screen("settings", "Settings")
    object Info     : Screen("info",     "Info")
}

@Composable
fun TunifyNavHost(
    tunerViewModel: TunerViewModel,
    tuningViewModel: TuningViewModel,
    themeViewModel: ThemeViewModel,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination

    val screens = listOf(Screen.Tuner, Screen.Tunings, Screen.Settings, Screen.Info)

    Scaffold(
        bottomBar = {
            NavigationBar {
                screens.forEach { screen ->
                    val selected = currentRoute?.hierarchy?.any { it.route == screen.route } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = when (screen) {
                                    is Screen.Tuner    -> Icons.Filled.MusicNote
                                    is Screen.Tunings  -> Icons.Filled.List
                                    is Screen.Settings -> Icons.Filled.Settings
                                    is Screen.Info     -> Icons.Filled.Info
                                },
                                contentDescription = screen.label
                            )
                        },
                        label = { Text(screen.label) }
                    )
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Tuner.route,
        ) {
            composable(Screen.Tuner.route) {
                val state by tunerViewModel.uiState.collectAsStateWithLifecycle()
                TunerScreen(
                    state = state,
                    onToggleTuning = { tunerViewModel.setIsTuning(!state.isTuning) },
                    modifier = Modifier.padding(innerPadding)
                )
            }
            composable(Screen.Tunings.route) {
                val tunings by tuningViewModel.filteredTunings.collectAsStateWithLifecycle()
                val sortOrder by tuningViewModel.sortOrder.collectAsStateWithLifecycle()
                val selectedTuningId by tunerViewModel.selectedTuningId.collectAsStateWithLifecycle()
                TuningsScreen(
                    tunings = tunings,
                    selectedTuningId = selectedTuningId,
                    sortOrder = sortOrder,
                    onSearchQueryChange = tuningViewModel::setSearchQuery,
                    onSortOrderChange = tuningViewModel::setSortOrder,
                    onSelectTuning = tunerViewModel::selectTuning,
                    onDeleteTuning = tuningViewModel::delete,
                    onSaveTuning = tuningViewModel::insert,
                    modifier = Modifier.padding(innerPadding)
                )
            }
            composable(Screen.Settings.route) {
                val themePref by themeViewModel.themePreference.collectAsStateWithLifecycle()
                val isTunerLocked by themeViewModel.isTunerLocked.collectAsStateWithLifecycle()
                val isLoadLastMutedState by themeViewModel.isLoadLastMutedState.collectAsStateWithLifecycle()
                SettingsScreen(
                    themePreference = themePref,
                    isTunerLocked = isTunerLocked,
                    isLoadLastMutedState = isLoadLastMutedState,
                    onThemeChange = themeViewModel::setThemePreference,
                    onTunerLockedChange = themeViewModel::setIsTunerLocked,
                    onLoadLastMutedStateChange = themeViewModel::setIsLoadLastMutedState,
                    onResetDatabase = tuningViewModel::resetToDefaults,
                    modifier = Modifier.padding(innerPadding)
                )
            }
            composable(Screen.Info.route) {
                InfoScreen(
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}
