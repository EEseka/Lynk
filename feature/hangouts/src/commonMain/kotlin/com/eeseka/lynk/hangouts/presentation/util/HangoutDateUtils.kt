package com.eeseka.lynk.hangouts.presentation.util

import androidx.compose.runtime.Composable
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.DayOfWeekNames
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime
import lynk.feature.hangouts.generated.resources.Res
import lynk.feature.hangouts.generated.resources.am
import lynk.feature.hangouts.generated.resources.pm
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Instant

@Composable
fun Instant.toHangoutDisplayDate(): String {
    val amMarker = stringResource(Res.string.am)
    val pmMarker = stringResource(Res.string.pm)
    return toLocalDateTime(TimeZone.currentSystemDefault()).format(
        LocalDateTime.Format {
            dayOfWeek(DayOfWeekNames.ENGLISH_ABBREVIATED)
            chars(" ")
            day()
            chars(" ")
            monthName(MonthNames.ENGLISH_ABBREVIATED)
            chars(" · ")
            amPmHour()
            char(':')
            minute()
            char(' ')
            amPmMarker(amMarker, pmMarker)
        }
    )
}

fun Instant.toDeadlineDisplayDate(): String =
    toLocalDateTime(TimeZone.currentSystemDefault()).date.format(
        LocalDate.Format {
            dayOfWeek(DayOfWeekNames.ENGLISH_ABBREVIATED)
            chars(" ")
            day()
            chars(" ")
            monthName(MonthNames.ENGLISH_ABBREVIATED)
        }
    )

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