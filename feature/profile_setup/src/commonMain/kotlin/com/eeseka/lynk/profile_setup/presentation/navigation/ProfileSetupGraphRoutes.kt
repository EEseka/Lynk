package com.eeseka.lynk.profile_setup.presentation.navigation

import kotlinx.serialization.Serializable

sealed interface ProfileSetupGraphRoutes {
    @Serializable
    data object Graph : ProfileSetupGraphRoutes

    @Serializable
    data object ProfileSetup : ProfileSetupGraphRoutes
}