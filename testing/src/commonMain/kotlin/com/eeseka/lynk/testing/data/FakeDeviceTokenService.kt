package com.eeseka.lynk.testing.data

import com.eeseka.lynk.shared.domain.notification.DeviceTokenService
import com.eeseka.lynk.shared.domain.notification.model.DevicePlatform
import com.eeseka.lynk.shared.domain.util.DataError
import com.eeseka.lynk.shared.domain.util.EmptyResult
import com.eeseka.lynk.shared.domain.util.Result

class FakeDeviceTokenService : DeviceTokenService {
    var shouldReturnError = false
    var errorToReturn = DataError.Remote.SERVER_ERROR
    var registeredTokens = mutableListOf<Pair<String, DevicePlatform>>()
    var unregisteredTokens = mutableListOf<String>()

    override suspend fun registerToken(
        token: String,
        platform: DevicePlatform
    ): EmptyResult<DataError.Remote> {
        if (shouldReturnError) return Result.Failure(errorToReturn)
        registeredTokens.add(token to platform)
        return Result.Success(Unit)
    }

    override suspend fun unregisterToken(token: String): EmptyResult<DataError.Remote> {
        if (shouldReturnError) return Result.Failure(errorToReturn)
        unregisteredTokens.add(token)
        return Result.Success(Unit)
    }
}
