package com.eeseka.lynk.profile.presentation.profile

import androidx.compose.foundation.text.input.TextFieldState
import com.eeseka.lynk.shared.domain.settings.AppTheme
import com.eeseka.lynk.shared.presentation.util.UiText

data class ProfileState(
    val isGuest: Boolean = false,

    val email: String = "",
    val username: String = "",
    val profilePictureUrl: String? = null,
    val displayNameTextState: TextFieldState = TextFieldState(),

    val localPhotoUri: String? = null,
    val localPhotoMimeType: String? = null,

    val displayNameError: UiText? = null,
    val imageError: UiText? = null,

    val isCompressingImage: Boolean = false,
    val isUploadingImage: Boolean = false,
    val isSaving: Boolean = false,
    val canSave: Boolean = false,

    val hostedCount: Long = 0L,
    val attendedCount: Long = 0L,
    val isStatsLoading: Boolean = false,

    val appTheme: AppTheme = AppTheme.SYSTEM,
    val arePushNotificationsEnabled: Boolean = true,

    val showSettingsSheet: Boolean = false,
    val showFullScreenImage: Boolean = false,
    val showSignOutConfirmation: Boolean = false,
    val showDeleteAccountConfirmation: Boolean = false,

    val isSigningOut: Boolean = false,
    val isDeletingAccount: Boolean = false
)