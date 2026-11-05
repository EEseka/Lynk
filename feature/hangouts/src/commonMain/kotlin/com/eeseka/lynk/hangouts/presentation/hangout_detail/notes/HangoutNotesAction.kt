package com.eeseka.lynk.hangouts.presentation.hangout_detail.notes

import com.eeseka.lynk.shared.domain.hangout.model.HangoutNoteAudience

sealed interface HangoutNotesAction {
    data class OnSelectHangout(val hangoutId: String?) : HangoutNotesAction

    // Compose sheet
    data object OnAddNoteClick : HangoutNotesAction
    data class OnEditNoteClick(val noteId: String) : HangoutNotesAction
    data class OnAudienceChange(val audience: HangoutNoteAudience) : HangoutNotesAction
    data object OnPostNoteClick : HangoutNotesAction
    data object OnDismissComposer : HangoutNotesAction

    // All notes sheet
    data object OnSeeAllNotesClick : HangoutNotesAction
    data object OnDismissAllNotes : HangoutNotesAction
    data object LoadNextPage : HangoutNotesAction

    data class OnDeleteNoteClick(val noteId: String) : HangoutNotesAction
    data object OnDismissDeleteDialog : HangoutNotesAction
    data object OnDeleteNoteConfirmed : HangoutNotesAction
}
