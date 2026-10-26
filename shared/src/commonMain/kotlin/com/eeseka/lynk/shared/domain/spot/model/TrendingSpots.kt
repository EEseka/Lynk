package com.eeseka.lynk.shared.domain.spot.model

data class TrendingSpots(
    val spots: List<Spot>,
    val areaName: String?
)
