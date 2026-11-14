package com.eeseka.lynk.core.data.profile.dto.requests

import kotlinx.serialization.Serializable

@Serializable
data class CreateProfileRequest(
    val username: String,
    val displayName: String,
    val profilePhotoUrl: String?
)