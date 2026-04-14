package com.eeseka.lynk.profile_setup.presentation

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import com.eeseka.lynk.shared.presentation.util.UiText
import kotlinx.coroutines.flow.MutableSharedFlow
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
    fun `error event triggers flash message snackbar`() = runTest {
        // We use runTest here so we can safely emit to the coroutine flow
        val eventFlow = MutableSharedFlow<ProfileSetupEvent>()

        runComposeUiTest {
            val robot = ProfileSetupRobot(this)
            robot.setContent(
                state = ProfileSetupState(),
                events = eventFlow
            )

            // Emit the error just like the ViewModel would
            eventFlow.emit(
                ProfileSetupEvent.Error(UiText.DynamicString("Network connection lost"))
            )

            // The UI should react to the event and display the snackbar
            robot.assertSnackbarMessageVisible("Network connection lost")
        }
    }
}