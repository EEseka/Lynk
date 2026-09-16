package com.eeseka.lynk.testing.data

import com.eeseka.lynk.shared.domain.notification.NotificationService
import com.eeseka.lynk.shared.domain.notification.model.Notification
import com.eeseka.lynk.shared.domain.util.DataError
import com.eeseka.lynk.shared.domain.util.EmptyResult
import com.eeseka.lynk.shared.domain.util.Result
import kotlin.time.Instant

class FakeNotificationService : NotificationService {
    var shouldReturnError = false
    var errorToReturn = DataError.Remote.SERVER_ERROR
    var notifications = mutableListOf<Notification>()

    // Like the backend, "before" is a createdAt cursor: the next page holds only older notifications
    override suspend fun getNotifications(before: String?): Result<List<Notification>, DataError.Remote> {
        if (shouldReturnError) return Result.Failure(errorToReturn)

        val page = notifications.filter { notification ->
            before == null || notification.createdAt < Instant.parse(before)
        }
        return Result.Success(page)
    }

    override suspend fun getUnreadCount(): Result<Long, DataError.Remote> {
        if (shouldReturnError) return Result.Failure(errorToReturn)
        return Result.Success(notifications.count { !it.isRead }.toLong())
    }

    override suspend fun markAsRead(notificationId: String): EmptyResult<DataError.Remote> {
        if (shouldReturnError) return Result.Failure(errorToReturn)

        val index = notifications.indexOfFirst { it.id == notificationId }
        if (index == -1) return Result.Failure(DataError.Remote.NOT_FOUND)
        notifications[index] = notifications[index].copy(isRead = true)
        return Result.Success(Unit)
    }

    override suspend fun markAllAsRead(): EmptyResult<DataError.Remote> {
        if (shouldReturnError) return Result.Failure(errorToReturn)
        notifications = notifications.map { it.copy(isRead = true) }.toMutableList()
        return Result.Success(Unit)
    }
}
