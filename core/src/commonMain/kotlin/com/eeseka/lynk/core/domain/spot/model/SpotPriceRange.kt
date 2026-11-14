package com.eeseka.lynk.core.domain.spot.model

data class SpotPriceRange(
    val currencyCode: String,
    val startAmount: Long?,
    val endAmount: Long?
)
