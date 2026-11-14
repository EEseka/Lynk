package com.eeseka.lynk.hangouts.presentation.hangouts_list

import com.eeseka.lynk.core.presentation.util.UiText

sealed interface HangoutsListEvent {
    data class Error(val message: UiText) : HangoutsListEvent
}
