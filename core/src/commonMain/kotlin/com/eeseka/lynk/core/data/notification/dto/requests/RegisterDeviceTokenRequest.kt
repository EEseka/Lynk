package com.eeseka.lynk.core.data.notification.dto.requests

import com.eeseka.lynk.core.domain.notification.model.DevicePlatform
import kotlinx.serialization.Serializable

@Serializable
data class RegisterDeviceTokenRequest(
    val token: String,
    val platform: DevicePlatform
)
