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
import com.eeseka.lynk.shared.domain.util.onFailure
import com.eeseka.lynk.shared.domain.util.onSuccess
import com.eeseka.lynk.shared.presentation.profile.mappers.toUiText
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
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import lynk.feature.profile.generated.resources.Res
import lynk.feature.profile.generated.resources.error_delete_account_blocked
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

    // What the form is compared against. Saving replaces it, so a saved form counts as clean again.
    private val savedProfile = MutableStateFlow(SavedProfile())

    private var compressedImageUrl: String? = null

    // Errors stay quiet until the first press, then track every keystroke.
    private var hasAttemptedSave = false

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
                observeDisplayNameValidation()
                hasLoadedInitialData = true
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = ProfileState()
        )

    private val displayNameFlow = snapshotFlow { _state.value.displayNameTextState.text.toString() }
        .map { it.trim() }
        .distinctUntilChanged()

    private val currentPhotoFlow = state.map {
        it.localPhotoUri ?: it.profilePictureUrl
    }.distinctUntilChanged()

    private val isFormDirtyFlow = combine(
        displayNameFlow,
        currentPhotoFlow,
        savedProfile
    ) { displayName, currentPhoto, saved ->
        displayName != saved.displayName || currentPhoto != saved.photoUrl
    }.distinctUntilChanged()

    private val isBusyFlow = state.map {
        it.isSaving || it.isUploadingImage || it.isCompressingImage
    }.distinctUntilChanged()

    private fun observeCanSave() {
        combine(isFormDirtyFlow, isBusyFlow) { isFormDirty, isBusy ->
            _state.update { it.copy(canSave = !isBusy && isFormDirty) }
        }.launchIn(viewModelScope)
    }

    private fun observeDisplayNameValidation() {
        snapshotFlow { _state.value.displayNameTextState.text.toString() }
            .onEach { displayName ->
                val validationState = DisplayNameValidator.validate(displayName)

                _state.update {
                    it.copy(displayNameError = if (hasAttemptedSave) validationState.toUiText() else null)
                }
            }
            .launchIn(viewModelScope)
    }

    fun onAction(action: ProfileAction) {
        when (action) {
            ProfileAction.OnImageClick -> _state.update { it.copy(showFullScreenImage = true) }
            ProfileAction.OnDismissFullScreenImage -> _state.update { it.copy(showFullScreenImage = false) }
            is ProfileAction.OnImagePicked -> processAndPreviewImage(
                action.rawPath,
                action.mimeType
            )

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
            compressedImageUrl = imageCompressor.compress(rawPath)
            _state.update { it.copy(isCompressingImage = false) }
        }
    }

    private fun saveProfile() {
        val currentState = state.value

        hasAttemptedSave = true

        if (currentState.isSaving ||
            currentState.isUploadingImage ||
            currentState.isCompressingImage ||
            !validateFormInputs()
        ) return

        _state.update { it.copy(isSaving = true) }

        viewModelScope.launch {
            val displayName = _state.value.displayNameTextState.text.toString().trim()

            val upload = uploadLocalImageIfPresent()

            if (upload is ImageUpload.Failed) {
                _state.update { it.copy(isSaving = false) }
                return@launch
            }

            val finalPhotoUrl = (upload as? ImageUpload.Uploaded)?.url ?: state.value.profilePictureUrl

            userService.updateProfile(displayName = displayName, profilePhotoUrl = finalPhotoUrl)
                .onSuccess { updatedUser ->
                    val currentAuth = sessionStorage.observeAuthInfo().firstOrNull()
                    if (currentAuth != null) {
                        sessionStorage.set(currentAuth.copy(user = updatedUser))
                    }

                    savedProfile.value = SavedProfile(displayName = displayName, photoUrl = finalPhotoUrl)
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

    private suspend fun uploadLocalImageIfPresent(): ImageUpload {
        val compressedUri = compressedImageUrl ?: state.value.localPhotoUri
        val mimeType = state.value.localPhotoMimeType

        if (compressedUri == null || mimeType == null) return ImageUpload.None

        _state.update { it.copy(isUploadingImage = true) }

        val imageBytes = imageCompressor.readBytes(compressedUri)
        if (imageBytes == null) {
            _state.update {
                it.copy(
                    isUploadingImage = false,
                    imageError = UiText.Resource(Res.string.error_image_read_failure)
                )
            }
            return ImageUpload.Failed
        }

        var upload: ImageUpload = ImageUpload.Failed

        userService.getProfilePictureUploadUrl(mimeType)
            .onSuccess { uploadUrls ->
                userService.uploadProfilePicture(
                    uploadUrl = uploadUrls.uploadUrl,
                    headers = uploadUrls.headers,
                    imageBytes = imageBytes
                )
                    .onSuccess {
                        upload = ImageUpload.Uploaded(uploadUrls.publicUrl)
                        _state.update { it.copy(isUploadingImage = false) }
                    }
                    .onFailure { error ->
                        _state.update {
                            it.copy(isUploadingImage = false, imageError = error.toUiText())
                        }
                    }
            }
            .onFailure { error ->
                _state.update { it.copy(isUploadingImage = false, imageError = error.toUiText()
                )
                }
            }

        return upload
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
                    if (error == DataError.Remote.NOT_FOUND) {
                        sessionStorage.set(null)
                        return@onFailure
                    }

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
                val hasUnsavedEdits = state.value.localPhotoUri != null || typedName != savedProfile.value.displayName

                if (!hasUnsavedEdits) {
                    applyUser(freshUser)
                }
            }
        }
    }

    private fun applyUser(user: User) {
        when (user) {
            is User.Guest -> _state.update { it.copy(isGuest = true, userId = user.id) }

            is User.Authenticated -> {
                savedProfile.value = SavedProfile(
                    displayName = user.displayName,
                    photoUrl = user.profilePictureUrl
                )

                _state.update {
                    it.copy(
                        isGuest = false,
                        userId = user.id,
                        email = user.email,
                        username = user.username,
                        profilePictureUrl = user.profilePictureUrl
                    )
                }
                _state.value.displayNameTextState.setTextAndPlaceCursorAtEnd(user.displayName)
            }

            is User.ProfileIncomplete -> {
                savedProfile.value = SavedProfile(
                    displayName = user.displayName.orEmpty(),
                    photoUrl = user.profilePictureUrl
                )

                _state.update {
                    it.copy(
                        isGuest = false,
                        userId = user.id,
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
                // Counts are decoration; a failure leaves them unknown rather than shouting.
                _state.update { it.copy(isStatsLoading = false) }
            }
    }

    private fun validateFormInputs(): Boolean {
        clearAllFormErrors()

        val displayName = _state.value.displayNameTextState.text.toString()
        val displayNameState = DisplayNameValidator.validate(displayName)

        _state.update { it.copy(displayNameError = displayNameState.toUiText()) }

        return displayNameState == DisplayNameValidationState.VALID
    }

    private fun clearAllFormErrors() {
        _state.update { it.copy(displayNameError = null, imageError = null) }
    }

    private data class SavedProfile(
        val displayName: String = "",
        val photoUrl: String? = null
    )

    private sealed interface ImageUpload {
        data object None : ImageUpload
        data class Uploaded(val url: String) : ImageUpload
        data object Failed : ImageUpload
    }
}