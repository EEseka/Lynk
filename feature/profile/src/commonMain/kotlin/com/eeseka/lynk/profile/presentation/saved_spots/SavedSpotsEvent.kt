package com.eeseka.lynk.profile.presentation.saved_spots

import com.eeseka.lynk.core.presentation.util.UiText

sealed interface SavedSpotsEvent {
    data class Error(val message: UiText) : SavedSpotsEvent
}