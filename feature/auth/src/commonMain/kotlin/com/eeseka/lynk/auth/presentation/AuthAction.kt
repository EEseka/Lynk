package com.eeseka.lynk.auth.presentation


sealed interface AuthAction {
    data object OnGoogleSignInClick : AuthAction
    data class OnGoogleTokenReceived(val idToken: String?) : AuthAction
    data object OnGuestClick : AuthAction
}