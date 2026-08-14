package com.eeseka.lynk.shared.domain.hangout.model

data class HangoutUser(
    val userId: String,
    val username: String,
    val displayName: String,
    val profilePictureUrl: String?
)