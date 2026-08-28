package com.eeseka.lynk.notifications.presentation.notifications

import androidx.compose.runtime.Stable
import com.eeseka.lynk.shared.presentation.notification.model.NotificationUi

@Stable
data class NotificationsState(
    val notifications: List<NotificationUi> = emptyList(),
    val isLoading: Boolean = false,
    val isEndReached: Boolean = false,
    val isMarkingAllRead: Boolean = false,
    val previewHangoutId: String? = null
)