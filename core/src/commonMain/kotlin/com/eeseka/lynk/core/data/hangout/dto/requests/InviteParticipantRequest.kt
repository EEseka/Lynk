package com.eeseka.lynk.core.data.hangout.dto.requests

import kotlinx.serialization.Serializable

@Serializable
data class InviteParticipantRequest(
    val userId: String
)
