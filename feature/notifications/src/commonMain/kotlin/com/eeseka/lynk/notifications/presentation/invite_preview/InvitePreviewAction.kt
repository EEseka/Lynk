package com.eeseka.lynk.notifications.presentation.invite_preview

sealed interface InvitePreviewAction {
    data class Init(val hangoutId: String) : InvitePreviewAction
    data object OnAcceptClick : InvitePreviewAction
    data object OnDeclineClick : InvitePreviewAction
}