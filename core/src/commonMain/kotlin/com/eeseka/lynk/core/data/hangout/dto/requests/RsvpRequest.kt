package com.eeseka.lynk.core.data.hangout.dto.requests

import com.eeseka.lynk.core.domain.hangout.model.RsvpStatus
import kotlinx.serialization.Serializable

@Serializable
data class RsvpRequest(
    val rsvpStatus: RsvpStatus
)
