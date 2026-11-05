package com.eeseka.lynk.hangouts.presentation.hangout_detail.notes.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.ChevronRight
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.NotebookPen
import com.composables.icons.lucide.SquarePen
import com.eeseka.lynk.hangouts.presentation.hangout_detail.components.DetailSection
import com.eeseka.lynk.hangouts.presentation.model.HangoutNoteUi
import com.eeseka.lynk.hangouts.presentation.preview.previewHangoutNotes
import com.eeseka.lynk.shared.design_system.components.layouts.LynkCard
import com.eeseka.lynk.shared.design_system.components.layouts.LynkCardStyle
import com.eeseka.lynk.shared.design_system.components.textfields.LynkText
import com.eeseka.lynk.shared.design_system.components.util.AppHaptic
import com.eeseka.lynk.shared.design_system.components.util.rememberAppHaptic
import com.eeseka.lynk.shared.design_system.theme.LynkTheme
import com.eeseka.lynk.shared.presentation.preview.PREVIEW_GUEST_ID
import com.eeseka.lynk.shared.presentation.preview.PREVIEW_HOST_ID
import com.eeseka.lynk.shared.presentation.util.toShortDateTimeLabel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf
import lynk.feature.hangouts.generated.resources.Res
import lynk.feature.hangouts.generated.resources.detail_see_all
import lynk.feature.hangouts.generated.resources.notes_empty_message
import lynk.feature.hangouts.generated.resources.notes_empty_title
import lynk.feature.hangouts.generated.resources.notes_field_placeholder
import lynk.feature.hangouts.generated.resources.notes_title
import lynk.feature.hangouts.generated.resources.notes_title_with_count
import org.jetbrains.compose.resources.stringResource

private const val PREVIEW_NOTE_COUNT = 3

@Composable
fun NotesSection(
    notes: ImmutableList<HangoutNoteUi>,
    noteCount: Long,
    currentUserId: String?,
    hostId: String,
    canAddNote: Boolean,
    deletingNoteIds: ImmutableSet<String>,
    onAddNoteClick: () -> Unit,
    onSeeAllClick: () -> Unit,
    onEditNoteClick: (String) -> Unit,
    onDeleteNoteClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val hapticFeedback = rememberAppHaptic()

    DetailSection(
        title = if (noteCount > 0) stringResource(Res.string.notes_title_with_count, noteCount)
        else stringResource(Res.string.notes_title),
        trailing = if (noteCount > PREVIEW_NOTE_COUNT) {
            {
                Row(
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .clip(CircleShape)
                        .clickable {
                            hapticFeedback(AppHaptic.ImpactLight)
                            onSeeAllClick()
                        }
                        .padding(start = 8.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LynkText(
                        text = stringResource(Res.string.detail_see_all),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                    Icon(
                        imageVector = Lucide.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        } else null,
        modifier = modifier
    ) {
        LynkCard(style = LynkCardStyle.OUTLINED, modifier = Modifier.fillMaxWidth()) {
            if (notes.isEmpty()) {
                NotesEmptyRow()
            }

            notes.take(PREVIEW_NOTE_COUNT).forEachIndexed { index, note ->
                if (index > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                val isAuthor = note.author.userId == currentUserId
                NoteRow(
                    note = note,
                    timeLabel = note.createdAt.toShortDateTimeLabel(),
                    isHostNote = note.author.userId == hostId,
                    canEdit = isAuthor && canAddNote,
                    canDelete = isAuthor || hostId == currentUserId,
                    isDeleting = note.id in deletingNoteIds,
                    onEditClick = { onEditNoteClick(note.id) },
                    onDeleteClick = { onDeleteNoteClick(note.id) }
                )
            }

            if (canAddNote) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                NoteFieldRow(
                    onClick = {
                        hapticFeedback(AppHaptic.ImpactLight)
                        onAddNoteClick()
                    }
                )
            }
        }
    }
}

@Composable
private fun NotesEmptyRow(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Lucide.NotebookPen,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            LynkText(
                text = stringResource(Res.string.notes_empty_title),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            LynkText(
                text = stringResource(Res.string.notes_empty_message),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun NoteFieldRow(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Lucide.SquarePen,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        LynkText(
            text = stringResource(Res.string.notes_field_placeholder),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@PreviewLightDark
@Composable
private fun NotesSectionNotesPreview() = NotesSectionPreview(notes = previewHangoutNotes)

@PreviewLightDark
@Composable
private fun NotesSectionEmptyPreview() = NotesSectionPreview()

@PreviewLightDark
@Composable
private fun NotesSectionReadOnlyPreview() = NotesSectionPreview(notes = previewHangoutNotes, canAddNote = false)

@Composable
private fun NotesSectionPreview(
    notes: ImmutableList<HangoutNoteUi> = persistentListOf(),
    canAddNote: Boolean = true
) {
    LynkTheme {
        NotesSection(
            notes = notes,
            noteCount = notes.size.toLong(),
            currentUserId = PREVIEW_GUEST_ID,
            hostId = PREVIEW_HOST_ID,
            canAddNote = canAddNote,
            deletingNoteIds = persistentSetOf(),
            onAddNoteClick = {},
            onSeeAllClick = {},
            onEditNoteClick = {},
            onDeleteNoteClick = {},
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        )
    }
}
