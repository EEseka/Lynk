package com.eeseka.lynk.hangouts.presentation.hangout_detail.notes.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.EllipsisVertical
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.SquarePen
import com.composables.icons.lucide.Trash2
import com.eeseka.lynk.hangouts.presentation.model.HangoutNoteUi
import com.eeseka.lynk.hangouts.presentation.preview.previewHangoutNotes
import com.eeseka.lynk.shared.design_system.components.modals_and_overlays.LynkDropDownItem
import com.eeseka.lynk.shared.design_system.components.modals_and_overlays.LynkDropDownMenu
import com.eeseka.lynk.shared.design_system.components.progress_indicator.LynkProgressIndicator
import com.eeseka.lynk.shared.design_system.components.textfields.LynkText
import com.eeseka.lynk.shared.design_system.components.util.AppHaptic
import com.eeseka.lynk.shared.design_system.components.util.rememberAppHaptic
import com.eeseka.lynk.shared.design_system.theme.LynkTheme
import com.eeseka.lynk.shared.presentation.hangout.components.ParticipantAvatar
import kotlinx.collections.immutable.toImmutableList
import lynk.feature.hangouts.generated.resources.Res
import lynk.feature.hangouts.generated.resources.notes_delete
import lynk.feature.hangouts.generated.resources.notes_edit
import lynk.feature.hangouts.generated.resources.notes_edited
import lynk.feature.hangouts.generated.resources.notes_host
import lynk.feature.hangouts.generated.resources.notes_more_actions
import org.jetbrains.compose.resources.stringResource

@Composable
fun NoteRow(
    note: HangoutNoteUi,
    timeLabel: String,
    isHostNote: Boolean,
    canEdit: Boolean,
    canDelete: Boolean,
    isDeleting: Boolean,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hapticFeedback = rememberAppHaptic()
    val scheme = MaterialTheme.colorScheme

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 16.dp, top = 12.dp, bottom = 12.dp, end = if (canDelete) 4.dp else 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        ParticipantAvatar(
            displayName = note.author.displayName,
            initials = note.author.initials,
            profilePictureUrl = note.author.profilePictureUrl,
            size = 32.dp
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LynkText(
                    text = note.author.displayName,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f, fill = false)
                )
                if (isHostNote) {
                    LynkText(
                        text = stringResource(Res.string.notes_host),
                        style = MaterialTheme.typography.labelSmall,
                        color = scheme.primary
                    )
                }
                LynkText(
                    text = if (note.updatedAt != null) "$timeLabel · ${stringResource(Res.string.notes_edited)}"
                    else timeLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = scheme.onSurfaceVariant
                )
            }
            LynkText(
                text = note.content,
                style = MaterialTheme.typography.bodyMedium,
                color = scheme.onSurface
            )
        }

        if (canDelete) {
            var isMenuExpanded by remember { mutableStateOf(false) }
            val editLabel = stringResource(Res.string.notes_edit)
            val deleteLabel = stringResource(Res.string.notes_delete)
            val menuItems = buildList {
                if (canEdit) {
                    add(
                        LynkDropDownItem(
                            title = editLabel,
                            icon = Lucide.SquarePen,
                            sfSymbol = "square.and.pencil",
                            onClick = {
                                hapticFeedback(AppHaptic.ImpactLight)
                                onEditClick()
                            }
                        )
                    )
                }
                add(
                    LynkDropDownItem(
                        title = deleteLabel,
                        icon = Lucide.Trash2,
                        sfSymbol = "trash",
                        isDestructive = true,
                        onClick = {
                            hapticFeedback(AppHaptic.ImpactLight)
                            onDeleteClick()
                        }
                    )
                )
            }.toImmutableList()

            LynkDropDownMenu(
                expanded = isMenuExpanded,
                onDismissRequest = { isMenuExpanded = false },
                items = menuItems,
                anchor = {
                    Box(
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .clip(CircleShape)
                            .clickable(enabled = !isDeleting) {
                                hapticFeedback(AppHaptic.ImpactLight)
                                isMenuExpanded = true
                            }
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isDeleting) {
                            LynkProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = scheme.error
                            )
                        } else {
                            Icon(
                                imageVector = Lucide.EllipsisVertical,
                                contentDescription = stringResource(Res.string.notes_more_actions),
                                tint = scheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun NoteRowOwnPreview() = NoteRowPreview(canDelete = true)

@PreviewLightDark
@Composable
private fun NoteRowDeletingPreview() = NoteRowPreview(canDelete = true, isDeleting = true)

@PreviewLightDark
@Composable
private fun NoteRowHostPreview() = NoteRowPreview(isHostNote = true)

@Composable
private fun NoteRowPreview(
    isHostNote: Boolean = false,
    canDelete: Boolean = false,
    isDeleting: Boolean = false
) {
    LynkTheme {
        NoteRow(
            note = previewHangoutNotes.first(),
            timeLabel = "7:42 PM",
            isHostNote = isHostNote,
            canEdit = canDelete,
            canDelete = canDelete,
            isDeleting = isDeleting,
            onEditClick = {},
            onDeleteClick = {},
            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
        )
    }
}
