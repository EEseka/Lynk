package com.eeseka.lynk.auth.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.eeseka.lynk.shared.design_system.components.buttons.LynkButton
import com.eeseka.lynk.shared.design_system.components.buttons.LynkButtonStyle
import com.eeseka.lynk.shared.design_system.components.util.AppHaptic
import com.eeseka.lynk.shared.design_system.components.util.rememberAppHaptic
import com.eeseka.lynk.shared.design_system.theme.LynkTheme
import com.mmk.kmpauth.core.auth.KMPAuthUserCancelledException
import com.mmk.kmpauth.google.rememberGoogleSignInState
import lynk.feature.auth.generated.resources.Res
import lynk.feature.auth.generated.resources.apple_logo
import lynk.feature.auth.generated.resources.continue_as_guest
import lynk.feature.auth.generated.resources.continue_with_apple
import lynk.feature.auth.generated.resources.continue_with_google
import lynk.feature.auth.generated.resources.google_logo
import lynk.feature.auth.generated.resources.please_wait
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun AuthActions(
    isGoogleSigningIn: Boolean,
    isAppleSigningIn: Boolean,
    isGuestSigningIn: Boolean,
    onGoogleClick: () -> Unit,
    onAppleClick: () -> Unit,
    onGuestClick: () -> Unit,
    onGoogleTokenReceived: (String) -> Unit,
    onGoogleSignInCancelled: () -> Unit,
    onGoogleSignInFailed: () -> Unit,
    modifier: Modifier = Modifier,
    enableButtons: Boolean = true
) {
    val hapticFeedback = rememberAppHaptic()

    val googleSignIn = rememberGoogleSignInState(
        onResult = { result ->
            result
                .onSuccess { googleUser -> onGoogleTokenReceived(googleUser.idToken) }
                .onFailure { error ->
                    if (error is KMPAuthUserCancelledException) onGoogleSignInCancelled()
                    else onGoogleSignInFailed()
                }
        }
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        LynkButton(
            text = stringResource(Res.string.continue_with_google),
            style = LynkButtonStyle.SECONDARY,
            isLoading = isGoogleSigningIn,
            enabled = enableButtons,
            onClick = {
                hapticFeedback(AppHaptic.ImpactMedium)
                onGoogleClick()
                googleSignIn.launch()
            },
            loadingText = stringResource(Res.string.please_wait),
            leadingIcon = {
                Icon(
                    painter = painterResource(Res.drawable.google_logo),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = Color.Unspecified
                )
            }
        )

        LynkButton(
            text = stringResource(Res.string.continue_with_apple),
            style = LynkButtonStyle.SECONDARY,
            isLoading = isAppleSigningIn,
            enabled = enableButtons,
            onClick = {
                hapticFeedback(AppHaptic.ImpactMedium)
                onAppleClick()
            },
            loadingText = stringResource(Res.string.please_wait),
            leadingIcon = {
                Icon(
                    painter = painterResource(Res.drawable.apple_logo),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = LocalContentColor.current
                )
            }
        )

        OrDivider(modifier = Modifier.padding(horizontal = 16.dp))

        LynkButton(
            text = stringResource(Res.string.continue_as_guest),
            style = LynkButtonStyle.TEXT,
            isLoading = isGuestSigningIn,
            enabled = enableButtons,
            onClick = {
                hapticFeedback(AppHaptic.ImpactMedium)
                onGuestClick()
            },
            loadingText = stringResource(Res.string.please_wait)
        )
    }
}

@PreviewLightDark
@Composable
private fun AuthActionsPreview() {
    LynkTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            PreviewAuthActions()
            PreviewAuthActions(isGoogleSigningIn = true)
            PreviewAuthActions(isAppleSigningIn = true)
            PreviewAuthActions(isGuestSigningIn = true)
            PreviewAuthActions(enableButtons = false)
        }
    }
}

@Composable
private fun PreviewAuthActions(
    isGoogleSigningIn: Boolean = false,
    isAppleSigningIn: Boolean = false,
    isGuestSigningIn: Boolean = false,
    enableButtons: Boolean = !isGoogleSigningIn && !isAppleSigningIn && !isGuestSigningIn
) {
    AuthActions(
        isGoogleSigningIn = isGoogleSigningIn,
        isAppleSigningIn = isAppleSigningIn,
        isGuestSigningIn = isGuestSigningIn,
        onGoogleClick = {},
        onAppleClick = {},
        onGuestClick = {},
        onGoogleTokenReceived = {},
        onGoogleSignInCancelled = {},
        onGoogleSignInFailed = {},
        enableButtons = enableButtons
    )
}