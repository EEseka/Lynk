package com.eeseka.lynk.core.data.hangout.dto

import kotlinx.serialization.Serializable

@Serializable
data class HangoutPhotoStatsDto(
    val photoCount: Int,
    val myPhotoCount: Int
)
