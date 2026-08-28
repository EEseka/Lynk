package com.eeseka.lynk.profile.presentation.profile

import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eeseka.lynk.shared.domain.auth.AuthService
import com.eeseka.lynk.shared.domain.auth.SessionStorage
import com.eeseka.lynk.shared.domain.auth.model.User
import com.eeseka.lynk.shared.domain.hangout.HangoutService
import com.eeseka.lynk.shared.domain.media.ImageCompressionService
import com.eeseka.lynk.shared.domain.notification.DeviceTokenService
import com.eeseka.lynk.shared.domain.notification.PushNotificationService
import com.eeseka.lynk.shared.domain.profile.UserService
import com.eeseka.lynk.shared.domain.profile.validation.DisplayNameValidationState
import com.eeseka.lynk.shared.domain.profile.validation.DisplayNameValidator
import com.eeseka.lynk.shared.domain.settings.AppPreferences
import com.eeseka.lynk.shared.domain.settings.AppTheme
import com.eeseka.lynk.shared.domain.util.DataError
import com.eeseka.lynk.shared.domain.util.Result
import com.eeseka.lynk.shared.domain.util.onFailure
import com.eeseka.lynk.shared.domain.util.onSuccess
import com.eeseka.lynk.shared.presentation.util.UiText
import com.eeseka.lynk.shared.presentation.util.toUiText
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import lynk.feature.profile.generated.resources.Res
import lynk.feature.profile.generated.resources.error_delete_account_blocked
import lynk.feature.profile.generated.resources.error_display_name_blank
import lynk.feature.profile.generated.resources.error_display_name_too_long
import lynk.feature.profile.generated.resources.error_image_read_failure

class ProfileViewModel(
    private val userService: UserService,
    private val hangoutService: HangoutService,
    private val authService: AuthService,
    private val sessionStorage: SessionStorage,
    private val appPreferences: AppPreferences,
    private val imageCompressor: ImageCompressionService,
    private val deviceTokenService: DeviceTokenService,
    private val pushNotificationService: PushNotificationService
) : ViewModel() {

    private val eventChannel = Channel<ProfileEvent>()
    val events = eventChannel.receiveAsFlow()

    private var hasLoadedInitialData = false

    private var originalDisplayName: String = ""
    private var originalPhotoUrl: String? = null

    private var compressedImageUrl: String? = null

    private val _state = MutableStateFlow(ProfileState())

    val state = combine(
        _state,
        appPreferences.theme,
        appPreferences.arePushNotificationsEnabled
    ) { currentState, theme, arePushNotificationsEnabled ->
        currentState.copy(
            appTheme = theme,
            arePushNotificationsEnabled = arePushNotificationsEnabled
        )
    }
        .onStart {
            if (!hasLoadedInitialData) {
                loadProfile()
                observeCanSave()
                hasLoadedInitialData = true
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = ProfileState()
        )

    // Blank-check only — real validation (too long) runs on Save press via validateFormInputs()
    private val isDisplayNameValidFlow =
        snapshotFlow { _state.value.displayNameTextState.text.toString() }
            .map { it.isNotBlank() }
            .distinctUntilChanged()

    private val isFormDirtyFlow = combine(
        snapshotFlow { _state.value.displayNameTextState.text.toString() },
        state
    ) { displayName, currentState ->
        val currentPhoto = currentState.localPhotoUri ?: currentState.profilePictureUrl

        displayName.trim() != originalDisplayName || currentPhoto != originalPhotoUrl
    }.distinctUntilChanged()

    private val isBusyFlow = state.map {
        it.isSaving || it.isUploadingImage || it.isCompressingImage
    }.distinctUntilChanged()

    private fun observeCanSave() {
        combine(
            isDisplayNameValidFlow,
            isFormDirtyFlow,
            isBusyFlow
        ) { isDisplayNameValid, isFormDirty, isBusy ->
            _state.update { it.copy(canSave = !isBusy && isFormDirty && isDisplayNameValid) }
        }.launchIn(viewModelScope)
    }

    fun onAction(action: ProfileAction) {
        when (action) {
            ProfileAction.OnImageClick -> _state.update { it.copy(showFullScreenImage = true) }
            ProfileAction.OnDismissFullScreenImage -> _state.update { it.copy(showFullScreenImage = false) }
            is ProfileAction.OnImagePicked -> processAndPreviewImage(action.rawPath, action.mimeType)
            ProfileAction.OnRemoveImageClick -> {
                compressedImageUrl = null
                _state.update {
                    it.copy(
                        localPhotoUri = null,
                        localPhotoMimeType = null,
                        profilePictureUrl = null,
                        imageError = null
                    )
                }
            }
            ProfileAction.OnSaveClick -> saveProfile()
            ProfileAction.OnSettingsClick -> _state.update { it.copy(showSettingsSheet = true) }
            ProfileAction.OnDismissSettings -> _state.update { it.copy(showSettingsSheet = false) }
            is ProfileAction.OnThemeSelected -> selectTheme(action.theme)
            is ProfileAction.OnPushNotificationsToggled -> togglePushNotifications(action.isEnabled)
            ProfileAction.OnCreateAccountClick -> deleteAccount() // Taking a Guest back to the auth screen
            ProfileAction.OnSignOutClick -> {
                if (state.value.isGuest) deleteAccount()
                else _state.update {
                    it.copy(showSettingsSheet = false, showSignOutConfirmation = true)
                }
            }
            ProfileAction.OnConfirmSignOut -> signOut()
            ProfileAction.OnDismissSignOutConfirmation -> _state.update {
                it.copy(showSignOutConfirmation = false)
            }
            ProfileAction.OnDeleteAccountClick -> _state.update {
                it.copy(showSettingsSheet = false, showDeleteAccountConfirmation = true)
            }
            ProfileAction.OnConfirmDeleteAccount -> deleteAccount()
            ProfileAction.OnDismissDeleteAccountConfirmation -> _state.update {
                it.copy(showDeleteAccountConfirmation = false)
            }
        }
    }

    private fun processAndPreviewImage(rawPath: String, mimeType: String) {
        _state.update {
            it.copy(
                localPhotoUri = rawPath,
                localPhotoMimeType = mimeType,
                isCompressingImage = true,
                imageError = null
            )
        }

        viewModelScope.launch {
            val compressed = imageCompressor.compress(rawPath)
            compressedImageUrl = compressed

            _state.update { it.copy(isCompressingImage = false) }
        }
    }

    private fun saveProfile() {
        val currentState = state.value

        if (!validateFormInputs() ||
            currentState.isSaving ||
            currentState.isUploadingImage ||
            currentState.isCompressingImage
        ) return

        _state.update { it.copy(isSaving = true) }

        viewModelScope.launch {
            val displayName = _state.value.displayNameTextState.text.toString().trim()

            val uploadedUrl = uploadLocalImageIfPresent()

            if (state.value.imageError != null) {
                _state.update { it.copy(isSaving = false) }
                return@launch
            }

            val finalPhotoUrl = uploadedUrl ?: state.value.profilePictureUrl

            userService.updateProfile(displayName = displayName, profilePhotoUrl = finalPhotoUrl)
                .onSuccess { updatedUser ->
                    val currentAuth = sessionStorage.observeAuthInfo().firstOrNull()
                    if (currentAuth != null) {
                        sessionStorage.set(currentAuth.copy(user = updatedUser))
                    }

                    originalDisplayName = displayName
                    originalPhotoUrl = finalPhotoUrl
                    compressedImageUrl = null

                    _state.update {
                        it.copy(
                            isSaving = false,
                            canSave = false,
                            profilePictureUrl = finalPhotoUrl,
                            localPhotoUri = null,
                            localPhotoMimeType = null
                        )
                    }
                    eventChannel.send(ProfileEvent.ProfileSaved)
                }
                .onFailure { error ->
                    _state.update { it.copy(isSaving = false) }
                    eventChannel.send(ProfileEvent.Error(error.toUiText()))
                }
        }
    }

    private suspend fun uploadLocalImageIfPresent(): String? {
        val compressedUri = compressedImageUrl ?: state.value.localPhotoUri
        val mimeType = state.value.localPhotoMimeType

        if (compressedUri == null || mimeType == null) return null

        _state.update { it.copy(isUploadingImage = true) }

        val imageBytes = imageCompressor.readBytes(compressedUri)
        if (imageBytes == null) {
            _state.update {
                it.copy(
                    isUploadingImage = false,
                    imageError = UiText.Resource(Res.string.error_image_read_failure)
                )
            }
            return null
        }

        return when (val urlResult = userService.getProfilePictureUploadUrl(mimeType)) {
            is Result.Success -> {
                val uploadResult = userService.uploadProfilePicture(
                    uploadUrl = urlResult.data.uploadUrl,
                    headers = urlResult.data.headers,
                    imageBytes = imageBytes
                )

                when (uploadResult) {
                    is Result.Success -> {
                        _state.update { it.copy(isUploadingImage = false) }
                        urlResult.data.publicUrl
                    }

                    is Result.Failure -> {
                        _state.update {
                            it.copy(
                                isUploadingImage = false,
                                imageError = uploadResult.error.toUiText()
                            )
                        }
                        null
                    }
                }
            }

            is Result.Failure -> {
                _state.update {
                    it.copy(
                        isUploadingImage = false,
                        imageError = urlResult.error.toUiText()
                    )
                }
                null
            }
        }
    }

    private fun selectTheme(theme: AppTheme) {
        viewModelScope.launch {
            appPreferences.setTheme(theme)
        }
    }

    private fun togglePushNotifications(isEnabled: Boolean) {
        viewModelScope.launch {
            appPreferences.setPushNotificationsEnabled(isEnabled)
        }
    }

    private fun signOut() {
        _state.update { it.copy(isSigningOut = true, showSignOutConfirmation = false) }

        viewModelScope.launch {
            val authInfo = sessionStorage.observeAuthInfo().firstOrNull()
            val refreshToken = authInfo?.refreshToken ?: run {
                _state.update { it.copy(isSigningOut = false) }
                return@launch
            }

            unregisterThisDevice()

            authService.logout(refreshToken)
                .onSuccess {
                    _state.update { it.copy(isSigningOut = false) }
                }
                .onFailure { error ->
                    _state.update { it.copy(isSigningOut = false) }
                    eventChannel.send(ProfileEvent.Error(error.toUiText()))
                }

            sessionStorage.set(null)
        }
    }

    private suspend fun unregisterThisDevice() {
        val deviceToken = pushNotificationService.observeDeviceToken().firstOrNull() ?: return
        deviceTokenService.unregisterToken(deviceToken)
    }

    private fun deleteAccount() {
        _state.update {
            it.copy(
                isDeletingAccount = true,
                showSettingsSheet = false, // Needed coz of guest path shows no dialog.
                showDeleteAccountConfirmation = false
            )
        }

        viewModelScope.launch {
            authService.deleteAccount()
                .onSuccess { sessionStorage.set(null) }
                .onFailure { error ->
                    val errorMessage = if (error == DataError.Remote.CONFLICT) {
                        UiText.Resource(Res.string.error_delete_account_blocked)
                    } else {
                        error.toUiText()
                    }
                    eventChannel.send(ProfileEvent.Error(errorMessage))
                }

            _state.update { it.copy(isDeletingAccount = false) }
        }
    }

    private fun loadProfile() {
        viewModelScope.launch {
            val user = sessionStorage.observeAuthInfo().firstOrNull()?.user ?: return@launch

            applyUser(user)

            if (user is User.Guest) return@launch

            loadStats()

            userService.getCurrentUser().onSuccess { freshUser ->
                val currentAuth = sessionStorage.observeAuthInfo().firstOrNull()
                if (currentAuth != null) {
                    sessionStorage.set(currentAuth.copy(user = freshUser))
                }

                val typedName = _state.value.displayNameTextState.text.toString()
                val hasUnsavedEdits = state.value.localPhotoUri != null ||
                        typedName != originalDisplayName

                if (!hasUnsavedEdits) {
                    applyUser(freshUser)
                }
            }
        }
    }

    private fun applyUser(user: User) {
        when (user) {
            is User.Guest -> _state.update { it.copy(isGuest = true) }

            is User.Authenticated -> {
                originalDisplayName = user.displayName
                originalPhotoUrl = user.profilePictureUrl

                _state.update {
                    it.copy(
                        isGuest = false,
                        email = user.email,
                        username = user.username,
                        profilePictureUrl = user.profilePictureUrl
                    )
                }
                _state.value.displayNameTextState.setTextAndPlaceCursorAtEnd(user.displayName)
            }

            is User.ProfileIncomplete -> {
                originalDisplayName = user.displayName.orEmpty()
                originalPhotoUrl = user.profilePictureUrl

                _state.update {
                    it.copy(
                        isGuest = false,
                        email = user.email,
                        username = "",
                        profilePictureUrl = user.profilePictureUrl
                    )
                }
                _state.value.displayNameTextState.setTextAndPlaceCursorAtEnd(user.displayName.orEmpty())
            }
        }
    }

    private suspend fun loadStats() {
        _state.update { it.copy(isStatsLoading = true) }

        hangoutService.getMyStats()
            .onSuccess { stats ->
                _state.update {
                    it.copy(
                        isStatsLoading = false,
                        hostedCount = stats.hostedCount,
                        attendedCount = stats.attendedCount
                    )
                }
            }
            .onFailure {
                // Counts are decoration; a failure leaves the zeroes rather than shouting.
                _state.update { it.copy(isStatsLoading = false) }
            }
    }

    private fun validateFormInputs(): Boolean {
        clearAllFormErrors()

        val displayName = _state.value.displayNameTextState.text.toString()
        val displayNameState = DisplayNameValidator.validate(displayName)

        val displayNameError = when (displayNameState) {
            DisplayNameValidationState.BLANK -> UiText.Resource(Res.string.error_display_name_blank)
            DisplayNameValidationState.TOO_LONG -> UiText.Resource(Res.string.error_display_name_too_long)
            DisplayNameValidationState.VALID -> null
        }

        _state.update { it.copy(displayNameError = displayNameError) }

        return displayNameState == DisplayNameValidationState.VALID
    }

    private fun clearAllFormErrors() {
        _state.update { it.copy(displayNameError = null, imageError = null) }
    }
}