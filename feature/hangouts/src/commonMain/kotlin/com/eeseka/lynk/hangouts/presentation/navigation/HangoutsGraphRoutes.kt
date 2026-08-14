package com.eeseka.lynk.hangouts.presentation.navigation

import kotlinx.serialization.Serializable

sealed interface HangoutsGraphRoutes {
    @Serializable
    data object Graph : HangoutsGraphRoutes

    @Serializable
    data class HangoutListDetail(val hangoutId: String? = null) : HangoutsGraphRoutes
}