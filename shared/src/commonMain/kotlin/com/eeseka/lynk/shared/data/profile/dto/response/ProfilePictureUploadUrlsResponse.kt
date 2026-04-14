package com.eeseka.lynk.shared.data.profile.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class ProfilePictureUploadUrlsResponse(
    val uploadUrl: String,
    val publicUrl: String,
    val headers: Map<String, String>,
    val expiresAt: String
)