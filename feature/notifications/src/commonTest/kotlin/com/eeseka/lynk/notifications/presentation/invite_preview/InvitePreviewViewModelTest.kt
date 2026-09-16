package com.eeseka.lynk.notifications.presentation.invite_preview

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isInstanceOf
import assertk.assertions.isNull
import com.eeseka.lynk.shared.domain.hangout.model.HangoutVibe
import com.eeseka.lynk.shared.domain.hangout.model.RsvpStatus
import com.eeseka.lynk.shared.domain.util.DataError
import com.eeseka.lynk.testing.data.FakeHangoutService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class InvitePreviewViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var hangoutService: FakeHangoutService
    private lateinit var viewModel: InvitePreviewViewModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        hangoutService = FakeHangoutService()
        viewModel = InvitePreviewViewModel(hangoutService)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `opening an invite loads the hangout preview`() = runTest {
        val hangoutId = createInvite()

        viewModel.onAction(InvitePreviewAction.Init(hangoutId))
        advanceUntilIdle()

        assertThat(viewModel.state.value.hangoutPreview?.name).isEqualTo("Night Out")
        assertThat(viewModel.state.value.isLoading).isFalse()
    }

    @Test
    fun `an invite already answered says so`() = runTest {
        val hangoutId = createInvite()
        hangoutService.shouldReturnError = true
        hangoutService.errorToReturn = DataError.Remote.FORBIDDEN

        viewModel.events.test {
            viewModel.onAction(InvitePreviewAction.Init(hangoutId))
            advanceUntilIdle()

            assertThat(awaitItem()).isEqualTo(InvitePreviewEvent.AlreadyAnswered(hangoutId))
            assertThat(viewModel.state.value.isLoading).isFalse()
        }
    }

    @Test
    fun `an invite that no longer exists says it was withdrawn`() = runTest {
        viewModel.events.test {
            viewModel.onAction(InvitePreviewAction.Init("missing_hangout"))
            advanceUntilIdle()

            assertThat(awaitItem()).isEqualTo(InvitePreviewEvent.InviteWithdrawn)
        }
    }

    @Test
    fun `a preview that fails for another reason sends an error`() = runTest {
        hangoutService.shouldReturnError = true
        hangoutService.errorToReturn = DataError.Remote.NO_INTERNET

        viewModel.events.test {
            viewModel.onAction(InvitePreviewAction.Init("hangout_1"))
            advanceUntilIdle()

            assertThat(awaitItem()).isInstanceOf(InvitePreviewEvent.Error::class)
        }
    }

    @Test
    fun `accepting joins the hangout`() = runTest {
        val hangoutId = createInvite()
        viewModel.onAction(InvitePreviewAction.Init(hangoutId))
        advanceUntilIdle()

        viewModel.events.test {
            viewModel.onAction(InvitePreviewAction.OnAcceptClick)
            advanceUntilIdle()

            assertThat(awaitItem()).isEqualTo(InvitePreviewEvent.Accepted(hangoutId))
            assertThat(viewModel.state.value.respondingTo).isNull()
            assertThat(inviteeRsvp(hangoutId)).isEqualTo(RsvpStatus.ATTENDING)
        }
    }

    @Test
    fun `declining closes the preview`() = runTest {
        val hangoutId = createInvite()
        viewModel.onAction(InvitePreviewAction.Init(hangoutId))
        advanceUntilIdle()

        viewModel.events.test {
            viewModel.onAction(InvitePreviewAction.OnDeclineClick)
            advanceUntilIdle()

            assertThat(awaitItem()).isEqualTo(InvitePreviewEvent.Dismissed)
            assertThat(inviteeRsvp(hangoutId)).isEqualTo(RsvpStatus.DECLINED)
        }
    }

    @Test
    fun `an accept the server refuses says you cannot join`() = runTest {
        val hangoutId = createInvite()
        viewModel.onAction(InvitePreviewAction.Init(hangoutId))
        advanceUntilIdle()
        hangoutService.shouldReturnError = true
        hangoutService.errorToReturn = DataError.Remote.CONFLICT

        viewModel.events.test {
            viewModel.onAction(InvitePreviewAction.OnAcceptClick)
            advanceUntilIdle()

            assertThat(awaitItem()).isInstanceOf(InvitePreviewEvent.Error::class)
            assertThat(viewModel.state.value.respondingTo).isNull()
        }
    }

    @Test
    fun `answering an invite that was withdrawn meanwhile says so`() = runTest {
        val hangoutId = createInvite()
        viewModel.onAction(InvitePreviewAction.Init(hangoutId))
        advanceUntilIdle()
        hangoutService.removeParticipant(hangoutId, INVITEE_ID)

        viewModel.events.test {
            viewModel.onAction(InvitePreviewAction.OnAcceptClick)
            advanceUntilIdle()

            assertThat(awaitItem()).isEqualTo(InvitePreviewEvent.InviteWithdrawn)
        }
    }

    @Test
    fun `answering before the preview has loaded does nothing`() = runTest {
        viewModel.events.test {
            viewModel.onAction(InvitePreviewAction.OnAcceptClick)
            advanceUntilIdle()

            expectNoEvents()
            assertThat(viewModel.state.value.respondingTo).isNull()
        }
    }

    // A hangout hosted by someone else, with the signed-in user invited and not yet answered
    private suspend fun createInvite(): String {
        hangoutService.currentUserId = "host_1"
        hangoutService.createHangout(
            name = "Night Out",
            description = null,
            vibe = HangoutVibe.CHILL,
            scheduledAt = Instant.fromEpochMilliseconds(4102444800000L),
            maxAttendees = null,
            spotId = null
        )
        val hangoutId = hangoutService.hangouts.last().id
        hangoutService.inviteParticipant(hangoutId, INVITEE_ID)
        hangoutService.currentUserId = INVITEE_ID
        return hangoutId
    }

    private fun inviteeRsvp(hangoutId: String): RsvpStatus? {
        return hangoutService.hangouts
            .first { it.id == hangoutId }
            .participants
            .find { it.user.userId == INVITEE_ID }
            ?.rsvpStatus
    }

    private companion object {
        const val INVITEE_ID = "invitee_1"
    }
}
