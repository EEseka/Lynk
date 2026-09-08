package com.eeseka.lynk.profile.presentation.profile

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.composables.icons.lucide.Camera
import com.composables.icons.lucide.Image
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Settings
import com.eeseka.lynk.profile.presentation.profile.components.ProfileSettingsSheet
import com.eeseka.lynk.profile.presentation.profile.components.SinglePaneProfile
import com.eeseka.lynk.profile.presentation.profile.components.TwoPaneProfile
import com.eeseka.lynk.shared.design_system.components.buttons.LynkIconButton
import com.eeseka.lynk.shared.design_system.components.buttons.LynkTonalIconButton
import com.eeseka.lynk.shared.design_system.components.layouts.LynkScaffold
import com.eeseka.lynk.shared.design_system.components.modals_and_overlays.LynkActionSheet
import com.eeseka.lynk.shared.design_system.components.modals_and_overlays.LynkActionSheetItem
import com.eeseka.lynk.shared.design_system.components.modals_and_overlays.LynkDialog
import com.eeseka.lynk.shared.design_system.components.modals_and_overlays.LynkFlashType
import com.eeseka.lynk.shared.design_system.components.modals_and_overlays.showFlashMessage
import com.eeseka.lynk.shared.design_system.components.navigation.LynkIosBarButtonItem
import com.eeseka.lynk.shared.design_system.components.navigation.LynkTopAppBar
import com.eeseka.lynk.shared.design_system.components.util.AppHaptic
import com.eeseka.lynk.shared.design_system.components.util.rememberAppHaptic
import com.eeseka.lynk.shared.design_system.theme.LynkTheme
import com.eeseka.lynk.shared.presentation.components.FullScreenAvatarViewer
import com.eeseka.lynk.shared.presentation.media.rememberMediaPicker
import com.eeseka.lynk.shared.presentation.permissions.Permission
import com.eeseka.lynk.shared.presentation.permissions.PermissionState
import com.eeseka.lynk.shared.presentation.permissions.rememberPermissionController
import com.eeseka.lynk.shared.presentation.util.DeviceConfiguration
import com.eeseka.lynk.shared.presentation.util.ObserveAsEvents
import com.eeseka.lynk.shared.presentation.util.buildMailtoUri
import com.eeseka.lynk.shared.presentation.util.clearFocusOnTap
import com.eeseka.lynk.shared.presentation.util.currentDeviceConfiguration
import kotlinx.collections.immutable.persistentListOf
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
import lynk.feature.profile.generated.resources.not_now
import lynk.feature.profile.generated.resources.notifications_required
import lynk.feature.profile.generated.resources.notifications_required_message
import lynk.feature.profile.generated.resources.open_settings
import lynk.feature.profile.generated.resources.profile
import lynk.feature.profile.generated.resources.profile_saved
import lynk.feature.profile.generated.resources.settings
import lynk.feature.profile.generated.resources.sign_out
import lynk.feature.profile.generated.resources.sign_out_confirm_message
import lynk.feature.profile.generated.resources.sign_out_confirm_title
import lynk.feature.profile.generated.resources.support_email_body
import lynk.feature.profile.generated.resources.support_email_subject
import lynk.feature.profile.generated.resources.take_photo
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

private const val TERMS_URL = "https://example.com/terms"
private const val PRIVACY_URL = "https://example.com/privacy"
private const val SUPPORT_EMAIL = "support@lynk.com.ng"
private const val APP_VERSION = "1.0.0"

@Composable
fun ProfileRoot(
    navigateToSavedSpots: () -> Unit,
    mainShellPadding: PaddingValues,
    viewModel: ProfileViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is ProfileEvent.Error -> {
                snackbarHostState.showFlashMessage(
                    message = event.message.asStringAsync(),
                    type = LynkFlashType.Error
                )
            }

            ProfileEvent.ProfileSaved -> {
                snackbarHostState.showFlashMessage(
                    message = getString(Res.string.profile_saved),
                    type = LynkFlashType.Success
                )
            }
        }
    }

    ProfileScreen(
        state = state,
        onAction = viewModel::onAction,
        snackbarHostState = snackbarHostState,
        navigateToSavedSpots = navigateToSavedSpots,
        mainShellPadding = mainShellPadding
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    state: ProfileState,
    onAction: (ProfileAction) -> Unit,
    snackbarHostState: SnackbarHostState,
    navigateToSavedSpots: () -> Unit,
    mainShellPadding: PaddingValues
) {
    val hapticFeedback = rememberAppHaptic()
    val permissionController = rememberPermissionController()

    var showNotificationSettingsDialog by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val mediaPicker = rememberMediaPicker()
    val uriHandler = LocalUriHandler.current
    val config = currentDeviceConfiguration()

    var showImagePickerSheet by remember { mutableStateOf(false) }

    val showRail = config.isWideScreen

    val supportSubject = stringResource(Res.string.support_email_subject)
    val supportBody = stringResource(Res.string.support_email_body, state.userId)

    // The stored preference is only half the story — the system permission can be revoked in
    // the OS settings at any time, which we never hear about. Re-read it whenever the sheet
    // opens so the switch cannot claim notifications are on while the system says otherwise.
    LaunchedEffect(state.showSettingsSheet) {
        if (state.isGuest || !state.showSettingsSheet || !state.arePushNotificationsEnabled) {
            return@LaunchedEffect
        }

        if (permissionController.getPermissionState(Permission.NOTIFICATIONS) != PermissionState.GRANTED) {
            onAction(ProfileAction.OnPushNotificationsToggled(false))
        }
    }

    LynkScaffold(
        snackbarHostState = snackbarHostState,
        topBar = {
            if (!showRail) {
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
                    iosTrailingItems = persistentListOf(
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
        }
    ) { scaffoldPadding ->
        val settingsButtonInset = if (showRail) 48.dp else 0.dp
        val topInset = scaffoldPadding.calculateTopPadding() + settingsButtonInset + 24.dp
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
                            navigateToSavedSpots = navigateToSavedSpots
                        )
                    } else {
                        TwoPaneProfile(
                            state = state,
                            topInset = topInset,
                            bottomInset = contentBottomInset,
                            onAction = onAction,
                            onPickImageClick = { showImagePickerSheet = true },
                            navigateToSavedSpots = navigateToSavedSpots
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
                        navigateToSavedSpots = navigateToSavedSpots
                    )
                }
            }

            if (showRail) {
                val settingsLabel = stringResource(Res.string.settings)

                LynkTonalIconButton(
                    onClick = {
                        hapticFeedback(AppHaptic.ImpactLight)
                        onAction(ProfileAction.OnSettingsClick)
                    },
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = scaffoldPadding.calculateTopPadding(), end = 24.dp)
                ) {
                    Icon(
                        imageVector = Lucide.Settings,
                        contentDescription = settingsLabel
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
            items = persistentListOf(
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
            supportEmail = SUPPORT_EMAIL,
            onThemeSelected = { onAction(ProfileAction.OnThemeSelected(it)) },
            onPushNotificationsToggled = { isEnabled ->
                if (isEnabled) {
                    scope.launch {
                        var permissionState = permissionController.getPermissionState(Permission.NOTIFICATIONS)
                        if (permissionState == PermissionState.NOT_DETERMINED || permissionState == PermissionState.DENIED) {
                            permissionState = permissionController.requestPermission(Permission.NOTIFICATIONS)
                        }

                        when (permissionState) {
                            PermissionState.GRANTED ->
                                onAction(ProfileAction.OnPushNotificationsToggled(true))

                            PermissionState.PERMANENTLY_DENIED ->
                                showNotificationSettingsDialog = true

                            else -> Unit
                        }
                    }
                } else {
                    onAction(ProfileAction.OnPushNotificationsToggled(false))
                }
            },
            onContactSupportClick = {
                uriHandler.openUri(
                    buildMailtoUri(
                        email = SUPPORT_EMAIL,
                        subject = supportSubject,
                        body = supportBody
                    )
                )
            },
            onTermsClick = { uriHandler.openUri(TERMS_URL) },
            onPrivacyClick = { uriHandler.openUri(PRIVACY_URL) },
            onSignOutClick = { onAction(ProfileAction.OnSignOutClick) },
            onDeleteAccountClick = { onAction(ProfileAction.OnDeleteAccountClick) },
            onDismissRequest = { onAction(ProfileAction.OnDismissSettings) }
        )
    }

    if (showNotificationSettingsDialog) {
        LynkDialog(
            title = stringResource(Res.string.notifications_required),
            message = stringResource(Res.string.notifications_required_message),
            confirmText = stringResource(Res.string.open_settings),
            dismissText = stringResource(Res.string.not_now),
            onConfirm = {
                showNotificationSettingsDialog = false
                permissionController.openAppSettings()
            },
            onDismissRequest = { showNotificationSettingsDialog = false }
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
            onAction = {},
            snackbarHostState = remember { SnackbarHostState() },
            navigateToSavedSpots = {},
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

@Preview(name = "Mobile landscape", widthDp = 900, heightDp = 400)
@Composable
private fun ProfileScreenLandscapePreview() = ProfileScreenPreview(previewState())

@Preview(name = "Tablet landscape", widthDp = 1280, heightDp = 800)
@Composable
private fun ProfileScreenTabletPreview() = ProfileScreenPreview(previewState())
