package com.eeseka.lynk.shared.data.hangout.dto

import com.eeseka.lynk.shared.domain.hangout.model.RsvpStatus
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class HangoutParticipantDto(
    val user: HangoutUserDto,
    val rsvpStatus: RsvpStatus,
    val hasPaid: Boolean,
    val arrivedAt: Instant?
)