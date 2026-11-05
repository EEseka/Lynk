package com.eeseka.lynk.shared.data.hangout.dto

import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class HangoutNoteDto(
    val id: String,
    val author: HangoutUserDto,
    val content: String,
    val createdAt: Instant,
    val updatedAt: Instant?
)
