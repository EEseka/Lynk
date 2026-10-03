package com.eeseka.lynk.hangouts.presentation.mappers

import com.eeseka.lynk.hangouts.presentation.model.HangoutPhotoUi
import com.eeseka.lynk.shared.domain.hangout.model.HangoutPhoto
import com.eeseka.lynk.shared.presentation.hangout.mappers.toHangoutUserUi

fun HangoutPhoto.toHangoutPhotoUi() = HangoutPhotoUi(
    id = id,
    uploader = uploader.toHangoutUserUi(),
    caption = caption,
    fullUrl = fullUrl,
    thumbnailUrl = thumbnailUrl,
    urlsExpireAt = urlsExpireAt,
    createdAt = createdAt
)
