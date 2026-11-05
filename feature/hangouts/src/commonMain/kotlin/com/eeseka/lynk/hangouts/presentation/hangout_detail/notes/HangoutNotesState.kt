package com.eeseka.lynk.hangouts.presentation.hangout_detail.notes

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Stable
import com.eeseka.lynk.hangouts.presentation.model.HangoutNoteUi
import com.eeseka.lynk.shared.domain.hangout.model.HangoutNoteAudience
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf

@Stable
data class HangoutNotesState(
    val notes: ImmutableList<HangoutNoteUi> = persistentListOf(),
    val noteCount: Long = 0,
    val isLoadingNotes: Boolean = false,
    val isEndReached: Boolean = false,
    val notesResetEpoch: Int = 0,

    // Compose sheet, for a new note or the author's edit
    val isComposerShowing: Boolean = false,
    val editingNoteId: String? = null,
    val noteState: TextFieldState = TextFieldState(),
    val audience: HangoutNoteAudience = HangoutNoteAudience.EVERYONE,
    val canPostNote: Boolean = false,
    val isPostingNote: Boolean = false,

    val isAllNotesShowing: Boolean = false,

    val pendingDeleteNoteId: String? = null,
    val deletingNoteIds: ImmutableSet<String> = persistentSetOf()
)
