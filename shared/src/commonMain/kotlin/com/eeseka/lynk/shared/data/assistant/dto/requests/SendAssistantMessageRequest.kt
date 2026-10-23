package com.eeseka.lynk.shared.data.assistant.dto.requests

import kotlinx.serialization.Serializable

@Serializable
data class SendAssistantMessageRequest(
    val hangoutId: String?,
    val discoverChatId: String?,
    val latitude: Double?,
    val longitude: Double?,
    val timeZone: String,
    val content: String
)
