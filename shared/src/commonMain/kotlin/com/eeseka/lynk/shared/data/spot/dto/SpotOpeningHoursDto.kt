package com.eeseka.lynk.shared.data.spot.dto

import kotlin.time.Instant
import kotlinx.serialization.Serializable

@Serializable
data class SpotOpeningHoursDto(
    val isOpenNow: Boolean?,
    val weekdayDescriptions: List<String>,
    val nextOpenTime: Instant?,
    val nextCloseTime: Instant?
)
