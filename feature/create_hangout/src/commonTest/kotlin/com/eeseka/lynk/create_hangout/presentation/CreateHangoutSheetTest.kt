package com.eeseka.lynk.create_hangout.presentation

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.v2.runComposeUiTest
import com.eeseka.lynk.create_hangout.domain.HangoutFormMode
import com.eeseka.lynk.shared.presentation.util.UiText
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class CreateHangoutSheetTest {

    @Test
    fun `step 1 shows The Basics title and next button`() = runComposeUiTest {
        val robot = CreateHangoutRobot(this)
        robot.setContent(state = CreateHangoutState(currentStep = 1))
            .assertTitleVisible("The Basics")
            .assertNextButtonDisabled("Next: Pick Location →")
    }

    @Test
    fun `step 1 next button enabled when canProceedToStepTwo is true`() = runComposeUiTest {
        val robot = CreateHangoutRobot(this)
        robot.setContent(state = CreateHangoutState(currentStep = 1, canProceedToStepTwo = true))
            .assertNextButtonEnabled("Next: Pick Location →")
    }

    @Test
    fun `step 2 shows Pick Location title`() = runComposeUiTest {
        val robot = CreateHangoutRobot(this)
        robot.setContent(state = CreateHangoutState(currentStep = 2))
            .assertTitleVisible("Pick Location")
    }

    @Test
    fun `step 2 next button disabled when canProceedToStepThree is false`() = runComposeUiTest {
        val robot = CreateHangoutRobot(this)
        robot.setContent(
            state = CreateHangoutState(
                currentStep = 2,
                canProceedToStepThree = false
            )
        ).assertNextButtonDisabled("Next: Review & Confirm →")
    }

    @Test
    fun `step 3 shows Review and Confirm title and Create Hangout button in create mode`() = runComposeUiTest {
        val robot = CreateHangoutRobot(this)
        robot.setContent(
            state = CreateHangoutState(
                currentStep = 3,
                mode = HangoutFormMode.CREATE
            )
        )
            .assertTitleVisible("Review & Confirm")
            .assertNextButtonDisabled("Create Hangout")
    }

    @Test
    fun `step 3 shows Save Changes button in edit mode`() = runComposeUiTest {
        val robot = CreateHangoutRobot(this)
        robot.setContent(
            state = CreateHangoutState(
                currentStep = 3,
                mode = HangoutFormMode.EDIT
            )
        ).assertNextButtonDisabled("Save Changes")
    }

    @Test
    fun `step 3 submit button enabled when canSubmit is true`() = runComposeUiTest {
        val robot = CreateHangoutRobot(this)
        robot.setContent(
            state = CreateHangoutState(
                currentStep = 3,
                mode = HangoutFormMode.CREATE,
                canSubmit = true
            )
        ).assertNextButtonEnabled("Create Hangout")
    }

    @Test
    fun `step 2 voting mode shows Group Vote UI`() = runComposeUiTest {
        val robot = CreateHangoutRobot(this)
        robot.setContent(
            state = CreateHangoutState(
                currentStep = 2,
                isVotingMode = true
            )
        ).assertTextVisible("Let the group decide!")
    }

    @Test
    fun `submit error visible on step 3`() = runTest {
        val eventFlow = MutableSharedFlow<CreateHangoutEvent>()
        runComposeUiTest {
            val robot = CreateHangoutRobot(this)
            robot.setContent(
                state = CreateHangoutState(
                    currentStep = 3,
                    submitError = UiText.DynamicString("Something went wrong")
                ),
                events = eventFlow
            )
            robot.assertTextVisible("Something went wrong")
        }
    }
}