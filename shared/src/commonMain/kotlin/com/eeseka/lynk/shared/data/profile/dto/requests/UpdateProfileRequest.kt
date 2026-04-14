package com.eeseka.lynk.shared.data.profile.dto.requests

import kotlinx.serialization.Serializable

@Serializable
data class UpdateProfileRequest(
    val username: String,
    val displayName: String,
    val profilePhotoUrl: String?
)