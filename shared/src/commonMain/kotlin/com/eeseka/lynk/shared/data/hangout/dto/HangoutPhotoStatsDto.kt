package com.eeseka.lynk.shared.data.hangout.dto

import kotlinx.serialization.Serializable

@Serializable
data class HangoutPhotoStatsDto(
    val photoCount: Int,
    val myPhotoCount: Int
)
