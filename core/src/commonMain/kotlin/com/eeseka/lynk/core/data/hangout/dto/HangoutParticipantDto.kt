package com.eeseka.lynk.core.data.hangout.dto

import com.eeseka.lynk.core.domain.hangout.model.RsvpStatus
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class HangoutParticipantDto(
    val user: HangoutUserDto,
    val rsvpStatus: RsvpStatus,
    val hasPaid: Boolean,
    val arrivedAt: Instant?
)