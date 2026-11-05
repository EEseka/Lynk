package com.eeseka.lynk.hangouts.presentation.model

import androidx.compose.runtime.Immutable
import com.eeseka.lynk.shared.presentation.hangout.model.HangoutUserUi
import kotlin.time.Instant

@Immutable
data class HangoutNoteUi(
    val id: String,
    val author: HangoutUserUi,
    val content: String,
    val createdAt: Instant,
    val updatedAt: Instant?
)
