package com.eeseka.lynk.hangouts.presentation.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.navDeepLink
import androidx.navigation.toRoute
import com.eeseka.lynk.hangouts.presentation.hangouts_list_detail.HangoutsListDetailAdaptiveLayout
import com.eeseka.lynk.notifications.presentation.notifications.NotificationsRoot

fun NavGraphBuilder.hangoutsGraph(
    navController: NavController,
    mainShellPadding: PaddingValues,
    onDetailPaneFullScreenChanged: (Boolean) -> Unit,
    unreadNotificationCount: Int
) {
    navigation<HangoutsGraphRoutes.Graph>(
        startDestination = HangoutsGraphRoutes.HangoutsListDetail()
    ) {
        composable<HangoutsGraphRoutes.HangoutsListDetail>(
            deepLinks = listOf(
                navDeepLink { uriPattern = "lynk://hangout_detail/{hangoutId}" }
            )
        ) { backStackEntry ->
            val route = backStackEntry.toRoute<HangoutsGraphRoutes.HangoutsListDetail>()

            HangoutsListDetailAdaptiveLayout(
                initialHangoutId = route.hangoutId,
                onDetailPaneFullScreenChanged = onDetailPaneFullScreenChanged,
                mainShellPadding = mainShellPadding,
                unreadNotificationCount = unreadNotificationCount,
                navigateToNotifications = {
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
                    navController.navigate(HangoutsGraphRoutes.HangoutsListDetail(hangoutId)) {
                        popUpTo<HangoutsGraphRoutes.HangoutsListDetail> { inclusive = true }
                    }
                }
            )
        }
    }
}
