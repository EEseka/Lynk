package com.eeseka.lynk.shared.data.assistant.dto.sse

import com.eeseka.lynk.shared.data.assistant.dto.AssistantRecommendationDto
import com.eeseka.lynk.shared.domain.assistant.model.AssistantFailureReason
import com.eeseka.lynk.shared.domain.assistant.model.AssistantStatusKind
import kotlinx.serialization.Serializable

enum class AssistantSseEventType {
    STARTED,
    STATUS,
    TEXT,
    SPOTS,
    DONE,
    ERROR
}

@Serializable
sealed interface AssistantSseEventDto {

    @Serializable
    data class StartedDto(
        val messageId: String,
        val discoverChatId: String?
    ) : AssistantSseEventDto

    @Serializable
    data class StatusDto(
        val kind: AssistantStatusKind
    ) : AssistantSseEventDto

    @Serializable
    data class TextDto(
        val text: String
    ) : AssistantSseEventDto

    @Serializable
    data class SpotsDto(
        val recommendations: List<AssistantRecommendationDto>
    ) : AssistantSseEventDto

    @Serializable
    data class ErrorDto(
        val reason: AssistantFailureReason
    ) : AssistantSseEventDto
}
