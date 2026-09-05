package com.eeseka.lynk.hangouts.presentation.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.navDeepLink
import androidx.navigation.toRoute
import com.eeseka.lynk.hangouts.presentation.hangouts_list_detail.HangoutsListDetailAdaptiveLayout
import com.eeseka.lynk.hangouts.presentation.hangouts_list_detail.HangoutsListDetailViewModel
import com.eeseka.lynk.notifications.presentation.notifications.NotificationsRoot
import org.koin.compose.viewmodel.koinViewModel

fun NavGraphBuilder.hangoutsGraph(
    navController: NavController,
    mainShellPadding: PaddingValues,
    onDetailPaneFullScreenChange: (Boolean) -> Unit,
    unreadNotificationCount: Int
) {
    navigation<HangoutsGraphRoutes.Graph>(
        startDestination = HangoutsGraphRoutes.HangoutListDetail()
    ) {
        composable<HangoutsGraphRoutes.HangoutListDetail>(
            deepLinks = listOf(
                navDeepLink { uriPattern = "lynk://hangout_detail/{hangoutId}" }
            )
        ) { backStackEntry ->
            val route = backStackEntry.toRoute<HangoutsGraphRoutes.HangoutListDetail>()
            val viewModel = koinViewModel<HangoutsListDetailViewModel>()
            val state by viewModel.state.collectAsStateWithLifecycle()

            HangoutsListDetailAdaptiveLayout(
                initialHangoutId = route.hangoutId,
                sharedState = state,
                events = viewModel.events,
                onAction = viewModel::onAction,
                onDetailPaneFullScreenChange = onDetailPaneFullScreenChange,
                mainShellPadding = mainShellPadding,
                unreadNotificationCount = unreadNotificationCount,
                onNavigateToNotifications = {
                    navController.navigate(HangoutsGraphRoutes.Notifications())
                }
            )
        }

        composable<HangoutsGraphRoutes.Notifications>(
            deepLinks = listOf(
                navDeepLink { uriPattern = "lynk://notifications/{previewHangoutId}" }
            )
        ) { backStackEntry ->
            val route = backStackEntry.toRoute<HangoutsGraphRoutes.Notifications>()

            NotificationsRoot(
                previewHangoutId = route.previewHangoutId,
                navigateBack = { navController.navigateUp() },
                navigateToHangout = { hangoutId ->
                    navController.navigate(HangoutsGraphRoutes.HangoutListDetail(hangoutId)) {
                        popUpTo<HangoutsGraphRoutes.HangoutListDetail> { inclusive = true }
                    }
                }
            )
        }
    }
}