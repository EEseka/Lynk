package com.eeseka.lynk.profile.presentation.profile.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.eeseka.lynk.profile.presentation.profile.ProfileAction
import com.eeseka.lynk.profile.presentation.profile.ProfileState
import com.eeseka.lynk.shared.design_system.components.buttons.LynkButton
import com.eeseka.lynk.shared.design_system.components.textfields.LynkTextField
import com.eeseka.lynk.shared.design_system.components.util.AppHaptic
import com.eeseka.lynk.shared.design_system.components.util.rememberAppHaptic
import com.eeseka.lynk.shared.design_system.theme.LynkTheme
import com.eeseka.lynk.shared.presentation.components.ProfileAvatarSection
import lynk.feature.profile.generated.resources.Res
import lynk.feature.profile.generated.resources.display_name
import lynk.feature.profile.generated.resources.display_name_placeholder
import lynk.feature.profile.generated.resources.save_changes
import lynk.feature.profile.generated.resources.saving_changes
import org.jetbrains.compose.resources.stringResource

@Composable
fun SinglePaneProfile(
    state: ProfileState,
    topInset: Dp,
    bottomInset: Dp,
    onAction: (ProfileAction) -> Unit,
    onPickImageClick: () -> Unit,
    navigateToSavedSpots: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .widthIn(max = 480.dp)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(
                top = topInset,
                bottom = bottomInset,
                start = 24.dp,
                end = 24.dp
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = if (state.isGuest) Arrangement.Center else Arrangement.Top
    ) {
        if (state.isGuest) {
            GuestProfileSection(
                isDeletingAccount = state.isDeletingAccount,
                onCreateAccountClick = { onAction(ProfileAction.OnCreateAccountClick) }
            )
        } else {
            ProfileHeaderSection(
                imagePayload = state.localPhotoUri ?: state.profilePictureUrl,
                imageError = state.imageError?.asString(),
                isCompressingImage = state.isCompressingImage,
                isUploadingImage = state.isUploadingImage,
                hostedCount = state.hostedCount.toString(),
                attendedCount = state.attendedCount.toString(),
                isStatsLoading = state.isStatsLoading,
                onPickImageClick = onPickImageClick,
                onRemoveImageClick = { onAction(ProfileAction.OnRemoveImageClick) },
                onViewImageClick = { onAction(ProfileAction.OnImageClick) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            ProfileDetailsSection(
                displayNameState = state.displayNameTextState,
                displayNameError = state.displayNameError?.asString(),
                email = state.email,
                username = state.username,
                navigateToSavedSpots = navigateToSavedSpots
            )

            Spacer(modifier = Modifier.height(32.dp))

            SaveChangesButton(
                canSave = state.canSave,
                isSaving = state.isSaving,
                onSaveClick = { onAction(ProfileAction.OnSaveClick) }
            )
        }
    }
}

@Composable
fun TwoPaneProfile(
    state: ProfileState,
    topInset: Dp,
    bottomInset: Dp,
    onAction: (ProfileAction) -> Unit,
    onPickImageClick: () -> Unit,
    navigateToSavedSpots: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
                .padding(top = topInset, bottom = bottomInset),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            ProfileHeaderSection(
                imagePayload = state.localPhotoUri ?: state.profilePictureUrl,
                imageError = state.imageError?.asString(),
                isCompressingImage = state.isCompressingImage,
                isUploadingImage = state.isUploadingImage,
                hostedCount = state.hostedCount.toString(),
                attendedCount = state.attendedCount.toString(),
                isStatsLoading = state.isStatsLoading,
                onPickImageClick = onPickImageClick,
                onRemoveImageClick = { onAction(ProfileAction.OnRemoveImageClick) },
                onViewImageClick = { onAction(ProfileAction.OnImageClick) }
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
                .padding(top = topInset, bottom = bottomInset),
            verticalArrangement = Arrangement.Center
        ) {
            ProfileDetailsSection(
                displayNameState = state.displayNameTextState,
                displayNameError = state.displayNameError?.asString(),
                email = state.email,
                username = state.username,
                navigateToSavedSpots = navigateToSavedSpots
            )

            Spacer(modifier = Modifier.height(32.dp))

            SaveChangesButton(
                canSave = state.canSave,
                isSaving = state.isSaving,
                onSaveClick = { onAction(ProfileAction.OnSaveClick) }
            )
        }
    }
}

@Composable
private fun SaveChangesButton(
    canSave: Boolean,
    isSaving: Boolean,
    onSaveClick: () -> Unit
) {
    val hapticFeedback = rememberAppHaptic()

    LynkButton(
        text = stringResource(Res.string.save_changes),
        loadingText = stringResource(Res.string.saving_changes),
        onClick = {
            hapticFeedback(AppHaptic.ImpactMedium)
            onSaveClick()
        },
        enabled = canSave,
        isLoading = isSaving,
        modifier = Modifier.widthIn(max = 480.dp)
    )
}

@Composable
private fun ProfileHeaderSection(
    imagePayload: String?,
    imageError: String?,
    isCompressingImage: Boolean,
    isUploadingImage: Boolean,
    hostedCount: String,
    attendedCount: String,
    isStatsLoading: Boolean,
    onPickImageClick: () -> Unit,
    onRemoveImageClick: () -> Unit,
    onViewImageClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        ProfileAvatarSection(
            currentImagePayload = imagePayload,
            imageError = imageError,
            isCompressingImage = isCompressingImage,
            isUploadingImage = isUploadingImage,
            onImageClick = onPickImageClick,
            onRemoveImage = onRemoveImageClick,
            onViewImageClick = onViewImageClick
        )

        Spacer(modifier = Modifier.height(24.dp))

        ProfileStatsRow(
            hostedCount = hostedCount,
            attendedCount = attendedCount,
            isLoading = isStatsLoading
        )
    }
}

@Composable
private fun ProfileDetailsSection(
    displayNameState: TextFieldState,
    displayNameError: String?,
    email: String,
    username: String,
    navigateToSavedSpots: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        LynkTextField(
            state = displayNameState,
            label = stringResource(Res.string.display_name),
            placeholder = stringResource(Res.string.display_name_placeholder),
            errorMessage = displayNameError,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Done
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        ProfileAccountSection(
            email = email,
            username = username,
            onSavedSpotsClick = navigateToSavedSpots
        )
    }
}

@PreviewLightDark
@Composable
private fun SinglePaneProfilePreview() {
    LynkTheme {
        SinglePaneProfile(
            state = previewState(),
            topInset = 24.dp,
            bottomInset = 24.dp,
            onAction = {},
            onPickImageClick = {},
            navigateToSavedSpots = {},
            modifier = Modifier.background(MaterialTheme.colorScheme.background)
        )
    }
}

@Preview(name = "Mobile landscape", widthDp = 900, heightDp = 400)
@Composable
private fun TwoPaneProfilePreview() {
    LynkTheme {
        TwoPaneProfile(
            state = previewState(),
            topInset = 24.dp,
            bottomInset = 24.dp,
            onAction = {},
            onPickImageClick = {},
            navigateToSavedSpots = {},
            modifier = Modifier.background(MaterialTheme.colorScheme.background)
        )
    }
}

private fun previewState() = ProfileState(
    email = "john.doe@example.com",
    username = "johndoe",
    displayNameTextState = TextFieldState("John Doe"),
    hostedCount = 12L,
    attendedCount = 34L
)
