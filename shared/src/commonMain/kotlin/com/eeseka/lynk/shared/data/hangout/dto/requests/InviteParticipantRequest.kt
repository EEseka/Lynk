package com.eeseka.lynk.shared.data.hangout.dto.requests

import kotlinx.serialization.Serializable

@Serializable
data class InviteParticipantRequest(
    val userId: String
)
