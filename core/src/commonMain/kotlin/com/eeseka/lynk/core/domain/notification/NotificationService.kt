package com.eeseka.lynk.core.domain.notification

import com.eeseka.lynk.core.domain.notification.model.Notification
import com.eeseka.lynk.core.domain.util.DataError
import com.eeseka.lynk.core.domain.util.EmptyResult
import com.eeseka.lynk.core.domain.util.Result

interface NotificationService {
    suspend fun getNotifications(
        before: String? = null
    ): Result<List<Notification>, DataError.Remote>

    suspend fun getUnreadCount(): Result<Long, DataError.Remote>

    suspend fun markAsRead(notificationId: String): EmptyResult<DataError.Remote>

    suspend fun markAllAsRead(): EmptyResult<DataError.Remote>
}