package com.eeseka.lynk.shared.domain.assistant.model

sealed interface AssistantStreamEvent {
    data class Started(
        val messageId: String?,
        val discoverChatId: String?
    ) : AssistantStreamEvent

    data class Status(val kind: AssistantStatusKind) : AssistantStreamEvent

    data class Text(val text: String) : AssistantStreamEvent

    data class Spots(val recommendations: List<AssistantRecommendation>) : AssistantStreamEvent

    data object Done : AssistantStreamEvent

    data class Error(val reason: AssistantFailureReason) : AssistantStreamEvent
}
