package com.eeseka.lynk.shared.domain.hangout.model

import com.eeseka.lynk.shared.domain.spot.model.Spot
import kotlin.time.Instant

data class HangoutPreview(
    val id: String,
    val hostId: String,
    val name: String,
    val description: String?,
    val vibe: HangoutVibe,
    val status: HangoutStatus,
    val scheduledAt: Instant,
    val maxAttendees: Int?,
    val participantCount: Int,
    val chosenSpot: Spot?, // null if still voting
    val attendees: List<HangoutUser>,
    val createdAt: Instant
)
