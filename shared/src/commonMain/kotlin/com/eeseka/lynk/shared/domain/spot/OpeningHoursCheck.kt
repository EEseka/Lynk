package com.eeseka.lynk.shared.domain.spot

import com.eeseka.lynk.shared.domain.spot.model.SpotOpeningHours
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.IllegalTimeZoneException
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.UtcOffset
import kotlinx.datetime.asTimeZone
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

/** Whether a place is open at a given moment, from its weekly periods on the place's own clock **/
object OpeningHoursCheck {
    private const val MINUTES_PER_DAY = 24 * 60
    private const val MINUTES_PER_WEEK = 7 * MINUTES_PER_DAY

    // Google ends its 7-day window at 23:59, so a night that runs past it shows as closing then
    private const val LAST_MINUTE_OF_DAY = MINUTES_PER_DAY - 1

    fun isOpenAt(openingHours: SpotOpeningHours, at: Instant): Boolean? {
        val placeTimeZone = placeTimeZoneOf(openingHours) ?: return null
        if (openingHours.periods.isEmpty()) return null

        val placeTime = at.toLocalDateTime(placeTimeZone)
        val minuteOfWeek = minuteOfWeek(placeTime.dayOfWeek, placeTime.hour * 60 + placeTime.minute)

        return openingHours.periods.any { period ->
            val closeDay = period.closeDay ?: return@any true
            val closeMinuteOfDay = period.closeMinuteOfDay ?: return@any true

            val opensAt = minuteOfWeek(period.openDay, period.openMinuteOfDay)
            var closesAt = minuteOfWeek(closeDay, closeMinuteOfDay)
            if (closeMinuteOfDay == LAST_MINUTE_OF_DAY) closesAt++ // Counting that cut-off minute joins it to the next morning's stretch, instead of a minute closed
            if (closesAt <= opensAt) closesAt += MINUTES_PER_WEEK

            minuteOfWeek in opensAt until closesAt || minuteOfWeek + MINUTES_PER_WEEK in opensAt until closesAt
        }
    }

    // When an open place next closes or a closed one next opens; null for a place that never closes
    fun nextChangeAt(openingHours: SpotOpeningHours, at: Instant): Instant? {
        val isOpenNow = isOpenAt(openingHours, at) ?: return null
        val placeTimeZone = placeTimeZoneOf(openingHours) ?: return null

        val opensAndCloses = openingHours.periods.flatMap { period ->
            val opens = nextTimeAfter(at, period.openDay, period.openMinuteOfDay, placeTimeZone)
            val closes = if (period.closeDay != null && period.closeMinuteOfDay != null) {
                // The 23:59 cut-off closes at midnight, as isOpenAt counts it
                val closeMinuteOfDay = if (period.closeMinuteOfDay == LAST_MINUTE_OF_DAY) MINUTES_PER_DAY else period.closeMinuteOfDay
                nextTimeAfter(at, period.closeDay, closeMinuteOfDay, placeTimeZone)
            } else null
            listOfNotNull(opens, closes)
        }

        // Skips a close that runs straight into the next opening, like a night that carries on past midnight
        return opensAndCloses.sorted().firstOrNull { isOpenAt(openingHours, it) != isOpenNow }
    }

    // The place's own time zone, else its fixed offset; null when Google sent neither
    fun placeTimeZoneOf(openingHours: SpotOpeningHours): TimeZone? {
        openingHours.timeZoneId?.let { timeZoneId ->
            try {
                return TimeZone.of(timeZoneId)
            } catch (_: IllegalTimeZoneException) {
                // A zone this phone doesn't know; the offset below is the next best thing
            }
        }
        return openingHours.utcOffsetMinutes?.let { UtcOffset(seconds = it * 60).asTimeZone() }
    }

    // The first time after the given moment that this day and minute come round on the place's clock
    private fun nextTimeAfter(after: Instant, day: DayOfWeek, minuteOfDay: Int, timeZone: TimeZone): Instant {
        val today = after.toLocalDateTime(timeZone).date
        val daysAhead = (day.isoDayNumber - today.dayOfWeek.isoDayNumber).mod(7) + minuteOfDay / MINUTES_PER_DAY
        val time = LocalTime(hour = minuteOfDay % MINUTES_PER_DAY / 60, minute = minuteOfDay % 60)

        val thisWeek = LocalDateTime(today.plus(daysAhead, DateTimeUnit.DAY), time).toInstant(timeZone)
        return if (thisWeek > after) thisWeek else LocalDateTime(today.plus(daysAhead + 7, DateTimeUnit.DAY), time).toInstant(timeZone)
    }

    private fun minuteOfWeek(day: DayOfWeek, minuteOfDay: Int): Int = (day.isoDayNumber - 1) * MINUTES_PER_DAY + minuteOfDay
}
