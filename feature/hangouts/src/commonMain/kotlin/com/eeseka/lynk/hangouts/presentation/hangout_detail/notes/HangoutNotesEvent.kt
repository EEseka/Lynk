package com.eeseka.lynk.hangouts.presentation.hangout_detail.notes

import com.eeseka.lynk.shared.presentation.util.UiText

sealed interface HangoutNotesEvent {
    data class Error(val message: UiText) : HangoutNotesEvent
    data object NotePosted : HangoutNotesEvent
    data object NoteEdited : HangoutNotesEvent
    data object NoteDeleted : HangoutNotesEvent
}
