package com.eeseka.lynk.discover.presentation.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.eeseka.lynk.discover.presentation.DiscoverScreen

fun NavGraphBuilder.discoverGraph(
    navController: NavController,
    mainShellPadding: PaddingValues
) {
    navigation<DiscoverGraphRoutes.Graph>(
        startDestination = DiscoverGraphRoutes.Discover
    ) {
        composable<DiscoverGraphRoutes.Discover> {
            DiscoverScreen(
                mainShellPadding = mainShellPadding
            )
        }
    }
}