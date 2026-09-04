package com.eeseka.lynk.profile_setup.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.eeseka.lynk.profile_setup.presentation.ProfileSetupRoot

fun NavGraphBuilder.profileSetupGraph(
    navController: NavController,
    onNavigateToMain: () -> Unit
) {
    navigation<ProfileSetupGraphRoutes.Graph>(
        startDestination = ProfileSetupGraphRoutes.ProfileSetup
    ) {
        composable<ProfileSetupGraphRoutes.ProfileSetup> {
            ProfileSetupRoot(onNavigateToMain = onNavigateToMain)
        }
    }
}
