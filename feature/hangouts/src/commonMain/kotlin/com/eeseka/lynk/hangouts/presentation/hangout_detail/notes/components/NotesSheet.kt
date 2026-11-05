package com.eeseka.lynk.hangouts.presentation.hangout_detail.notes.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.eeseka.lynk.hangouts.presentation.model.HangoutNoteUi
import com.eeseka.lynk.hangouts.presentation.preview.previewHangoutNotes
import com.eeseka.lynk.shared.design_system.components.progress_indicator.LynkProgressIndicator
import com.eeseka.lynk.shared.design_system.components.modals_and_overlays.LynkAdaptiveSheet
import com.eeseka.lynk.shared.design_system.components.textfields.LynkText
import com.eeseka.lynk.shared.design_system.theme.LynkTheme
import com.eeseka.lynk.shared.presentation.preview.PREVIEW_GUEST_ID
import com.eeseka.lynk.shared.presentation.preview.PREVIEW_HOST_ID
import com.eeseka.lynk.shared.presentation.util.PaginationScrollListener
import com.eeseka.lynk.shared.presentation.util.toShortDateTimeLabel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentSetOf
import lynk.feature.hangouts.generated.resources.Res
import lynk.feature.hangouts.generated.resources.notes_title_with_count
import org.jetbrains.compose.resources.stringResource

@Composable
fun NotesSheet(
    notes: ImmutableList<HangoutNoteUi>,
    noteCount: Long,
    isLoadingNotes: Boolean,
    isEndReached: Boolean,
    notesResetEpoch: Int,
    currentUserId: String?,
    hostId: String,
    canEditNotes: Boolean,
    deletingNoteIds: ImmutableSet<String>,
    onLoadNextPage: () -> Unit,
    onEditNoteClick: (String) -> Unit,
    onDeleteNoteClick: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    LynkAdaptiveSheet(
        onDismissRequest = onDismiss,
        modifier = modifier,
        skipBottomSheetPartiallyExpanded = false
    ) {
        NotesSheetContent(
            notes = notes,
            noteCount = noteCount,
            isLoadingNotes = isLoadingNotes,
            isEndReached = isEndReached,
            notesResetEpoch = notesResetEpoch,
            currentUserId = currentUserId,
            hostId = hostId,
            canEditNotes = canEditNotes,
            deletingNoteIds = deletingNoteIds,
            onLoadNextPage = onLoadNextPage,
            onEditNoteClick = onEditNoteClick,
            onDeleteNoteClick = onDeleteNoteClick
        )
    }
}

@Composable
private fun NotesSheetContent(
    notes: ImmutableList<HangoutNoteUi>,
    noteCount: Long,
    isLoadingNotes: Boolean,
    isEndReached: Boolean,
    notesResetEpoch: Int,
    currentUserId: String?,
    hostId: String,
    canEditNotes: Boolean,
    deletingNoteIds: ImmutableSet<String>,
    onLoadNextPage: () -> Unit,
    onEditNoteClick: (String) -> Unit,
    onDeleteNoteClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    PaginationScrollListener(
        lazyListState = listState,
        itemCount = notes.size,
        isPaginationLoading = isLoadingNotes,
        isEndReached = isEndReached,
        onNearBottom = onLoadNextPage,
        resetKey = notesResetEpoch
    )

    Column(modifier = modifier.fillMaxWidth()) {
        LynkText(
            text = stringResource(Res.string.notes_title_with_count, noteCount),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
        )

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(start = 8.dp, end = 8.dp, bottom = 24.dp)
        ) {
            itemsIndexed(items = notes, key = { _, note -> note.id }) { index, note ->
                if (index > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                val isAuthor = note.author.userId == currentUserId
                NoteRow(
                    note = note,
                    timeLabel = note.createdAt.toShortDateTimeLabel(),
                    isHostNote = note.author.userId == hostId,
                    canEdit = isAuthor && canEditNotes,
                    canDelete = isAuthor || hostId == currentUserId,
                    isDeleting = note.id in deletingNoteIds,
                    onEditClick = { onEditNoteClick(note.id) },
                    onDeleteClick = { onDeleteNoteClick(note.id) }
                )
            }

            if (isLoadingNotes) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        LynkProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun NotesSheetContentPreview() {
    LynkTheme {
        NotesSheetContent(
            notes = previewHangoutNotes,
            noteCount = previewHangoutNotes.size.toLong(),
            isLoadingNotes = false,
            isEndReached = true,
            notesResetEpoch = 0,
            currentUserId = PREVIEW_GUEST_ID,
            hostId = PREVIEW_HOST_ID,
            canEditNotes = true,
            deletingNoteIds = persistentSetOf(),
            onLoadNextPage = {},
            onEditNoteClick = {},
            onDeleteNoteClick = {},
            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
        )
    }
}
