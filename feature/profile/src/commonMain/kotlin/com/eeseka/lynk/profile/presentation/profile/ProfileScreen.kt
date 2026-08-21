package com.eeseka.lynk.profile.presentation.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Camera
import com.composables.icons.lucide.Image
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Settings
import com.eeseka.lynk.profile.presentation.profile.components.GuestProfileSection
import com.eeseka.lynk.profile.presentation.profile.components.ProfileAccountSection
import com.eeseka.lynk.profile.presentation.profile.components.ProfileSettingsSheet
import com.eeseka.lynk.profile.presentation.profile.components.ProfileStatsRow
import com.eeseka.lynk.shared.design_system.components.buttons.LynkButton
import com.eeseka.lynk.shared.design_system.components.buttons.LynkIconButton
import com.eeseka.lynk.shared.design_system.components.layouts.LynkScaffold
import com.eeseka.lynk.shared.design_system.components.modals_and_overlays.LynkActionSheet
import com.eeseka.lynk.shared.design_system.components.modals_and_overlays.LynkActionSheetItem
import com.eeseka.lynk.shared.design_system.components.modals_and_overlays.LynkDialog
import com.eeseka.lynk.shared.design_system.components.modals_and_overlays.LynkFlashType
import com.eeseka.lynk.shared.design_system.components.modals_and_overlays.showFlashMessage
import com.eeseka.lynk.shared.design_system.components.navigation.LynkIosBarButtonItem
import com.eeseka.lynk.shared.design_system.components.navigation.LynkTopAppBar
import com.eeseka.lynk.shared.design_system.components.textfields.LynkTextField
import com.eeseka.lynk.shared.design_system.components.util.AppHaptic
import com.eeseka.lynk.shared.design_system.components.util.rememberAppHaptic
import com.eeseka.lynk.shared.design_system.theme.LynkTheme
import com.eeseka.lynk.shared.presentation.components.FullScreenAvatarViewer
import com.eeseka.lynk.shared.presentation.components.ProfileAvatarSection
import com.eeseka.lynk.shared.presentation.media.rememberMediaPicker
import com.eeseka.lynk.shared.presentation.util.DeviceConfiguration
import com.eeseka.lynk.shared.presentation.util.ObserveAsEvents
import com.eeseka.lynk.shared.presentation.util.clearFocusOnTap
import com.eeseka.lynk.shared.presentation.util.currentDeviceConfiguration
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import lynk.feature.profile.generated.resources.Res
import lynk.feature.profile.generated.resources.app_version
import lynk.feature.profile.generated.resources.cancel
import lynk.feature.profile.generated.resources.choose_from_gallery
import lynk.feature.profile.generated.resources.choose_source
import lynk.feature.profile.generated.resources.choose_source_message
import lynk.feature.profile.generated.resources.delete
import lynk.feature.profile.generated.resources.delete_account_confirm_message
import lynk.feature.profile.generated.resources.delete_account_confirm_title
import lynk.feature.profile.generated.resources.display_name
import lynk.feature.profile.generated.resources.display_name_placeholder
import lynk.feature.profile.generated.resources.profile
import lynk.feature.profile.generated.resources.profile_saved
import lynk.feature.profile.generated.resources.save_changes
import lynk.feature.profile.generated.resources.saving_changes
import lynk.feature.profile.generated.resources.settings
import lynk.feature.profile.generated.resources.sign_out
import lynk.feature.profile.generated.resources.sign_out_confirm_message
import lynk.feature.profile.generated.resources.sign_out_confirm_title
import lynk.feature.profile.generated.resources.take_photo
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource

private const val TERMS_AND_PRIVACY_URL = "https://example.com/privacy"
private const val APP_VERSION = "1.0.0"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    state: ProfileState,
    events: Flow<ProfileEvent>,
    onAction: (ProfileAction) -> Unit,
    onNavigateToSavedSpots: () -> Unit,
    mainShellPadding: PaddingValues
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val hapticFeedback = rememberAppHaptic()
    val scope = rememberCoroutineScope()
    val mediaPicker = rememberMediaPicker()
    val uriHandler = LocalUriHandler.current
    val config = currentDeviceConfiguration()

    var showImagePickerSheet by remember { mutableStateOf(false) }

    ObserveAsEvents(events) { event ->
        when (event) {
            is ProfileEvent.Error -> {
                hapticFeedback(AppHaptic.Error)
                snackbarHostState.showFlashMessage(
                    message = event.message.asStringAsync(),
                    type = LynkFlashType.Error
                )
            }

            ProfileEvent.ProfileSaved -> {
                hapticFeedback(AppHaptic.Success)
                snackbarHostState.showFlashMessage(
                    message = getString(Res.string.profile_saved),
                    type = LynkFlashType.Success
                )
            }
        }
    }

    LynkScaffold(
        snackbarHostState = snackbarHostState,
        topBar = {
            val settingsLabel = stringResource(Res.string.settings)

            LynkTopAppBar(
                title = stringResource(Res.string.profile),
                actions = {
                    LynkIconButton(
                        onClick = {
                            hapticFeedback(AppHaptic.ImpactLight)
                            onAction(ProfileAction.OnSettingsClick)
                        }
                    ) {
                        Icon(
                            imageVector = Lucide.Settings,
                            contentDescription = settingsLabel
                        )
                    }
                },
                iosTrailingItems = listOf(
                    LynkIosBarButtonItem(
                        sfSymbol = "gearshape",
                        onClick = {
                            hapticFeedback(AppHaptic.ImpactLight)
                            onAction(ProfileAction.OnSettingsClick)
                        }
                    )
                )
            )
        }
    ) { scaffoldPadding ->
        val topInset = scaffoldPadding.calculateTopPadding() + 24.dp
        val contentBottomInset = mainShellPadding.calculateBottomPadding() + 24.dp

        Box(
            modifier = Modifier
                .fillMaxSize()
                .clearFocusOnTap(),
            contentAlignment = Alignment.TopCenter
        ) {
            when (config) {
                DeviceConfiguration.MOBILE_LANDSCAPE -> {
                    if (state.isGuest) {
                        SinglePaneProfile(
                            state = state,
                            topInset = topInset,
                            bottomInset = contentBottomInset,
                            onAction = onAction,
                            onPickImageClick = { showImagePickerSheet = true },
                            onNavigateToSavedSpots = onNavigateToSavedSpots
                        )
                    } else {
                        TwoPaneProfile(
                            state = state,
                            topInset = topInset,
                            bottomInset = contentBottomInset,
                            onAction = onAction,
                            onPickImageClick = { showImagePickerSheet = true },
                            onNavigateToSavedSpots = onNavigateToSavedSpots
                        )
                    }
                }

                else -> {
                    SinglePaneProfile(
                        state = state,
                        topInset = topInset,
                        bottomInset = contentBottomInset,
                        onAction = onAction,
                        onPickImageClick = { showImagePickerSheet = true },
                        onNavigateToSavedSpots = onNavigateToSavedSpots
                    )
                }
            }
        }
    }

    if (showImagePickerSheet) {
        LynkActionSheet(
            onDismissRequest = { showImagePickerSheet = false },
            title = stringResource(Res.string.choose_source),
            message = stringResource(Res.string.choose_source_message),
            items = listOf(
                LynkActionSheetItem(
                    text = stringResource(Res.string.take_photo),
                    icon = Lucide.Camera,
                    onClick = {
                        scope.launch {
                            val image = mediaPicker.captureImage()
                            if (image != null) {
                                onAction(ProfileAction.OnImagePicked(image.uri, image.mimeType))
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
                            if (image != null) {
                                onAction(ProfileAction.OnImagePicked(image.uri, image.mimeType))
                            }
                        }
                    }
                )
            )
        )
    }

    if (state.showSettingsSheet) {
        ProfileSettingsSheet(
            theme = state.appTheme,
            arePushNotificationsEnabled = state.arePushNotificationsEnabled,
            isGuest = state.isGuest,
            isSigningOut = state.isSigningOut,
            isDeletingAccount = state.isDeletingAccount,
            appVersion = stringResource(Res.string.app_version, APP_VERSION),
            onThemeSelected = { onAction(ProfileAction.OnThemeSelected(it)) },
            onPushNotificationsToggled = {
                onAction(ProfileAction.OnPushNotificationsToggled(it))
            },
            onTermsClick = { uriHandler.openUri(TERMS_AND_PRIVACY_URL) },
            onSignOutClick = { onAction(ProfileAction.OnSignOutClick) },
            onDeleteAccountClick = { onAction(ProfileAction.OnDeleteAccountClick) },
            onDismissRequest = { onAction(ProfileAction.OnDismissSettings) }
        )
    }

    if (state.showSignOutConfirmation) {
        LynkDialog(
            onDismissRequest = { onAction(ProfileAction.OnDismissSignOutConfirmation) },
            title = stringResource(Res.string.sign_out_confirm_title),
            message = stringResource(Res.string.sign_out_confirm_message),
            confirmText = stringResource(Res.string.sign_out),
            dismissText = stringResource(Res.string.cancel),
            onConfirm = { onAction(ProfileAction.OnConfirmSignOut) },
            isDestructive = true
        )
    }

    if (state.showDeleteAccountConfirmation) {
        LynkDialog(
            onDismissRequest = { onAction(ProfileAction.OnDismissDeleteAccountConfirmation) },
            title = stringResource(Res.string.delete_account_confirm_title),
            message = stringResource(Res.string.delete_account_confirm_message),
            confirmText = stringResource(Res.string.delete),
            dismissText = stringResource(Res.string.cancel),
            onConfirm = { onAction(ProfileAction.OnConfirmDeleteAccount) },
            isDestructive = true
        )
    }

    val fullScreenImage = state.localPhotoUri ?: state.profilePictureUrl
    if (state.showFullScreenImage && fullScreenImage != null) {
        FullScreenAvatarViewer(
            model = fullScreenImage,
            onDismiss = { onAction(ProfileAction.OnDismissFullScreenImage) }
        )
    }
}

@Composable
private fun SinglePaneProfile(
    state: ProfileState,
    topInset: Dp,
    bottomInset: Dp,
    onAction: (ProfileAction) -> Unit,
    onPickImageClick: () -> Unit,
    onNavigateToSavedSpots: () -> Unit
) {
    Column(
        modifier = Modifier
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
                state = state,
                onAction = onAction,
                onPickImageClick = onPickImageClick
            )

            Spacer(modifier = Modifier.height(16.dp))

            ProfileDetailsSection(
                state = state,
                onNavigateToSavedSpots = onNavigateToSavedSpots
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
private fun TwoPaneProfile(
    state: ProfileState,
    topInset: Dp,
    bottomInset: Dp,
    onAction: (ProfileAction) -> Unit,
    onPickImageClick: () -> Unit,
    onNavigateToSavedSpots: () -> Unit
) {
    Row(
        modifier = Modifier
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
                state = state,
                onAction = onAction,
                onPickImageClick = onPickImageClick
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
                state = state,
                onNavigateToSavedSpots = onNavigateToSavedSpots
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
        modifier = Modifier
            .widthIn(max = 480.dp)
            .height(56.dp)
    )
}

@Composable
private fun ProfileHeaderSection(
    state: ProfileState,
    onAction: (ProfileAction) -> Unit,
    onPickImageClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        ProfileAvatarSection(
            currentImagePayload = state.localPhotoUri ?: state.profilePictureUrl,
            imageError = state.imageError?.asString(),
            isCompressingImage = state.isCompressingImage,
            isUploadingImage = state.isUploadingImage,
            onImageClick = onPickImageClick,
            onRemoveImage = { onAction(ProfileAction.OnRemoveImageClick) },
            onViewImageClick = { onAction(ProfileAction.OnImageClick) }
        )

        Spacer(modifier = Modifier.height(24.dp))

        ProfileStatsRow(
            hostedCount = state.hostedCount.toString(),
            attendedCount = state.attendedCount.toString(),
            isLoading = state.isStatsLoading
        )
    }
}

@Composable
private fun ProfileDetailsSection(
    state: ProfileState,
    onNavigateToSavedSpots: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        LynkTextField(
            state = state.displayNameTextState,
            label = stringResource(Res.string.display_name),
            placeholder = stringResource(Res.string.display_name_placeholder),
            errorMessage = state.displayNameError?.asString(),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Done
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        ProfileAccountSection(
            email = state.email,
            username = state.username,
            onSavedSpotsClick = onNavigateToSavedSpots
        )
    }
}

private fun previewState(isGuest: Boolean = false) = ProfileState(
    isGuest = isGuest,
    email = "john.doe@example.com",
    username = "johndoe",
    displayNameTextState = TextFieldState("John Doe"),
    hostedCount = 12L,
    attendedCount = 34L
)

@Composable
private fun ProfileScreenPreview(state: ProfileState) {
    LynkTheme {
        ProfileScreen(
            state = state,
            events = emptyFlow(),
            onAction = {},
            onNavigateToSavedSpots = {},
            mainShellPadding = PaddingValues(0.dp)
        )
    }
}

@PreviewLightDark
@Composable
private fun ProfileScreenPreview() = ProfileScreenPreview(previewState())

@PreviewLightDark
@Composable
private fun ProfileScreenGuestPreview() = ProfileScreenPreview(previewState(isGuest = true))

@Preview(name = "Tablet landscape", widthDp = 1280, heightDp = 800)
@Composable
private fun ProfileScreenTabletPreview() = ProfileScreenPreview(previewState())