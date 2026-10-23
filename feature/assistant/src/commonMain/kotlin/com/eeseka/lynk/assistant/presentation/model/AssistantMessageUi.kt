package com.eeseka.lynk.assistant.presentation.model

import androidx.compose.runtime.Immutable
import com.eeseka.lynk.shared.domain.assistant.model.AssistantFailureReason
import com.eeseka.lynk.shared.domain.assistant.model.AssistantMessageRole
import com.eeseka.lynk.shared.domain.assistant.model.AssistantMessageStatus
import kotlinx.collections.immutable.ImmutableList

@Immutable
data class AssistantMessageUi(
    val id: String,
    val role: AssistantMessageRole,
    val status: AssistantMessageStatus?,
    val content: String,
    val failureReason: AssistantFailureReason?, // Null on a failed answer means it never reached the phone
    val recommendations: ImmutableList<AssistantRecommendationUi>
)
