package com.eeseka.lynk.shared.domain.notification.model

import kotlin.time.Instant

data class Notification(
    val id: String,
    val type: NotificationType,
    val hangoutId: String,
    val hangoutName: String,
    val actorDisplayName: String?,
    val amountKobo: Long?,
    val isRead: Boolean,
    val createdAt: Instant
)