package com.eeseka.lynk.shared.presentation.hangout.model

import androidx.compose.runtime.Immutable
import com.eeseka.lynk.shared.domain.hangout.model.RsvpStatus

@Immutable
data class HangoutParticipantUi(
    val user: HangoutUserUi,
    val rsvpStatus: RsvpStatus,
    val hasPaid: Boolean
)