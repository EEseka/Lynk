package com.eeseka.lynk.hangouts.presentation.hangouts_list_detail

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import com.eeseka.lynk.shared.domain.hangout.model.HangoutStatus
import com.eeseka.lynk.shared.domain.hangout.model.HangoutVibe
import com.eeseka.lynk.shared.domain.hangout.model.RsvpStatus
import com.eeseka.lynk.shared.domain.lobby.model.LobbyEvent
import com.eeseka.lynk.shared.presentation.hangout.model.HangoutUi
import com.eeseka.lynk.testing.collectInBackground
import com.eeseka.lynk.testing.data.FakeLobbyConnectionClient
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class HangoutsListDetailViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var connectionClient: FakeLobbyConnectionClient
    private lateinit var viewModel: HangoutsListDetailViewModel

    private val hangout = HangoutUi(
        id = "hangout_1", hostId = "host_1",
        name = "Night Out", description = null,
        vibe = HangoutVibe.CHILL, status = HangoutStatus.SCHEDULED,
        scheduledAt = Instant.fromEpochMilliseconds(4102444800000L), maxAttendees = null,
        participantCount = 1, chosenSpot = null,
        participants = persistentListOf(),
        payment = null,
        createdAt = Instant.fromEpochMilliseconds(4102444800000L)
    )

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        connectionClient = FakeLobbyConnectionClient()
        viewModel = HangoutsListDetailViewModel(connectionClient)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `selecting a hangout stores its id`() = runTest {
        collectInBackground(viewModel.state)

        viewModel.onAction(HangoutsListDetailAction.OnSelectHangout("hangout_1"))

        assertThat(viewModel.state.value.selectedHangoutId).isEqualTo("hangout_1")
    }

    @Test
    fun `create opens the create sheet and dismiss hides it`() = runTest {
        collectInBackground(viewModel.state)

        viewModel.onAction(HangoutsListDetailAction.OnCreateHangoutClick)
        assertThat(viewModel.state.value.sheetState).isEqualTo(SheetState.CreateHangout)

        viewModel.onAction(HangoutsListDetailAction.OnDismissCurrentSheet)
        assertThat(viewModel.state.value.sheetState).isEqualTo(SheetState.Hidden)
    }

    @Test
    fun `editing a hangout opens the edit sheet with it`() = runTest {
        collectInBackground(viewModel.state)

        viewModel.onAction(HangoutsListDetailAction.OnEditHangoutClick(hangout))

        assertThat(viewModel.state.value.sheetState).isEqualTo(SheetState.EditHangout(hangout))
    }

    @Test
    fun `refreshing the list sends a refresh event`() = runTest {
        viewModel.events.test {
            viewModel.onAction(HangoutsListDetailAction.RefreshList)

            assertThat(awaitItem()).isEqualTo(HangoutsListDetailEvent.RefreshList)
        }
    }

    @Test
    fun `lobby events that change a hangout card refresh the list`() = runTest {
        collectInBackground(viewModel.state)
        val listChangingEvents = listOf(
            LobbyEvent.NonPayerRemoved(hangoutId = "hangout_1", userId = "user_2", displayName = "Bola"),
            LobbyEvent.ParticipantLeft(hangoutId = "hangout_1", userId = "user_2", displayName = "Bola"),
            LobbyEvent.RsvpUpdated(
                hangoutId = "hangout_1",
                userId = "user_2",
                displayName = "Bola",
                rsvpStatus = RsvpStatus.ATTENDING
            ),
            LobbyEvent.HangoutUpdated(hangoutId = "hangout_1", hostDisplayName = "Ada"),
            LobbyEvent.HangoutCompleted(hangoutId = "hangout_1", hostDisplayName = "Ada"),
            LobbyEvent.HangoutCancelled(hangoutId = "hangout_1", hostDisplayName = "Ada")
        )

        viewModel.events.test {
            listChangingEvents.forEach { event ->
                connectionClient.sendEvent(event)
                assertThat(awaitItem()).isEqualTo(HangoutsListDetailEvent.RefreshList)
            }
        }
    }

    @Test
    fun `lobby events that only matter inside a lobby do not refresh the list`() = runTest {
        collectInBackground(viewModel.state)

        viewModel.events.test {
            connectionClient.sendEvent(
                LobbyEvent.PresenceUpdate(hangoutId = "hangout_1", presentUserIds = setOf("user_2"))
            )
            connectionClient.sendEvent(LobbyEvent.CandidateRemoved(hangoutId = "hangout_1", spotId = "spot_1"))

            expectNoEvents()
        }
    }
}
