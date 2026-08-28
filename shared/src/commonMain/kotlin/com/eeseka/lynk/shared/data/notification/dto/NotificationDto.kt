package com.eeseka.lynk.shared.data.notification.dto

import com.eeseka.lynk.shared.domain.notification.model.NotificationType
import kotlin.time.Instant
import kotlinx.serialization.Serializable

@Serializable
data class NotificationDto(
    val id: String,
    val type: NotificationType,
    val hangoutId: String,
    val hangoutName: String,
    val actorDisplayName: String?,
    val amountKobo: Long?,
    val isRead: Boolean,
    val createdAt: Instant
)
