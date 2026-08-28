package com.eeseka.lynk.notifications.presentation.notifications

import com.eeseka.lynk.shared.presentation.util.UiText

sealed interface NotificationsEvent {
    data class Error(val message: UiText) : NotificationsEvent

    data class NavigateToHangout(val hangoutId: String) : NotificationsEvent
}