package com.eeseka.lynk.shared.presentation.hangout.model

import androidx.compose.runtime.Stable
import com.eeseka.lynk.shared.domain.hangout.model.HangoutStatus
import com.eeseka.lynk.shared.domain.hangout.model.HangoutVibe
import kotlin.time.Instant

@Stable
data class HangoutSummaryUi(
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
