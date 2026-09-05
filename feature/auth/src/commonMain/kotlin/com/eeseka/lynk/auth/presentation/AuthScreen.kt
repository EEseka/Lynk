package com.eeseka.lynk.auth.presentation

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
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eeseka.lynk.auth.presentation.components.AuthActions
import com.eeseka.lynk.auth.presentation.components.AuthBranding
import com.eeseka.lynk.auth.presentation.components.AuthDisclosure
import com.eeseka.lynk.auth.presentation.components.BreathingSpotlightBackground
import com.eeseka.lynk.shared.design_system.components.layouts.LynkScaffold
import com.eeseka.lynk.shared.design_system.components.modals_and_overlays.LynkFlashType
import com.eeseka.lynk.shared.design_system.components.modals_and_overlays.showFlashMessage
import com.eeseka.lynk.shared.design_system.theme.LynkTheme
import com.eeseka.lynk.shared.domain.auth.model.User
import com.eeseka.lynk.shared.presentation.util.DeviceConfiguration
import com.eeseka.lynk.shared.presentation.util.ObserveAsEvents
import com.eeseka.lynk.shared.presentation.util.currentDeviceConfiguration
import kotlinx.coroutines.launch
import lynk.feature.auth.generated.resources.Res
import lynk.feature.auth.generated.resources.apple_sign_in_coming_soon
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AuthRoot(
    navigateToProfileSetup: () -> Unit,
    navigateToMain: () -> Unit,
    viewModel: AuthViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is AuthEvent.Error -> {
                snackbarHostState.showFlashMessage(
                    message = event.error.asStringAsync(),
                    type = LynkFlashType.Error
                )
            }

            is AuthEvent.Success -> {
                if (event.user is User.ProfileIncomplete) navigateToProfileSetup()
                else navigateToMain()
            }
        }
    }

    AuthScreen(
        state = state,
        onAction = viewModel::onAction,
        snackbarHostState = snackbarHostState
    )
}

@Composable
fun AuthScreen(
    state: AuthState,
    onAction: (AuthAction) -> Unit,
    snackbarHostState: SnackbarHostState
) {
    val config = currentDeviceConfiguration()

    // I will remove 'scope', 'appleSignInComingSoon' and 'onAppleClick' when I can afford Apple Developer's account
    val scope = rememberCoroutineScope()
    val appleSignInComingSoon = stringResource(Res.string.apple_sign_in_coming_soon)
    val onAppleClick = {
        scope.launch { snackbarHostState.showFlashMessage(message = appleSignInComingSoon) }
        Unit
    }

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
                            .widthIn(max = 400.dp)
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.weight(1f))

                        AuthBranding()

                        Spacer(modifier = Modifier.weight(1f))

                        AuthActionsPanel(
                            state = state,
                            onAction = onAction,
                            onAppleClick = onAppleClick
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        AuthDisclosure()
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
                                AuthBranding()
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
                                AuthActionsPanel(
                                    state = state,
                                    onAction = onAction,
                                    onAppleClick = onAppleClick
                                )

                                Spacer(modifier = Modifier.height(24.dp))

                                AuthDisclosure()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AuthActionsPanel(
    state: AuthState,
    onAction: (AuthAction) -> Unit,
    onAppleClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AuthActions(
        isGoogleSigningIn = state.isGoogleSigningIn,
        isAppleSigningIn = state.isAppleSigningIn,
        isGuestSigningIn = state.isGuestSigningIn,
        onGoogleClick = { onAction(AuthAction.OnGoogleSignInClick) },
        onAppleClick = onAppleClick,
        onGuestClick = { onAction(AuthAction.OnGuestClick) },
        onGoogleTokenReceived = { onAction(AuthAction.OnGoogleTokenReceived(it)) },
        onGoogleSignInCancelled = { onAction(AuthAction.OnGoogleSignInCancelled) },
        onGoogleSignInFailed = { onAction(AuthAction.OnGoogleSignInFailed) },
        enableButtons = !state.isGoogleSigningIn && !state.isGuestSigningIn && !state.isAppleSigningIn,
        modifier = modifier
    )
}

@PreviewLightDark
@Composable
private fun AuthScreenPreview() {
    LynkTheme {
        AuthScreen(
            state = AuthState(),
            onAction = {},
            snackbarHostState = remember { SnackbarHostState() }
        )
    }
}

@Preview(name = "Mobile landscape", widthDp = 900, heightDp = 400)
@Composable
private fun AuthScreenLandscapePreview() {
    LynkTheme {
        AuthScreen(
            state = AuthState(),
            onAction = {},
            snackbarHostState = remember { SnackbarHostState() }
        )
    }
}