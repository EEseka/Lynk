package com.eeseka.lynk.profile.presentation.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.eeseka.lynk.profile.presentation.profile.ProfileScreen
import com.eeseka.lynk.profile.presentation.profile.ProfileViewModel
import com.eeseka.lynk.profile.presentation.saved_spots.SavedSpotsScreen
import com.eeseka.lynk.profile.presentation.saved_spots.SavedSpotsViewModel
import org.koin.compose.viewmodel.koinViewModel

fun NavGraphBuilder.profileGraph(
    navController: NavController,
    mainShellPadding: PaddingValues
) {
    navigation<ProfileGraphRoutes.Graph>(
        startDestination = ProfileGraphRoutes.Profile
    ) {
        composable<ProfileGraphRoutes.Profile> {
            val viewModel = koinViewModel<ProfileViewModel>()
            val state by viewModel.state.collectAsStateWithLifecycle()

            ProfileScreen(
                state = state,
                events = viewModel.events,
                onAction = viewModel::onAction,
                onNavigateToSavedSpots = {
                    navController.navigate(ProfileGraphRoutes.SavedSpots)
                },
                mainShellPadding = mainShellPadding
            )
        }

        composable<ProfileGraphRoutes.SavedSpots> {
            val viewModel = koinViewModel<SavedSpotsViewModel>()
            val state by viewModel.state.collectAsStateWithLifecycle()

            SavedSpotsScreen(
                state = state,
                events = viewModel.events,
                onAction = viewModel::onAction,
                onNavigateBack = { navController.navigateUp() }
            )
        }
    }
}
