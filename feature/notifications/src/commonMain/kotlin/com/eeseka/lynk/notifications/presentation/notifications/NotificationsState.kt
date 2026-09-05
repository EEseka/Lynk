package com.eeseka.lynk.notifications.presentation.notifications

import com.eeseka.lynk.shared.presentation.notification.model.NotificationUi
import com.eeseka.lynk.shared.presentation.util.UiText
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class NotificationsState(
    val notifications: ImmutableList<NotificationUi> = persistentListOf(),
    val isLoading: Boolean = false,
    val isEndReached: Boolean = false,
    val isMarkingAllRead: Boolean = false,
    val previewHangoutId: String? = null,
    val loadError: UiText? = null
)