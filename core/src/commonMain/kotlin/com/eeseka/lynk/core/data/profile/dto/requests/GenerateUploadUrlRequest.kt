package com.eeseka.lynk.core.data.profile.dto.requests

import kotlinx.serialization.Serializable

@Serializable
data class GenerateUploadUrlRequest(
    val mimeType: String
)