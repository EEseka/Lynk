package com.eeseka.lynk.shared.data.hangout.dto.requests

import kotlinx.serialization.Serializable

@Serializable
data class UpdateHangoutNoteRequest(
    val content: String
)
