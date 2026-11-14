package com.eeseka.lynk.core.data.notification

import com.eeseka.lynk.core.data.networking.get
import com.eeseka.lynk.core.data.networking.patch
import com.eeseka.lynk.core.data.notification.dto.NotificationDto
import com.eeseka.lynk.core.data.notification.dto.UnreadCountDto
import com.eeseka.lynk.core.data.notification.mappers.toDomain
import com.eeseka.lynk.core.domain.notification.NotificationService
import com.eeseka.lynk.core.domain.notification.model.Notification
import com.eeseka.lynk.core.domain.util.DataError
import com.eeseka.lynk.core.domain.util.EmptyResult
import com.eeseka.lynk.core.domain.util.Result
import com.eeseka.lynk.core.domain.util.map
import io.ktor.client.HttpClient

class KtorNotificationService(
    private val httpClient: HttpClient
) : NotificationService {

    override suspend fun getNotifications(
        before: String?
    ): Result<List<Notification>, DataError.Remote> {
        return httpClient.get<List<NotificationDto>>(
            route = "/notifications",
            queryParams = buildMap {
                put("pageSize", NotificationConstants.PAGE_SIZE)
                before?.let { put("before", it) }
            }
        ).map { notifications ->
            notifications.map { it.toDomain() }
        }
    }

    override suspend fun getUnreadCount(): Result<Long, DataError.Remote> {
        return httpClient.get<UnreadCountDto>(
            route = "/notifications/unread-count"
        ).map { it.count }
    }

    override suspend fun markAsRead(notificationId: String): EmptyResult<DataError.Remote> {
        return httpClient.patch<Unit>(
            route = "/notifications/$notificationId/read"
        )
    }

    override suspend fun markAllAsRead(): EmptyResult<DataError.Remote> {
        return httpClient.patch<Unit>(
            route = "/notifications/read-all"
        )
    }
}
