package com.eeseka.lynk.notifications.presentation.notifications

import com.eeseka.lynk.shared.domain.notification.model.NotificationType

sealed interface NotificationsAction {
    data class OnNotificationClick(
        val notificationId: String,
        val hangoutId: String,
        val type: NotificationType
    ) : NotificationsAction

    data class OnOpenInvitePreview(val hangoutId: String) : NotificationsAction
    data object OnMarkAllReadClick : NotificationsAction
    data object OnDismissInvitePreview : NotificationsAction
    data object LoadNextPage : NotificationsAction
}