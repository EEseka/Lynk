package com.eeseka.lynk.hangouts.presentation.mappers

import com.eeseka.lynk.hangouts.presentation.model.HangoutNoteUi
import com.eeseka.lynk.shared.domain.hangout.model.HangoutNote
import com.eeseka.lynk.shared.presentation.hangout.mappers.toHangoutUserUi

fun HangoutNote.toHangoutNoteUi() = HangoutNoteUi(
    id = id,
    author = author.toHangoutUserUi(),
    content = content,
    createdAt = createdAt,
    updatedAt = updatedAt
)
