package com.eeseka.lynk.shared.presentation.hangout.model

data class HangoutUserUi(
    val userId: String,
    val username: String,
    val displayName: String,
    val initials: String,
    val profilePictureUrl: String?
)