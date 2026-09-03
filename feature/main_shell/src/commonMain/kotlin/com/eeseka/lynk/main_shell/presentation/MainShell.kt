package com.eeseka.lynk.main_shell.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.eeseka.lynk.discover.presentation.navigation.DiscoverGraphRoutes
import com.eeseka.lynk.discover.presentation.navigation.discoverGraph
import com.eeseka.lynk.hangouts.presentation.navigation.HangoutsGraphRoutes
import com.eeseka.lynk.hangouts.presentation.navigation.hangoutsGraph
import com.eeseka.lynk.main_shell.domain.LynkNavigationItem
import com.eeseka.lynk.main_shell.presentation.components.LynkBottomBar
import com.eeseka.lynk.main_shell.presentation.components.LynkNavigationRail
import com.eeseka.lynk.profile.presentation.navigation.ProfileGraphRoutes
import com.eeseka.lynk.profile.presentation.navigation.profileGraph
import com.eeseka.lynk.shared.design_system.components.layouts.LynkScaffold
import com.eeseka.lynk.shared.domain.util.PlatformUtils.isIOS
import com.eeseka.lynk.shared.presentation.navigation.DeepLinkListener
import com.eeseka.lynk.shared.presentation.util.currentDeviceConfiguration
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun MainShell() {
    val innerNavController = rememberNavController()
    val navBackStackEntry by innerNavController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val viewModel = koinViewModel<MainShellViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(currentDestination) {
        viewModel.onAction(MainShellAction.RefreshUnreadCount)
    }

    val showRail = currentDeviceConfiguration().isWideScreen

    // The hangout detail is the only screen whose bar visibility depends on the layout rather
    // than on which route is open, so it is the only one that still reports up to us.
    var isDetailPaneFullScreen by remember { mutableStateOf(false) }

    // Routes that take over the whole screen and so hide the bottom bar.
    val isFullScreenRoute = remember(currentDestination) {
        currentDestination?.hierarchy?.any {
            it.hasRoute(HangoutsGraphRoutes.Notifications::class) || it.hasRoute(ProfileGraphRoutes.SavedSpots::class)
        } == true
    }

    val isNavigationBarVisible = !isFullScreenRoute && !isDetailPaneFullScreen

    val selectedItem = remember(currentDestination) {
        when {
            currentDestination?.hierarchy?.any { it.hasRoute(HangoutsGraphRoutes.Graph::class) } == true -> LynkNavigationItem.HANGOUTS
            currentDestination?.hierarchy?.any { it.hasRoute(ProfileGraphRoutes.Graph::class) } == true -> LynkNavigationItem.PROFILE
            else -> LynkNavigationItem.DISCOVER
        }
    }

    // Arriving on the Hangouts tab is what clears the dot
    LaunchedEffect(selectedItem) {
        if (selectedItem == LynkNavigationItem.HANGOUTS) {
            viewModel.onAction(MainShellAction.HangoutsTabSeen)
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
        applyHorizontalInsets = false,
        bottomBar = {
            if (!showRail) {
                // Can't animate Native IOS components with compose
                AnimatedVisibility(
                    visible = isNavigationBarVisible,
                    enter = if (isIOS()) EnterTransition.None else slideInVertically { it } + fadeIn(),
                    exit = if (isIOS()) ExitTransition.None else slideOutVertically { it } + fadeOut()
                ) {
                    LynkBottomBar(
                        selectedItem = selectedItem,
                        onItemSelected = onNavigate,
                        hasUnseenNotifications = state.hasUnseenNotifications
                    )
                }
            }
        }
    ) { paddingValues ->
        if (showRail) {
            Row(modifier = Modifier.fillMaxSize()) {
                // This uses M3 NavRail and no Native IOS component so it can be animated
                AnimatedVisibility(
                    visible = isNavigationBarVisible,
                    enter = expandHorizontally() + fadeIn(),
                    exit = shrinkHorizontally() + fadeOut()
                ) {
                    LynkNavigationRail(
                        selectedItem = selectedItem,
                        onItemSelected = onNavigate,
                        hasUnseenNotifications = state.hasUnseenNotifications
                    )
                }

                MainShellNavHost(
                    navController = innerNavController,
                    paddingValues = PaddingValues(0.dp),
                    onDetailPaneFullScreenChange = { isDetailPaneFullScreen = it },
                    unreadNotificationCount = state.unreadNotificationCount,
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )
            }
        } else {
            MainShellNavHost(
                navController = innerNavController,
                paddingValues = paddingValues,
                onDetailPaneFullScreenChange = { isDetailPaneFullScreen = it },
                unreadNotificationCount = state.unreadNotificationCount,
                modifier = Modifier.fillMaxSize()
            )
        }

        DeepLinkListener(navController = innerNavController)
    }
}

@Composable
private fun MainShellNavHost(
    navController: NavHostController,
    paddingValues: PaddingValues,
    onDetailPaneFullScreenChange: (Boolean) -> Unit,
    unreadNotificationCount: Int,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = DiscoverGraphRoutes.Graph,
        modifier = modifier,
        enterTransition = {
            if (isIOS()) {
                EnterTransition.None
            } else {
                val fadeThrough = tween<Float>(durationMillis = 210, delayMillis = 90)
                fadeIn(fadeThrough) + scaleIn(fadeThrough, initialScale = 0.92f)
            }
        },
        exitTransition = {
            if (isIOS()) ExitTransition.None else fadeOut(tween(durationMillis = 90))
        }
    ) {
        discoverGraph(
            navController = navController,
            mainShellPadding = paddingValues,
            navigateToHangouts = { hangoutId ->
                navController.navigate(HangoutsGraphRoutes.HangoutListDetail(hangoutId)) {
                    popUpTo<HangoutsGraphRoutes.HangoutListDetail> { inclusive = true }
                    launchSingleTop = true
                }
            }
        )
        hangoutsGraph(
            navController = navController,
            mainShellPadding = paddingValues,
            onDetailPaneFullScreenChange = onDetailPaneFullScreenChange,
            unreadNotificationCount = unreadNotificationCount
        )
        profileGraph(
            navController = navController,
            mainShellPadding = paddingValues
        )
    }
}