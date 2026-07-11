package com.eeseka.lynk.hangouts.presentation.mappers

import androidx.compose.runtime.Composable
import com.eeseka.lynk.hangouts.presentation.model.HangoutStatusFilter
import com.eeseka.lynk.shared.domain.hangout.model.HangoutStatus
import lynk.feature.hangouts.generated.resources.Res
import lynk.feature.hangouts.generated.resources.cancelled
import lynk.feature.hangouts.generated.resources.completed
import lynk.feature.hangouts.generated.resources.ongoing
import lynk.feature.hangouts.generated.resources.upcoming
import org.jetbrains.compose.resources.stringResource

@Composable
fun HangoutStatusFilter.getTitle(): String = stringResource(
    when (this) {
        HangoutStatusFilter.UPCOMING -> Res.string.upcoming
        HangoutStatusFilter.ONGOING -> Res.string.ongoing
        HangoutStatusFilter.COMPLETED -> Res.string.completed
        HangoutStatusFilter.CANCELLED -> Res.string.cancelled
    }
)

// UPCOMING has no single backend status: the API treats "no status filter" as VOTING + SCHEDULED
fun HangoutStatusFilter.toHangoutStatus(): HangoutStatus? = when (this) {
    HangoutStatusFilter.UPCOMING -> null
    HangoutStatusFilter.ONGOING -> HangoutStatus.ONGOING
    HangoutStatusFilter.COMPLETED -> HangoutStatus.COMPLETED
    HangoutStatusFilter.CANCELLED -> HangoutStatus.CANCELLED
}