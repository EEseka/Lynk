package com.eeseka.lynk.shared.data.notification.dto

import kotlinx.serialization.Serializable

@Serializable
data class UnreadCountDto(
    val count: Long
)
