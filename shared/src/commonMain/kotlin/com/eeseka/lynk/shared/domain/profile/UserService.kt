package com.eeseka.lynk.shared.domain.profile

import com.eeseka.lynk.shared.domain.auth.model.User
import com.eeseka.lynk.shared.domain.profile.model.ProfilePictureUploadUrls
import com.eeseka.lynk.shared.domain.util.DataError
import com.eeseka.lynk.shared.domain.util.EmptyResult
import com.eeseka.lynk.shared.domain.util.Result

interface UserService {
    suspend fun isUsernameAvailable(username: String): Result<Boolean, DataError.Remote>
    suspend fun getProfilePictureUploadUrl(mimeType: String): Result<ProfilePictureUploadUrls, DataError.Remote>
    suspend fun uploadProfilePicture(
        uploadUrl: String,
        headers: Map<String, String>,
        imageBytes: ByteArray
    ): EmptyResult<DataError.Remote>

    suspend fun updateProfile(
        username: String,
        displayName: String,
        profilePhotoUrl: String?
    ): Result<User, DataError.Remote>
}