package com.eeseka.lynk.shared.presentation.notification.mappers

import com.eeseka.lynk.shared.domain.notification.model.Notification
import com.eeseka.lynk.shared.presentation.notification.model.NotificationUi

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
