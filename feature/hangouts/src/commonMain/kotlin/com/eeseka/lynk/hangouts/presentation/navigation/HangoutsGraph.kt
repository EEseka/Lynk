package com.eeseka.lynk.hangouts.presentation.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.toRoute
import com.eeseka.lynk.hangouts.presentation.hangouts_list_detail.HangoutsListDetailAdaptiveLayout
import com.eeseka.lynk.hangouts.presentation.hangouts_list_detail.HangoutsListDetailViewModel
import org.koin.compose.viewmodel.koinViewModel

fun NavGraphBuilder.hangoutsGraph(
    navController: NavController,
    mainShellPadding: PaddingValues,
    onToggleNavigation: (Boolean) -> Unit
) {
    navigation<HangoutsGraphRoutes.Graph>(
        startDestination = HangoutsGraphRoutes.HangoutListDetail()
    ) {
        composable<HangoutsGraphRoutes.HangoutListDetail> { backStackEntry ->
            val route = backStackEntry.toRoute<HangoutsGraphRoutes.HangoutListDetail>()
            val viewModel = koinViewModel<HangoutsListDetailViewModel>()
            val state by viewModel.state.collectAsStateWithLifecycle()

            HangoutsListDetailAdaptiveLayout(
                initialHangoutId = route.hangoutId,
                sharedState = state,
                events = viewModel.events,
                onAction = viewModel::onAction,
                onToggleNavigation = onToggleNavigation,
                mainShellPadding = mainShellPadding
            )
        }
    }
}