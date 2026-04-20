package com.eeseka.lynk

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.eeseka.lynk.auth.presentation.navigation.AuthGraphRoutes
import com.eeseka.lynk.main_shell.presentation.navigation.MainShellGraphRoutes
import com.eeseka.lynk.navigation.NavigationRoot
import com.eeseka.lynk.onboarding.presentation.navigation.OnboardingGraphRoutes
import com.eeseka.lynk.profile_setup.presentation.navigation.ProfileSetupGraphRoutes
import com.eeseka.lynk.shared.design_system.theme.LynkTheme
import com.eeseka.lynk.shared.domain.auth.model.User
import com.eeseka.lynk.shared.domain.settings.AppTheme
import com.eeseka.lynk.shared.presentation.util.ObserveAsEvents
import org.koin.compose.viewmodel.koinViewModel

@Composable
@Preview
fun App(
    onAuthenticationChecked: () -> Unit = {},
    viewModel: MainViewModel = koinViewModel()
) {
    val navController = rememberNavController()
    val state by viewModel.state.collectAsStateWithLifecycle()

    val isDarkTheme = when (state.theme) {
        AppTheme.SYSTEM -> isSystemInDarkTheme()
        AppTheme.LIGHT -> false
        AppTheme.DARK -> true
    }

    LaunchedEffect(state.isCheckingAuth) {
        if (!state.isCheckingAuth) {
            onAuthenticationChecked()
        }
    }

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is MainEvent.OnSessionExpired -> {
                navController.navigate(AuthGraphRoutes.Graph) {
                    popUpTo(navController.graph.id) {
                        inclusive = true
                    }
                }
            }
        }
    }

    LynkTheme(darkTheme = isDarkTheme) {
        if (!state.isCheckingAuth) {
            val startDestination = when {
                !state.hasSeenOnboarding -> OnboardingGraphRoutes.Graph
                state.user is User.ProfileIncomplete -> ProfileSetupGraphRoutes.Graph
                state.user != null -> MainShellGraphRoutes.Graph
                else -> AuthGraphRoutes.Graph
            }

            NavigationRoot(
                navController = navController,
                startDestination = startDestination
            )
        }
    }
}