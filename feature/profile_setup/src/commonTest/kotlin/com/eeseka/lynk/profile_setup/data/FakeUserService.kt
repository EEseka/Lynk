package com.eeseka.lynk.profile_setup.data

import com.eeseka.lynk.shared.domain.auth.model.AuthProvider
import com.eeseka.lynk.shared.domain.auth.model.User
import com.eeseka.lynk.shared.domain.profile.UserService
import com.eeseka.lynk.shared.domain.profile.model.ProfilePictureUploadUrls
import com.eeseka.lynk.shared.domain.util.DataError
import com.eeseka.lynk.shared.domain.util.Result

class FakeUserService : UserService {
    var shouldReturnError = false
    var isAvailable = true
    var publicUrl = "https://fake.supabase.com/photo.jpg"
    var uploadUrl = "https://fake.supabase.com/upload"

    // Simulating backend username check
    override suspend fun isUsernameAvailable(username: String): Result<Boolean, DataError.Remote> {
        if (shouldReturnError) return Result.Failure(DataError.Remote.SERVER_ERROR)
        return Result.Success(isAvailable)
    }

    override suspend fun updateProfile(
        username: String,
        displayName: String,
        profilePhotoUrl: String?
    ): Result<User, DataError.Remote> {
        if (shouldReturnError) return Result.Failure(DataError.Remote.SERVER_ERROR)
        return Result.Success(
            User.Authenticated(
                id = "123",
                provider = AuthProvider.GOOGLE,
                email = "test@test.com",
                username = username,
                displayName = displayName,
                profilePictureUrl = profilePhotoUrl
            )
        )
    }

    override suspend fun getProfilePictureUploadUrl(mimeType: String): Result<ProfilePictureUploadUrls, DataError.Remote> {
        if (shouldReturnError) return Result.Failure(DataError.Remote.SERVER_ERROR)
        return Result.Success(
            ProfilePictureUploadUrls(
                uploadUrl = uploadUrl,
                publicUrl = publicUrl,
                headers = emptyMap()
            )
        )
    }

    override suspend fun uploadProfilePicture(
        uploadUrl: String,
        headers: Map<String, String>,
        imageBytes: ByteArray
    ): Result<Unit, DataError.Remote> {
        if (shouldReturnError) return Result.Failure(DataError.Remote.SERVER_ERROR)
        return Result.Success(Unit)
    }
}