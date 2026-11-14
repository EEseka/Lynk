package com.eeseka.lynk.core.domain.assistant.model

data class AssistantUsage(
    val used: Int,
    val limit: Int,
    val resetsInSeconds: Long? // Null until the first message of the day
)
