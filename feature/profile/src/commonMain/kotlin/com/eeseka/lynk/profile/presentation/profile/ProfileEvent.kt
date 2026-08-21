package com.eeseka.lynk.profile.presentation.profile

import com.eeseka.lynk.shared.presentation.util.UiText

sealed interface ProfileEvent {
    data class Error(val message: UiText) : ProfileEvent
    data object ProfileSaved : ProfileEvent
}