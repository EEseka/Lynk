package com.eeseka.lynk.shared.data.hangout.dto

import com.eeseka.lynk.shared.domain.hangout.model.HangoutStatus
import com.eeseka.lynk.shared.domain.hangout.model.HangoutVibe
import kotlin.time.Instant
import kotlinx.serialization.Serializable

@Serializable
data class HangoutSummaryDto(
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