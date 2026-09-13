package com.eeseka.lynk.hangouts.presentation.hangout_detail

import com.eeseka.lynk.shared.design_system.components.modals_and_overlays.LynkFlashType
import com.eeseka.lynk.shared.presentation.util.UiText

sealed interface HangoutDetailEvent {
    data class Error(val message: UiText) : HangoutDetailEvent

    data object HangoutCompleted : HangoutDetailEvent
    data object HangoutCancelled : HangoutDetailEvent
    data object HangoutLeft : HangoutDetailEvent
    data object InviteSent : HangoutDetailEvent
    data object InviteWithdrawn : HangoutDetailEvent

    data class LobbyAnnouncement(
        val message: UiText,
        val type: LynkFlashType
    ) : HangoutDetailEvent

    data object NavigateBack : HangoutDetailEvent
}
