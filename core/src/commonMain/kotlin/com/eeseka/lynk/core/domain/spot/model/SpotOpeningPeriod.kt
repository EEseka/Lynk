package com.eeseka.lynk.core.domain.spot.model

import kotlinx.datetime.DayOfWeek

data class SpotOpeningPeriod(
    val openDay: DayOfWeek,
    val openMinuteOfDay: Int,
    val closeDay: DayOfWeek?, // Null when the place never closes
    val closeMinuteOfDay: Int?
)
