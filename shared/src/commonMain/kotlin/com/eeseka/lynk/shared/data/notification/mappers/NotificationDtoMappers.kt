package com.eeseka.lynk.shared.data.notification.mappers

import com.eeseka.lynk.shared.data.notification.dto.NotificationDto
import com.eeseka.lynk.shared.domain.notification.model.Notification

fun NotificationDto.toDomain(): Notification {
    return Notification(
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
