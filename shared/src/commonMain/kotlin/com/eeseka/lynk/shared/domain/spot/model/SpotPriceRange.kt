package com.eeseka.lynk.shared.domain.spot.model

data class SpotPriceRange(
    val currencyCode: String,
    val startAmount: Long?,
    val endAmount: Long?
)
