package com.eeseka.lynk.core.data.spot.dto

import kotlinx.serialization.Serializable

@Serializable
data class SpotPriceRangeDto(
    val currencyCode: String,
    val startAmount: Long?,
    val endAmount: Long?
)
