package com.eeseka.lynk.hangouts.presentation.preview

import com.eeseka.lynk.hangouts.presentation.model.HangoutPhotoUi
import com.eeseka.lynk.shared.presentation.hangout.model.HangoutUserUi
import com.eeseka.lynk.shared.presentation.preview.previewUser
import kotlinx.collections.immutable.persistentListOf
import kotlin.time.Instant

val previewHangoutPhotos = persistentListOf(
    previewHangoutPhoto(id = "p1", uploader = previewUser(0), caption = "Suya run after the show"),
    previewHangoutPhoto(id = "p2", uploader = previewUser(1), caption = null),
    previewHangoutPhoto(id = "p3", uploader = previewUser(1), caption = "Group picture"),
    previewHangoutPhoto(id = "p4", uploader = previewUser(0), caption = null)
)

private fun previewHangoutPhoto(
    id: String,
    uploader: HangoutUserUi,
    caption: String?
) = HangoutPhotoUi(
    id = id,
    uploader = uploader,
    caption = caption,
    fullUrl = "",
    thumbnailUrl = "",
    urlsExpireAt = Instant.fromEpochMilliseconds(4102444800000L),
    createdAt = Instant.fromEpochMilliseconds(1790000000000L)
)
