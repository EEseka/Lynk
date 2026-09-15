package com.eeseka.lynk.profile_setup.presentation

import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eeseka.lynk.shared.domain.profile.validation.DisplayNameValidationState
import com.eeseka.lynk.shared.domain.profile.validation.DisplayNameValidator
import com.eeseka.lynk.profile_setup.domain.UsernameValidationState
import com.eeseka.lynk.profile_setup.domain.UsernameValidator
import com.eeseka.lynk.shared.domain.auth.SessionStorage
import com.eeseka.lynk.shared.domain.auth.model.User
import com.eeseka.lynk.shared.domain.media.ImageCompressionService
import com.eeseka.lynk.shared.domain.profile.UserService
import com.eeseka.lynk.profile_setup.presentation.mappers.toUiText
import com.eeseka.lynk.profile_setup.presentation.mappers.toUsernameCheckUiText
import com.eeseka.lynk.shared.presentation.profile.mappers.toUiText
import com.eeseka.lynk.shared.domain.util.onFailure
import com.eeseka.lynk.shared.domain.util.onSuccess
import com.eeseka.lynk.shared.presentation.util.UiText
import com.eeseka.lynk.shared.presentation.util.toUiText
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.debounce
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
import lynk.feature.profile_setup.generated.resources.Res
import lynk.feature.profile_setup.generated.resources.error_image_read_failure
import lynk.feature.profile_setup.generated.resources.error_username_taken
import kotlin.time.Duration.Companion.milliseconds

class ProfileSetupViewModel(
    private val userService: UserService,
    private val sessionStorage: SessionStorage,
    private val imageCompressor: ImageCompressionService
) : ViewModel() {

    private var hasLoadedInitialData = false

    private val eventChannel = Channel<ProfileSetupEvent>()
    val events = eventChannel.receiveAsFlow()

    private val _state = MutableStateFlow(ProfileSetupState())

    val state = _state
        .onStart {
            if (!hasLoadedInitialData) {
                loadInitialData()
                observeCanSubmit()
                observeUsernameValidationAndAvailability()
                observeDisplayNameValidation()
                hasLoadedInitialData = true
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = ProfileSetupState()
        )

    private val isBusyFlow = state.map {
        it.isSubmitting || it.isUploadingImage || it.isCompressingImage
    }.distinctUntilChanged()

    private var compressedImageUrl: String? = null

    // Errors stay quiet until the first press, then track every keystroke.
    private var hasAttemptedSubmit = false

    // The username the availability answer belongs to, so a stale answer is never trusted.
    private var checkedUsername: String? = null

    private fun loadInitialData() {
        viewModelScope.launch {
            val authInfo = sessionStorage.observeAuthInfo().firstOrNull()
            if (authInfo != null) {
                when (val user = authInfo.user) {
                    is User.ProfileIncomplete -> {
                        _state.value.displayNameTextState.setTextAndPlaceCursorAtEnd(
                            user.displayName ?: ""
                        )
                        _state.update {
                            it.copy(
                                email = user.email,
                                profilePictureUrl = user.profilePictureUrl
                            )
                        }
                    }

                    else -> Unit
                }
            }
        }
    }

    private fun observeCanSubmit() {
        isBusyFlow
            .onEach { isBusy -> _state.update { it.copy(canSubmit = !isBusy) } }
            .launchIn(viewModelScope)
    }

    @OptIn(FlowPreview::class)
    private fun observeUsernameValidationAndAvailability() {
        snapshotFlow { _state.value.usernameTextState.text.toString() }
            .map { it.trim() }
            .distinctUntilChanged()
            .onEach {
                checkedUsername = null
                _state.update { it.copy(isUsernameAvailable = null) }
            }
            .debounce(500L.milliseconds)
            .onEach { username ->
                val validationState = UsernameValidator.validate(username)

                if (validationState != UsernameValidationState.VALID) {
                    _state.update {
                        it.copy(usernameError = if (hasAttemptedSubmit) validationState.toUiText() else null)
                    }
                    return@onEach
                }

                // It passed local validation! Now ping the backend to see if it's taken.
                // These validations where done to reduce the number of backend requests
                // Because invalid usernames are not allowed, we can skip the backend check.
                confirmUsernameAvailable(username)
            }
            .launchIn(viewModelScope)
    }

    private fun observeDisplayNameValidation() {
        snapshotFlow { _state.value.displayNameTextState.text.toString() }
            .onEach { displayName ->
                val validationState = DisplayNameValidator.validate(displayName)

                _state.update {
                    it.copy(displayNameError = if (hasAttemptedSubmit) validationState.toUiText() else null)
                }
            }
            .launchIn(viewModelScope)
    }

    fun onAction(action: ProfileSetupAction) {
        when (action) {
            is ProfileSetupAction.OnImagePicked -> processAndPreviewImage(
                action.rawPath,
                action.mimeType
            )

            ProfileSetupAction.OnRemoveImageClick -> {
                compressedImageUrl = null
                _state.update {
                    it.copy(
                        profilePictureUrl = null,
                        localPhotoUri = null,
                        localPhotoMimeType = null,
                        imageError = null
                    )
                }
            }

            ProfileSetupAction.OnSubmitClick -> submitProfile()
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

    private fun submitProfile() {
        val currentState = state.value

        hasAttemptedSubmit = true

        if (currentState.isSubmitting ||
            currentState.isUploadingImage ||
            currentState.isCompressingImage ||
            !validateFormInputs()
        ) return

        _state.update { it.copy(isSubmitting = true) }

        viewModelScope.launch {
            val username = _state.value.usernameTextState.text.toString().trim()
            val displayName = _state.value.displayNameTextState.text.toString().trim()

            // The debounced check may not have run for what is on screen right now, so an answer
            // is only trusted when it belongs to this exact username. Anything else is asked again.
            val isUsernameFree = if (checkedUsername == username) {
                state.value.isUsernameAvailable == true
            } else {
                confirmUsernameAvailable(username)
            }

            if (!isUsernameFree) {
                _state.update { it.copy(isSubmitting = false) }
                return@launch
            }

            // Handle the image upload if a new one was picked
            val uploadedUrl = uploadLocalImageIfPresent()

            // If the upload failed, the helper function already set the error state. Just abort.
            if (state.value.imageError != null) {
                _state.update { it.copy(isSubmitting = false) }
                return@launch
            }

            // Determine final URL (New uploaded URL, or the existing one)
            val finalPhotoUrl = uploadedUrl ?: state.value.profilePictureUrl

            userService.createProfile(
                username = username,
                displayName = displayName,
                profilePhotoUrl = finalPhotoUrl
            )
                .onSuccess { updatedUser ->
                    val currentAuth = sessionStorage.observeAuthInfo().firstOrNull()
                    if (currentAuth != null) {
                        sessionStorage.set(currentAuth.copy(user = updatedUser))
                    }
                    _state.update { it.copy(isSubmitting = false) }
                    eventChannel.send(ProfileSetupEvent.Success)
                }
                .onFailure { error ->
                    _state.update { it.copy(isSubmitting = false) }
                    eventChannel.send(ProfileSetupEvent.Error(error.toUiText()))
                }
        }
    }

    private suspend fun confirmUsernameAvailable(username: String): Boolean {
        _state.update { it.copy(isCheckingUsername = true, usernameError = null) }

        var isAvailable = false

        userService.isUsernameAvailable(username)
            .onSuccess { available ->
                isAvailable = available
                checkedUsername = username
                _state.update {
                    it.copy(
                        isCheckingUsername = false,
                        isUsernameAvailable = available,
                        usernameError = if (available) null else UiText.Resource(Res.string.error_username_taken)
                    )
                }
            }
            .onFailure { error ->
                checkedUsername = null
                _state.update {
                    it.copy(
                        isCheckingUsername = false,
                        isUsernameAvailable = null,
                        usernameError = error.toUsernameCheckUiText()
                    )
                }
            }

        return isAvailable
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

        var uploadedUrl: String? = null

        userService.getProfilePictureUploadUrl(mimeType)
            .onSuccess { uploadUrls ->
                userService.uploadProfilePicture(
                    uploadUrl = uploadUrls.uploadUrl,
                    headers = uploadUrls.headers,
                    imageBytes = imageBytes
                )
                    .onSuccess {
                        uploadedUrl = uploadUrls.publicUrl
                        _state.update { it.copy(isUploadingImage = false) }
                    }
                    .onFailure { error ->
                        _state.update {
                            it.copy(isUploadingImage = false, imageError = error.toUiText())
                        }
                    }
            }
            .onFailure { error ->
                _state.update {
                    it.copy(isUploadingImage = false, imageError = error.toUiText())
                }
            }

        return uploadedUrl
    }

    private fun validateFormInputs(): Boolean {
        clearAllFormErrors()

        val username = _state.value.usernameTextState.text.toString()
        val displayName = _state.value.displayNameTextState.text.toString()

        val usernameState = UsernameValidator.validate(username)
        val displayNameState = DisplayNameValidator.validate(displayName)

        _state.update {
            it.copy(
                usernameError = usernameState.toUiText(),
                displayNameError = displayNameState.toUiText()
            )
        }

        return usernameState == UsernameValidationState.VALID &&
                displayNameState == DisplayNameValidationState.VALID
    }

    private fun clearAllFormErrors() {
        _state.update { it.copy(usernameError = null, displayNameError = null, imageError = null) }
    }
}