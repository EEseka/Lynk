package com.eeseka.lynk.onboarding.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.eeseka.lynk.onboarding.presentation.OnboardingRoot

fun NavGraphBuilder.onboardingGraph(
    navController: NavController,
    onNavigateToAuth: () -> Unit
) {
    navigation<OnboardingGraphRoutes.Graph>(
        startDestination = OnboardingGraphRoutes.Welcome
    ) {
        composable<OnboardingGraphRoutes.Welcome> {
            OnboardingRoot(onNavigateToAuth = onNavigateToAuth)
        }
    }
}