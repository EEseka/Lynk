package com.eeseka.lynk.core.data.hangout.dto

import kotlinx.serialization.Serializable

@Serializable
data class HangoutStatsDto(
    val hostedCount: Long,
    val attendedCount: Long
)
