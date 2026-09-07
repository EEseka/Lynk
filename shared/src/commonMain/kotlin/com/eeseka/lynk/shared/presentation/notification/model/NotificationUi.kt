package com.eeseka.lynk.shared.presentation.notification.model

import androidx.compose.runtime.Immutable
import com.eeseka.lynk.shared.domain.notification.model.NotificationType
import kotlin.time.Instant

@Immutable
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