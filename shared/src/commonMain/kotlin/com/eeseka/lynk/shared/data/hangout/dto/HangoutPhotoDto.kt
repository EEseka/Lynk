package com.eeseka.lynk.shared.data.hangout.dto

import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class HangoutPhotoDto(
    val id: String,
    val uploader: HangoutUserDto,
    val caption: String?,
    val fullUrl: String,
    val thumbnailUrl: String,
    val urlsExpireAt: Instant,
    val createdAt: Instant
)
