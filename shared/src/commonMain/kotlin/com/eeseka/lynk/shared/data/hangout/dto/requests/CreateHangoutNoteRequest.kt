package com.eeseka.lynk.shared.data.hangout.dto.requests

import com.eeseka.lynk.shared.domain.hangout.model.HangoutNoteAudience
import kotlinx.serialization.Serializable

@Serializable
data class CreateHangoutNoteRequest(
    val content: String,
    val audience: HangoutNoteAudience
)
