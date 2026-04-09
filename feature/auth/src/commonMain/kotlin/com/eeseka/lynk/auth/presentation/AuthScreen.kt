package com.eeseka.lynk.auth.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.eeseka.lynk.auth.presentation.components.AuthActions
import com.eeseka.lynk.auth.presentation.components.AuthBranding
import com.eeseka.lynk.auth.presentation.components.BreathingSpotlightBackground
import com.eeseka.lynk.shared.design_system.components.layouts.LynkScaffold
import com.eeseka.lynk.shared.design_system.components.modals_and_overlays.LynkFlashType
import com.eeseka.lynk.shared.design_system.components.modals_and_overlays.showFlashMessage
import com.eeseka.lynk.shared.design_system.components.textfields.LynkText
import com.eeseka.lynk.shared.design_system.components.util.AppHaptic
import com.eeseka.lynk.shared.design_system.components.util.rememberAppHaptic
import com.eeseka.lynk.shared.domain.auth.User
import com.eeseka.lynk.shared.presentation.util.DeviceConfiguration
import com.eeseka.lynk.shared.presentation.util.ObserveAsEvents
import com.eeseka.lynk.shared.presentation.util.currentDeviceConfiguration
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import lynk.feature.auth.generated.resources.Res
import lynk.feature.auth.generated.resources.auth_disclosure
import lynk.feature.auth.generated.resources.apple_sign_in_coming_soon
import org.jetbrains.compose.resources.stringResource

private const val PRIVACY_URL = "https://example.com/privacy"

@Composable
fun AuthScreen(
    state: AuthState,
    events: Flow<AuthEvent>,
    onAction: (AuthAction) -> Unit,
    onAuthSuccess: (User) -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val uriHandler = LocalUriHandler.current
    val config = currentDeviceConfiguration()
    val hapticFeedback = rememberAppHaptic()

    val brandingSize = when (config) {
        DeviceConfiguration.TABLET_PORTRAIT -> 400.dp
        else -> 250.dp
    }

    ObserveAsEvents(events) { event ->
        when (event) {
            is AuthEvent.Error -> {
                hapticFeedback(AppHaptic.Error)

                snackbarHostState.showFlashMessage(
                    message = event.message,
                    type = LynkFlashType.Error
                )
            }

            is AuthEvent.Success -> {
                onAuthSuccess(event.user)
            }
        }
    }

    // I will remove 'scope' and 'appleSignInComingSoon' when I can afford Apple Developer's account
    val scope = rememberCoroutineScope()
    val appleSignInComingSoon = stringResource(Res.string.apple_sign_in_coming_soon)

    LynkScaffold(
        snackbarHostState = snackbarHostState
    ) { paddingValues ->
        BreathingSpotlightBackground(modifier = Modifier.fillMaxSize())
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            when (config) {
                DeviceConfiguration.MOBILE_PORTRAIT,
                DeviceConfiguration.TABLET_PORTRAIT -> {
                    Column(
                        modifier = Modifier
                            .widthIn(max = 600.dp)
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.weight(1f))

                        AuthBranding(iconSize = brandingSize)

                        Spacer(modifier = Modifier.weight(1f))

                        AuthActions(
                            isGoogleSigningIn = state.isGoogleSigningIn,
                            isAppleSigningIn = state.isAppleSigningIn,
                            isGuestSigningIn = state.isGuestSigningIn,
                            onGoogleClick = {
                                onAction(AuthAction.OnGoogleSignInClick)
                            },
                            onAppleClick = {
                                scope.launch {
                                    snackbarHostState.showFlashMessage(
                                        message = appleSignInComingSoon
                                    )
                                }
                            },
                            onGuestClick = {
                                onAction(AuthAction.OnGuestClick)
                            },
                            onGoogleTokenReceived = {
                                onAction(AuthAction.OnGoogleTokenReceived(it))
                            },
                            enableButtons = !state.isGoogleSigningIn && !state.isGuestSigningIn && !state.isAppleSigningIn
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        LynkText(
                            text = stringResource(Res.string.auth_disclosure),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            textDecoration = TextDecoration.Underline,
                            modifier = Modifier.clickable {
                                uriHandler.openUri(PRIVACY_URL)
                            }
                        )
                    }
                }

                else -> {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState()),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                AuthBranding(iconSize = brandingSize)
                            }
                        }
                        Box(
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                modifier = Modifier
                                    .widthIn(max = 400.dp)
                                    .verticalScroll(rememberScrollState()),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                AuthActions(
                                    isGoogleSigningIn = state.isGoogleSigningIn,
                                    isAppleSigningIn = state.isAppleSigningIn,
                                    isGuestSigningIn = state.isGuestSigningIn,
                                    onGoogleClick = {
                                        onAction(AuthAction.OnGoogleSignInClick)
                                    },
                                    onAppleClick = {
                                        scope.launch {
                                            snackbarHostState.showFlashMessage(
                                                message = appleSignInComingSoon
                                            )
                                        }
                                    },
                                    onGuestClick = {
                                        onAction(AuthAction.OnGuestClick)
                                    },
                                    onGoogleTokenReceived = {
                                        onAction(AuthAction.OnGoogleTokenReceived(it))
                                    },
                                    enableButtons = !state.isGoogleSigningIn && !state.isGuestSigningIn && !state.isAppleSigningIn
                                )

                                Spacer(modifier = Modifier.height(24.dp))

                                LynkText(
                                    text = stringResource(Res.string.auth_disclosure),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                    textDecoration = TextDecoration.Underline,
                                    modifier = Modifier.clickable {
                                        uriHandler.openUri(PRIVACY_URL)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}