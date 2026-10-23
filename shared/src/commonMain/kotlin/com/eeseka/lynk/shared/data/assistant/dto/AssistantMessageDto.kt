package com.eeseka.lynk.shared.data.assistant.dto

import com.eeseka.lynk.shared.domain.assistant.model.AssistantFailureReason
import com.eeseka.lynk.shared.domain.assistant.model.AssistantMessageRole
import com.eeseka.lynk.shared.domain.assistant.model.AssistantMessageStatus
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
