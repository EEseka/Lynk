package com.eeseka.lynk.auth.presentation

data class AuthState(
    val isGoogleSigningIn: Boolean = false,
    val isGuestSigningIn: Boolean = false,
    val isAppleSigningIn: Boolean = false
)