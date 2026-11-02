package com.eeseka.lynk.hangouts.presentation.hangout_detail.live_location

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import com.eeseka.lynk.shared.domain.lobby.model.LiveLocation
import com.eeseka.lynk.shared.domain.lobby.model.LobbyEvent
import com.eeseka.lynk.testing.collectInBackground
import com.eeseka.lynk.testing.data.FakeLobbyConnectionClient
import com.eeseka.lynk.testing.data.FakeLobbyService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
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
class HangoutLiveLocationViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var connectionClient: FakeLobbyConnectionClient
    private lateinit var lobbyService: FakeLobbyService

    private val sharedAt = Instant.fromEpochMilliseconds(4102444800000L)

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        connectionClient = FakeLobbyConnectionClient()
        lobbyService = FakeLobbyService()
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `a snapshot replaces everyone held before it`() = runTest {
        val viewModel = createViewModel()
        connectionClient.sendEvent(LobbyEvent.LiveLocationUpdated(HANGOUT_ID, liveLocation("ada")))

        connectionClient.sendEvent(LobbyEvent.LiveLocationSnapshot(HANGOUT_ID, listOf(liveLocation("bola"))))
        advanceUntilIdle()

        assertThat(viewModel.state.value.liveLocations.keys.toList()).containsExactly("bola")
    }

    @Test
    fun `a new position moves that person`() = runTest {
        val viewModel = createViewModel()
        connectionClient.sendEvent(LobbyEvent.LiveLocationUpdated(HANGOUT_ID, liveLocation("ada", latitude = 6.5)))

        connectionClient.sendEvent(LobbyEvent.LiveLocationUpdated(HANGOUT_ID, liveLocation("ada", latitude = 6.6)))
        advanceUntilIdle()

        assertThat(viewModel.state.value.liveLocations["ada"]?.latitude).isEqualTo(6.6)
    }

    @Test
    fun `a person who stops comes off the map`() = runTest {
        val viewModel = createViewModel()
        connectionClient.sendEvent(LobbyEvent.LiveLocationUpdated(HANGOUT_ID, liveLocation("ada")))

        connectionClient.sendEvent(LobbyEvent.LiveLocationStopped(HANGOUT_ID, "ada"))
        advanceUntilIdle()

        assertThat(viewModel.state.value.liveLocations).isEmpty()
    }

    @Test
    fun `positions from another hangout are ignored`() = runTest {
        val viewModel = createViewModel()

        connectionClient.sendEvent(LobbyEvent.LiveLocationUpdated("another_hangout", liveLocation("ada")))
        advanceUntilIdle()

        assertThat(viewModel.state.value.liveLocations).isEmpty()
    }

    @Test
    fun `a location is only sent while sharing`() = runTest {
        val viewModel = createViewModel()

        viewModel.onAction(HangoutLiveLocationAction.OnLocationChange(6.5, 3.3))
        viewModel.onAction(HangoutLiveLocationAction.OnSharingChange(true))
        viewModel.onAction(HangoutLiveLocationAction.OnLocationChange(6.6, 3.4))
        advanceUntilIdle()

        assertThat(lobbyService.sharedLiveLocations).containsExactly(6.6 to 3.4)
    }

    @Test
    fun `turning sharing off tells the server once`() = runTest {
        val viewModel = createViewModel()
        viewModel.onAction(HangoutLiveLocationAction.OnSharingChange(true))

        viewModel.onAction(HangoutLiveLocationAction.OnSharingChange(false))
        viewModel.onAction(HangoutLiveLocationAction.OnSharingChange(false))
        advanceUntilIdle()

        assertThat(viewModel.state.value.isSharingLocation).isFalse()
        assertThat(lobbyService.stoppedLiveLocationIds).containsExactly(HANGOUT_ID)
    }

    @Test
    fun `closing a map you never shared on sends nothing`() = runTest {
        val viewModel = createViewModel()

        viewModel.onAction(HangoutLiveLocationAction.OnSharingChange(false))
        advanceUntilIdle()

        assertThat(lobbyService.stoppedLiveLocationIds).isEmpty()
    }

    @Test
    fun `moving to another hangout stops sharing on the old one and clears the map`() = runTest {
        val viewModel = createViewModel()
        viewModel.onAction(HangoutLiveLocationAction.OnSharingChange(true))
        connectionClient.sendEvent(LobbyEvent.LiveLocationUpdated(HANGOUT_ID, liveLocation("ada")))
        advanceUntilIdle()
        assertThat(viewModel.state.value.isSharingLocation).isTrue()

        viewModel.onAction(HangoutLiveLocationAction.OnSelectHangout("another_hangout"))
        advanceUntilIdle()

        assertThat(viewModel.state.value.isSharingLocation).isFalse()
        assertThat(viewModel.state.value.liveLocations).isEmpty()
        assertThat(lobbyService.stoppedLiveLocationIds).containsExactly(HANGOUT_ID)
    }

    private fun TestScope.createViewModel(): HangoutLiveLocationViewModel {
        val viewModel = HangoutLiveLocationViewModel(
            connectionClient = connectionClient,
            lobbyService = lobbyService
        )
        collectInBackground(viewModel.state)
        viewModel.onAction(HangoutLiveLocationAction.OnSelectHangout(HANGOUT_ID))
        advanceUntilIdle()
        return viewModel
    }

    private fun liveLocation(userId: String, latitude: Double = 6.5) = LiveLocation(
        userId = userId,
        latitude = latitude,
        longitude = 3.3,
        sharedAt = sharedAt
    )

    private companion object {
        const val HANGOUT_ID = "hangout_1"
    }
}
