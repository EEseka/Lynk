package com.eeseka.lynk.shared.data.spot.dto

import kotlinx.serialization.Serializable

@Serializable
data class SpotAiSummaryDto(
    val text: String,
    val disclosure: String
)
