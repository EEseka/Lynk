package com.eeseka.lynk.profile_setup.presentation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.v2.runComposeUiTest
import com.eeseka.lynk.shared.design_system.components.modals_and_overlays.LynkFlashType
import com.eeseka.lynk.shared.design_system.components.modals_and_overlays.showFlashMessage
import com.eeseka.lynk.shared.presentation.util.UiText
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class ProfileSetupScreenTest {

    @Test
    fun `submit button is disabled when canSubmit is false`() = runComposeUiTest {
        val robot = ProfileSetupRobot(this)
        robot
            .setContent(state = ProfileSetupState(canSubmit = false))
            .assertSubmitButtonDisabled("Complete Profile")
    }

    @Test
    fun `submit button shows loading state when isSubmitting is true`() = runComposeUiTest {
        val robot = ProfileSetupRobot(this)
        robot
            .setContent(
                state = ProfileSetupState(
                    canSubmit = true,
                    isSubmitting = true
                )
            )
            .assertSubmitButtonLoading("Completing Profile...")
    }

    @Test
    fun `username field shows success icon when available`() = runComposeUiTest {
        val robot = ProfileSetupRobot(this)
        robot
            .setContent(
                state = ProfileSetupState(isUsernameAvailable = true)
            )
            .assertUsernameAvailableIconVisible("Available")
    }

    @Test
    fun `username field shows alert icon when unavailable`() = runComposeUiTest {
        val robot = ProfileSetupRobot(this)
        robot
            .setContent(
                state = ProfileSetupState(isUsernameAvailable = false)
            )
            .assertUsernameUnavailableIconVisible("Unavailable")
    }

    @Test
    fun `clicking avatar opens image picker action sheet`() = runComposeUiTest {
        val robot = ProfileSetupRobot(this)
        robot
            .setContent(state = ProfileSetupState())
            .clickAvatarCircle("Choose") // The content description on the camera icon
            .assertActionSheetVisible("Choose Source")
    }

    @Test
    fun `username error message is visible when usernameError is set`() = runComposeUiTest {
        val robot = ProfileSetupRobot(this)
        robot.setContent(
            state = ProfileSetupState(
                usernameError = UiText.DynamicString("Username is taken")
            )
        ).assertErrorTextVisible("Username is taken")
    }

    @Test
    fun `displayName error message is visible when displayNameError is set`() = runComposeUiTest {
        val robot = ProfileSetupRobot(this)
        robot.setContent(
            state = ProfileSetupState(
                displayNameError = UiText.DynamicString("Display name is too long")
            )
        ).assertErrorTextVisible("Display name is too long")
    }

    @Test
    fun `error message triggers flash message snackbar`() = runTest {
        val snackbarHostState = SnackbarHostState()

        runComposeUiTest {
            val robot = ProfileSetupRobot(this)
            robot.setContent(
                state = ProfileSetupState(),
                snackbarHostState = snackbarHostState
            )

            // Show the flash just like the root does when an error event arrives
            launch {
                snackbarHostState.showFlashMessage(
                    message = "Network connection lost",
                    type = LynkFlashType.Error
                )
            }

            robot.assertSnackbarMessageVisible("Network connection lost")
        }
    }
}