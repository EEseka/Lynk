package com.eeseka.lynk.shared.domain.spot.model

data class SpotOpeningHours(
    val weekdayDescriptions: List<String>, // Google's own lines, one per day, e.g. "Monday: 11:00 AM – 1:00 AM"
    val utcOffsetMinutes: Int?, // The place's own offset from UTC, so its times read in its local clock, not the viewer's
    val timeZoneId: String?, // The place's time zone, e.g. "Africa/Lagos", so its clock changes are followed too
    val periods: List<SpotOpeningPeriod>
)
