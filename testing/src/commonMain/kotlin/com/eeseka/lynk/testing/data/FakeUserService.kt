package com.eeseka.lynk.testing.data

import com.eeseka.lynk.shared.domain.auth.model.AuthProvider
import com.eeseka.lynk.shared.domain.auth.model.User
import com.eeseka.lynk.shared.domain.profile.UserService
import com.eeseka.lynk.shared.domain.profile.model.ProfilePictureUploadUrls
import com.eeseka.lynk.shared.domain.util.DataError
import com.eeseka.lynk.shared.domain.util.EmptyResult
import com.eeseka.lynk.shared.domain.util.Result

class FakeUserService : UserService {
    var shouldReturnError = false
    var errorToReturn = DataError.Remote.SERVER_ERROR
    var isAvailable = true
    var uploadUrl = "https://fake.supabase.com/upload"
    var publicUrl = "https://fake.supabase.com/photo.jpg"

    // The server's copy of the signed-in user; creating or updating the profile changes it
    var currentUser = User.Authenticated(
        id = "123",
        provider = AuthProvider.GOOGLE,
        email = "test@test.com",
        displayName = "Tester",
        username = "tester"
    )

    override suspend fun isUsernameAvailable(username: String): Result<Boolean, DataError.Remote> {
        if (shouldReturnError) return Result.Failure(errorToReturn)
        return Result.Success(isAvailable)
    }

    override suspend fun getProfilePictureUploadUrl(mimeType: String): Result<ProfilePictureUploadUrls, DataError.Remote> {
        if (shouldReturnError) return Result.Failure(errorToReturn)
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
    ): EmptyResult<DataError.Remote> {
        if (shouldReturnError) return Result.Failure(errorToReturn)
        return Result.Success(Unit)
    }

    override suspend fun getCurrentUser(): Result<User, DataError.Remote> {
        if (shouldReturnError) return Result.Failure(errorToReturn)
        return Result.Success(currentUser)
    }

    override suspend fun createProfile(
        username: String,
        displayName: String,
        profilePhotoUrl: String?
    ): Result<User, DataError.Remote> {
        if (shouldReturnError) return Result.Failure(errorToReturn)
        currentUser = currentUser.copy(
            username = username,
            displayName = displayName,
            profilePictureUrl = profilePhotoUrl
        )
        return Result.Success(currentUser)
    }

    override suspend fun updateProfile(
        displayName: String,
        profilePhotoUrl: String?
    ): Result<User, DataError.Remote> {
        if (shouldReturnError) return Result.Failure(errorToReturn)
        currentUser = currentUser.copy(
            displayName = displayName,
            profilePictureUrl = profilePhotoUrl
        )
        return Result.Success(currentUser)
    }
}
