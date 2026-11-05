package com.eeseka.lynk.hangouts.presentation.preview

import com.eeseka.lynk.hangouts.presentation.model.HangoutNoteUi
import com.eeseka.lynk.shared.presentation.hangout.model.HangoutUserUi
import com.eeseka.lynk.shared.presentation.preview.previewUser
import kotlinx.collections.immutable.persistentListOf
import kotlin.time.Instant

val previewHangoutNotes = persistentListOf(
    previewHangoutNote(id = "n1", author = previewUser(0), content = "Gate code is 4471, we're at the back table by the DJ"),
    previewHangoutNote(id = "n2", author = previewUser(1), content = "Running 15 mins late, start without me"),
    previewHangoutNote(id = "n3", author = previewUser(2), content = "Parking is full, use the mall lot"),
    previewHangoutNote(id = "n4", author = previewUser(1), content = "On my way")
)

private fun previewHangoutNote(
    id: String,
    author: HangoutUserUi,
    content: String
) = HangoutNoteUi(
    id = id,
    author = author,
    content = content,
    createdAt = Instant.fromEpochMilliseconds(1790000000000L),
    updatedAt = null
)
