package com.eeseka.lynk.notifications.presentation.util

import androidx.compose.runtime.Composable
import com.eeseka.lynk.shared.presentation.util.yearSuffixIfNotCurrent
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime
import lynk.feature.notifications.generated.resources.Res
import lynk.feature.notifications.generated.resources.time_days_ago
import lynk.feature.notifications.generated.resources.time_hours_ago
import lynk.feature.notifications.generated.resources.time_just_now
import lynk.feature.notifications.generated.resources.time_minutes_ago
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Clock
import kotlin.time.Instant

@Composable
fun Instant.toNotificationTimeLabel(): String {
    val elapsed = Clock.System.now() - this

    val minutes = elapsed.inWholeMinutes
    val hours = elapsed.inWholeHours
    val days = elapsed.inWholeDays

    return when {
        minutes < 1 -> stringResource(Res.string.time_just_now)
        minutes < 60 -> stringResource(Res.string.time_minutes_ago, minutes)
        hours < 24 -> stringResource(Res.string.time_hours_ago, hours)
        days < 7 -> stringResource(Res.string.time_days_ago, days)
        else -> toShortDate()
    }
}

private fun Instant.toShortDate(): String {
    val date = toLocalDateTime(TimeZone.currentSystemDefault()).date
    return date.format(
        LocalDate.Format {
            day()
            char(' ')
            monthName(MonthNames.ENGLISH_ABBREVIATED)
        }
    ) + date.yearSuffixIfNotCurrent()
}