package com.eeseka.lynk.shared.presentation.notification.model

import androidx.compose.runtime.Stable
import com.eeseka.lynk.shared.domain.notification.model.NotificationType
import kotlin.time.Instant

@Stable
data class NotificationUi(
    val id: String,
    val type: NotificationType,
    val hangoutId: String,
    val hangoutName: String,
    val actorDisplayName: String?,
    val amountKobo: Long?,
    val isRead: Boolean,
    val createdAt: Instant
)