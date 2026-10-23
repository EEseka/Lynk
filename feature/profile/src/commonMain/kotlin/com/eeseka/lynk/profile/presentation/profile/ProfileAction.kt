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
    data class OnPureBlackToggled(val isEnabled: Boolean) : ProfileAction
    data class OnPushNotificationsToggled(val isEnabled: Boolean) : ProfileAction
    data class OnHapticsToggled(val isEnabled: Boolean) : ProfileAction

    data object OnCreateAccountClick : ProfileAction

    data object OnSignOutClick : ProfileAction
    data object OnSignOutConfirmed : ProfileAction
    data object OnDismissSignOutDialog : ProfileAction

    data object OnDeleteAccountClick : ProfileAction
    data object OnDeleteAccountConfirmed : ProfileAction
    data object OnDismissDeleteAccountDialog : ProfileAction
}