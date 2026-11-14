package com.eeseka.lynk.core.data.notification.dto

import kotlinx.serialization.Serializable

@Serializable
data class UnreadCountDto(
    val count: Long
)
