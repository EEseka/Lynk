package com.eeseka.lynk.shared.data.hangout.dto.requests

import com.eeseka.lynk.shared.domain.hangout.model.HangoutVibe
import kotlin.time.Instant
import kotlinx.serialization.Serializable

@Serializable
data class CreateHangoutRequest(
    val name: String,
    val description: String?,
    val vibe: HangoutVibe,
    val scheduledAt: Instant,
    val maxAttendees: Int?,
    val spotId: String?
)