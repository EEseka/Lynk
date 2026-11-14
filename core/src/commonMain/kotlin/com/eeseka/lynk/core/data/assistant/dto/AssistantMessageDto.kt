package com.eeseka.lynk.core.data.assistant.dto

import com.eeseka.lynk.core.domain.assistant.model.AssistantFailureReason
import com.eeseka.lynk.core.domain.assistant.model.AssistantMessageRole
import com.eeseka.lynk.core.domain.assistant.model.AssistantMessageStatus
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class AssistantMessageDto(
    val id: String,
    val role: AssistantMessageRole,
    val status: AssistantMessageStatus?,
    val content: String,
    val failureReason: AssistantFailureReason?,
    val recommendations: List<AssistantRecommendationDto>,
    val createdAt: Instant
)
