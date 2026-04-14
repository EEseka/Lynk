package com.eeseka.lynk.profile_setup.presentation.navigation

import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.eeseka.lynk.profile_setup.presentation.ProfileSetupScreen
import com.eeseka.lynk.profile_setup.presentation.ProfileSetupViewModel
import org.koin.compose.viewmodel.koinViewModel

fun NavGraphBuilder.profileSetupGraph(
    navController: NavController,
    onNavigateToMain: () -> Unit,
) {
    navigation<ProfileSetupGraphRoutes.Graph>(
        startDestination = ProfileSetupGraphRoutes.ProfileSetup
    ) {
        composable<ProfileSetupGraphRoutes.ProfileSetup> {
            val viewModel = koinViewModel<ProfileSetupViewModel>()
            val state by viewModel.state.collectAsStateWithLifecycle()

            ProfileSetupScreen(
                state = state,
                events = viewModel.events,
                onAction = viewModel::onAction,
                onProfileSetupComplete = { onNavigateToMain() }
            )
        }
    }
}
