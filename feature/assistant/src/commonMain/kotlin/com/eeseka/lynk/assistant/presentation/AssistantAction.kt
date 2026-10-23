package com.eeseka.lynk.assistant.presentation

sealed interface AssistantAction {
    data class OnOpenHangoutChat(val hangoutId: String) : AssistantAction
    data class OnOpenDiscoverChat(
        val latitude: Double,
        val longitude: Double,
        val areaName: String?
    ) : AssistantAction

    // Asking
    data object OnSendClick : AssistantAction
    data class OnStarterPromptClick(val prompt: String) : AssistantAction
    data class OnRetryAnswerClick(val answerId: String) : AssistantAction

    // Hangout history
    data object LoadNextPage : AssistantAction
    data object OnRetryClick : AssistantAction
    data object OnCheckPendingAnswerClick : AssistantAction
    data object OnClearChatConfirmed : AssistantAction

    // Spot cards
    data class OnRecommendationClick(val spotId: String) : AssistantAction
    data class OnCoverPhotoLoadFailed(val spotId: String) : AssistantAction
    data object OnDismissSpotSheet : AssistantAction
    data class OnToggleSaveSpot(val spotId: String, val isCurrentlySaved: Boolean) : AssistantAction
    data class OnSuggestSpot(val spotId: String) : AssistantAction
}
