package com.eeseka.lynk.core.domain.notification

import com.eeseka.lynk.core.domain.notification.model.DevicePlatform
import com.eeseka.lynk.core.domain.util.DataError
import com.eeseka.lynk.core.domain.util.EmptyResult

interface DeviceTokenService {

    suspend fun registerToken(
        token: String,
        platform: DevicePlatform
    ): EmptyResult<DataError.Remote>

    suspend fun unregisterToken(token: String): EmptyResult<DataError.Remote>
}