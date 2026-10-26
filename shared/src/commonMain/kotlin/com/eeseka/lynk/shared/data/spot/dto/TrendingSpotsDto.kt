package com.eeseka.lynk.shared.data.spot.dto

import kotlinx.serialization.Serializable

@Serializable
data class TrendingSpotsDto(
    val spots: List<SpotDto>,
    val areaName: String?
)
