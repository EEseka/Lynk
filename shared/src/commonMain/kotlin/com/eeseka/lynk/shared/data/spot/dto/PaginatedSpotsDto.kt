package com.eeseka.lynk.shared.data.spot.dto

import kotlinx.serialization.Serializable

@Serializable
data class PaginatedSpotsDto(
    val spots: List<SpotDto>,
    val nextPageToken: String?
)