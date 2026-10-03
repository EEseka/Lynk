package com.eeseka.lynk.hangouts.presentation.model

import androidx.compose.runtime.Immutable
import com.eeseka.lynk.shared.presentation.hangout.model.HangoutUserUi
import kotlin.time.Instant

@Immutable
data class HangoutPhotoUi(
    val id: String,
    val uploader: HangoutUserUi,
    val caption: String?,
    val fullUrl: String,
    val thumbnailUrl: String,
    val urlsExpireAt: Instant,
    val createdAt: Instant
)
