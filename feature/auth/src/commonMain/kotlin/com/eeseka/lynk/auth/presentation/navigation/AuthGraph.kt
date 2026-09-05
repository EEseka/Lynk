package com.eeseka.lynk.auth.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.eeseka.lynk.auth.presentation.AuthRoot

fun NavGraphBuilder.authGraph(
    navController: NavController,
    onNavigateToProfileSetup: () -> Unit,
    onNavigateToMain: () -> Unit,
) {
    navigation<AuthGraphRoutes.Graph>(
        startDestination = AuthGraphRoutes.Auth
    ) {
        composable<AuthGraphRoutes.Auth> {
            AuthRoot(
                navigateToProfileSetup = onNavigateToProfileSetup,
                navigateToMain = onNavigateToMain
            )
        }
    }
}