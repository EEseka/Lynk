package com.eeseka.lynk.shared.domain.spot.model

data class PaginatedSpots(
    val spots: List<Spot>,
    val nextPageToken: String?
)