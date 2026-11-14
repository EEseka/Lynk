package com.eeseka.lynk.core.domain.hangout.model

import kotlin.time.Instant

data class HangoutParticipant(
    val user: HangoutUser,
    val rsvpStatus: RsvpStatus,
    val hasPaid: Boolean,
    val arrivedAt: Instant?
)