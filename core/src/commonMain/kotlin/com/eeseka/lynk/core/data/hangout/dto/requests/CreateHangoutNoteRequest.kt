package com.eeseka.lynk.core.data.hangout.dto.requests

import com.eeseka.lynk.core.domain.hangout.model.HangoutNoteAudience
import kotlinx.serialization.Serializable

@Serializable
data class CreateHangoutNoteRequest(
    val content: String,
    val audience: HangoutNoteAudience
)
