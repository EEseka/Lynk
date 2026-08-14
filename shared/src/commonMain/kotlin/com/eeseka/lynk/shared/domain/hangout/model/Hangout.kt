package com.eeseka.lynk.shared.domain.hangout.model

import com.eeseka.lynk.shared.domain.spot.model.Spot
import kotlin.time.Instant

data class Hangout(
    val id: String,
    val hostId: String,
    val name: String,
    val description: String?,
    val vibe: HangoutVibe,
    val status: HangoutStatus,
    val scheduledAt: Instant,
    val maxAttendees: Int?, // null means unlimited
    val participantCount: Int,
    val chosenSpot: Spot?, // NULL if they are still voting
    val participants: List<HangoutParticipant>,
    val payment: HangoutPayment?, // NULL until the host turns payments on
    val createdAt: Instant
)