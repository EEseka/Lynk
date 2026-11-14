package com.eeseka.lynk.core.data.hangout.dto.requests

import kotlinx.serialization.Serializable

@Serializable
data class GenerateHangoutPhotoUploadUrlsRequest(
    val count: Int
)
