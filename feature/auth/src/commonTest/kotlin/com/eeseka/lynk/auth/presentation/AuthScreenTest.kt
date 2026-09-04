package com.eeseka.lynk.auth.presentation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.v2.runComposeUiTest
import com.eeseka.lynk.shared.design_system.components.modals_and_overlays.LynkFlashType
import com.eeseka.lynk.shared.design_system.components.modals_and_overlays.showFlashMessage
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class AuthScreenTest {

    @Test
    fun `initial state shows enabled buttons`() = runComposeUiTest {
        val robot = AuthRobot(this)
        robot.setContent(state = AuthState())
            .assertButtonEnabled("Continue with Google")
            .assertButtonEnabled("Continue as Guest")
    }

    @Test
    fun `buttons are disabled when signing in`() = runComposeUiTest {
        val robot = AuthRobot(this)
        robot.setContent(state = AuthState(isGoogleSigningIn = true))
            .assertButtonDisabled("Please wait")
            .assertButtonDisabled("Continue as Guest")
    }

    @Test
    fun `error message shows snackbar`() = runTest {
        val snackbarHostState = SnackbarHostState()
        runComposeUiTest {
            val robot = AuthRobot(this)
            robot.setContent(snackbarHostState = snackbarHostState)

            launch {
                snackbarHostState.showFlashMessage(
                    message = "Auth Failed",
                    type = LynkFlashType.Error
                )
            }

            robot.assertTextVisible("Auth Failed")
        }
    }
}