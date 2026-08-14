package com.eeseka.lynk.shared.data.hangout.dto.requests

import com.eeseka.lynk.shared.domain.hangout.model.RsvpStatus
import kotlinx.serialization.Serializable

@Serializable
data class RsvpRequest(
    val rsvpStatus: RsvpStatus
)
