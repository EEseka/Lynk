package com.eeseka.lynk.auth.presentation.navigation

import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.eeseka.lynk.auth.presentation.AuthScreen
import com.eeseka.lynk.auth.presentation.AuthViewModel
import com.eeseka.lynk.shared.domain.auth.User
import org.koin.compose.viewmodel.koinViewModel

fun NavGraphBuilder.authGraph(
    navController: NavController,
    onAuthSuccess: () -> Unit
) {
    navigation<AuthGraphRoutes.Graph>(
        startDestination = AuthGraphRoutes.Auth
    ) {
        composable<AuthGraphRoutes.Auth> {
            val viewModel = koinViewModel<AuthViewModel>()
            val state by viewModel.state.collectAsStateWithLifecycle()

            AuthScreen(
                state = state,
                events = viewModel.events,
                onAction = viewModel::onAction,
                onAuthSuccess = { user ->
                    if (user is User.ProfileIncomplete) {
                        navController.navigate(AuthGraphRoutes.ProfileSetup)
                    } else {
                        onAuthSuccess()
                    }
                },
            )
        }

        composable<AuthGraphRoutes.ProfileSetup> {
            // TODO: Call your ProfileSetupScreen composable here
            // ProfileSetupScreen(
            //     onProfileComplete = {
            //         // Once they pick a username, send them to the main app!
            //         onAuthSuccess()
            //     }
            // )
        }
    }
}