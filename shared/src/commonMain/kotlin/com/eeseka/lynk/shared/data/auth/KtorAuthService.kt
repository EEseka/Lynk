package com.eeseka.lynk.shared.data.auth

import com.eeseka.lynk.shared.data.dto.AuthInfoSerializable
import com.eeseka.lynk.shared.data.dto.requests.GoogleAuthRequest
import com.eeseka.lynk.shared.data.dto.requests.RefreshRequest
import com.eeseka.lynk.shared.data.mappers.toDomain
import com.eeseka.lynk.shared.data.networking.post
import com.eeseka.lynk.shared.domain.auth.AuthInfo
import com.eeseka.lynk.shared.domain.auth.AuthService
import com.eeseka.lynk.shared.domain.util.DataError
import com.eeseka.lynk.shared.domain.util.EmptyResult
import com.eeseka.lynk.shared.domain.util.Result
import com.eeseka.lynk.shared.domain.util.map
import com.eeseka.lynk.shared.domain.util.onSuccess
import io.ktor.client.HttpClient
import io.ktor.client.plugins.auth.authProvider
import io.ktor.client.plugins.auth.providers.BearerAuthProvider

class KtorAuthService(
    private val httpClient: HttpClient
) : AuthService {

    override suspend fun continueWithGoogle(idToken: String): Result<AuthInfo, DataError.Remote> {
        return httpClient.post<GoogleAuthRequest, AuthInfoSerializable>(
            route = "/auth/google",
            body = GoogleAuthRequest(idToken = idToken)
        ).map { authInfoSerializable ->
            authInfoSerializable.toDomain()
        }
    }

    override suspend fun continueAsGuest(): Result<AuthInfo, DataError.Remote> {
        return httpClient.post<Unit, AuthInfoSerializable>(
            route = "/auth/guest",
            body = Unit
        ).map { authInfoSerializable ->
            authInfoSerializable.toDomain()
        }
    }

    override suspend fun logout(refreshToken: String): EmptyResult<DataError.Remote> {
        return httpClient.post<RefreshRequest, Unit>(
            route = "/auth/logout",
            body = RefreshRequest(refreshToken)
        ).onSuccess {
            httpClient.authProvider<BearerAuthProvider>()?.clearToken()
        }
    }

    override suspend fun deleteAccount(refreshToken: String): EmptyResult<DataError.Remote> {
        return httpClient.post<RefreshRequest, Unit>(
            route = "/auth/delete",
            body = RefreshRequest(refreshToken)
        ).onSuccess {
            httpClient.authProvider<BearerAuthProvider>()?.clearToken()
        }
    }
}