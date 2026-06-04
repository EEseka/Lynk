package com.eeseka.lynk.shared.data.hangout.dto

import com.eeseka.lynk.shared.data.spot.dto.SpotDto
import com.eeseka.lynk.shared.domain.hangout.model.HangoutStatus
import com.eeseka.lynk.shared.domain.hangout.model.HangoutVibe
import kotlin.time.Instant
import kotlinx.serialization.Serializable

@Serializable
data class HangoutDto(
    val id: String,
    val hostId: String,
    val name: String,
    val description: String?,
    val vibe: HangoutVibe,
    val status: HangoutStatus,
    val scheduledAt: Instant,
    val maxAttendees: Int?,
    val participantCount: Int,
    val chosenSpot: SpotDto?,
    val totalCost: Double?,
    val participants: List<HangoutParticipantDto>,
    val createdAt: Instant
)