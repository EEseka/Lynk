package com.eeseka.lynk.notifications.presentation.invite_preview

import com.eeseka.lynk.shared.presentation.util.UiText

sealed interface InvitePreviewEvent {
    data class Error(val message: UiText) : InvitePreviewEvent

    data class Accepted(val hangoutId: String) : InvitePreviewEvent

    data object Dismissed : InvitePreviewEvent

    data class AlreadyAnswered(val hangoutId: String) : InvitePreviewEvent

    data object InviteWithdrawn : InvitePreviewEvent
}