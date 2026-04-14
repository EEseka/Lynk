package com.eeseka.lynk.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.eeseka.lynk.profile_setup.presentation.navigation.ProfileSetupGraphRoutes
import com.eeseka.lynk.profile_setup.presentation.navigation.profileSetupGraph
import com.eeseka.lynk.shared.design_system.components.buttons.LynkButton
import com.eeseka.lynk.shared.design_system.components.layouts.LynkScaffold
import com.eeseka.lynk.shared.design_system.components.modals_and_overlays.LynkFlashType
import com.eeseka.lynk.shared.design_system.components.modals_and_overlays.showFlashMessage
import com.eeseka.lynk.shared.design_system.components.textfields.LynkText
import com.eeseka.lynk.shared.domain.auth.SessionStorage
import com.eeseka.lynk.shared.domain.auth.model.User
import kotlinx.coroutines.launch
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
            onNavigateToProfileSetup = {
                navController.navigate(ProfileSetupGraphRoutes.Graph) {
                    popUpTo(AuthGraphRoutes.Graph) { inclusive = true }
                }
            },
            onNavigateToMain = {
                navController.navigate(MainGraphRoutes.Graph) {
                    popUpTo(AuthGraphRoutes.Graph) { inclusive = true }
                }
            }
        )

        profileSetupGraph(
            navController = navController,
            onNavigateToMain = {
                navController.navigate(MainGraphRoutes.Graph) {
                    popUpTo(ProfileSetupGraphRoutes.Graph) { inclusive = true }
                }
            }
        )

        // --- THE DUMMY MAIN SCREEN ---
        composable<MainGraphRoutes.Graph> {
            val sessionStorage = koinInject<SessionStorage>()
            val authInfo by sessionStorage.observeAuthInfo().collectAsStateWithLifecycle(null)
            val user = authInfo?.user as? User.Authenticated

            val snackbarHostState = remember { SnackbarHostState() }
            val scope = rememberCoroutineScope()

            LynkScaffold(snackbarHostState = snackbarHostState) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(it).padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        LynkText(text = "🎉 YOU MADE IT TO THE MAIN APP! 🎉")

                        Spacer(modifier = Modifier.height(16.dp))

                        if (user != null) {
                            LynkText(text = "Welcome back ID ${user.id}!")
                            LynkText(text = "Email: ${user.email}")
                            LynkText(text = "Username: ${user.username}")
                            LynkText(text = "Display Name: ${user.displayName}")
                            LynkText(text = "PfpUrl: @${user.profilePictureUrl}")
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        LynkButton(text = "Show Snackbar", onClick = {
                            scope.launch {
                                snackbarHostState.showFlashMessage(
                                    message = "Hello ${user?.username ?: "Anonymous"}!",
                                    type = LynkFlashType.Success
                                )
                            }
                        })
                    }
                }
            }
        }
    }
}