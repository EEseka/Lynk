package com.eeseka.lynk.shared.presentation.hangout.model

import androidx.compose.runtime.Stable

@Stable
data class HangoutUserUi(
    val userId: String,
    val username: String,
    val displayName: String,
    val initials: String,
    val profilePictureUrl: String?
)