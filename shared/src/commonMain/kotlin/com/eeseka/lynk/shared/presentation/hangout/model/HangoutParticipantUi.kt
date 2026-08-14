package com.eeseka.lynk.shared.presentation.hangout.model

import androidx.compose.runtime.Stable
import com.eeseka.lynk.shared.domain.hangout.model.RsvpStatus

@Stable
data class HangoutParticipantUi(
    val user: HangoutUserUi,
    val rsvpStatus: RsvpStatus,
    val hasPaid: Boolean
)