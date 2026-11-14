package com.eeseka.lynk.core.data.assistant.dto

import kotlinx.serialization.Serializable

@Serializable
data class AssistantUsageDto(
    val used: Int,
    val limit: Int,
    val resetsInSeconds: Long?
)
