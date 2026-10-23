package com.eeseka.lynk.assistant.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.eeseka.lynk.assistant.presentation.AssistantAction
import com.eeseka.lynk.assistant.presentation.mappers.getMessage
import com.eeseka.lynk.assistant.presentation.mappers.getTitle
import com.eeseka.lynk.assistant.presentation.model.AssistantMessageUi
import com.eeseka.lynk.assistant.presentation.model.SpotSuggestState
import com.eeseka.lynk.assistant.presentation.preview.previewAssistantMessages
import com.eeseka.lynk.assistant.presentation.util.rememberRevealedText
import com.eeseka.lynk.shared.design_system.components.util.AppHaptic
import com.eeseka.lynk.shared.design_system.components.util.rememberAppHaptic
import com.eeseka.lynk.shared.design_system.theme.LynkTheme
import com.eeseka.lynk.shared.domain.assistant.model.AssistantMessageStatus
import com.eeseka.lynk.shared.domain.assistant.model.AssistantStatusKind

@Composable
fun AssistantAnswer(
    message: AssistantMessageUi,
    isStreaming: Boolean,
    answerStatus: AssistantStatusKind?,
    canRetry: Boolean,
    loadingSpotId: String?,
    suggestStateOf: (String) -> SpotSuggestState?,
    onAction: (AssistantAction) -> Unit,
    onCopyClick: () -> Unit,
    onShareClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hapticFeedback = rememberAppHaptic()

    // Only an answer that opened mid-stream types out; one already written shows whole
    val wasStreamed = remember { isStreaming }
    val revealedText = if (wasStreamed) rememberRevealedText(message.content) else message.content

    val isFinished = message.status == AssistantMessageStatus.ANSWERED && revealedText.length == message.content.length
    LaunchedEffect(isFinished) {
        if (isFinished && wasStreamed) hapticFeedback(AppHaptic.Success)
    }

    val hasText = message.content.isNotBlank()

    Column(modifier = modifier.fillMaxWidth()) {
        if (hasText) {
            AssistantAnswerText(
                text = revealedText,
                isStreamed = wasStreamed
            )
        }

        AnimatedVisibility(
            visible = message.status == AssistantMessageStatus.PENDING && isStreaming,
            exit = fadeOut() + shrinkVertically()
        ) {
            AssistantStatusLine(
                title = answerStatus.getTitle(),
                modifier = Modifier.padding(top = if (hasText) 12.dp else 0.dp)
            )
        }

        when (message.status) {
            AssistantMessageStatus.PENDING -> {
                if (!isStreaming) {
                    AssistantPendingAnswer(
                        onCheckAgainClick = {
                            hapticFeedback(AppHaptic.ImpactLight)
                            onAction(AssistantAction.OnCheckPendingAnswerClick)
                        },
                        modifier = Modifier.padding(top = if (hasText) 12.dp else 0.dp)
                    )
                }
            }

            AssistantMessageStatus.FAILED -> {
                AssistantFailedAnswer(
                    message = message.failureReason.getMessage(),
                    onRetryClick = if (canRetry) {
                        {
                            hapticFeedback(AppHaptic.ImpactLight)
                            onAction(AssistantAction.OnRetryAnswerClick(message.id))
                        }
                    } else null,
                    modifier = Modifier.padding(top = if (hasText) 12.dp else 0.dp)
                )
            }

            AssistantMessageStatus.ANSWERED, null -> Unit
        }

        if (isFinished) {
            message.recommendations.forEachIndexed { index, recommendation ->
                val cardVisibility = remember { MutableTransitionState(!wasStreamed).apply { targetState = true } }

                AnimatedVisibility(
                    visibleState = cardVisibility,
                    enter = fadeIn() + expandVertically()
                ) {
                    AssistantSpotCard(
                        modifier = Modifier.padding(top = if (hasText || index > 0) 12.dp else 0.dp),
                        spotName = recommendation.spot.name,
                        spotPhotos = recommendation.spot.photoUrls,
                        spotCategory = recommendation.spot.category,
                        spotPriceLevel = recommendation.spot.priceLevel,
                        reason = recommendation.reason,
                        suggestState = suggestStateOf(recommendation.spot.id),
                        isLoading = recommendation.spot.id == loadingSpotId,
                        onClick = {
                            hapticFeedback(AppHaptic.ImpactLight)
                            onAction(AssistantAction.OnRecommendationClick(recommendation.spot.id))
                        },
                        onSuggestClick = {
                            hapticFeedback(AppHaptic.ImpactMedium)
                            onAction(AssistantAction.OnSuggestSpot(recommendation.spot.id))
                        },
                        onPhotoLoadFailed = { onAction(AssistantAction.OnCoverPhotoLoadFailed(recommendation.spot.id)) }
                    )
                }
            }
        }

        if (message.status == AssistantMessageStatus.ANSWERED && hasText) {
            AnimatedVisibility(
                visible = isFinished,
                enter = fadeIn() + expandVertically()
            ) {
                AssistantAnswerActions(
                    modifier = Modifier.padding(top = 12.dp),
                    onCopyClick = onCopyClick,
                    onShareClick = onShareClick,
                    onRetryClick = if (canRetry) {
                        {
                            hapticFeedback(AppHaptic.ImpactLight)
                            onAction(AssistantAction.OnRetryAnswerClick(message.id))
                        }
                    } else null
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun AssistantAnswerPreview() {
    val (failedAnswer, _, answer) = previewAssistantMessages
    LynkTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            AssistantAnswer(
                message = answer,
                isStreaming = false,
                answerStatus = null,
                canRetry = true,
                loadingSpotId = null,
                suggestStateOf = { SpotSuggestState.SUGGEST },
                onAction = {},
                onCopyClick = {},
                onShareClick = {}
            )
            AssistantAnswer(
                message = failedAnswer,
                isStreaming = false,
                answerStatus = null,
                canRetry = true,
                loadingSpotId = null,
                suggestStateOf = { null },
                onAction = {},
                onCopyClick = {},
                onShareClick = {}
            )
        }
    }
}
