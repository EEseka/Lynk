package com.eeseka.lynk.shared.data.spot.dto

import kotlinx.serialization.Serializable

@Serializable
data class SpotAmenitiesDto(
    val isGoodForGroups: Boolean?,
    val isReservable: Boolean?,
    val hasLiveMusic: Boolean?,
    val hasOutdoorSeating: Boolean?,
    val servesCocktails: Boolean?,
    val isGoodForWatchingSports: Boolean?
)
