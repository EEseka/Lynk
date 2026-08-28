package com.eeseka.lynk.shared.presentation.util

import androidx.compose.runtime.Composable
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.DayOfWeekNames
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime
import lynk.shared.generated.resources.Res
import lynk.shared.generated.resources.am
import lynk.shared.generated.resources.pm
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Clock
import kotlin.time.Instant

@Composable
fun Instant.toHangoutDisplayDate(): String {
    val amMarker = stringResource(Res.string.am)
    val pmMarker = stringResource(Res.string.pm)

    val dateTime = toLocalDateTime(TimeZone.currentSystemDefault())

    val datePart = dateTime.date.format(
        LocalDate.Format {
            dayOfWeek(DayOfWeekNames.ENGLISH_ABBREVIATED)
            chars(" ")
            day()
            chars(" ")
            monthName(MonthNames.ENGLISH_ABBREVIATED)
        }
    )

    val timePart = dateTime.time.format(
        LocalTime.Format {
            amPmHour()
            char(':')
            minute()
            char(' ')
            amPmMarker(amMarker, pmMarker)
        }
    )

    return "$datePart${dateTime.date.yearSuffixIfNotCurrent()} · $timePart"
}

fun LocalDate.yearSuffixIfNotCurrent(): String {
    val currentYear = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).year
    return if (year == currentYear) "" else " $year"
}