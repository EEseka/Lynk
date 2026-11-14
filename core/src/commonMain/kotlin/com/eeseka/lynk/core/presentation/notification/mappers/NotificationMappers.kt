package com.eeseka.lynk.core.presentation.notification.mappers

import com.eeseka.lynk.core.domain.notification.model.Notification
import com.eeseka.lynk.core.presentation.notification.model.NotificationUi

fun Notification.toNotificationUi(): NotificationUi {
    return NotificationUi(
        id = id,
        type = type,
        hangoutId = hangoutId,
        hangoutName = hangoutName,
        actorDisplayName = actorDisplayName,
        amountKobo = amountKobo,
        isRead = isRead,
        createdAt = createdAt
    )
}
