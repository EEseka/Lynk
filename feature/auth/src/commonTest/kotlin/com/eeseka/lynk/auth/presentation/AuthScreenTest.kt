package com.eeseka.lynk.auth.presentation

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import kotlinx.coroutines.flow.MutableSharedFlow
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
    fun `error event shows snackbar`() = runTest {
        val eventFlow = MutableSharedFlow<AuthEvent>()
        runComposeUiTest {
            val robot = AuthRobot(this)
            robot.setContent(events = eventFlow)

            eventFlow.emit(AuthEvent.Error("Auth Failed"))

            robot.assertTextVisible("Auth Failed")
        }
    }
}