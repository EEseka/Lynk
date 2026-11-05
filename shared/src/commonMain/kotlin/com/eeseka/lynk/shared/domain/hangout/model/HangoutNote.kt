package com.eeseka.lynk.shared.domain.hangout.model

import kotlin.time.Instant

data class HangoutNote(
    val id: String,
    val author: HangoutUser,
    val content: String,
    val createdAt: Instant,
    val updatedAt: Instant?
)
