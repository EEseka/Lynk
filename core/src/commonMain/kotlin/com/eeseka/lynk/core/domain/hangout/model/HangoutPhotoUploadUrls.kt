package com.eeseka.lynk.core.domain.hangout.model

data class HangoutPhotoUploadUrls(
    val photoId: String,
    val fullUploadUrl: String,
    val thumbnailUploadUrl: String,
    val headers: Map<String, String>
)
