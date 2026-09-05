package com.eeseka.lynk.profile_setup.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.composables.icons.lucide.Camera
import com.composables.icons.lucide.Image
import com.composables.icons.lucide.Lucide
import com.eeseka.lynk.profile_setup.presentation.components.ProfileFormSection
import com.eeseka.lynk.profile_setup.presentation.components.ProfileSetupHeader
import com.eeseka.lynk.shared.design_system.components.buttons.LynkButton
import com.eeseka.lynk.shared.design_system.components.layouts.LynkCard
import com.eeseka.lynk.shared.design_system.components.layouts.LynkCardStyle
import com.eeseka.lynk.shared.design_system.components.layouts.LynkScaffold
import com.eeseka.lynk.shared.design_system.components.modals_and_overlays.LynkActionSheet
import com.eeseka.lynk.shared.design_system.components.modals_and_overlays.LynkActionSheetItem
import com.eeseka.lynk.shared.design_system.components.modals_and_overlays.LynkFlashType
import com.eeseka.lynk.shared.design_system.components.modals_and_overlays.showFlashMessage
import com.eeseka.lynk.shared.design_system.components.util.AppHaptic
import com.eeseka.lynk.shared.design_system.components.util.rememberAppHaptic
import com.eeseka.lynk.shared.design_system.theme.LynkTheme
import com.eeseka.lynk.shared.presentation.components.ProfileAvatarSection
import com.eeseka.lynk.shared.presentation.media.rememberMediaPicker
import com.eeseka.lynk.shared.presentation.util.DeviceConfiguration
import com.eeseka.lynk.shared.presentation.util.ObserveAsEvents
import com.eeseka.lynk.shared.presentation.util.clearFocusOnTap
import com.eeseka.lynk.shared.presentation.util.currentDeviceConfiguration
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.launch
import lynk.feature.profile_setup.generated.resources.Res
import lynk.feature.profile_setup.generated.resources.choose_from_gallery
import lynk.feature.profile_setup.generated.resources.choose_source
import lynk.feature.profile_setup.generated.resources.choose_source_message
import lynk.feature.profile_setup.generated.resources.complete_profile
import lynk.feature.profile_setup.generated.resources.complete_profile_loading
import lynk.feature.profile_setup.generated.resources.take_photo
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ProfileSetupRoot(
    navigateToMain: () -> Unit,
    viewModel: ProfileSetupViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val hapticFeedback = rememberAppHaptic()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is ProfileSetupEvent.Error -> {
                snackbarHostState.showFlashMessage(
                    message = event.error.asStringAsync(),
                    type = LynkFlashType.Error
                )
            }

            ProfileSetupEvent.Success -> {
                hapticFeedback(AppHaptic.Success)
                navigateToMain()
            }
        }
    }

    ProfileSetupScreen(
        state = state,
        onAction = viewModel::onAction,
        snackbarHostState = snackbarHostState
    )
}

@Composable
fun ProfileSetupScreen(
    state: ProfileSetupState,
    onAction: (ProfileSetupAction) -> Unit,
    snackbarHostState: SnackbarHostState
) {
    val config = currentDeviceConfiguration()
    val hapticFeedback = rememberAppHaptic()
    val scope = rememberCoroutineScope()
    val mediaPicker = rememberMediaPicker()

    var showImagePickerSheet by remember { mutableStateOf(false) }

    LynkScaffold(
        snackbarHostState = snackbarHostState
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .clearFocusOnTap(),
            contentAlignment = Alignment.Center
        ) {
            when (config) {
                DeviceConfiguration.MOBILE_PORTRAIT -> {
                    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState()),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(32.dp)
                        ) {
                            ProfileSetupHeader()

                            ProfileAvatarSection(
                                currentImagePayload = state.localPhotoUri ?: state.profilePictureUrl,
                                imageError = state.imageError?.asString(),
                                isCompressingImage = state.isCompressingImage,
                                isUploadingImage = state.isUploadingImage,
                                onImageClick = { showImagePickerSheet = true },
                                onRemoveImage = { onAction(ProfileSetupAction.OnRemoveImageClick) }
                            )

                            ProfileFormSection(
                                email = state.email,
                                displayNameState = state.displayNameTextState,
                                displayNameErrorMessage = state.displayNameError?.asString(),
                                usernameState = state.usernameTextState,
                                usernameErrorMessage = state.usernameError?.asString(),
                                isCheckingUsername = state.isCheckingUsername,
                                isUsernameAvailable = state.isUsernameAvailable
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        LynkButton(
                            text = stringResource(Res.string.complete_profile),
                            onClick = {
                                hapticFeedback(AppHaptic.ImpactMedium)
                                onAction(ProfileSetupAction.OnSubmitClick)
                            },
                            enabled = state.canSubmit,
                            isLoading = state.isSubmitting,
                            loadingText = stringResource(Res.string.complete_profile_loading)
                        )
                    }
                }

                DeviceConfiguration.MOBILE_LANDSCAPE -> {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(32.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .verticalScroll(rememberScrollState()),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            ProfileAvatarSection(
                                currentImagePayload = state.localPhotoUri
                                    ?: state.profilePictureUrl,
                                imageError = state.imageError?.asString(),
                                isCompressingImage = state.isCompressingImage,
                                isUploadingImage = state.isUploadingImage,
                                onImageClick = { showImagePickerSheet = true },
                                onRemoveImage = { onAction(ProfileSetupAction.OnRemoveImageClick) }
                            )
                        }

                        Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(
                                    space = 24.dp,
                                    alignment = Alignment.CenterVertically
                                )
                            ) {
                                ProfileSetupHeader()

                                ProfileFormSection(
                                    email = state.email,
                                    displayNameState = state.displayNameTextState,
                                    displayNameErrorMessage = state.displayNameError?.asString(),
                                    usernameState = state.usernameTextState,
                                    usernameErrorMessage = state.usernameError?.asString(),
                                    isCheckingUsername = state.isCheckingUsername,
                                    isUsernameAvailable = state.isUsernameAvailable
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            LynkButton(
                                text = stringResource(Res.string.complete_profile),
                                onClick = {
                                    hapticFeedback(AppHaptic.ImpactMedium)
                                    onAction(ProfileSetupAction.OnSubmitClick)
                                },
                                enabled = state.canSubmit,
                                isLoading = state.isSubmitting,
                                loadingText = stringResource(Res.string.complete_profile_loading)
                            )
                        }
                    }
                }

                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 48.dp, bottom = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        LynkCard(
                            style = LynkCardStyle.ELEVATED,
                            modifier = Modifier.widthIn(max = 480.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .weight(1f, fill = false)
                                        .verticalScroll(rememberScrollState()),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(32.dp)
                                ) {
                                    ProfileSetupHeader()

                                    ProfileAvatarSection(
                                        currentImagePayload = state.localPhotoUri
                                            ?: state.profilePictureUrl,
                                        imageError = state.imageError?.asString(),
                                        isCompressingImage = state.isCompressingImage,
                                        isUploadingImage = state.isUploadingImage,
                                        onImageClick = { showImagePickerSheet = true },
                                        onRemoveImage = { onAction(ProfileSetupAction.OnRemoveImageClick) }
                                    )

                                    ProfileFormSection(
                                        email = state.email,
                                        displayNameState = state.displayNameTextState,
                                        displayNameErrorMessage = state.displayNameError?.asString(),
                                        usernameState = state.usernameTextState,
                                        usernameErrorMessage = state.usernameError?.asString(),
                                        isCheckingUsername = state.isCheckingUsername,
                                        isUsernameAvailable = state.isUsernameAvailable
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                LynkButton(
                                    text = stringResource(Res.string.complete_profile),
                                    onClick = {
                                        hapticFeedback(AppHaptic.ImpactMedium)
                                        onAction(ProfileSetupAction.OnSubmitClick)
                                    },
                                    enabled = state.canSubmit,
                                    isLoading = state.isSubmitting,
                                    loadingText = stringResource(Res.string.complete_profile_loading)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showImagePickerSheet) {
        LynkActionSheet(
            onDismissRequest = { showImagePickerSheet = false },
            title = stringResource(Res.string.choose_source),
            message = stringResource(Res.string.choose_source_message),
            items = persistentListOf(
                LynkActionSheetItem(
                    text = stringResource(Res.string.take_photo),
                    icon = Lucide.Camera,
                    onClick = {
                        scope.launch {
                            val image = mediaPicker.captureImage()
                            image?.let {
                                onAction(ProfileSetupAction.OnImagePicked(it.uri, it.mimeType))
                            }
                        }
                    }
                ),
                LynkActionSheetItem(
                    text = stringResource(Res.string.choose_from_gallery),
                    icon = Lucide.Image,
                    onClick = {
                        scope.launch {
                            val image = mediaPicker.pickImage()
                            image?.let {
                                onAction(ProfileSetupAction.OnImagePicked(it.uri, it.mimeType))
                            }
                        }
                    }
                )
            )
        )
    }
}

@PreviewLightDark
@Composable
private fun ProfileSetupScreenPreview() {
    LynkTheme {
        ProfileSetupScreen(
            state = ProfileSetupState(email = "eseka@lynk.app"),
            onAction = {},
            snackbarHostState = remember { SnackbarHostState() }
        )
    }
}

@Preview(name = "Mobile landscape", widthDp = 900, heightDp = 400)
@Composable
private fun ProfileSetupScreenLandscapePreview() {
    LynkTheme {
        ProfileSetupScreen(
            state = ProfileSetupState(email = "eseka@lynk.app"),
            onAction = {},
            snackbarHostState = remember { SnackbarHostState() }
        )
    }
}

@Preview(name = "Tablet landscape", widthDp = 1280, heightDp = 800)
@Composable
private fun ProfileSetupScreenTabletPreview() {
    LynkTheme {
        ProfileSetupScreen(
            state = ProfileSetupState(email = "eseka@lynk.app"),
            onAction = {},
            snackbarHostState = remember { SnackbarHostState() }
        )
    }
}
