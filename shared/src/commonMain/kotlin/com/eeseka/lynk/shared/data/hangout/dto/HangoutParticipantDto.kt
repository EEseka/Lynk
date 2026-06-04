package com.eeseka.lynk.shared.data.hangout.dto

import com.eeseka.lynk.shared.domain.hangout.model.RsvpStatus
import kotlinx.serialization.Serializable

@Serializable
data class HangoutParticipantDto(
    val userId: String,
    val username: String,
    val displayName: String,
    val profilePictureUrl: String?,
    val rsvpStatus: RsvpStatus,
    val hasPaid: Boolean
)