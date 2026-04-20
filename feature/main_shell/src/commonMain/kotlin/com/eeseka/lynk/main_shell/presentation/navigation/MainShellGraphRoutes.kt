package com.eeseka.lynk.main_shell.presentation.navigation

import kotlinx.serialization.Serializable

sealed interface MainShellGraphRoutes {
    @Serializable
    data object Graph : MainShellGraphRoutes

    @Serializable
    data object MainShell : MainShellGraphRoutes
}