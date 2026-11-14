package com.eeseka.lynk.core.domain.spot.model

data class TrendingSpots(
    val spots: List<Spot>,
    val areaName: String?
)
