package com.eeseka.lynk.shared.data.profile

import com.eeseka.lynk.shared.data.auth.dto.UserSerializable
import com.eeseka.lynk.shared.data.auth.mappers.toDomain
import com.eeseka.lynk.shared.data.networking.get
import com.eeseka.lynk.shared.data.networking.post
import com.eeseka.lynk.shared.data.networking.put
import com.eeseka.lynk.shared.data.networking.safeCall
import com.eeseka.lynk.shared.data.profile.dto.requests.GenerateUploadUrlRequest
import com.eeseka.lynk.shared.data.profile.dto.requests.UpdateProfileRequest
import com.eeseka.lynk.shared.data.profile.dto.response.ProfilePictureUploadUrlsResponse
import com.eeseka.lynk.shared.data.profile.mappers.toDomain
import com.eeseka.lynk.shared.domain.auth.model.User
import com.eeseka.lynk.shared.domain.profile.UserService
import com.eeseka.lynk.shared.domain.profile.model.ProfilePictureUploadUrls
import com.eeseka.lynk.shared.domain.util.DataError
import com.eeseka.lynk.shared.domain.util.EmptyResult
import com.eeseka.lynk.shared.domain.util.Result
import com.eeseka.lynk.shared.domain.util.map
import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.request.url

class KtorUserService(
    private val httpClient: HttpClient
) : UserService {

    override suspend fun isUsernameAvailable(username: String): Result<Boolean, DataError.Remote> {
        return httpClient.get<Map<String, Boolean>>(
            route = "/users/username-available",
            queryParams = mapOf("username" to username)
        ).map { responseMap ->
            responseMap["isAvailable"] ?: false
        }
    }

    override suspend fun getProfilePictureUploadUrl(mimeType: String): Result<ProfilePictureUploadUrls, DataError.Remote> {
        return httpClient.post<GenerateUploadUrlRequest, ProfilePictureUploadUrlsResponse>(
            route = "/users/profile-picture/generate-upload-url",
            body = GenerateUploadUrlRequest(mimeType = mimeType)
        ).map { it.toDomain() }
    }

    override suspend fun uploadProfilePicture(
        uploadUrl: String,
        headers: Map<String, String>,
        imageBytes: ByteArray
    ): EmptyResult<DataError.Remote> {
        return safeCall {
            httpClient.put {
                url(uploadUrl)
                headers.forEach { (key, value) ->
                    header(key, value)
                }
                setBody(imageBytes)
            }
        }
    }

    override suspend fun updateProfile(
        username: String,
        displayName: String,
        profilePhotoUrl: String?
    ): Result<User, DataError.Remote> {
        return httpClient.put<UpdateProfileRequest, UserSerializable>(
            route = "/users/profile",
            body = UpdateProfileRequest(
                username = username,
                displayName = displayName,
                profilePhotoUrl = profilePhotoUrl
            )
        ).map { userSerializable ->
            userSerializable.toDomain()
        }
    }
}