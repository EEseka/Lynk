package com.eeseka.lynk.shared.domain.hangout.model

import kotlin.time.Instant

data class HangoutPhoto(
    val id: String,
    val uploader: HangoutUser,
    val caption: String?,
    val fullUrl: String,
    val thumbnailUrl: String,
    val urlsExpireAt: Instant,
    val createdAt: Instant
)
