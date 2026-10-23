package com.eeseka.lynk.shared.domain.assistant.model

import kotlin.time.Instant

data class AssistantMessage(
    val id: String,
    val role: AssistantMessageRole,
    val status: AssistantMessageStatus?,
    val content: String,
    val failureReason: AssistantFailureReason?,
    val recommendations: List<AssistantRecommendation>,
    val createdAt: Instant
)
