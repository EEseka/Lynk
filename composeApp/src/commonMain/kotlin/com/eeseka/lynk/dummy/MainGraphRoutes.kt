package com.eeseka.lynk.dummy

import kotlinx.serialization.Serializable

sealed interface MainGraphRoutes {
    @Serializable
    data object Graph : MainGraphRoutes
}