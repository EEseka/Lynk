package com.eeseka.lynk.hangouts.presentation.hangout_detail.live_location.model

import androidx.compose.runtime.Immutable
import kotlin.time.Instant

@Immutable
data class LiveLocationUi(
    val latitude: Double,
    val longitude: Double,
    val sharedAt: Instant
)
