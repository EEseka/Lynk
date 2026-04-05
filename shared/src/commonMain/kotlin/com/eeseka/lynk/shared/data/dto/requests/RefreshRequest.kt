package com.eeseka.lynk.shared.data.dto.requests

import kotlinx.serialization.Serializable

@Serializable
data class RefreshRequest(
    val refreshToken: String
)
