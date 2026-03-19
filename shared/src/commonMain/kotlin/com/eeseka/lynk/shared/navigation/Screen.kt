package com.eeseka.lynk.shared.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface Screen {
    @Serializable
    data object Onboarding : Screen

    @Serializable
    data object Auth : Screen

    @Serializable
    data object HomeGraph : Screen

    @Serializable
    data object Discover : Screen

    @Serializable
    data object Profile : Screen

    @Serializable
    data object Settings : Screen
}