package com.eeseka.lynk.shared.domain.auth

import com.eeseka.lynk.shared.domain.auth.model.AuthInfo
import com.eeseka.lynk.shared.domain.util.DataError
import com.eeseka.lynk.shared.domain.util.EmptyResult
import com.eeseka.lynk.shared.domain.util.Result

interface AuthService {
    suspend fun continueWithGoogle(idToken: String): Result<AuthInfo, DataError.Remote>
    suspend fun continueAsGuest(): Result<AuthInfo, DataError.Remote>
    suspend fun logout(refreshToken: String): EmptyResult<DataError.Remote>
    suspend fun deleteAccount(): EmptyResult<DataError.Remote>
}