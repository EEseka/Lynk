package com.eeseka.lynk.auth.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eeseka.lynk.shared.domain.auth.AuthService
import com.eeseka.lynk.shared.domain.auth.SessionStorage
import com.eeseka.lynk.shared.domain.util.onFailure
import com.eeseka.lynk.shared.domain.util.onSuccess
import com.eeseka.lynk.shared.presentation.util.toUiText
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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
                _state.update {
                    it.copy(isGoogleSigningIn = true)
                }
            }

            is AuthAction.OnGoogleTokenReceived -> continueWithGoogle(action.idToken)

            AuthAction.OnGuestClick -> continueAsGuest()
        }
    }

    private fun continueWithGoogle(idToken: String?) {
        if (idToken.isNullOrBlank()) {
            _state.update { it.copy(isGoogleSigningIn = false) }
            return
        }
        viewModelScope.launch {
            authService.continueWithGoogle(idToken)
                .onSuccess { authInfo ->
                    sessionStorage.set(authInfo)
                    _state.update {
                        it.copy(isGoogleSigningIn = false)
                    }
                    eventChannel.send(AuthEvent.Success(authInfo.user))
                }.onFailure { error ->
                    _state.update {
                        it.copy(isGoogleSigningIn = false)
                    }
                    eventChannel.send(AuthEvent.Error(error.toUiText().asStringAsync()))
                }
        }
    }

    private fun continueAsGuest() {
        _state.update {
            it.copy(isGuestSigningIn = true)
        }
        viewModelScope.launch {
            authService.continueAsGuest()
                .onSuccess { authInfo ->
                    sessionStorage.set(authInfo)
                    _state.update {
                        it.copy(isGuestSigningIn = false)
                    }
                    eventChannel.send(AuthEvent.Success(authInfo.user))
                }.onFailure { error ->
                    _state.update {
                        it.copy(isGuestSigningIn = false)
                    }
                    eventChannel.send(AuthEvent.Error(error.toUiText().asStringAsync()))
                }
        }
    }
}