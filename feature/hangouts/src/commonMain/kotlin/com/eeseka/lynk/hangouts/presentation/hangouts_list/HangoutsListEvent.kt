package com.eeseka.lynk.hangouts.presentation.hangouts_list

import com.eeseka.lynk.shared.presentation.util.UiText

sealed interface HangoutsListEvent {
    data class Error(val error: UiText) : HangoutsListEvent
}