package com.eeseka.lynk.assistant.presentation.preview

import com.eeseka.lynk.assistant.presentation.model.AssistantMessageUi
import com.eeseka.lynk.assistant.presentation.model.AssistantRecommendationUi
import com.eeseka.lynk.shared.domain.assistant.model.AssistantFailureReason
import com.eeseka.lynk.shared.domain.assistant.model.AssistantMessageRole
import com.eeseka.lynk.shared.domain.assistant.model.AssistantMessageStatus
import com.eeseka.lynk.shared.presentation.preview.previewSpots
import kotlinx.collections.immutable.persistentListOf

val previewAssistantMessages = persistentListOf(
    AssistantMessageUi(
        id = "m4",
        role = AssistantMessageRole.ASSISTANT,
        status = AssistantMessageStatus.FAILED,
        content = "",
        failureReason = AssistantFailureReason.TIMEOUT,
        recommendations = persistentListOf()
    ),
    AssistantMessageUi(
        id = "m3",
        role = AssistantMessageRole.USER,
        status = null,
        content = "Anything with live music?",
        failureReason = null,
        recommendations = persistentListOf()
    ),
    AssistantMessageUi(
        id = "m2",
        role = AssistantMessageRole.ASSISTANT,
        status = AssistantMessageStatus.ANSWERED,
        content = "Here are two relaxed spots near the group that suit a chill Friday.",
        failureReason = null,
        recommendations = persistentListOf(
            AssistantRecommendationUi(
                spot = previewSpots[0],
                reason = "Calm garden seating and small plates, a short drive for most of the group."
            ),
            AssistantRecommendationUi(
                spot = previewSpots[1],
                reason = "Already on the ballot, and it has a quieter lounge upstairs."
            )
        )
    ),
    AssistantMessageUi(
        id = "m1",
        role = AssistantMessageRole.USER,
        status = null,
        content = "Somewhere chill for drinks, not too pricey",
        failureReason = null,
        recommendations = persistentListOf()
    )
)
