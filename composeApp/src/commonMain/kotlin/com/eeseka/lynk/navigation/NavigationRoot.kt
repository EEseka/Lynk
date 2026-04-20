package com.eeseka.lynk.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.eeseka.lynk.auth.presentation.navigation.AuthGraphRoutes
import com.eeseka.lynk.auth.presentation.navigation.authGraph
import com.eeseka.lynk.main_shell.presentation.navigation.MainShellGraphRoutes
import com.eeseka.lynk.main_shell.presentation.navigation.mainShellGraph
import com.eeseka.lynk.onboarding.presentation.navigation.OnboardingGraphRoutes
import com.eeseka.lynk.onboarding.presentation.navigation.onboardingGraph
import com.eeseka.lynk.profile_setup.presentation.navigation.ProfileSetupGraphRoutes
import com.eeseka.lynk.profile_setup.presentation.navigation.profileSetupGraph

@Composable
fun NavigationRoot(
    navController: NavHostController,
    startDestination: Any
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        onboardingGraph(
            navController = navController,
            onOnboardingComplete = {
                navController.navigate(AuthGraphRoutes.Graph) {
                    popUpTo(OnboardingGraphRoutes.Graph) { inclusive = true }
                }
            }
        )

        authGraph(
            navController = navController,
            onNavigateToProfileSetup = {
                navController.navigate(ProfileSetupGraphRoutes.Graph) {
                    popUpTo(AuthGraphRoutes.Graph) { inclusive = true }
                }
            },
            onNavigateToMain = {
                navController.navigate(MainShellGraphRoutes.Graph) {
                    popUpTo(AuthGraphRoutes.Graph) { inclusive = true }
                }
            }
        )

        profileSetupGraph(
            navController = navController,
            onNavigateToMain = {
                navController.navigate(MainShellGraphRoutes.Graph) {
                    popUpTo(ProfileSetupGraphRoutes.Graph) { inclusive = true }
                }
            }
        )

        mainShellGraph(
            navController = navController
        )
    }
}