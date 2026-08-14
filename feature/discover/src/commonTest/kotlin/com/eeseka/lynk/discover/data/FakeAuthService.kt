package com.eeseka.lynk.discover.data

import com.eeseka.lynk.shared.domain.auth.AuthService
import com.eeseka.lynk.shared.domain.auth.model.AuthInfo
import com.eeseka.lynk.shared.domain.util.DataError
import com.eeseka.lynk.shared.domain.util.EmptyResult
import com.eeseka.lynk.shared.domain.util.Result

class FakeAuthService : AuthService {
    var shouldReturnError = false

    override suspend fun continueWithGoogle(idToken: String): Result<AuthInfo, DataError.Remote> =
        Result.Failure(DataError.Remote.SERVER_ERROR)

    override suspend fun continueAsGuest(): Result<AuthInfo, DataError.Remote> =
        Result.Failure(DataError.Remote.SERVER_ERROR)

    override suspend fun logout(refreshToken: String): EmptyResult<DataError.Remote> {
        if (shouldReturnError) return Result.Failure(DataError.Remote.SERVER_ERROR)
        return Result.Success(Unit)
    }

    override suspend fun deleteAccount(): EmptyResult<DataError.Remote> =
        Result.Failure(DataError.Remote.SERVER_ERROR)
}