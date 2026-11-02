package com.eeseka.lynk.shared.domain.lobby.model

import kotlin.time.Instant

data class LiveLocation(
    val userId: String,
    val latitude: Double,
    val longitude: Double,
    val sharedAt: Instant
)
