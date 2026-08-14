package com.eeseka.lynk.shared.domain.hangout.model

import kotlin.time.Instant

data class HangoutSummary(
    val id: String,
    val hostId: String,
    val name: String,
    val vibe: HangoutVibe,
    val status: HangoutStatus,
    val scheduledAt: Instant,
    val maxAttendees: Int?,
    val participantCount: Int,
    val createdAt: Instant
)
