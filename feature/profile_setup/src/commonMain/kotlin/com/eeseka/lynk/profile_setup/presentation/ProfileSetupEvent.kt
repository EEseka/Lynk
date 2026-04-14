package com.eeseka.lynk.profile_setup.presentation

import com.eeseka.lynk.shared.presentation.util.UiText

sealed interface ProfileSetupEvent {
    data object Success : ProfileSetupEvent
    data class Error(val error: UiText) : ProfileSetupEvent
}