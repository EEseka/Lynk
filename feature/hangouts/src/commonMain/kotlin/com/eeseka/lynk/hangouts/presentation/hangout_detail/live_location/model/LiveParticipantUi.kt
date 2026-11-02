package com.eeseka.lynk.hangouts.presentation.hangout_detail.live_location.model

import androidx.compose.runtime.Immutable
import com.eeseka.lynk.shared.presentation.hangout.model.HangoutParticipantUi

@Immutable
data class LiveParticipantUi(
    val participant: HangoutParticipantUi,
    val latitude: Double,
    val longitude: Double,
    val minutesSinceShared: Int
)
