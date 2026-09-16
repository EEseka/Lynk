package com.eeseka.lynk.hangouts.presentation.hangout_detail.voting

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isInstanceOf
import assertk.assertions.isNull
import assertk.assertions.isTrue
import com.eeseka.lynk.hangouts.presentation.hangout_detail.voting.model.SearchTab
import com.eeseka.lynk.shared.data.hangout.InMemoryHangoutDetailRepository
import com.eeseka.lynk.shared.domain.auth.model.AuthInfo
import com.eeseka.lynk.shared.domain.auth.model.AuthProvider
import com.eeseka.lynk.shared.domain.auth.model.User
import com.eeseka.lynk.shared.domain.hangout.model.HangoutStatus
import com.eeseka.lynk.shared.domain.hangout.model.HangoutVibe
import com.eeseka.lynk.shared.domain.lobby.model.ConnectionState
import com.eeseka.lynk.shared.domain.lobby.model.LobbyEvent
import com.eeseka.lynk.shared.domain.location.LocationCoordinates
import com.eeseka.lynk.shared.domain.spot.model.Spot
import com.eeseka.lynk.shared.domain.spot.model.SpotCategory
import com.eeseka.lynk.testing.collectInBackground
import com.eeseka.lynk.testing.data.FakeHangoutService
import com.eeseka.lynk.testing.data.FakeLobbyConnectionClient
import com.eeseka.lynk.testing.data.FakeLobbyService
import com.eeseka.lynk.testing.data.FakeSessionStorage
import com.eeseka.lynk.testing.data.FakeSpotService
import com.eeseka.lynk.testing.typeText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class HangoutVotingViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var hangoutService: FakeHangoutService
    private lateinit var sessionStorage: FakeSessionStorage
    private lateinit var spotService: FakeSpotService
    private lateinit var connectionClient: FakeLobbyConnectionClient
    private lateinit var lobbyService: FakeLobbyService
    private lateinit var repository: InMemoryHangoutDetailRepository

    private val savedAt = Instant.fromEpochMilliseconds(4102444800000L)
    private val lounge = spot(id = "spot_1", name = "The Lounge")
    private val rooftop = spot(id = "spot_2", name = "Rooftop Bar")

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        hangoutService = FakeHangoutService()
        sessionStorage = FakeSessionStorage()
        spotService = FakeSpotService()
        connectionClient = FakeLobbyConnectionClient()
        lobbyService = FakeLobbyService()
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `a voting snapshot fills in the candidates and votes and group center`() = runTest {
        val hangoutId = createVotingHangout()
        val viewModel = createViewModel(hangoutId)

        connectionClient.sendEvent(
            LobbyEvent.VotingSnapshot(
                hangoutId = hangoutId,
                candidates = listOf(lounge, rooftop),
                votes = mapOf(HOST_ID to "spot_1"),
                latitude = 6.5,
                longitude = 3.3
            )
        )
        advanceUntilIdle()

        val state = viewModel.state.value
        assertThat(state.candidates.map { it.id }).containsExactly("spot_1", "spot_2")
        assertThat(state.votes).isEqualTo(mapOf(HOST_ID to "spot_1"))
        assertThat(state.center).isEqualTo(LocationCoordinates(6.5, 3.3))
    }

    @Test
    fun `ballot events for a different hangout are ignored`() = runTest {
        val hangoutId = createVotingHangout()
        val viewModel = createViewModel(hangoutId)

        connectionClient.sendEvent(snapshot("another_hangout", candidates = listOf(lounge)))
        connectionClient.sendEvent(LobbyEvent.CenterUpdate("another_hangout", 6.5, 3.3))
        advanceUntilIdle()

        assertThat(viewModel.state.value.candidates).isEmpty()
        assertThat(viewModel.state.value.center).isNull()
    }

    @Test
    fun `a suggested spot waits for the server and is confirmed once it arrives`() = runTest {
        val hangoutId = createVotingHangout()
        val viewModel = createViewModel(hangoutId)

        viewModel.events.test {
            viewModel.onAction(HangoutVotingAction.OnProposeSpot("spot_1"))
            assertThat(viewModel.state.value.proposingSpotIds).isEqualTo(setOf("spot_1"))
            assertThat(lobbyService.proposedSpotIds).containsExactly("spot_1")

            connectionClient.sendEvent(LobbyEvent.CandidateAdded(hangoutId, lounge))
            advanceUntilIdle()

            assertThat(awaitItem()).isEqualTo(HangoutVotingEvent.SpotSuggested)
            assertThat(viewModel.state.value.proposingSpotIds).isEmpty()
            assertThat(viewModel.state.value.candidates.map { it.id }).containsExactly("spot_1")
        }
    }

    @Test
    fun `a spot someone else suggests is added without a confirmation`() = runTest {
        val hangoutId = createVotingHangout()
        val viewModel = createViewModel(hangoutId)

        viewModel.events.test {
            connectionClient.sendEvent(LobbyEvent.CandidateAdded(hangoutId, lounge))
            advanceUntilIdle()

            expectNoEvents()
            assertThat(viewModel.state.value.candidates.map { it.id }).containsExactly("spot_1")
        }
    }

    @Test
    fun `a suggestion that fails to send stops waiting and sends an error`() = runTest {
        val hangoutId = createVotingHangout()
        val viewModel = createViewModel(hangoutId)
        lobbyService.shouldReturnError = true

        viewModel.events.test {
            viewModel.onAction(HangoutVotingAction.OnProposeSpot("spot_1"))
            advanceUntilIdle()

            assertThat(awaitItem()).isInstanceOf(HangoutVotingEvent.Error::class)
            assertThat(viewModel.state.value.proposingSpotIds).isEmpty()
        }
    }

    @Test
    fun `removing a candidate drops it and every vote for it`() = runTest {
        val hangoutId = createVotingHangout()
        val viewModel = createViewModel(hangoutId)
        connectionClient.sendEvent(
            snapshot(
                hangoutId,
                candidates = listOf(lounge, rooftop),
                votes = mapOf(HOST_ID to "spot_1", GUEST_ID to "spot_2")
            )
        )
        advanceUntilIdle()

        viewModel.onAction(HangoutVotingAction.OnRemoveSpot("spot_1"))
        assertThat(viewModel.state.value.removingSpotIds).isEqualTo(setOf("spot_1"))

        connectionClient.sendEvent(LobbyEvent.CandidateRemoved(hangoutId, "spot_1"))
        advanceUntilIdle()

        val state = viewModel.state.value
        assertThat(state.candidates.map { it.id }).containsExactly("spot_2")
        assertThat(state.votes).isEqualTo(mapOf(GUEST_ID to "spot_2"))
        assertThat(state.removingSpotIds).isEmpty()
    }

    @Test
    fun `a new vote tally replaces the votes and clears a stale tie`() = runTest {
        val hangoutId = createVotingHangout()
        val viewModel = createViewModel(hangoutId)

        // The host is told about the tie, and nothing else is handled until that message is read
        viewModel.events.test {
            connectionClient.sendEvent(LobbyEvent.VotingTie(hangoutId, listOf("spot_1", "spot_2")))
            advanceUntilIdle()
            assertThat(awaitItem()).isEqualTo(HangoutVotingEvent.VotingTied)

            connectionClient.sendEvent(LobbyEvent.VoteTally(hangoutId, mapOf(GUEST_ID to "spot_2")))
            advanceUntilIdle()

            assertThat(viewModel.state.value.votes).isEqualTo(mapOf(GUEST_ID to "spot_2"))
            assertThat(viewModel.state.value.tiedSpotIds).isEmpty()
        }
    }

    @Test
    fun `casting a vote sends it and a failed vote sends an error`() = runTest {
        val hangoutId = createVotingHangout()
        val viewModel = createViewModel(hangoutId)

        viewModel.onAction(HangoutVotingAction.OnCastVote("spot_1"))
        assertThat(lobbyService.votedSpotIds).containsExactly("spot_1")

        viewModel.events.test {
            lobbyService.shouldReturnError = true
            viewModel.onAction(HangoutVotingAction.OnCastVote("spot_2"))
            advanceUntilIdle()

            assertThat(awaitItem()).isInstanceOf(HangoutVotingEvent.Error::class)
        }
    }

    @Test
    fun `closing voting keeps waiting for the server's answer`() = runTest {
        val hangoutId = createVotingHangout()
        val viewModel = createViewModel(hangoutId)

        viewModel.onAction(HangoutVotingAction.OnCloseVotingClick)
        advanceUntilIdle()

        assertThat(viewModel.state.value.isClosingVoting).isTrue()
        assertThat(lobbyService.closedVotingChosenSpotIds).containsExactly(null)
    }

    @Test
    fun `closing voting that fails to send stops waiting and sends an error`() = runTest {
        val hangoutId = createVotingHangout()
        val viewModel = createViewModel(hangoutId)
        lobbyService.shouldReturnError = true

        viewModel.events.test {
            viewModel.onAction(HangoutVotingAction.OnCloseVotingClick)
            advanceUntilIdle()

            assertThat(awaitItem()).isInstanceOf(HangoutVotingEvent.Error::class)
            assertThat(viewModel.state.value.isClosingVoting).isFalse()
        }
    }

    @Test
    fun `a tie stops the closing spinner and tells the host`() = runTest {
        val hangoutId = createVotingHangout()
        val viewModel = createViewModel(hangoutId)
        viewModel.onAction(HangoutVotingAction.OnCloseVotingClick)

        viewModel.events.test {
            connectionClient.sendEvent(LobbyEvent.VotingTie(hangoutId, listOf("spot_1", "spot_2")))
            advanceUntilIdle()

            assertThat(awaitItem()).isEqualTo(HangoutVotingEvent.VotingTied)
            assertThat(viewModel.state.value.tiedSpotIds).containsExactly("spot_1", "spot_2")
            assertThat(viewModel.state.value.isClosingVoting).isFalse()
        }
    }

    @Test
    fun `a tie is shown but not announced to someone who is not the host`() = runTest {
        val hangoutId = createVotingHangout()
        signIn(GUEST_ID)
        val viewModel = createViewModel(hangoutId)

        viewModel.events.test {
            connectionClient.sendEvent(LobbyEvent.VotingTie(hangoutId, listOf("spot_1", "spot_2")))
            advanceUntilIdle()

            expectNoEvents()
            assertThat(viewModel.state.value.tiedSpotIds).containsExactly("spot_1", "spot_2")
        }
    }

    @Test
    fun `breaking a tie closes voting with the chosen spot`() = runTest {
        val hangoutId = createVotingHangout()
        val viewModel = createViewModel(hangoutId)

        viewModel.onAction(HangoutVotingAction.OnBreakTie("spot_2"))
        advanceUntilIdle()

        assertThat(lobbyService.closedVotingChosenSpotIds).containsExactly("spot_2")
    }

    @Test
    fun `losing the connection stops every action still waiting on the server`() = runTest {
        val hangoutId = createVotingHangout()
        connectionClient.connectionState.value = ConnectionState.CONNECTED
        val viewModel = createViewModel(hangoutId)
        viewModel.onAction(HangoutVotingAction.OnProposeSpot("spot_1"))
        viewModel.onAction(HangoutVotingAction.OnRemoveSpot("spot_2"))
        viewModel.onAction(HangoutVotingAction.OnCloseVotingClick)

        connectionClient.connectionState.value = ConnectionState.DISCONNECTED
        advanceUntilIdle()

        val state = viewModel.state.value
        assertThat(state.proposingSpotIds).isEmpty()
        assertThat(state.removingSpotIds).isEmpty()
        assertThat(state.isClosingVoting).isFalse()
    }

    @Test
    fun `a lobby error stops every action still waiting on the server`() = runTest {
        val hangoutId = createVotingHangout()
        val viewModel = createViewModel(hangoutId)
        viewModel.onAction(HangoutVotingAction.OnProposeSpot("spot_1"))
        viewModel.onAction(HangoutVotingAction.OnCloseVotingClick)

        connectionClient.sendEvent(LobbyEvent.LobbyError(code = "FORBIDDEN", message = "Nope"))
        advanceUntilIdle()

        assertThat(viewModel.state.value.proposingSpotIds).isEmpty()
        assertThat(viewModel.state.value.isClosingVoting).isFalse()
    }

    @Test
    fun `when voting ends the ballot is cleared and the suggest sheet closes`() = runTest {
        val hangoutId = createVotingHangout()
        val viewModel = createViewModel(hangoutId)
        connectionClient.sendEvent(snapshot(hangoutId, candidates = listOf(lounge), votes = mapOf(HOST_ID to "spot_1")))
        viewModel.onAction(HangoutVotingAction.OnProposeSpotClick)
        advanceUntilIdle()

        changeStatusOnServer(hangoutId, HangoutStatus.SCHEDULED)
        repository.refreshHangout(hangoutId)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertThat(state.candidates).isEmpty()
        assertThat(state.votes).isEmpty()
        assertThat(state.isProposeSpotSheetOpen).isFalse()
    }

    @Test
    fun `a refresh that stays in voting keeps the ballot`() = runTest {
        val hangoutId = createVotingHangout()
        val viewModel = createViewModel(hangoutId)
        connectionClient.sendEvent(snapshot(hangoutId, candidates = listOf(lounge)))
        advanceUntilIdle()

        repository.refreshHangout(hangoutId)
        advanceUntilIdle()

        assertThat(viewModel.state.value.candidates.map { it.id }).containsExactly("spot_1")
    }

    @Test
    fun `selecting another hangout clears the ballot`() = runTest {
        val hangoutId = createVotingHangout()
        val otherId = createVotingHangout()
        val viewModel = createViewModel(hangoutId)
        connectionClient.sendEvent(snapshot(hangoutId, candidates = listOf(lounge)))
        advanceUntilIdle()

        viewModel.onAction(HangoutVotingAction.OnSelectHangout(otherId))
        advanceUntilIdle()

        assertThat(viewModel.state.value.candidates).isEmpty()
    }

    @Test
    fun `sharing a location keeps it and sends it to the lobby`() = runTest {
        val hangoutId = createVotingHangout()
        val viewModel = createViewModel(hangoutId)

        viewModel.onAction(HangoutVotingAction.OnShareLocation(6.5, 3.3))
        advanceUntilIdle()

        assertThat(viewModel.state.value.myLocation).isEqualTo(LocationCoordinates(6.5, 3.3))
        assertThat(lobbyService.sharedLocations).containsExactly(6.5 to 3.3)
    }

    @Test
    fun `trending spots load once the group has a center`() = runTest {
        spotService.trendingSpotsList = mutableListOf(lounge)
        val hangoutId = createVotingHangout()
        val viewModel = createViewModel(hangoutId)
        assertThat(viewModel.state.value.trendingSpots).isEmpty()

        connectionClient.sendEvent(LobbyEvent.CenterUpdate(hangoutId, 6.5, 3.3))
        advanceUntilIdle()

        assertThat(viewModel.state.value.trendingSpots.map { it.id }).containsExactly("spot_1")
        assertThat(viewModel.state.value.isTrendingLoading).isFalse()
    }

    @Test
    fun `searching in the suggest sheet finds spots after the debounce`() = runTest {
        spotService.searchSpotsList = mutableListOf(lounge, rooftop)
        val hangoutId = createVotingHangout()
        val viewModel = createViewModel(hangoutId)
        viewModel.onAction(HangoutVotingAction.OnShareLocation(6.5, 3.3))
        viewModel.onAction(HangoutVotingAction.OnProposeSpotClick)

        viewModel.state.value.proposeSpotSheetSearchTextState.typeText("roof")
        advanceTimeBy(400.milliseconds)
        assertThat(viewModel.state.value.spotSearchResults).isEmpty()

        advanceTimeBy(101.milliseconds)
        advanceUntilIdle()
        assertThat(viewModel.state.value.spotSearchResults.map { it.id }).containsExactly("spot_2")
    }

    @Test
    fun `the favorites tab loads saved spots`() = runTest {
        spotService.savedSpotsList = mutableListOf(lounge)
        val hangoutId = createVotingHangout()
        val viewModel = createViewModel(hangoutId)
        viewModel.onAction(HangoutVotingAction.OnProposeSpotClick)

        viewModel.onAction(HangoutVotingAction.OnTabSelected(SearchTab.FAVORITES))
        advanceUntilIdle()

        assertThat(viewModel.state.value.favoriteSpotSearchResults.map { it.id }).containsExactly("spot_1")
    }

    @Test
    fun `a favorites page with nothing saved earlier marks the end`() = runTest {
        spotService.savedSpotsList = mutableListOf(lounge.copy(savedAt = savedAt))
        val hangoutId = createVotingHangout()
        val viewModel = createViewModel(hangoutId)
        viewModel.onAction(HangoutVotingAction.OnProposeSpotClick)
        viewModel.onAction(HangoutVotingAction.OnTabSelected(SearchTab.FAVORITES))
        advanceUntilIdle()

        viewModel.onAction(HangoutVotingAction.LoadNextFavoriteSpotPage)
        advanceUntilIdle()

        assertThat(viewModel.state.value.favoriteSpotSearchResults.size).isEqualTo(1)
        assertThat(viewModel.state.value.favoriteSpotSearchEndReached).isTrue()
    }

    @Test
    fun `dismissing the suggest sheet resets it to the all spots tab`() = runTest {
        val hangoutId = createVotingHangout()
        val viewModel = createViewModel(hangoutId)
        viewModel.onAction(HangoutVotingAction.OnProposeSpotClick)
        viewModel.onAction(HangoutVotingAction.OnTabSelected(SearchTab.FAVORITES))

        viewModel.onAction(HangoutVotingAction.OnDismissProposeSpotSheet)

        assertThat(viewModel.state.value.isProposeSpotSheetOpen).isFalse()
        assertThat(viewModel.state.value.activeProposeSpotSheetSearchTab).isEqualTo(SearchTab.ALL_SPOTS)
    }

    // The real in-memory store fed by the fake service, holding the hangout the ballot belongs to
    private suspend fun TestScope.createViewModel(hangoutId: String): HangoutVotingViewModel {
        repository = InMemoryHangoutDetailRepository(hangoutService, sessionStorage, backgroundScope)
        runCurrent()
        val viewModel = HangoutVotingViewModel(
            hangoutDetailRepository = repository,
            spotService = spotService,
            connectionClient = connectionClient,
            lobbyService = lobbyService,
            sessionStorage = sessionStorage
        )
        collectInBackground(viewModel.state)
        viewModel.onAction(HangoutVotingAction.OnSelectHangout(hangoutId))
        repository.refreshHangout(hangoutId)
        advanceUntilIdle()
        return viewModel
    }

    private suspend fun createVotingHangout(): String {
        signIn(HOST_ID)
        hangoutService.currentUserId = HOST_ID
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

    private fun changeStatusOnServer(hangoutId: String, status: HangoutStatus) {
        val index = hangoutService.hangouts.indexOfFirst { it.id == hangoutId }
        hangoutService.hangouts[index] = hangoutService.hangouts[index].copy(status = status)
    }

    private suspend fun signIn(userId: String) {
        sessionStorage.set(
            AuthInfo(
                accessToken = "access",
                refreshToken = "refresh",
                user = User.Authenticated(
                    id = userId,
                    provider = AuthProvider.GOOGLE,
                    email = "$userId@test.com",
                    displayName = userId,
                    username = userId
                )
            )
        )
    }

    private fun snapshot(
        hangoutId: String,
        candidates: List<Spot>,
        votes: Map<String, String> = emptyMap()
    ) = LobbyEvent.VotingSnapshot(
        hangoutId = hangoutId,
        candidates = candidates,
        votes = votes,
        latitude = null,
        longitude = null
    )

    private fun spot(id: String, name: String) = Spot(
        id = id, name = name, category = SpotCategory.CAFE,
        latitude = 6.5, longitude = 3.3, isSaved = false,
        photoUrls = emptyList(), rating = 4.5, reviewCount = 100,
        isOpenNow = true, shortAddress = "VI, Lagos",
        websiteUrl = null, googleMapsUrl = null, priceLevel = null,
        description = null, tags = emptyList(), savedAt = null
    )

    private companion object {
        const val HOST_ID = "host_1"
        const val GUEST_ID = "guest_1"
    }
}
