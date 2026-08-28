package com.eeseka.lynk.shared.data.notification.dto.requests

import com.eeseka.lynk.shared.domain.notification.model.DevicePlatform
import kotlinx.serialization.Serializable

@Serializable
data class RegisterDeviceTokenRequest(
    val token: String,
    val platform: DevicePlatform
)
