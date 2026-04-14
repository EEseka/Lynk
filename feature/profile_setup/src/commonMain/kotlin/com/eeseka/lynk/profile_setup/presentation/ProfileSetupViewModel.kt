package com.eeseka.lynk.profile_setup.presentation

import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eeseka.lynk.profile_setup.domain.DisplayNameValidationState
import com.eeseka.lynk.profile_setup.domain.DisplayNameValidator
import com.eeseka.lynk.profile_setup.domain.UsernameValidationState
import com.eeseka.lynk.profile_setup.domain.UsernameValidator
import com.eeseka.lynk.shared.domain.auth.SessionStorage
import com.eeseka.lynk.shared.domain.auth.model.User
import com.eeseka.lynk.shared.domain.media.ImageCompressionService
import com.eeseka.lynk.shared.domain.profile.UserService
import com.eeseka.lynk.shared.domain.util.DataError
import com.eeseka.lynk.shared.domain.util.Result
import com.eeseka.lynk.shared.domain.util.onFailure
import com.eeseka.lynk.shared.domain.util.onSuccess
import com.eeseka.lynk.shared.presentation.util.UiText
import com.eeseka.lynk.shared.presentation.util.toUiText
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
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
import lynk.feature.profile_setup.generated.resources.error_display_name_blank
import lynk.feature.profile_setup.generated.resources.error_display_name_too_long
import lynk.feature.profile_setup.generated.resources.error_image_read_failure
import lynk.feature.profile_setup.generated.resources.error_username_blank
import lynk.feature.profile_setup.generated.resources.error_username_consecutive_underscores
import lynk.feature.profile_setup.generated.resources.error_username_edge_underscore
import lynk.feature.profile_setup.generated.resources.error_username_generic_verify
import lynk.feature.profile_setup.generated.resources.error_username_invalid_chars
import lynk.feature.profile_setup.generated.resources.error_username_needs_letter
import lynk.feature.profile_setup.generated.resources.error_username_network_drop
import lynk.feature.profile_setup.generated.resources.error_username_rate_limit
import lynk.feature.profile_setup.generated.resources.error_username_taken
import lynk.feature.profile_setup.generated.resources.error_username_too_long
import lynk.feature.profile_setup.generated.resources.error_username_too_short

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
                observeValidationStates()
                observeUsernameAvailability()
                hasLoadedInitialData = true
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = ProfileSetupState()
        )

    private val isUsernameValidFlow = snapshotFlow { state.value.usernameTextState.text.toString() }
        .map { UsernameValidator.validate(it) == UsernameValidationState.VALID }
        .distinctUntilChanged()

    private val isDisplayNameValidFlow =
        snapshotFlow { state.value.displayNameTextState.text.toString() }
            .map { DisplayNameValidator.validate(it) == DisplayNameValidationState.VALID }
            .distinctUntilChanged()

    private val isUsernameAvailableFlow =
        state.map { it.isUsernameAvailable == true }.distinctUntilChanged()

    private val isBusyFlow = state.map {
        it.isSubmitting || it.isUploadingImage || it.isCompressingImage || it.isCheckingUsername
    }.distinctUntilChanged()

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

    private fun observeValidationStates() {
        combine(
            isUsernameValidFlow,
            isDisplayNameValidFlow,
            isUsernameAvailableFlow,
            isBusyFlow
        ) { isUsernameValid, isDisplayNameValid, isUsernameAvailable, isBusy ->
            val allValid = isUsernameValid && isDisplayNameValid && isUsernameAvailable
            _state.update { it.copy(canSubmit = !isBusy && allValid) }
        }.launchIn(viewModelScope)
    }

    private var compressedImageUrl: String? = null

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
                        localPhotoMimeType = null
                    )
                }
            }

            ProfileSetupAction.OnSubmitClick -> submitProfile()
        }
    }

    @OptIn(FlowPreview::class)
    private fun observeUsernameAvailability() {
        snapshotFlow { state.value.usernameTextState.text.toString() }
            .map { it.trim() }
            .debounce(500L)
            .distinctUntilChanged()
            .onEach { username ->
                val validationState = UsernameValidator.validate(username)

                if (validationState != UsernameValidationState.VALID) {
                    val usernameError = when (validationState) {
                        UsernameValidationState.BLANK -> null
                        UsernameValidationState.TOO_SHORT -> UiText.Resource(Res.string.error_username_too_short)
                        UsernameValidationState.TOO_LONG -> UiText.Resource(Res.string.error_username_too_long)
                        UsernameValidationState.NEEDS_LETTER -> UiText.Resource(Res.string.error_username_needs_letter)
                        UsernameValidationState.EDGE_UNDERSCORE -> UiText.Resource(Res.string.error_username_edge_underscore)
                        UsernameValidationState.CONSECUTIVE_UNDERSCORES -> UiText.Resource(Res.string.error_username_consecutive_underscores)
                        UsernameValidationState.INVALID_CHARACTERS -> UiText.Resource(Res.string.error_username_invalid_chars)
                        UsernameValidationState.VALID -> null
                    }
                    _state.update {
                        it.copy(
                            isUsernameAvailable = if (validationState == UsernameValidationState.BLANK) null else false,
                            usernameError = usernameError
                        )
                    }
                    return@onEach
                }

                // It passed local validation! Now ping the backend to see if it's taken.
                // These validations where done to reduce the number of backend requests
                // Because invalid usernames are not allowed, we can skip the backend check.
                _state.update { it.copy(isCheckingUsername = true, usernameError = null) }

                userService.isUsernameAvailable(username)
                    .onSuccess { isAvailable ->
                        _state.update {
                            it.copy(
                                isCheckingUsername = false,
                                isUsernameAvailable = isAvailable,
                                usernameError = if (!isAvailable) UiText.Resource(Res.string.error_username_taken) else null
                            )
                        }
                    }
                    .onFailure { error ->
                        val fallbackError = when (error) {
                            DataError.Remote.NO_INTERNET -> UiText.Resource(Res.string.error_username_network_drop)
                            DataError.Remote.TOO_MANY_REQUESTS -> UiText.Resource(Res.string.error_username_rate_limit)
                            else -> UiText.Resource(Res.string.error_username_generic_verify)
                        }

                        _state.update {
                            it.copy(
                                isCheckingUsername = false,
                                isUsernameAvailable = null,
                                usernameError = fallbackError
                            )
                        }
                    }
            }
            .launchIn(viewModelScope)
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

    private fun submitProfile() {
        val currentState = state.value

        if (!validateFormInputs() ||
            currentState.isSubmitting ||
            currentState.isUploadingImage ||
            currentState.isCompressingImage ||
            currentState.isCheckingUsername
        ) return

        _state.update { it.copy(isSubmitting = true) }

        viewModelScope.launch {
            val username = state.value.usernameTextState.text.toString().trim()
            val displayName = state.value.displayNameTextState.text.toString().trim()

            // Handle the image upload if a new one was picked
            val uploadedUrl = uploadLocalImageIfPresent()

            // If the upload failed, the helper function already set the error state. Just abort.
            if (state.value.imageError != null) {
                _state.update { it.copy(isSubmitting = false) }
                return@launch
            }

            // Determine final URL (New uploaded URL, or the existing one)
            val finalPhotoUrl = uploadedUrl ?: state.value.profilePictureUrl

            // Update the backend profile
            userService.updateProfile(username, displayName, finalPhotoUrl)
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

    private suspend fun uploadLocalImageIfPresent(): String? {
        val compressedUri = compressedImageUrl ?: state.value.localPhotoUri
        val mimeType = state.value.localPhotoMimeType

        if (compressedUri == null || mimeType == null) return null

        _state.update { it.copy(isUploadingImage = true) }

        // Read bytes from the file path — this is the only place bytes are needed
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

    private fun clearAllFormErrors() {
        _state.update { it.copy(usernameError = null, displayNameError = null, imageError = null) }
    }

    private fun validateFormInputs(): Boolean {
        clearAllFormErrors()

        val currentState = state.value
        val username = currentState.usernameTextState.text.toString()
        val displayName = currentState.displayNameTextState.text.toString()

        val usernameState = UsernameValidator.validate(username)
        val displayNameState = DisplayNameValidator.validate(displayName)

        val usernameError = when (usernameState) {
            UsernameValidationState.BLANK -> UiText.Resource(Res.string.error_username_blank)
            UsernameValidationState.TOO_SHORT -> UiText.Resource(Res.string.error_username_too_short)
            UsernameValidationState.TOO_LONG -> UiText.Resource(Res.string.error_username_too_long)
            UsernameValidationState.NEEDS_LETTER -> UiText.Resource(Res.string.error_username_needs_letter)
            UsernameValidationState.EDGE_UNDERSCORE -> UiText.Resource(Res.string.error_username_edge_underscore)
            UsernameValidationState.CONSECUTIVE_UNDERSCORES -> UiText.Resource(Res.string.error_username_consecutive_underscores)
            UsernameValidationState.INVALID_CHARACTERS -> UiText.Resource(Res.string.error_username_invalid_chars)
            UsernameValidationState.VALID -> {
                if (currentState.isUsernameAvailable == false) {
                    UiText.Resource(Res.string.error_username_taken)
                } else null
            }
        }

        val displayNameError = when (displayNameState) {
            DisplayNameValidationState.BLANK -> UiText.Resource(Res.string.error_display_name_blank)
            DisplayNameValidationState.TOO_LONG -> UiText.Resource(Res.string.error_display_name_too_long)
            DisplayNameValidationState.VALID -> null
        }

        _state.update {
            it.copy(
                usernameError = usernameError,
                displayNameError = displayNameError
            )
        }

        return usernameState == UsernameValidationState.VALID &&
                displayNameState == DisplayNameValidationState.VALID &&
                currentState.isUsernameAvailable == true
    }
}