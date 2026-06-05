package com.eeseka.lynk.main_shell.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.eeseka.lynk.discover.presentation.navigation.DiscoverGraphRoutes
import com.eeseka.lynk.discover.presentation.navigation.discoverGraph
import com.eeseka.lynk.hangouts.presentation.navigation.HangoutsGraphRoutes
import com.eeseka.lynk.hangouts.presentation.navigation.hangoutsGraph
import com.eeseka.lynk.main_shell.domain.LynkNavigationItem
import com.eeseka.lynk.main_shell.presentation.components.LynkBottomBar
import com.eeseka.lynk.main_shell.presentation.components.LynkNavigationRail
import com.eeseka.lynk.main_shell.presentation.navigation.ProfileRoute
import com.eeseka.lynk.shared.design_system.components.layouts.LynkScaffold
import com.eeseka.lynk.shared.presentation.util.DeviceConfiguration
import com.eeseka.lynk.shared.presentation.util.currentDeviceConfiguration

@Composable
fun MainShell() {
    val innerNavController = rememberNavController()
    val navBackStackEntry by innerNavController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val config = currentDeviceConfiguration()

    val showRail = config.isWideScreen || config == DeviceConfiguration.MOBILE_LANDSCAPE

    // Controls visibility of the navigation components (bottom bar and nav rail)
    var isNavigationVisible by remember { mutableStateOf(true) }

    val selectedItem = remember(currentDestination) {
        when {
            currentDestination?.hierarchy?.any { it.hasRoute(HangoutsGraphRoutes.Graph::class) } == true -> LynkNavigationItem.HANGOUTS
            currentDestination?.hierarchy?.any { it.hasRoute(ProfileRoute::class) } == true -> LynkNavigationItem.PROFILE
            else -> LynkNavigationItem.DISCOVER
        }
    }

    // Common navigation action passed to both Rail and BottomBar
    val onNavigate: (LynkNavigationItem) -> Unit = { item ->
        innerNavController.navigate(item.route) {
            popUpTo(innerNavController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    LynkScaffold(
        bottomBar = {
            if (!showRail) {
                AnimatedVisibility(
                    visible = isNavigationVisible,
                    enter = slideInVertically { it } + fadeIn(),
                    exit = slideOutVertically { it } + fadeOut()
                ) {
                    LynkBottomBar(
                        selectedItem = selectedItem,
                        onItemSelected = onNavigate
                    )
                }
            }
        }
    ) { paddingValues ->
        if (showRail) {
            Row(modifier = Modifier.fillMaxSize()) {
                AnimatedVisibility(
                    visible = isNavigationVisible,
                    enter = slideInHorizontally { -it } + fadeIn(),
                    exit = slideOutHorizontally { -it } + fadeOut()
                ) {
                    LynkNavigationRail(
                        selectedItem = selectedItem,
                        onItemSelected = onNavigate
                    )
                }

                MainShellNavHost(
                    navController = innerNavController,
                    paddingValues = PaddingValues(0.dp),
                    onToggleNavigation = { isNavigationVisible = it },
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )
            }
        } else {
            MainShellNavHost(
                navController = innerNavController,
                paddingValues = paddingValues,
                onToggleNavigation = { isNavigationVisible = it },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun MainShellNavHost(
    navController: NavHostController,
    paddingValues: PaddingValues,
    onToggleNavigation: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = DiscoverGraphRoutes.Graph,
        modifier = modifier,
        enterTransition = { fadeIn() },
        exitTransition = { fadeOut() },
        popEnterTransition = { fadeIn() },
        popExitTransition = { fadeOut() }
    ) {
        discoverGraph(
            navController = navController,
            mainShellPadding = paddingValues,
            navigateToHangouts = { hangoutId ->
                navController.navigate(HangoutsGraphRoutes.HangoutListDetail(hangoutId))
            }
        )
        hangoutsGraph(
            navController = navController,
            mainShellPadding = paddingValues
        )
        composable<ProfileRoute> {
            // Temporary Placeholder
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Profile Screen")
            }
        }
    }
}