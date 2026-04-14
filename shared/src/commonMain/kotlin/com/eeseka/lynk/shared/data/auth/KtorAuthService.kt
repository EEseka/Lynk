package com.eeseka.lynk.shared.data.auth

import com.eeseka.lynk.shared.data.auth.dto.AuthInfoSerializable
import com.eeseka.lynk.shared.data.auth.dto.requests.GoogleAuthRequest
import com.eeseka.lynk.shared.data.auth.dto.requests.RefreshRequest
import com.eeseka.lynk.shared.data.auth.mappers.toDomain
import com.eeseka.lynk.shared.data.networking.delete
import com.eeseka.lynk.shared.data.networking.post
import com.eeseka.lynk.shared.domain.auth.model.AuthInfo
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
            body = GoogleAuthRequest(token = idToken)
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
            body = RefreshRequest(refreshToken = refreshToken)
        ).onSuccess {
            httpClient.authProvider<BearerAuthProvider>()?.clearToken()
        }
    }

    override suspend fun deleteAccount(): EmptyResult<DataError.Remote> {
        return httpClient.delete<Unit>(
            route = "/auth/account"
        ).onSuccess {
            httpClient.authProvider<BearerAuthProvider>()?.clearToken()
        }
    }
}