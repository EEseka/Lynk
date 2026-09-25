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
import androidx.compose.runtime.remember
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
import com.eeseka.lynk.main_shell.presentation.components.LynkBottomBar
import com.eeseka.lynk.main_shell.presentation.components.LynkNavigationRail
import com.eeseka.lynk.main_shell.presentation.model.LynkNavigationItem
import com.eeseka.lynk.profile.presentation.navigation.ProfileGraphRoutes
import com.eeseka.lynk.profile.presentation.navigation.profileGraph
import com.eeseka.lynk.shared.design_system.components.layouts.LynkScaffold
import com.eeseka.lynk.shared.domain.util.PlatformUtils.isIOS
import com.eeseka.lynk.shared.presentation.navigation.DeepLinkListener
import com.eeseka.lynk.shared.presentation.permissions.NotificationPermissionEffect
import com.eeseka.lynk.shared.presentation.permissions.PermissionState
import com.eeseka.lynk.shared.presentation.util.currentDeviceConfiguration
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun MainShellRoot(
    viewModel: MainShellViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    MainShellScreen(
        state = state,
        onAction = viewModel::onAction
    )
}

@Composable
fun MainShellScreen(
    state: MainShellState,
    onAction: (MainShellAction) -> Unit
) {
    val innerNavController = rememberNavController()
    val navBackStackEntry by innerNavController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val showRail = currentDeviceConfiguration().isWideScreen

    // Routes that take over the whole screen and so hide the nav bar. The hangout detail is
    // the only screen whose bar visibility depends on the layout rather than on which route is
    // open, so it is the only one that still reports up to us.
    val isFullScreenRoute = remember(currentDestination) {
        currentDestination?.hierarchy?.any {
            it.hasRoute(HangoutsGraphRoutes.Notifications::class) || it.hasRoute(ProfileGraphRoutes.SavedSpots::class)
        } == true
    }

    val isNavigationBarVisible = !isFullScreenRoute && !state.isHangoutDetailPaneFullScreen

    val selectedItem = remember(currentDestination) {
        when {
            currentDestination?.hierarchy?.any { it.hasRoute(HangoutsGraphRoutes.Graph::class) } == true -> LynkNavigationItem.HANGOUTS
            currentDestination?.hierarchy?.any { it.hasRoute(ProfileGraphRoutes.Graph::class) } == true -> LynkNavigationItem.PROFILE
            else -> LynkNavigationItem.DISCOVER
        }
    }

    LaunchedEffect(selectedItem) {
        onAction(MainShellAction.OnHangoutsTabActiveChanged(isActive = selectedItem == LynkNavigationItem.HANGOUTS))
    }

    NotificationPermissionEffect(isEnabled = state.canReceiveNotifications) { permissionState ->
        if (permissionState != PermissionState.GRANTED) {
            onAction(MainShellAction.OnNotificationPermissionDenied)
        }
    }

    // Common navigation action passed to both Rail and BottomBar
    val onNavigate: (LynkNavigationItem) -> Unit = { item ->
        val startDestinationId = innerNavController.graph.findStartDestination().id

        if (item == LynkNavigationItem.DISCOVER) {
            innerNavController.popBackStack(startDestinationId, inclusive = false, saveState = true)
        } else {
            innerNavController.navigate(item.route) {
                popUpTo(startDestinationId) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
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
                    onHangoutDetailPaneFullScreenChanged = {
                        onAction(MainShellAction.OnHangoutDetailPaneFullScreenChanged(it))
                    },
                    unreadNotificationCount = state.unreadNotificationCount,
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )
            }
        } else {
            MainShellNavHost(
                navController = innerNavController,
                paddingValues = paddingValues,
                onHangoutDetailPaneFullScreenChanged = {
                    onAction(MainShellAction.OnHangoutDetailPaneFullScreenChanged(it))
                },
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
    onHangoutDetailPaneFullScreenChanged: (Boolean) -> Unit,
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
            onNavigateToHangouts = { hangoutId ->
                navController.navigate(HangoutsGraphRoutes.HangoutsListDetail(hangoutId)) {
                    popUpTo<HangoutsGraphRoutes.HangoutsListDetail> { inclusive = true }
                }
            }
        )
        hangoutsGraph(
            navController = navController,
            mainShellPadding = paddingValues,
            onDetailPaneFullScreenChanged = onHangoutDetailPaneFullScreenChanged,
            unreadNotificationCount = unreadNotificationCount
        )
        profileGraph(
            navController = navController,
            mainShellPadding = paddingValues
        )
    }
}