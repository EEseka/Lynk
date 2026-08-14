package com.eeseka.lynk.shared.data.hangout.dto

import kotlinx.serialization.Serializable

@Serializable
data class HangoutUserDto(
    val userId: String,
    val username: String,
    val displayName: String,
    val profilePictureUrl: String?
)
