package com.eeseka.lynk.shared.presentation.hangout.model

import androidx.compose.runtime.Stable
import com.eeseka.lynk.shared.domain.hangout.model.ParticipantRole
import com.eeseka.lynk.shared.domain.hangout.model.RsvpStatus

@Stable
data class HangoutParticipantUi(
    val userId: String,
    val username: String,
    val displayName: String,
    val initials: String,
    val profilePictureUrl: String?,
    val role: ParticipantRole,
    val rsvpStatus: RsvpStatus,
    val hasPaid: Boolean
)