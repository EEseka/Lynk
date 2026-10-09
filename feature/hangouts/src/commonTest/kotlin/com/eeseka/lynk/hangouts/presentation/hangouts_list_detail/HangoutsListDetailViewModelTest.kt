package com.eeseka.lynk.hangouts.presentation.hangouts_list_detail

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import com.eeseka.lynk.hangouts.presentation.model.HangoutStatusFilter
import com.eeseka.lynk.shared.data.hangout.InMemoryHangoutDetailRepository
import com.eeseka.lynk.shared.domain.hangout.model.HangoutStatus
import com.eeseka.lynk.shared.domain.hangout.model.HangoutVibe
import com.eeseka.lynk.shared.domain.hangout.model.RsvpStatus
import com.eeseka.lynk.shared.domain.lobby.model.LobbyEvent
import com.eeseka.lynk.shared.presentation.hangout.model.HangoutUi
import com.eeseka.lynk.testing.collectInBackground
import com.eeseka.lynk.testing.data.FakeHangoutService
import com.eeseka.lynk.testing.data.FakeLobbyConnectionClient
import com.eeseka.lynk.testing.data.FakeSessionStorage
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
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
    private lateinit var hangoutService: FakeHangoutService
    private lateinit var sessionStorage: FakeSessionStorage
    private lateinit var repository: InMemoryHangoutDetailRepository
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
        hangoutService = FakeHangoutService()
        sessionStorage = FakeSessionStorage()
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `selecting a hangout stores its id`() = runTest {
        createViewModel()
        collectInBackground(viewModel.state)

        viewModel.onAction(HangoutsListDetailAction.OnSelectHangout("hangout_1"))

        assertThat(viewModel.state.value.selectedHangoutId).isEqualTo("hangout_1")
    }

    @Test
    fun `create opens the create sheet and dismiss hides it`() = runTest {
        createViewModel()
        collectInBackground(viewModel.state)

        viewModel.onAction(HangoutsListDetailAction.OnCreateHangoutClick)
        assertThat(viewModel.state.value.sheetState).isEqualTo(SheetState.CreateHangout)

        viewModel.onAction(HangoutsListDetailAction.OnDismissCurrentSheet)
        assertThat(viewModel.state.value.sheetState).isEqualTo(SheetState.Hidden)
    }

    @Test
    fun `editing a hangout opens the edit sheet with it`() = runTest {
        createViewModel()
        collectInBackground(viewModel.state)

        viewModel.onAction(HangoutsListDetailAction.OnEditHangoutClick(hangout))

        assertThat(viewModel.state.value.sheetState).isEqualTo(SheetState.EditHangout(hangout))
    }

    @Test
    fun `refreshing the list sends a refresh event`() = runTest {
        createViewModel()
        viewModel.events.test {
            viewModel.onAction(HangoutsListDetailAction.RefreshList)

            assertThat(awaitItem()).isEqualTo(HangoutsListDetailEvent.RefreshList)
        }
    }

    @Test
    fun `lobby events that change a hangout card refresh the list`() = runTest {
        createViewModel()
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
            LobbyEvent.HangoutStarted(hangoutId = "hangout_1"),
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
        createViewModel()
        collectInBackground(viewModel.state)

        viewModel.events.test {
            connectionClient.sendEvent(
                LobbyEvent.PresenceUpdate(hangoutId = "hangout_1", presentUserIds = setOf("user_2"))
            )
            connectionClient.sendEvent(LobbyEvent.CandidateRemoved(hangoutId = "hangout_1", spotId = "spot_1"))

            expectNoEvents()
        }
    }

    @Test
    fun `opening a hangout from outside the list moves the list to its tab once it loads`() = runTest {
        createViewModel()
        collectInBackground(viewModel.state)
        val hangoutId = createHangout()
        hangoutService.completeHangout(hangoutId)

        viewModel.events.test {
            viewModel.onAction(HangoutsListDetailAction.OnSelectHangoutAndShowInList(hangoutId))
            // What the detail pane does as it opens
            repository.refreshHangout(hangoutId)

            assertThat(awaitItem()).isEqualTo(HangoutsListDetailEvent.ShowHangoutInList(HangoutStatusFilter.COMPLETED))
        }
        assertThat(viewModel.state.value.selectedHangoutId).isEqualTo(hangoutId)
    }

    @Test
    fun `a phone shows the hangout in the list once it goes back to it`() = runTest {
        createViewModel()
        collectInBackground(viewModel.state)
        val hangoutId = createHangout()
        hangoutService.completeHangout(hangoutId)

        // The list pane is hidden behind the detail, so nothing listens until back
        viewModel.onAction(HangoutsListDetailAction.OnSelectHangoutAndShowInList(hangoutId))
        repository.refreshHangout(hangoutId)
        viewModel.onAction(HangoutsListDetailAction.OnSelectHangout(null))

        viewModel.events.test {
            assertThat(awaitItem()).isEqualTo(HangoutsListDetailEvent.ShowHangoutInList(HangoutStatusFilter.COMPLETED))
        }
    }

    @Test
    fun `tapping a row leaves the list on its tab`() = runTest {
        createViewModel()
        collectInBackground(viewModel.state)
        val hangoutId = createHangout()

        viewModel.events.test {
            viewModel.onAction(HangoutsListDetailAction.OnSelectHangout(hangoutId))
            repository.refreshHangout(hangoutId)

            expectNoEvents()
        }
    }

    @Test
    fun `picking another hangout before the first loads never moves the list for the first`() = runTest {
        createViewModel()
        collectInBackground(viewModel.state)
        val firstId = createHangout()
        hangoutService.completeHangout(firstId)

        viewModel.events.test {
            viewModel.onAction(HangoutsListDetailAction.OnSelectHangoutAndShowInList(firstId))
            viewModel.onAction(HangoutsListDetailAction.OnSelectHangout(null))
            repository.refreshHangout(firstId)

            expectNoEvents()
        }
    }

    // The real in-memory store fed by the fake service, the one the detail pane loads into
    private fun TestScope.createViewModel() {
        repository = InMemoryHangoutDetailRepository(hangoutService, sessionStorage, backgroundScope)
        viewModel = HangoutsListDetailViewModel(connectionClient, repository)
    }

    private suspend fun createHangout(): String {
        hangoutService.createHangout(
            name = "Night Out",
            description = null,
            vibe = HangoutVibe.CHILL,
            scheduledAt = Instant.fromEpochMilliseconds(4102444800000L),
            maxAttendees = null,
            spotId = null
        )
        return hangoutService.hangouts.last().id
    }
}
