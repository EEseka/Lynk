package com.eeseka.lynk.onboarding.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.eeseka.lynk.onboarding.presentation.OnboardingScreen
import com.eeseka.lynk.onboarding.presentation.OnboardingViewModel
import org.koin.compose.viewmodel.koinViewModel

fun NavGraphBuilder.onboardingGraph(
    navController: NavController,
    onOnboardingComplete: () -> Unit
) {
    navigation<OnboardingGraphRoutes.Graph>(
        startDestination = OnboardingGraphRoutes.Welcome
    ) {
        composable<OnboardingGraphRoutes.Welcome> {
            val viewModel = koinViewModel<OnboardingViewModel>()

            OnboardingScreen(
                events = viewModel.events,
                onAction = viewModel::onAction,
                onOnboardingComplete = onOnboardingComplete
            )
        }
    }
}