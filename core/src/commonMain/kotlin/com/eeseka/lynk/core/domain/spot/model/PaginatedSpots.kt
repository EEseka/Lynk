package com.eeseka.lynk.core.domain.spot.model

data class PaginatedSpots(
    val spots: List<Spot>,
    val nextPageToken: String?
)