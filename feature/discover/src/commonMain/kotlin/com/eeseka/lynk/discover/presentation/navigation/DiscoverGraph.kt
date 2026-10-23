package com.eeseka.lynk.discover.presentation.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.toRoute
import com.eeseka.lynk.assistant.presentation.AssistantRoot
import com.eeseka.lynk.discover.presentation.DiscoverRoot

fun NavGraphBuilder.discoverGraph(
    navController: NavController,
    mainShellPadding: PaddingValues,
    onNavigateToHangouts: (String) -> Unit
) {
    navigation<DiscoverGraphRoutes.Graph>(
        startDestination = DiscoverGraphRoutes.Discover
    ) {
        composable<DiscoverGraphRoutes.Discover> {
            DiscoverRoot(
                navigateToHangouts = onNavigateToHangouts,
                navigateToAssistant = { latitude, longitude, areaName ->
                    navController.navigate(DiscoverGraphRoutes.Assistant(latitude, longitude, areaName))
                },
                mainShellPadding = mainShellPadding
            )
        }

        composable<DiscoverGraphRoutes.Assistant> { backStackEntry ->
            val route = backStackEntry.toRoute<DiscoverGraphRoutes.Assistant>()

            AssistantRoot(
                hangoutId = null,
                latitude = route.latitude,
                longitude = route.longitude,
                areaName = route.areaName,
                navigateBack = { navController.navigateUp() }
            )
        }
    }
}
