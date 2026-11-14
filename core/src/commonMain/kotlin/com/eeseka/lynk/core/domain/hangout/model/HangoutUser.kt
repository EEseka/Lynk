package com.eeseka.lynk.core.domain.hangout.model

data class HangoutUser(
    val userId: String,
    val username: String,
    val displayName: String,
    val profilePictureUrl: String?
)