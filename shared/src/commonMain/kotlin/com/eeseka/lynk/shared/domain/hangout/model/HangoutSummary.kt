package com.eeseka.lynk.shared.domain.hangout.model

import kotlin.time.Instant

data class HangoutSummary(
    val id: String,
    val hostId: String,
    val name: String,
    val description: String?,
    val vibe: HangoutVibe,
    val status: HangoutStatus,
    val scheduledAt: Instant,
    val maxAttendees: Int?,
    val participantCount: Int,
    val hasChosenSpot: Boolean,
    val totalCost: Double?,
    val createdAt: Instant
)
