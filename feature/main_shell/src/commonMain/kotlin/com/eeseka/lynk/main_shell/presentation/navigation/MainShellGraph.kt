package com.eeseka.lynk.main_shell.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.eeseka.lynk.main_shell.presentation.MainShell

fun NavGraphBuilder.mainShellGraph(
    navController: NavHostController
) {
    navigation<MainShellGraphRoutes.Graph>(
        startDestination = MainShellGraphRoutes.MainShell
    ) {
        composable<MainShellGraphRoutes.MainShell> {
            MainShell()
        }
    }
}