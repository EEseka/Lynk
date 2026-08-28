package com.eeseka.lynk.hangouts.presentation.util

import com.eeseka.lynk.shared.presentation.util.yearSuffixIfNotCurrent
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.DayOfWeekNames
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

fun Instant.toDeadlineDisplayDate(): String {
    val date = toLocalDateTime(TimeZone.currentSystemDefault()).date
    return date.format(
        LocalDate.Format {
            dayOfWeek(DayOfWeekNames.ENGLISH_ABBREVIATED)
            chars(" ")
            day()
            chars(" ")
            monthName(MonthNames.ENGLISH_ABBREVIATED)
        }
    ) + date.yearSuffixIfNotCurrent()
}

fun Long.toLocalDate(): LocalDate =
    Instant.fromEpochMilliseconds(this).toLocalDateTime(TimeZone.UTC).date

fun LocalDate.toDeadlineLabel(): String = format(
    LocalDate.Format {
        day()
        char(' ')
        monthName(MonthNames.ENGLISH_ABBREVIATED)
        char(' ')
        year()
    }
)