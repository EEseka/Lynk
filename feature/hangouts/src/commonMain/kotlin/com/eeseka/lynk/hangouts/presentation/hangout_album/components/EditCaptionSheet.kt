package com.eeseka.lynk.hangouts.presentation.hangout_album.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import com.eeseka.lynk.shared.design_system.components.buttons.LynkButton
import com.eeseka.lynk.shared.design_system.components.buttons.LynkButtonStyle
import com.eeseka.lynk.shared.design_system.components.modals_and_overlays.LynkAdaptiveSheet
import com.eeseka.lynk.shared.design_system.components.textfields.LynkText
import com.eeseka.lynk.shared.design_system.components.textfields.LynkTextField
import com.eeseka.lynk.shared.design_system.components.util.AppHaptic
import com.eeseka.lynk.shared.design_system.components.util.rememberAppHaptic
import com.eeseka.lynk.shared.design_system.theme.LynkTheme
import lynk.feature.hangouts.generated.resources.Res
import lynk.feature.hangouts.generated.resources.album_caption_counter
import lynk.feature.hangouts.generated.resources.album_caption_hint
import lynk.feature.hangouts.generated.resources.album_caption_save
import lynk.feature.hangouts.generated.resources.album_caption_saving
import lynk.feature.hangouts.generated.resources.album_caption_title
import org.jetbrains.compose.resources.stringResource

private const val MAX_CAPTION_LENGTH = 200

@Composable
fun EditCaptionSheet(
    captionState: TextFieldState,
    canSave: Boolean,
    isSaving: Boolean,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    LynkAdaptiveSheet(
        onDismissRequest = onDismiss,
        modifier = modifier
    ) {
        EditCaptionSheetContent(
            captionState = captionState,
            canSave = canSave,
            isSaving = isSaving,
            onSave = onSave
        )
    }
}

@Composable
private fun EditCaptionSheetContent(
    captionState: TextFieldState,
    canSave: Boolean,
    isSaving: Boolean,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hapticFeedback = rememberAppHaptic()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        LynkText(
            text = stringResource(Res.string.album_caption_title),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(top = 16.dp)
        )

        LynkTextField(
            state = captionState,
            placeholder = stringResource(Res.string.album_caption_hint),
            singleLine = false,
            maxLines = 4,
            helperText = stringResource(Res.string.album_caption_counter, captionState.text.length, MAX_CAPTION_LENGTH),
            inputTransformation = InputTransformation.maxLength(MAX_CAPTION_LENGTH)
        )

        LynkButton(
            text = stringResource(Res.string.album_caption_save),
            onClick = {
                hapticFeedback(AppHaptic.ImpactMedium)
                onSave()
            },
            style = LynkButtonStyle.PRIMARY,
            enabled = canSave,
            isLoading = isSaving,
            loadingText = stringResource(Res.string.album_caption_saving),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@PreviewLightDark
@Composable
private fun EditCaptionSheetContentPreview() {
    LynkTheme {
        EditCaptionSheetContent(
            captionState = TextFieldState("Suya run after the show"),
            canSave = true,
            isSaving = false,
            onSave = {},
            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
        )
    }
}
