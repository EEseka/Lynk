package com.eeseka.lynk.shared.presentation.util

import androidx.compose.runtime.Composable
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.format
import kotlinx.datetime.format.DayOfWeekNames
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime
import lynk.shared.generated.resources.Res
import lynk.shared.generated.resources.am
import lynk.shared.generated.resources.pm
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Clock
import kotlin.time.Instant

fun Instant.toDateLabel(): String =
    toLocalDateTime(TimeZone.currentSystemDefault()).date.toDateLabel()

// Sat 5 Sep or Sat 5 Sep 202x if it's not the current year
fun LocalDate.toDateLabel(): String {
    val currentYear = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).year
    val label = format(
        LocalDate.Format {
            dayOfWeek(DayOfWeekNames.ENGLISH_ABBREVIATED)
            char(' ')
            day(Padding.NONE)
            char(' ')
            monthName(MonthNames.ENGLISH_ABBREVIATED)
        }
    )
    return if (year == currentYear) label else "$label $year"
}

// 8:00 PM
@Composable
fun LocalTime.toTimeLabel(): String {
    val amMarker = stringResource(Res.string.am)
    val pmMarker = stringResource(Res.string.pm)

    return format(
        LocalTime.Format {
            amPmHour(Padding.NONE)
            char(':')
            minute()
            char(' ')
            amPmMarker(amMarker, pmMarker)
        }
    )
}

// "Sat 5 Sep · 8:00 PM"
@Composable
fun Instant.toDateTimeLabel(): String {
    val dateTime = toLocalDateTime(TimeZone.currentSystemDefault())
    return "${dateTime.date.toDateLabel()} · ${dateTime.time.toTimeLabel()}"
}

// The date picker hands back, and takes, midnight UTC. Converting in the phone's own time zone
// instead would land on the day before for anyone west of UTC.
fun Long.toPickerDate(): LocalDate =
    Instant.fromEpochMilliseconds(this).toLocalDateTime(TimeZone.UTC).date

fun LocalDate.toPickerMillis(): Long =
    atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()
