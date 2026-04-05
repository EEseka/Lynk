package com.eeseka.lynk.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.eeseka.lynk.auth.presentation.navigation.AuthGraphRoutes
import com.eeseka.lynk.auth.presentation.navigation.authGraph
import com.eeseka.lynk.dummy.MainGraphRoutes
import com.eeseka.lynk.onboarding.presentation.navigation.OnboardingGraphRoutes
import com.eeseka.lynk.onboarding.presentation.navigation.onboardingGraph
import com.eeseka.lynk.shared.design_system.components.layouts.LynkScaffold
import com.eeseka.lynk.shared.design_system.components.textfields.LynkText
import com.eeseka.lynk.shared.domain.auth.SessionStorage
import com.eeseka.lynk.shared.domain.auth.User
import org.koin.compose.koinInject

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
            onAuthSuccess = {
                navController.navigate(MainGraphRoutes.Graph) {
                    popUpTo(AuthGraphRoutes.Graph) { inclusive = true }
                }
            }
        )

        // --- THE DUMMY MAIN SCREEN ---
        composable<MainGraphRoutes.Graph> {
            val sessionStorage = koinInject<SessionStorage>()
            val authInfo by sessionStorage.observeAuthInfo().collectAsStateWithLifecycle(null)
            val user = authInfo?.user as? User.ProfileIncomplete

            LynkScaffold {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        LynkText(text = "🎉 YOU MADE IT TO THE MAIN APP! 🎉")

                        Spacer(modifier = Modifier.height(16.dp))

                        if (user != null) {
                            LynkText(text = "Welcome back, ${user.displayName} with ID ${user.displayName}!")
                            LynkText(text = "Email: ${user.email}")
                            LynkText(text = "PfpUrl: @${user.profilePictureUrl}")
                        }
                    }
                }
            }
        }
    }
}