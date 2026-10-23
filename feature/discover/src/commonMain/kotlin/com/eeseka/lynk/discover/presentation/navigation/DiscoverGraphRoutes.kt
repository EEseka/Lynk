package com.eeseka.lynk.discover.presentation.navigation

import kotlinx.serialization.Serializable

sealed interface DiscoverGraphRoutes {
    @Serializable
    data object Graph : DiscoverGraphRoutes

    @Serializable
    data object Discover : DiscoverGraphRoutes

    @Serializable
    data class Assistant(
        val latitude: Double,
        val longitude: Double,
        val areaName: String? = null
    ) : DiscoverGraphRoutes
}