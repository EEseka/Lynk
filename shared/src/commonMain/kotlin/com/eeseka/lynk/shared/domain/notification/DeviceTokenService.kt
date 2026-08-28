package com.eeseka.lynk.shared.domain.notification

import com.eeseka.lynk.shared.domain.notification.model.DevicePlatform
import com.eeseka.lynk.shared.domain.util.DataError
import com.eeseka.lynk.shared.domain.util.EmptyResult

interface DeviceTokenService {

    suspend fun registerToken(
        token: String,
        platform: DevicePlatform
    ): EmptyResult<DataError.Remote>

    suspend fun unregisterToken(token: String): EmptyResult<DataError.Remote>
}