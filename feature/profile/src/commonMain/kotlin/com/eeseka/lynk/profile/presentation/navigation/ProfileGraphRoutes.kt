package com.eeseka.lynk.profile.presentation.navigation

import kotlinx.serialization.Serializable

sealed interface ProfileGraphRoutes {
    @Serializable
    data object Graph : ProfileGraphRoutes

    @Serializable
    data object Profile : ProfileGraphRoutes

    @Serializable
    data object SavedSpots : ProfileGraphRoutes
}
