package com.eeseka.lynk.shared.presentation.hangout.model

import androidx.compose.runtime.Stable
import com.eeseka.lynk.shared.domain.hangout.model.HangoutStatus
import com.eeseka.lynk.shared.domain.hangout.model.HangoutVibe
import com.eeseka.lynk.shared.presentation.spot.model.SpotUi
import kotlin.time.Instant

@Stable
data class HangoutUi(
    val id: String,
    val hostId: String,
    val name: String,
    val description: String?,
    val vibe: HangoutVibe,
    val status: HangoutStatus,
    val scheduledAt: Instant,
    val maxAttendees: Int?,
    val chosenSpot: SpotUi?,
    val totalCost: Double?,
    val costPerPerson: Double?,
    val participants: List<HangoutParticipantUi>,
    val createdAt: Instant
)