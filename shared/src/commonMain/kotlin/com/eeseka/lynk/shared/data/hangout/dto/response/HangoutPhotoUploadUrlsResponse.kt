package com.eeseka.lynk.shared.data.hangout.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class HangoutPhotoUploadUrlsResponse(
    val photoId: String,
    val fullUploadUrl: String,
    val thumbnailUploadUrl: String,
    val headers: Map<String, String>,
    val expiresAt: String
)
