package com.eeseka.lynk.auth.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eeseka.lynk.shared.domain.auth.AuthService
import com.eeseka.lynk.shared.domain.auth.SessionStorage
import com.eeseka.lynk.shared.domain.util.onFailure
import com.eeseka.lynk.shared.domain.util.onSuccess
import com.eeseka.lynk.shared.presentation.util.UiText
import com.eeseka.lynk.shared.presentation.util.toUiText
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import lynk.feature.auth.generated.resources.Res
import lynk.feature.auth.generated.resources.error_google_sign_in_failed

class AuthViewModel(
    private val authService: AuthService,
    private val sessionStorage: SessionStorage
) : ViewModel() {
    private val _state = MutableStateFlow(AuthState())
    val state = _state.asStateFlow()

    private val eventChannel = Channel<AuthEvent>()
    val events = eventChannel.receiveAsFlow()

    fun onAction(action: AuthAction) {
        when (action) {
            AuthAction.OnGoogleSignInClick -> {
                _state.update { it.copy(isGoogleSigningIn = true) }
            }

            is AuthAction.OnGoogleTokenReceived -> continueWithGoogle(action.idToken)

            AuthAction.OnGoogleSignInCancelled -> {
                _state.update { it.copy(isGoogleSigningIn = false) }
            }

            AuthAction.OnGoogleSignInFailed -> showGoogleSignInError()

            AuthAction.OnGuestClick -> continueAsGuest()
        }
    }

    private fun continueWithGoogle(idToken: String) {
        viewModelScope.launch {
            authService.continueWithGoogle(idToken)
                .onSuccess { authInfo ->
                    sessionStorage.set(authInfo)
                    _state.update { it.copy(isGoogleSigningIn = false) }
                    eventChannel.send(AuthEvent.Success(authInfo.user))
                }.onFailure { error ->
                    _state.update { it.copy(isGoogleSigningIn = false) }
                    eventChannel.send(AuthEvent.Error(error.toUiText()))
                }
        }
    }

    private fun showGoogleSignInError() {
        _state.update { it.copy(isGoogleSigningIn = false) }
        viewModelScope.launch {
            eventChannel.send(
                AuthEvent.Error(UiText.Resource(Res.string.error_google_sign_in_failed))
            )
        }
    }

    private fun continueAsGuest() {
        _state.update { it.copy(isGuestSigningIn = true) }
        viewModelScope.launch {
            authService.continueAsGuest()
                .onSuccess { authInfo ->
                    sessionStorage.set(authInfo)
                    _state.update { it.copy(isGuestSigningIn = false) }
                    eventChannel.send(AuthEvent.Success(authInfo.user))
                }.onFailure { error ->
                    _state.update { it.copy(isGuestSigningIn = false) }
                    eventChannel.send(AuthEvent.Error(error.toUiText()))
                }
        }
    }
}