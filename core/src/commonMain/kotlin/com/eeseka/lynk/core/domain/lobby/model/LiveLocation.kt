package com.eeseka.lynk.core.domain.lobby.model

import kotlin.time.Instant

data class LiveLocation(
    val userId: String,
    val latitude: Double,
    val longitude: Double,
    val sharedAt: Instant
)
