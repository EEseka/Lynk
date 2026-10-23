package com.eeseka.lynk.assistant.presentation

import com.eeseka.lynk.shared.presentation.util.UiText

sealed interface AssistantEvent {
    data class Error(val message: UiText) : AssistantEvent
    data object SpotSuggested : AssistantEvent
    data object ChatCleared : AssistantEvent
    data object ChatExpired : AssistantEvent
    data object AnswerFailed : AssistantEvent
}
