package com.eeseka.lynk.create_hangout.presentation

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.v2.runComposeUiTest
import com.eeseka.lynk.shared.domain.hangout.model.HangoutStatus
import com.eeseka.lynk.shared.domain.hangout.model.HangoutVibe
import com.eeseka.lynk.shared.presentation.hangout.model.HangoutUi
import com.eeseka.lynk.shared.presentation.util.UiText
import kotlinx.collections.immutable.persistentListOf
import kotlin.test.Test
import kotlin.time.Instant

@OptIn(ExperimentalTestApi::class)
class CreateHangoutSheetTest {

    private val editedHangout = HangoutUi(
        id = "hangout_1", hostId = "host_1",
        name = "Existing Hangout", description = "Fun times",
        vibe = HangoutVibe.FOOD, status = HangoutStatus.SCHEDULED,
        scheduledAt = Instant.fromEpochMilliseconds(4102444800000L), maxAttendees = 10,
        participantCount = 3, chosenSpot = null,
        participants = persistentListOf(),
        payment = null,
        createdAt = Instant.fromEpochMilliseconds(4102444800000L)
    )

    @Test
    fun `step 1 shows The Basics title`() = runComposeUiTest {
        val robot = CreateHangoutRobot(this)
        robot.setContent(state = CreateHangoutState(currentStep = 1))
            .assertTitleVisible("The Basics")
    }

    @Test
    fun `step 1 next button is never gated so pressing it can reveal errors`() =
        runComposeUiTest {
            val robot = CreateHangoutRobot(this)
            robot.setContent(state = CreateHangoutState(currentStep = 1))
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
                currentStep = 3
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
                originalHangout = editedHangout
            )
        ).assertNextButtonDisabled("Save Changes")
    }

    @Test
    fun `step 3 submit button enabled when canSubmit is true`() = runComposeUiTest {
        val robot = CreateHangoutRobot(this)
        robot.setContent(
            state = CreateHangoutState(
                currentStep = 3,
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
    fun `submit error visible on step 3`() = runComposeUiTest {
        val robot = CreateHangoutRobot(this)
        robot.setContent(
            state = CreateHangoutState(
                currentStep = 3,
                submitError = UiText.DynamicString("Something went wrong")
            )
        ).assertTextVisible("Something went wrong")
    }
}