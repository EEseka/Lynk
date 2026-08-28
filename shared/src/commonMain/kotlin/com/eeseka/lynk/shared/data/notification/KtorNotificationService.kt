package com.eeseka.lynk.shared.data.notification

import com.eeseka.lynk.shared.data.networking.get
import com.eeseka.lynk.shared.data.networking.patch
import com.eeseka.lynk.shared.data.notification.dto.NotificationDto
import com.eeseka.lynk.shared.data.notification.dto.UnreadCountDto
import com.eeseka.lynk.shared.data.notification.mappers.toDomain
import com.eeseka.lynk.shared.domain.notification.NotificationService
import com.eeseka.lynk.shared.domain.notification.model.Notification
import com.eeseka.lynk.shared.domain.util.DataError
import com.eeseka.lynk.shared.domain.util.EmptyResult
import com.eeseka.lynk.shared.domain.util.Result
import com.eeseka.lynk.shared.domain.util.map
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
