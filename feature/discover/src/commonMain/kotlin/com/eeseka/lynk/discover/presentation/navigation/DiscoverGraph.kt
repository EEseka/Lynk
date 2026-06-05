package com.eeseka.lynk.discover.presentation.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.eeseka.lynk.discover.presentation.DiscoverScreen
import com.eeseka.lynk.discover.presentation.DiscoverViewModel
import org.koin.compose.viewmodel.koinViewModel

fun NavGraphBuilder.discoverGraph(
    navController: NavController,
    mainShellPadding: PaddingValues,
    navigateToHangouts: (String) -> Unit
) {
    navigation<DiscoverGraphRoutes.Graph>(
        startDestination = DiscoverGraphRoutes.Discover
    ) {
        composable<DiscoverGraphRoutes.Discover> {
            val viewModel = koinViewModel<DiscoverViewModel>()
            val state by viewModel.state.collectAsStateWithLifecycle()

            DiscoverScreen(
                state = state,
                events = viewModel.events,
                onAction = viewModel::onAction,
                navigateToHangouts = navigateToHangouts,
                mainShellPadding = mainShellPadding
            )
        }
    }
}