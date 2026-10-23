package com.eeseka.lynk.shared.data.spot.dto

import kotlinx.datetime.DayOfWeek
import kotlinx.serialization.Serializable

@Serializable
data class SpotOpeningPeriodDto(
    val openDay: DayOfWeek,
    val openMinuteOfDay: Int,
    val closeDay: DayOfWeek?,
    val closeMinuteOfDay: Int?
)
