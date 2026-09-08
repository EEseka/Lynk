package com.eeseka.lynk.profile.presentation.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.eeseka.lynk.profile.presentation.profile.ProfileRoot
import com.eeseka.lynk.profile.presentation.saved_spots.SavedSpotsRoot

fun NavGraphBuilder.profileGraph(
    navController: NavController,
    mainShellPadding: PaddingValues
) {
    navigation<ProfileGraphRoutes.Graph>(
        startDestination = ProfileGraphRoutes.Profile
    ) {
        composable<ProfileGraphRoutes.Profile> {
            ProfileRoot(
                navigateToSavedSpots = {
                    navController.navigate(ProfileGraphRoutes.SavedSpots)
                },
                mainShellPadding = mainShellPadding
            )
        }

        composable<ProfileGraphRoutes.SavedSpots> {
            SavedSpotsRoot(
                navigateBack = { navController.navigateUp() }
            )
        }
    }
}
