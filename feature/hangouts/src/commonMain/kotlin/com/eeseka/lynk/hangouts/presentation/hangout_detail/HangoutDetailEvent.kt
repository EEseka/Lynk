package com.eeseka.lynk.hangouts.presentation.hangout_detail

import com.eeseka.lynk.shared.design_system.components.modals_and_overlays.LynkFlashType
import com.eeseka.lynk.shared.presentation.util.UiText

sealed interface HangoutDetailEvent {
    data class ShowMessage(
        val message: UiText,
        val type: LynkFlashType
    ) : HangoutDetailEvent

    // Left the hangout — the detail can no longer be viewed, so close it.
    data object NavigateBack : HangoutDetailEvent
}