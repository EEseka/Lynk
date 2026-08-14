package com.eeseka.lynk.shared.domain.hangout.model

data class HangoutParticipant(
    val user: HangoutUser,
    val rsvpStatus: RsvpStatus,
    val hasPaid: Boolean
)