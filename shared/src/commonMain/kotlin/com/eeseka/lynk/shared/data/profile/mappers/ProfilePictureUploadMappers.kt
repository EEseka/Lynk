package com.eeseka.lynk.shared.data.profile.mappers

import com.eeseka.lynk.shared.data.profile.dto.response.ProfilePictureUploadUrlsResponse
import com.eeseka.lynk.shared.domain.profile.model.ProfilePictureUploadUrls

fun ProfilePictureUploadUrlsResponse.toDomain(): ProfilePictureUploadUrls {
    return ProfilePictureUploadUrls(
        uploadUrl = uploadUrl,
        publicUrl = publicUrl,
        headers = headers
    )
}