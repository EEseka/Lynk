package com.eeseka.lynk.shared.data.assistant.mappers

import com.eeseka.lynk.shared.data.assistant.dto.AssistantMessageDto
import com.eeseka.lynk.shared.data.assistant.dto.AssistantRecommendationDto
import com.eeseka.lynk.shared.data.assistant.dto.AssistantUsageDto
import com.eeseka.lynk.shared.data.spot.mappers.toDomain
import com.eeseka.lynk.shared.domain.assistant.model.AssistantMessage
import com.eeseka.lynk.shared.domain.assistant.model.AssistantRecommendation
import com.eeseka.lynk.shared.domain.assistant.model.AssistantUsage

fun AssistantMessageDto.toDomain(): AssistantMessage {
    return AssistantMessage(
        id = id,
        role = role,
        status = status,
        content = content,
        failureReason = failureReason,
        recommendations = recommendations.map { it.toDomain() },
        createdAt = createdAt
    )
}

fun AssistantRecommendationDto.toDomain(): AssistantRecommendation {
    return AssistantRecommendation(
        spot = spot.toDomain(),
        reason = reason
    )
}

fun AssistantUsageDto.toDomain(): AssistantUsage {
    return AssistantUsage(
        used = used,
        limit = limit,
        resetsInSeconds = resetsInSeconds
    )
}
