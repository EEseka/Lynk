package com.eeseka.lynk.shared.data.spot.dto

import kotlinx.serialization.Serializable

@Serializable
data class SpotOpeningHoursDto(
    val weekdayDescriptions: List<String>,
    val utcOffsetMinutes: Int?,
    val timeZoneId: String?,
    val periods: List<SpotOpeningPeriodDto>
)
