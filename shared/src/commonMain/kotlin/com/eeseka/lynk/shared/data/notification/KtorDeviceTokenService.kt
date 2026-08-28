package com.eeseka.lynk.shared.data.notification

import com.eeseka.lynk.shared.data.networking.delete
import com.eeseka.lynk.shared.data.networking.post
import com.eeseka.lynk.shared.data.notification.dto.requests.RegisterDeviceTokenRequest
import com.eeseka.lynk.shared.domain.notification.DeviceTokenService
import com.eeseka.lynk.shared.domain.notification.model.DevicePlatform
import com.eeseka.lynk.shared.domain.util.DataError
import com.eeseka.lynk.shared.domain.util.EmptyResult
import io.ktor.client.HttpClient

class KtorDeviceTokenService(
    private val httpClient: HttpClient
) : DeviceTokenService {

    override suspend fun registerToken(
        token: String,
        platform: DevicePlatform
    ): EmptyResult<DataError.Remote> {
        return httpClient.post<RegisterDeviceTokenRequest, Unit>(
            route = "/notifications/register",
            body = RegisterDeviceTokenRequest(
                token = token,
                platform = platform
            )
        )
    }

    override suspend fun unregisterToken(token: String): EmptyResult<DataError.Remote> {
        return httpClient.delete<Unit>(
            route = "/notifications/$token"
        )
    }
}
