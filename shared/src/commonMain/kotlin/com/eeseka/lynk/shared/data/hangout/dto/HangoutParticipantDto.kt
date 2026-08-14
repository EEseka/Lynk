package com.eeseka.lynk.shared.data.hangout.dto

import com.eeseka.lynk.shared.domain.hangout.model.RsvpStatus
import kotlinx.serialization.Serializable

@Serializable
data class HangoutParticipantDto(
    val user: HangoutUserDto,
    val rsvpStatus: RsvpStatus,
    val hasPaid: Boolean
)