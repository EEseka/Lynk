package com.eeseka.lynk.hangouts.presentation.hangout_detail.notes.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.maxLength
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Crown
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Users
import com.eeseka.lynk.shared.design_system.components.buttons.LynkButton
import com.eeseka.lynk.shared.design_system.components.modals_and_overlays.LynkAdaptiveSheet
import com.eeseka.lynk.shared.design_system.components.textfields.LynkText
import com.eeseka.lynk.shared.design_system.components.textfields.LynkTextField
import com.eeseka.lynk.shared.design_system.components.toggles_and_control.LynkSegmentedControl
import com.eeseka.lynk.shared.design_system.components.toggles_and_control.LynkSegmentedItem
import com.eeseka.lynk.shared.design_system.components.toggles_and_control.LynkSegmentedStyle
import com.eeseka.lynk.shared.design_system.components.util.AppHaptic
import com.eeseka.lynk.shared.design_system.components.util.rememberAppHaptic
import com.eeseka.lynk.shared.design_system.theme.LynkTheme
import com.eeseka.lynk.shared.domain.hangout.HangoutConstants.MAX_NOTE_LENGTH
import com.eeseka.lynk.shared.domain.hangout.model.HangoutNoteAudience
import com.eeseka.lynk.shared.presentation.util.clearFocusOnTap
import kotlinx.collections.immutable.persistentListOf
import lynk.feature.hangouts.generated.resources.Res
import lynk.feature.hangouts.generated.resources.notes_counter
import lynk.feature.hangouts.generated.resources.notes_edit_title
import lynk.feature.hangouts.generated.resources.notes_hint
import lynk.feature.hangouts.generated.resources.notes_new_title
import lynk.feature.hangouts.generated.resources.notes_post
import lynk.feature.hangouts.generated.resources.notes_posting
import lynk.feature.hangouts.generated.resources.notes_save
import lynk.feature.hangouts.generated.resources.notes_saving
import lynk.feature.hangouts.generated.resources.notes_tell_everyone
import lynk.feature.hangouts.generated.resources.notes_tell_helper
import lynk.feature.hangouts.generated.resources.notes_tell_host
import lynk.feature.hangouts.generated.resources.notes_tell_title
import org.jetbrains.compose.resources.stringResource

@Composable
fun NoteComposerSheet(
    noteState: TextFieldState,
    isEditing: Boolean,
    canChooseAudience: Boolean,
    audience: HangoutNoteAudience,
    canPost: Boolean,
    isPosting: Boolean,
    onAudienceChange: (HangoutNoteAudience) -> Unit,
    onPost: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    LynkAdaptiveSheet(
        onDismissRequest = onDismiss,
        modifier = modifier
    ) {
        NoteComposerSheetContent(
            noteState = noteState,
            isEditing = isEditing,
            canChooseAudience = canChooseAudience,
            audience = audience,
            canPost = canPost,
            isPosting = isPosting,
            onAudienceChange = onAudienceChange,
            onPost = onPost
        )
    }
}

@Composable
private fun NoteComposerSheetContent(
    noteState: TextFieldState,
    isEditing: Boolean,
    canChooseAudience: Boolean,
    audience: HangoutNoteAudience,
    canPost: Boolean,
    isPosting: Boolean,
    onAudienceChange: (HangoutNoteAudience) -> Unit,
    onPost: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hapticFeedback = rememberAppHaptic()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clearFocusOnTap()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        LynkText(
            text = stringResource(if (isEditing) Res.string.notes_edit_title else Res.string.notes_new_title),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(top = 16.dp)
        )

        LynkTextField(
            state = noteState,
            placeholder = stringResource(Res.string.notes_hint),
            singleLine = false,
            maxLines = 4,
            helperText = stringResource(Res.string.notes_counter, noteState.text.length, MAX_NOTE_LENGTH),
            inputTransformation = InputTransformation.maxLength(MAX_NOTE_LENGTH)
        )

        if (canChooseAudience && !isEditing) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                LynkText(
                    text = stringResource(Res.string.notes_tell_title),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                LynkSegmentedControl(
                    items = persistentListOf(
                        LynkSegmentedItem(stringResource(Res.string.notes_tell_everyone), Lucide.Users),
                        LynkSegmentedItem(stringResource(Res.string.notes_tell_host), Lucide.Crown)
                    ),
                    selectedIndex = if (audience == HangoutNoteAudience.HOST) 1 else 0,
                    onItemSelected = { index ->
                        hapticFeedback(AppHaptic.Selection)
                        onAudienceChange(if (index == 1) HangoutNoteAudience.HOST else HangoutNoteAudience.EVERYONE)
                    },
                    style = LynkSegmentedStyle.FIXED_BAR,
                    contentPadding = PaddingValues(0.dp)
                )
                LynkText(
                    text = stringResource(Res.string.notes_tell_helper),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        LynkButton(
            text = stringResource(if (isEditing) Res.string.notes_save else Res.string.notes_post),
            onClick = {
                hapticFeedback(AppHaptic.ImpactMedium)
                onPost()
            },
            enabled = canPost,
            isLoading = isPosting,
            loadingText = stringResource(if (isEditing) Res.string.notes_saving else Res.string.notes_posting),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@PreviewLightDark
@Composable
private fun NoteComposerSheetGuestPreview() = NoteComposerSheetPreview(canChooseAudience = true)

@PreviewLightDark
@Composable
private fun NoteComposerSheetEditPreview() = NoteComposerSheetPreview(isEditing = true)

@Composable
private fun NoteComposerSheetPreview(
    isEditing: Boolean = false,
    canChooseAudience: Boolean = false
) {
    LynkTheme {
        NoteComposerSheetContent(
            noteState = TextFieldState("Running 15 mins late, start without me"),
            isEditing = isEditing,
            canChooseAudience = canChooseAudience,
            audience = HangoutNoteAudience.EVERYONE,
            canPost = true,
            isPosting = false,
            onAudienceChange = {},
            onPost = {},
            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
        )
    }
}
