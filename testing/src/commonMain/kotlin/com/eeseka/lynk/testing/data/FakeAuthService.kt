package com.eeseka.lynk.testing.data

import com.eeseka.lynk.core.domain.auth.AuthService
import com.eeseka.lynk.core.domain.auth.model.AuthInfo
import com.eeseka.lynk.core.domain.auth.model.User
import com.eeseka.lynk.core.domain.util.DataError
import com.eeseka.lynk.core.domain.util.EmptyResult
import com.eeseka.lynk.core.domain.util.Result

class FakeAuthService : AuthService {
    var shouldReturnError = false
    var errorToReturn = DataError.Remote.SERVER_ERROR
    var authInfoToReturn = AuthInfo(
        accessToken = "fake_access",
        refreshToken = "fake_refresh",
        user = User.Guest(id = "123")
    )

    override suspend fun continueWithGoogle(idToken: String): Result<AuthInfo, DataError.Remote> {
        if (shouldReturnError) return Result.Failure(errorToReturn)
        return Result.Success(authInfoToReturn)
    }

    override suspend fun continueAsGuest(): Result<AuthInfo, DataError.Remote> {
        if (shouldReturnError) return Result.Failure(errorToReturn)
        return Result.Success(authInfoToReturn)
    }

    override suspend fun logout(refreshToken: String): EmptyResult<DataError.Remote> {
        if (shouldReturnError) return Result.Failure(errorToReturn)
        return Result.Success(Unit)
    }

    override suspend fun deleteAccount(): EmptyResult<DataError.Remote> {
        if (shouldReturnError) return Result.Failure(errorToReturn)
        return Result.Success(Unit)
    }
}
