package com.eeseka.lynk.hangouts.presentation.hangout_detail

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import assertk.assertions.isTrue
import com.eeseka.lynk.shared.data.hangout.InMemoryHangoutDetailRepository
import com.eeseka.lynk.shared.design_system.components.modals_and_overlays.LynkFlashType
import com.eeseka.lynk.shared.domain.auth.model.AuthInfo
import com.eeseka.lynk.shared.domain.auth.model.AuthProvider
import com.eeseka.lynk.shared.domain.auth.model.User
import com.eeseka.lynk.shared.domain.hangout.model.HangoutStatus
import com.eeseka.lynk.shared.domain.hangout.model.HangoutUser
import com.eeseka.lynk.shared.domain.hangout.model.HangoutVibe
import com.eeseka.lynk.shared.domain.hangout.model.RsvpStatus
import com.eeseka.lynk.shared.domain.lobby.model.ConnectionState
import com.eeseka.lynk.shared.domain.lobby.model.LobbyEvent
import com.eeseka.lynk.shared.domain.spot.model.Spot
import com.eeseka.lynk.shared.domain.spot.model.SpotCategory
import com.eeseka.lynk.shared.domain.util.DataError
import com.eeseka.lynk.testing.collectInBackground
import com.eeseka.lynk.testing.data.FakeHangoutParticipantService
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
class HangoutDetailViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var hangoutService: FakeHangoutService
    private lateinit var sessionStorage: FakeSessionStorage
    private lateinit var participantService: FakeHangoutParticipantService
    private lateinit var spotService: FakeSpotService
    private lateinit var connectionClient: FakeLobbyConnectionClient
    private lateinit var lobbyService: FakeLobbyService

    private val chosenSpot = Spot(
        id = "spot_1", name = "The Lounge", category = SpotCategory.CAFE,
        latitude = 6.5, longitude = 3.3, isSaved = false,
        photoUrls = emptyList(), rating = 4.5, reviewCount = 100,
        isOpenNow = true, shortAddress = "VI, Lagos",
        websiteUrl = null, googleMapsUrl = null, priceLevel = null,
        description = null, tags = emptyList(), savedAt = null
    )

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        hangoutService = FakeHangoutService()
        sessionStorage = FakeSessionStorage()
        participantService = FakeHangoutParticipantService()
        spotService = FakeSpotService()
        connectionClient = FakeLobbyConnectionClient()
        lobbyService = FakeLobbyService()
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `selecting a hangout loads it`() = runTest {
        val hangoutId = createHangoutAsHost()
        val viewModel = createViewModel()

        viewModel.onAction(HangoutDetailAction.OnSelectHangout(hangoutId))
        advanceUntilIdle()

        val state = viewModel.state.value
        assertThat(state.hangout?.name).isEqualTo("Night Out")
        assertThat(state.currentUserId).isEqualTo(HOST_ID)
        assertThat(state.isLoading).isFalse()
        assertThat(state.error).isNull()
    }

    @Test
    fun `a hangout that fails to load shows an error and retry loads it`() = runTest {
        val hangoutId = createHangoutAsHost()
        val viewModel = createViewModel()
        hangoutService.shouldReturnError = true

        viewModel.onAction(HangoutDetailAction.OnSelectHangout(hangoutId))
        advanceUntilIdle()
        assertThat(viewModel.state.value.error).isNotNull()
        assertThat(viewModel.state.value.isLoading).isFalse()

        hangoutService.shouldReturnError = false
        viewModel.onAction(HangoutDetailAction.OnRetryClick)
        advanceUntilIdle()

        assertThat(viewModel.state.value.error).isNull()
        assertThat(viewModel.state.value.hangout?.name).isEqualTo("Night Out")
    }

    @Test
    fun `clearing the selection clears the hangout`() = runTest {
        val hangoutId = createHangoutAsHost()
        val viewModel = createViewModel()
        viewModel.onAction(HangoutDetailAction.OnSelectHangout(hangoutId))
        advanceUntilIdle()

        viewModel.onAction(HangoutDetailAction.OnSelectHangout(null))
        advanceUntilIdle()

        assertThat(viewModel.state.value.hangout).isNull()
        assertThat(viewModel.state.value.isLoading).isFalse()
    }

    @Test
    fun `while connected the socket moves from one hangout's lobby to the next`() = runTest {
        val firstId = createHangoutAsHost("Night Out")
        val secondId = createHangoutAsHost("Brunch")
        connectionClient.connectionState.value = ConnectionState.CONNECTED
        val viewModel = createViewModel()

        viewModel.onAction(HangoutDetailAction.OnSelectHangout(firstId))
        advanceUntilIdle()
        viewModel.onAction(HangoutDetailAction.OnSelectHangout(secondId))
        advanceUntilIdle()
        viewModel.onAction(HangoutDetailAction.OnSelectHangout(null))
        advanceUntilIdle()

        assertThat(lobbyService.enteredLobbyIds).containsExactly(firstId, secondId)
        assertThat(lobbyService.leftLobbyIds).containsExactly(firstId, secondId)
    }

    @Test
    fun `presence shows who is in the lobby and clears when the connection drops`() = runTest {
        val hangoutId = createHangoutAsHost()
        connectionClient.connectionState.value = ConnectionState.CONNECTED
        val viewModel = createViewModel()
        viewModel.onAction(HangoutDetailAction.OnSelectHangout(hangoutId))
        advanceUntilIdle()

        connectionClient.sendEvent(LobbyEvent.PresenceUpdate(hangoutId, setOf(HOST_ID, GUEST_ID)))
        advanceUntilIdle()
        assertThat(viewModel.state.value.presentUserIds).isEqualTo(setOf(HOST_ID, GUEST_ID))

        connectionClient.connectionState.value = ConnectionState.ERROR_NETWORK
        advanceUntilIdle()
        assertThat(viewModel.state.value.presentUserIds).isEmpty()
        assertThat(viewModel.state.value.connectionState).isEqualTo(ConnectionState.ERROR_NETWORK)
    }

    @Test
    fun `presence for a different hangout is ignored`() = runTest {
        val hangoutId = createHangoutAsHost()
        val viewModel = createViewModel()
        viewModel.onAction(HangoutDetailAction.OnSelectHangout(hangoutId))
        advanceUntilIdle()

        connectionClient.sendEvent(LobbyEvent.PresenceUpdate("another_hangout", setOf(GUEST_ID)))
        advanceUntilIdle()

        assertThat(viewModel.state.value.presentUserIds).isEmpty()
    }

    @Test
    fun `a lobby event refreshes the hangout and announces it`() = runTest {
        val hangoutId = createHangoutAsHost()
        val viewModel = createViewModel()
        viewModel.onAction(HangoutDetailAction.OnSelectHangout(hangoutId))
        advanceUntilIdle()

        viewModel.events.test {
            renameOnServer(hangoutId, "Night Out Moved")
            connectionClient.sendEvent(
                LobbyEvent.RsvpUpdated(hangoutId, GUEST_ID, "Bola", RsvpStatus.ATTENDING)
            )
            advanceUntilIdle()

            val event = awaitItem() as HangoutDetailEvent.LobbyAnnouncement
            assertThat(event.type).isEqualTo(LynkFlashType.Success)
            assertThat(viewModel.state.value.hangout?.name).isEqualTo("Night Out Moved")
        }
    }

    @Test
    fun `the host is not told about a change they made themselves`() = runTest {
        val hangoutId = createHangoutAsHost()
        val viewModel = createViewModel()
        viewModel.onAction(HangoutDetailAction.OnSelectHangout(hangoutId))
        advanceUntilIdle()

        viewModel.events.test {
            renameOnServer(hangoutId, "Night Out Moved")
            connectionClient.sendEvent(LobbyEvent.HangoutUpdated(hangoutId, hostDisplayName = "Ada"))
            advanceUntilIdle()

            expectNoEvents()
            assertThat(viewModel.state.value.hangout?.name).isEqualTo("Night Out Moved")
        }
    }

    @Test
    fun `an attendee is told when the host changes the hangout`() = runTest {
        val hangoutId = createHangoutAsHost()
        joinAsGuest(hangoutId)
        val viewModel = createViewModel()
        viewModel.onAction(HangoutDetailAction.OnSelectHangout(hangoutId))
        advanceUntilIdle()

        viewModel.events.test {
            connectionClient.sendEvent(LobbyEvent.HangoutUpdated(hangoutId, hostDisplayName = "Ada"))
            advanceUntilIdle()

            assertThat(awaitItem()).isInstanceOf(HangoutDetailEvent.LobbyAnnouncement::class)
        }
    }

    @Test
    fun `a payer is not told about their own payment`() = runTest {
        val hangoutId = createHangoutAsHost()
        joinAsGuest(hangoutId)
        val viewModel = createViewModel()
        viewModel.onAction(HangoutDetailAction.OnSelectHangout(hangoutId))
        advanceUntilIdle()

        viewModel.events.test {
            connectionClient.sendEvent(LobbyEvent.PaymentReceived(hangoutId, GUEST_ID, "Bola"))
            advanceUntilIdle()

            expectNoEvents()
        }
    }

    @Test
    fun `lobby events for a different hangout are ignored`() = runTest {
        val hangoutId = createHangoutAsHost()
        val viewModel = createViewModel()
        viewModel.onAction(HangoutDetailAction.OnSelectHangout(hangoutId))
        advanceUntilIdle()

        viewModel.events.test {
            connectionClient.sendEvent(LobbyEvent.ParticipantLeft("another_hangout", GUEST_ID, "Bola"))
            advanceUntilIdle()

            expectNoEvents()
        }
    }

    @Test
    fun `completing a hangout marks it completed`() = runTest {
        val hangoutId = createHangoutAsHost()
        val viewModel = createViewModel()
        viewModel.onAction(HangoutDetailAction.OnSelectHangout(hangoutId))
        advanceUntilIdle()

        viewModel.events.test {
            viewModel.onAction(HangoutDetailAction.OnCompleteHangoutConfirmed)
            advanceUntilIdle()

            assertThat(awaitItem()).isEqualTo(HangoutDetailEvent.HangoutCompleted)
            assertThat(viewModel.state.value.hangout?.status).isEqualTo(HangoutStatus.COMPLETED)
            assertThat(viewModel.state.value.isCompleting).isFalse()
        }
    }

    @Test
    fun `a failed complete sends an error and clears the busy flag`() = runTest {
        val hangoutId = createHangoutAsHost()
        val viewModel = createViewModel()
        viewModel.onAction(HangoutDetailAction.OnSelectHangout(hangoutId))
        advanceUntilIdle()

        viewModel.events.test {
            hangoutService.shouldReturnError = true
            viewModel.onAction(HangoutDetailAction.OnCompleteHangoutConfirmed)
            advanceUntilIdle()

            assertThat(awaitItem()).isInstanceOf(HangoutDetailEvent.Error::class)
            assertThat(viewModel.state.value.isCompleting).isFalse()
        }
    }

    @Test
    fun `cancelling a hangout marks it cancelled`() = runTest {
        val hangoutId = createHangoutAsHost()
        val viewModel = createViewModel()
        viewModel.onAction(HangoutDetailAction.OnSelectHangout(hangoutId))
        advanceUntilIdle()

        viewModel.events.test {
            viewModel.onAction(HangoutDetailAction.OnCancelHangoutConfirmed)
            advanceUntilIdle()

            assertThat(awaitItem()).isEqualTo(HangoutDetailEvent.HangoutCancelled)
            assertThat(viewModel.state.value.hangout?.status).isEqualTo(HangoutStatus.CANCELLED)
            assertThat(viewModel.state.value.isCancelling).isFalse()
        }
    }

    @Test
    fun `leaving a hangout confirms it and then navigates back`() = runTest {
        val hangoutId = createHangoutAsHost()
        joinAsGuest(hangoutId)
        val viewModel = createViewModel()
        viewModel.onAction(HangoutDetailAction.OnSelectHangout(hangoutId))
        advanceUntilIdle()

        viewModel.events.test {
            viewModel.onAction(HangoutDetailAction.OnLeaveHangoutConfirmed)
            advanceUntilIdle()

            assertThat(awaitItem()).isEqualTo(HangoutDetailEvent.HangoutLeft)
            assertThat(awaitItem()).isEqualTo(HangoutDetailEvent.NavigateBack)
            assertThat(viewModel.state.value.isLeaving).isFalse()
        }
    }

    @Test
    fun `searching a username that exists shows that user`() = runTest {
        participantService.users.add(hangoutUser(GUEST_ID, username = "bola"))
        val viewModel = createViewModel()
        viewModel.onAction(HangoutDetailAction.OnInviteClick)

        viewModel.state.value.inviteQueryState.typeText("bola")
        advanceTimeBy(501.milliseconds)
        advanceUntilIdle()

        assertThat(viewModel.state.value.inviteResult?.userId).isEqualTo(GUEST_ID)
        assertThat(viewModel.state.value.inviteNotFound).isFalse()
        assertThat(viewModel.state.value.isInviteSearching).isFalse()
    }

    @Test
    fun `searching a username nobody has shows not found without an error`() = runTest {
        val viewModel = createViewModel()
        viewModel.onAction(HangoutDetailAction.OnInviteClick)

        viewModel.events.test {
            viewModel.state.value.inviteQueryState.typeText("nobody")
            advanceTimeBy(501.milliseconds)
            advanceUntilIdle()

            assertThat(viewModel.state.value.inviteNotFound).isTrue()
            assertThat(viewModel.state.value.inviteResult).isNull()
            expectNoEvents()
        }
    }

    @Test
    fun `a username search that fails for another reason sends an error`() = runTest {
        participantService.shouldReturnError = true
        participantService.errorToReturn = DataError.Remote.NO_INTERNET
        val viewModel = createViewModel()
        viewModel.onAction(HangoutDetailAction.OnInviteClick)

        viewModel.events.test {
            viewModel.state.value.inviteQueryState.typeText("bola")
            advanceTimeBy(501.milliseconds)
            advanceUntilIdle()

            assertThat(awaitItem()).isInstanceOf(HangoutDetailEvent.Error::class)
            assertThat(viewModel.state.value.inviteNotFound).isFalse()
        }
    }

    @Test
    fun `closing the invite sheet clears what was searched`() = runTest {
        participantService.users.add(hangoutUser(GUEST_ID, username = "bola"))
        val viewModel = createViewModel()
        viewModel.onAction(HangoutDetailAction.OnInviteClick)
        viewModel.state.value.inviteQueryState.typeText("bola")
        advanceTimeBy(501.milliseconds)
        advanceUntilIdle()
        assertThat(viewModel.state.value.inviteResult).isNotNull()

        viewModel.onAction(HangoutDetailAction.OnDismissInviteSheet)

        val state = viewModel.state.value
        assertThat(state.isInviteSheetOpen).isFalse()
        assertThat(state.inviteResult).isNull()
        assertThat(state.inviteQueryState.text.toString()).isEqualTo("")
    }

    @Test
    fun `inviting a user adds them as pending`() = runTest {
        val hangoutId = createHangoutAsHost()
        val viewModel = createViewModel()
        viewModel.onAction(HangoutDetailAction.OnSelectHangout(hangoutId))
        advanceUntilIdle()

        viewModel.events.test {
            viewModel.onAction(HangoutDetailAction.OnInviteUser(GUEST_ID))
            advanceUntilIdle()

            assertThat(awaitItem()).isEqualTo(HangoutDetailEvent.InviteSent)
            val invitee = viewModel.state.value.hangout?.participants?.find { it.user.userId == GUEST_ID }
            assertThat(invitee?.rsvpStatus).isEqualTo(RsvpStatus.PENDING)
            assertThat(viewModel.state.value.isInviting).isFalse()
        }
    }

    @Test
    fun `withdrawing an invite removes the invitee`() = runTest {
        val hangoutId = createHangoutAsHost()
        hangoutService.inviteParticipant(hangoutId, GUEST_ID)
        val viewModel = createViewModel()
        viewModel.onAction(HangoutDetailAction.OnSelectHangout(hangoutId))
        advanceUntilIdle()

        viewModel.events.test {
            viewModel.onAction(HangoutDetailAction.OnWithdrawParticipantInvite(GUEST_ID))
            advanceUntilIdle()

            assertThat(awaitItem()).isEqualTo(HangoutDetailEvent.InviteWithdrawn)
            val invitee = viewModel.state.value.hangout?.participants?.find { it.user.userId == GUEST_ID }
            assertThat(invitee).isNull()
            assertThat(viewModel.state.value.withdrawingUserIds).isEmpty()
        }
    }

    @Test
    fun `the invite sheet closes once the hangout is no longer upcoming`() = runTest {
        val hangoutId = createHangoutAsHost()
        joinAsGuest(hangoutId)
        val viewModel = createViewModel()
        viewModel.onAction(HangoutDetailAction.OnSelectHangout(hangoutId))
        advanceUntilIdle()
        viewModel.onAction(HangoutDetailAction.OnInviteClick)

        hangoutService.cancelHangout(hangoutId)
        connectionClient.sendEvent(LobbyEvent.HangoutCancelled(hangoutId, hostDisplayName = "Ada"))
        advanceUntilIdle()

        assertThat(viewModel.state.value.isInviteSheetOpen).isFalse()
    }

    @Test
    fun `selecting another hangout closes the invite sheet`() = runTest {
        val firstId = createHangoutAsHost("Night Out")
        val secondId = createHangoutAsHost("Brunch")
        val viewModel = createViewModel()
        viewModel.onAction(HangoutDetailAction.OnSelectHangout(firstId))
        advanceUntilIdle()
        viewModel.onAction(HangoutDetailAction.OnInviteClick)

        viewModel.onAction(HangoutDetailAction.OnSelectHangout(secondId))
        advanceUntilIdle()

        assertThat(viewModel.state.value.isInviteSheetOpen).isFalse()
        assertThat(viewModel.state.value.hangout?.name).isEqualTo("Brunch")
    }

    @Test
    fun `saving the chosen spot shows at once and rolls back when it fails`() = runTest {
        val hangoutId = createHangoutAsHost()
        val index = hangoutService.hangouts.indexOfFirst { it.id == hangoutId }
        hangoutService.hangouts[index] = hangoutService.hangouts[index].copy(chosenSpot = chosenSpot)
        spotService.shouldReturnError = true
        val viewModel = createViewModel()
        viewModel.onAction(HangoutDetailAction.OnSelectHangout(hangoutId))
        advanceUntilIdle()

        viewModel.events.test {
            viewModel.onAction(HangoutDetailAction.OnToggleSaveSpot(spotId = "spot_1", isCurrentlySaved = false))
            assertThat(viewModel.state.value.hangout?.chosenSpot?.isSaved).isEqualTo(true)

            advanceTimeBy(301.milliseconds)
            advanceUntilIdle()

            assertThat(awaitItem()).isInstanceOf(HangoutDetailEvent.Error::class)
            assertThat(viewModel.state.value.hangout?.chosenSpot?.isSaved).isEqualTo(false)
        }
    }

    // The real in-memory store, fed by the fake service, so every refresh behaves like the app's
    private fun TestScope.createViewModel(): HangoutDetailViewModel {
        val repository = InMemoryHangoutDetailRepository(hangoutService, sessionStorage, backgroundScope)
        runCurrent()
        val viewModel = HangoutDetailViewModel(
            hangoutService = hangoutService,
            hangoutDetailRepository = repository,
            participantService = participantService,
            spotService = spotService,
            connectionClient = connectionClient,
            lobbyService = lobbyService,
            sessionStorage = sessionStorage
        )
        collectInBackground(viewModel.state)
        return viewModel
    }

    private suspend fun createHangoutAsHost(name: String = "Night Out"): String {
        signIn(HOST_ID)
        hangoutService.currentUserId = HOST_ID
        hangoutService.createHangout(
            name = name,
            description = null,
            vibe = HangoutVibe.CHILL,
            scheduledAt = Instant.fromEpochMilliseconds(4102444800000L),
            maxAttendees = null,
            spotId = null
        )
        return hangoutService.hangouts.last().id
    }

    private suspend fun joinAsGuest(hangoutId: String) {
        hangoutService.inviteParticipant(hangoutId, GUEST_ID)
        hangoutService.currentUserId = GUEST_ID
        hangoutService.updateRsvp(hangoutId, RsvpStatus.ATTENDING)
        signIn(GUEST_ID)
    }

    private suspend fun renameOnServer(hangoutId: String, name: String) {
        val hangout = hangoutService.hangouts.first { it.id == hangoutId }
        hangoutService.updateHangout(
            hangoutId = hangoutId,
            name = name,
            description = hangout.description,
            vibe = hangout.vibe,
            scheduledAt = hangout.scheduledAt,
            maxAttendees = hangout.maxAttendees,
            spotId = null
        )
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

    private fun hangoutUser(userId: String, username: String) = HangoutUser(
        userId = userId,
        username = username,
        displayName = username,
        profilePictureUrl = null
    )

    private companion object {
        const val HOST_ID = "host_1"
        const val GUEST_ID = "guest_1"
    }
}
