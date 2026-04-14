package com.eeseka.lynk.shared.data.auth.dto.requests

import kotlinx.serialization.Serializable


@Serializable
data class GoogleAuthRequest(
    val token: String
)