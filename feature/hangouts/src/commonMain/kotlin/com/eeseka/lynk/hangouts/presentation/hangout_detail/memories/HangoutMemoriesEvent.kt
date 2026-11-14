package com.eeseka.lynk.hangouts.presentation.hangout_detail.memories

import com.eeseka.lynk.core.presentation.util.UiText

sealed interface HangoutMemoriesEvent {
    data class Error(val message: UiText) : HangoutMemoriesEvent
    data object PhotosAdded : HangoutMemoriesEvent
}
