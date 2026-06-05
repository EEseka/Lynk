package com.eeseka.lynk.hangouts.presentation.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.toRoute
import com.eeseka.lynk.hangouts.presentation.HangoutsViewModel
import com.eeseka.lynk.shared.design_system.components.textfields.LynkText
import org.koin.compose.viewmodel.koinViewModel

fun NavGraphBuilder.hangoutsGraph(
    navController: NavController,
    mainShellPadding: PaddingValues
) {
    navigation<HangoutsGraphRoutes.Graph>(
        startDestination = HangoutsGraphRoutes.HangoutListDetail()
    ) {
        composable<HangoutsGraphRoutes.HangoutListDetail> { backStackEntry ->
            val route = backStackEntry.toRoute<HangoutsGraphRoutes.HangoutListDetail>()
            val viewModel = koinViewModel<HangoutsViewModel>()

            Box(
                modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                LynkText(
                    text = "Welcome to Hangouts Screen with ID: ${route.hangoutId ?: ""}",
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.headlineLarge
                )
            }
        }
    }
}