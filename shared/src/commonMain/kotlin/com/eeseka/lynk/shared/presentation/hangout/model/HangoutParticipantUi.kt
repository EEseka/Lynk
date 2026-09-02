package com.eeseka.lynk.shared.presentation.hangout.model

import com.eeseka.lynk.shared.domain.hangout.model.RsvpStatus

data class HangoutParticipantUi(
    val user: HangoutUserUi,
    val rsvpStatus: RsvpStatus,
    val hasPaid: Boolean
)