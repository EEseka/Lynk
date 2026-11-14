package com.eeseka.lynk.core.data.profile.mappers

import com.eeseka.lynk.core.data.profile.dto.response.ProfilePictureUploadUrlsResponse
import com.eeseka.lynk.core.domain.profile.model.ProfilePictureUploadUrls

fun ProfilePictureUploadUrlsResponse.toDomain(): ProfilePictureUploadUrls {
    return ProfilePictureUploadUrls(
        uploadUrl = uploadUrl,
        publicUrl = publicUrl,
        headers = headers
    )
}