package com.eeseka.lynk.core.presentation.hangout.model

import androidx.compose.runtime.Immutable
import com.eeseka.lynk.core.domain.hangout.model.RsvpStatus

@Immutable
data class HangoutParticipantUi(
    val user: HangoutUserUi,
    val rsvpStatus: RsvpStatus,
    val hasPaid: Boolean,
    val hasArrived: Boolean
)