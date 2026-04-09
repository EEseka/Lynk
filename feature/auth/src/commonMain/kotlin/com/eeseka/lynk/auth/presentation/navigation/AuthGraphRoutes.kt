package com.eeseka.lynk.auth.presentation.navigation

import kotlinx.serialization.Serializable

sealed interface AuthGraphRoutes {
    @Serializable
    data object Graph : AuthGraphRoutes

    @Serializable
    data object Auth : AuthGraphRoutes
}