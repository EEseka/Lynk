package com.eeseka.lynk.profile.presentation.profile

import com.eeseka.lynk.shared.domain.settings.AppTheme

sealed interface ProfileAction {
    data object OnImageClick : ProfileAction
    data class OnImagePicked(val rawPath: String, val mimeType: String) : ProfileAction
    data object OnRemoveImageClick : ProfileAction
    data object OnDismissFullScreenImage : ProfileAction
    data object OnSaveClick : ProfileAction

    data object OnSettingsClick : ProfileAction
    data object OnDismissSettings : ProfileAction
    data class OnThemeSelected(val theme: AppTheme) : ProfileAction
    data class OnPushNotificationsToggled(val isEnabled: Boolean) : ProfileAction

    data object OnCreateAccountClick : ProfileAction

    data object OnSignOutClick : ProfileAction
    data object OnConfirmSignOut : ProfileAction
    data object OnDismissSignOutConfirmation : ProfileAction

    data object OnDeleteAccountClick : ProfileAction
    data object OnConfirmDeleteAccount : ProfileAction
    data object OnDismissDeleteAccountConfirmation : ProfileAction
}