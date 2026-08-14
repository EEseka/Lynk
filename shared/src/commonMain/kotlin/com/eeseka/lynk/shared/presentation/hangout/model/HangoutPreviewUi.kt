package com.eeseka.lynk.shared.presentation.hangout.model

import androidx.compose.runtime.Stable
import com.eeseka.lynk.shared.domain.hangout.model.HangoutStatus
import com.eeseka.lynk.shared.domain.hangout.model.HangoutVibe
import com.eeseka.lynk.shared.presentation.spot.model.SpotUi
import kotlin.time.Instant

@Stable
data class HangoutPreviewUi(
    val id: String,
    val hostId: String,
    val name: String,
    val description: String?,
    val vibe: HangoutVibe,
    val status: HangoutStatus,
    val scheduledAt: Instant,
    val maxAttendees: Int?,
    val participantCount: Int,
    val chosenSpot: SpotUi?,
    val attendees: List<HangoutUserUi>,
    val createdAt: Instant
)