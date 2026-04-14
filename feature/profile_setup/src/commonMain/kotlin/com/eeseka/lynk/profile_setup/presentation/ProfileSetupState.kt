package com.eeseka.lynk.profile_setup.presentation

import androidx.compose.foundation.text.input.TextFieldState
import com.eeseka.lynk.shared.presentation.util.UiText

data class ProfileSetupState(
    val email: String = "",
    val profilePictureUrl: String? = null,

    val localPhotoUri: String? = null,
    val localPhotoMimeType: String? = null,

    val usernameTextState: TextFieldState = TextFieldState(),
    val displayNameTextState: TextFieldState = TextFieldState(),

    val imageError: UiText? = null,
    val usernameError: UiText? = null,
    val displayNameError: UiText? = null,

    val isCheckingUsername: Boolean = false,
    val isCompressingImage: Boolean = false,
    val isUploadingImage: Boolean = false,
    val isSubmitting: Boolean = false,
    val canSubmit: Boolean = false,

    val isUsernameAvailable: Boolean? = null
)