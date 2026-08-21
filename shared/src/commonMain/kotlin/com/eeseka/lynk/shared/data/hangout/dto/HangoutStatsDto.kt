package com.eeseka.lynk.shared.data.hangout.dto

import kotlinx.serialization.Serializable

@Serializable
data class HangoutStatsDto(
    val hostedCount: Long,
    val attendedCount: Long
)
