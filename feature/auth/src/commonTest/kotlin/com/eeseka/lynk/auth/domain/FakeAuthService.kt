package com.eeseka.lynk.auth.domain

import com.eeseka.lynk.shared.domain.auth.AuthInfo
import com.eeseka.lynk.shared.domain.auth.AuthService
import com.eeseka.lynk.shared.domain.auth.User
import com.eeseka.lynk.shared.domain.util.DataError
import com.eeseka.lynk.shared.domain.util.EmptyResult
import com.eeseka.lynk.shared.domain.util.Result

class FakeAuthService : AuthService {
    var shouldReturnError = false
    var errorToReturn = DataError.Remote.SERVER_ERROR
    var authInfoToReturn = AuthInfo(
        accessToken = "fake_access",
        refreshToken = "fake_refresh",
        user = User.Guest(id = "123")
    )

    override suspend fun continueWithGoogle(idToken: String): Result<AuthInfo, DataError.Remote> {
        return if (shouldReturnError) {
            Result.Failure(errorToReturn)
        } else {
            Result.Success(authInfoToReturn)
        }
    }

    override suspend fun continueAsGuest(): Result<AuthInfo, DataError.Remote> {
        return if (shouldReturnError) {
            Result.Failure(errorToReturn)
        } else {
            Result.Success(authInfoToReturn)
        }
    }

    override suspend fun logout(refreshToken: String): EmptyResult<DataError.Remote> {
        return Result.Success(Unit)
    }

    override suspend fun deleteAccount(): EmptyResult<DataError.Remote> {
        return Result.Success(Unit)
    }
}