package com.eeseka.lynk.assistant.presentation.mappers

import androidx.compose.runtime.Composable
import com.eeseka.lynk.assistant.presentation.model.AssistantMessageUi
import com.eeseka.lynk.assistant.presentation.model.AssistantRecommendationUi
import com.eeseka.lynk.shared.domain.assistant.model.AssistantFailureReason
import com.eeseka.lynk.shared.domain.assistant.model.AssistantMessage
import com.eeseka.lynk.shared.domain.assistant.model.AssistantRecommendation
import com.eeseka.lynk.shared.domain.assistant.model.AssistantStatusKind
import com.eeseka.lynk.shared.presentation.spot.mappers.toSpotUi
import kotlinx.collections.immutable.toImmutableList
import lynk.feature.assistant.generated.resources.Res
import lynk.feature.assistant.generated.resources.failure_error
import lynk.feature.assistant.generated.resources.failure_lost
import lynk.feature.assistant.generated.resources.failure_not_counted
import lynk.feature.assistant.generated.resources.failure_refused
import lynk.feature.assistant.generated.resources.failure_timeout
import lynk.feature.assistant.generated.resources.status_reading_details
import lynk.feature.assistant.generated.resources.status_searching
import lynk.feature.assistant.generated.resources.status_thinking
import org.jetbrains.compose.resources.stringResource

fun AssistantMessage.toAssistantMessageUi() = AssistantMessageUi(
    id = id,
    role = role,
    status = status,
    content = content,
    failureReason = failureReason,
    recommendations = recommendations.map { it.toAssistantRecommendationUi() }.toImmutableList()
)

fun AssistantRecommendation.toAssistantRecommendationUi() = AssistantRecommendationUi(
    spot = spot.toSpotUi(),
    reason = reason
)

@Composable
fun AssistantFailureReason?.getMessage(): String {
    val reason = when (this) {
        AssistantFailureReason.TIMEOUT -> stringResource(Res.string.failure_timeout)
        AssistantFailureReason.REFUSED -> stringResource(Res.string.failure_refused)
        AssistantFailureReason.ERROR -> stringResource(Res.string.failure_error)
        null -> return stringResource(Res.string.failure_lost)
    }
    return "$reason ${stringResource(Res.string.failure_not_counted)}"
}

@Composable
fun AssistantStatusKind?.getTitle(): String {
    return when (this) {
        AssistantStatusKind.SEARCHING -> stringResource(Res.string.status_searching)
        AssistantStatusKind.READING_DETAILS -> stringResource(Res.string.status_reading_details)
        null -> stringResource(Res.string.status_thinking)
    }
}
