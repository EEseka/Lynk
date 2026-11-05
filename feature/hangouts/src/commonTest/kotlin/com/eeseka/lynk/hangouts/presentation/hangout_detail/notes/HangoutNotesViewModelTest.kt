package com.eeseka.lynk.hangouts.presentation.hangout_detail.notes

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.containsOnly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isInstanceOf
import assertk.assertions.isNull
import assertk.assertions.isTrue
import com.eeseka.lynk.shared.domain.auth.model.AuthInfo
import com.eeseka.lynk.shared.domain.auth.model.AuthProvider
import com.eeseka.lynk.shared.domain.auth.model.User
import com.eeseka.lynk.shared.domain.hangout.model.HangoutNote
import com.eeseka.lynk.shared.domain.hangout.model.HangoutNoteAudience
import com.eeseka.lynk.shared.domain.hangout.model.HangoutUser
import com.eeseka.lynk.shared.domain.hangout.model.HangoutVibe
import com.eeseka.lynk.shared.domain.hangout.model.RsvpStatus
import com.eeseka.lynk.shared.domain.lobby.model.ConnectionState
import com.eeseka.lynk.shared.domain.lobby.model.LobbyEvent
import com.eeseka.lynk.testing.collectInBackground
import com.eeseka.lynk.testing.data.FakeHangoutNoteService
import com.eeseka.lynk.testing.data.FakeHangoutService
import com.eeseka.lynk.testing.data.FakeLobbyConnectionClient
import com.eeseka.lynk.testing.data.FakeSessionStorage
import com.eeseka.lynk.testing.typeText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class HangoutNotesViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var hangoutService: FakeHangoutService
    private lateinit var hangoutNoteService: FakeHangoutNoteService
    private lateinit var sessionStorage: FakeSessionStorage
    private lateinit var connectionClient: FakeLobbyConnectionClient

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        hangoutService = FakeHangoutService()
        hangoutNoteService = FakeHangoutNoteService()
        sessionStorage = FakeSessionStorage()
        connectionClient = FakeLobbyConnectionClient()
        hangoutNoteService.notes = mutableListOf(note("n1", "Running late"))
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loads the notes of a hangout with a spot`() = runTest {
        val hangoutId = createHangout(attendees = listOf(GUEST_ID))
        val viewModel = createViewModel(hangoutId)

        assertThat(viewModel.state.value.notes.map { it.id }).containsExactly("n1")
    }

    @Test
    fun `shows the full count while only the newest page is loaded and loads more on request`() = runTest {
        hangoutNoteService.pageSize = 2
        hangoutNoteService.notes = mutableListOf(
            note("n3", "Third", createdAtMillis = 3_000),
            note("n2", "Second", createdAtMillis = 2_000),
            note("n1", "First", createdAtMillis = 1_000)
        )
        val hangoutId = createHangout(attendees = listOf(GUEST_ID))
        val viewModel = createViewModel(hangoutId)

        assertThat(viewModel.state.value.notes.map { it.id }).containsExactly("n3", "n2")
        assertThat(viewModel.state.value.noteCount).isEqualTo(3L)

        viewModel.onAction(HangoutNotesAction.LoadNextPage)
        advanceUntilIdle()

        assertThat(viewModel.state.value.notes.map { it.id }).containsExactly("n3", "n2", "n1")
    }

    @Test
    fun `deleting a note lowers the count`() = runTest {
        val hangoutId = createHangout(attendees = listOf(GUEST_ID))
        val viewModel = createViewModel(hangoutId)

        viewModel.onAction(HangoutNotesAction.OnDeleteNoteClick("n1"))
        viewModel.onAction(HangoutNotesAction.OnDeleteNoteConfirmed)
        advanceUntilIdle()

        assertThat(viewModel.state.value.noteCount).isEqualTo(0L)
    }

    @Test
    fun `shows a note added on another phone`() = runTest {
        val hangoutId = createHangout(attendees = listOf(GUEST_ID))
        val viewModel = createViewModel(hangoutId)
        hangoutNoteService.notes.add(0, note("n2", "We are at the back"))

        connectionClient.sendEvent(LobbyEvent.NotesChanged(hangoutId))
        advanceUntilIdle()

        assertThat(viewModel.state.value.notes.map { it.id }).containsExactly("n2", "n1")
    }

    @Test
    fun `shows notes added while the app was in the background once it reconnects`() = runTest {
        connectionClient.connectionState.value = ConnectionState.CONNECTED
        val hangoutId = createHangout(attendees = listOf(GUEST_ID))
        val viewModel = createViewModel(hangoutId)
        hangoutNoteService.notes.add(0, note("n2", "On my way"))

        connectionClient.connectionState.value = ConnectionState.DISCONNECTED
        connectionClient.connectionState.value = ConnectionState.CONNECTED
        advanceUntilIdle()

        assertThat(viewModel.state.value.notes.map { it.id }).containsExactly("n2", "n1")
    }

    @Test
    fun `posting a note for the host only sends that choice and says so`() = runTest {
        val hangoutId = createHangout(attendees = listOf(GUEST_ID))
        val viewModel = createViewModel(hangoutId)

        viewModel.events.test {
            viewModel.onAction(HangoutNotesAction.OnAddNoteClick)
            viewModel.state.value.noteState.typeText("  Parking is full  ")
            viewModel.onAction(HangoutNotesAction.OnAudienceChange(HangoutNoteAudience.HOST))
            viewModel.onAction(HangoutNotesAction.OnPostNoteClick)
            advanceUntilIdle()

            assertThat(awaitItem()).isEqualTo(HangoutNotesEvent.NotePosted)
        }
        assertThat(hangoutNoteService.postedAudiences).containsExactly(HangoutNoteAudience.HOST)
        assertThat(viewModel.state.value.notes.first().content).isEqualTo("Parking is full")
        assertThat(viewModel.state.value.isComposerShowing).isFalse()
    }

    @Test
    fun `cannot post a blank note`() = runTest {
        val hangoutId = createHangout(attendees = listOf(GUEST_ID))
        val viewModel = createViewModel(hangoutId)

        viewModel.onAction(HangoutNotesAction.OnAddNoteClick)
        viewModel.state.value.noteState.typeText("   ")
        advanceUntilIdle()

        assertThat(viewModel.state.value.canPostNote).isFalse()
    }

    @Test
    fun `editing opens with the saved words and only allows saving a change`() = runTest {
        val hangoutId = createHangout(attendees = listOf(GUEST_ID))
        val viewModel = createViewModel(hangoutId)

        viewModel.onAction(HangoutNotesAction.OnEditNoteClick("n1"))
        advanceUntilIdle()
        assertThat(viewModel.state.value.noteState.text.toString()).isEqualTo("Running late")
        assertThat(viewModel.state.value.canPostNote).isFalse()

        viewModel.state.value.noteState.typeText("Running 10 minutes late")
        advanceUntilIdle()
        assertThat(viewModel.state.value.canPostNote).isTrue()

        viewModel.onAction(HangoutNotesAction.OnPostNoteClick)
        advanceUntilIdle()
        assertThat(viewModel.state.value.notes.first().content).isEqualTo("Running 10 minutes late")
        assertThat(viewModel.state.value.editingNoteId).isNull()
    }

    @Test
    fun `deleting a note asks first and then removes it`() = runTest {
        val hangoutId = createHangout(attendees = listOf(GUEST_ID))
        val viewModel = createViewModel(hangoutId)

        viewModel.onAction(HangoutNotesAction.OnDeleteNoteClick("n1"))
        assertThat(viewModel.state.value.pendingDeleteNoteId).isEqualTo("n1")

        viewModel.onAction(HangoutNotesAction.OnDeleteNoteConfirmed)
        advanceUntilIdle()

        assertThat(viewModel.state.value.notes).isEmpty()
        assertThat(viewModel.state.value.pendingDeleteNoteId).isNull()
        assertThat(hangoutNoteService.deletedNoteIds).containsExactly("n1")
    }

    @Test
    fun `two notes deleted one after the other both stay marked until each is done`() = runTest {
        hangoutNoteService.notes = mutableListOf(
            note("n2", "Second", createdAtMillis = 2_000),
            note("n1", "First", createdAtMillis = 1_000)
        )
        hangoutNoteService.deleteResponseDelay = 1.seconds
        val hangoutId = createHangout(attendees = listOf(GUEST_ID))
        val viewModel = createViewModel(hangoutId)
        collectInBackground(viewModel.events)

        viewModel.onAction(HangoutNotesAction.OnDeleteNoteClick("n2"))
        viewModel.onAction(HangoutNotesAction.OnDeleteNoteConfirmed)
        viewModel.onAction(HangoutNotesAction.OnDeleteNoteClick("n1"))
        viewModel.onAction(HangoutNotesAction.OnDeleteNoteConfirmed)
        runCurrent()
        assertThat(viewModel.state.value.deletingNoteIds).containsOnly("n2", "n1")

        advanceUntilIdle()
        assertThat(viewModel.state.value.deletingNoteIds).isEmpty()
        assertThat(viewModel.state.value.notes).isEmpty()
    }

    @Test
    fun `a note already deleted on another phone still leaves the list`() = runTest {
        val hangoutId = createHangout(attendees = listOf(GUEST_ID))
        val viewModel = createViewModel(hangoutId)
        hangoutNoteService.notes.clear()

        viewModel.events.test {
            viewModel.onAction(HangoutNotesAction.OnDeleteNoteClick("n1"))
            viewModel.onAction(HangoutNotesAction.OnDeleteNoteConfirmed)
            advanceUntilIdle()

            assertThat(awaitItem()).isEqualTo(HangoutNotesEvent.NoteDeleted)
        }
        assertThat(viewModel.state.value.notes).isEmpty()
    }

    @Test
    fun `editing a note deleted on another phone closes the sheet and drops it`() = runTest {
        val hangoutId = createHangout(attendees = listOf(GUEST_ID))
        val viewModel = createViewModel(hangoutId)

        viewModel.onAction(HangoutNotesAction.OnEditNoteClick("n1"))
        viewModel.state.value.noteState.typeText("Running 10 minutes late")
        hangoutNoteService.notes.clear()

        viewModel.events.test {
            viewModel.onAction(HangoutNotesAction.OnPostNoteClick)
            advanceUntilIdle()

            assertThat(awaitItem()).isInstanceOf(HangoutNotesEvent.Error::class)
        }
        assertThat(viewModel.state.value.isComposerShowing).isFalse()
        assertThat(viewModel.state.value.notes).isEmpty()
    }

    @Test
    fun `a failed post keeps the sheet open with the words and reports it`() = runTest {
        val hangoutId = createHangout(attendees = listOf(GUEST_ID))
        val viewModel = createViewModel(hangoutId)
        hangoutNoteService.shouldReturnError = true

        viewModel.events.test {
            viewModel.onAction(HangoutNotesAction.OnAddNoteClick)
            viewModel.state.value.noteState.typeText("On my way")
            viewModel.onAction(HangoutNotesAction.OnPostNoteClick)
            advanceUntilIdle()

            assertThat(awaitItem()).isInstanceOf(HangoutNotesEvent.Error::class)
        }
        assertThat(viewModel.state.value.isComposerShowing).isTrue()
        assertThat(viewModel.state.value.noteState.text.toString()).isEqualTo("On my way")
    }

    private suspend fun TestScope.createViewModel(hangoutId: String): HangoutNotesViewModel {
        val viewModel = HangoutNotesViewModel(
            hangoutNoteService,
            connectionClient
        )
        collectInBackground(viewModel.state)
        viewModel.onAction(HangoutNotesAction.OnSelectHangout(hangoutId))
        advanceUntilIdle()
        return viewModel
    }

    // The host creates it with a spot unless [spotId] is null, and everyone in [attendees] accepts
    private suspend fun createHangout(
        attendees: List<String>,
        spotId: String? = "spot_1"
    ): String {
        signIn(HOST_ID)
        hangoutService.currentUserId = HOST_ID
        hangoutService.createHangout(
            name = "Night Out",
            description = null,
            vibe = HangoutVibe.CHILL,
            scheduledAt = Instant.fromEpochMilliseconds(4102444800000L),
            maxAttendees = null,
            spotId = spotId
        )
        val hangoutId = hangoutService.hangouts.last().id

        attendees.forEach { hangoutService.inviteParticipant(hangoutId, it) }
        attendees.forEach { attendeeId ->
            hangoutService.currentUserId = attendeeId
            hangoutService.updateRsvp(hangoutId, RsvpStatus.ATTENDING)
        }
        hangoutService.currentUserId = HOST_ID
        return hangoutId
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

    private fun note(id: String, content: String, createdAtMillis: Long = 0) = HangoutNote(
        id = id,
        author = HangoutUser(userId = GUEST_ID, username = "bola", displayName = "Bola", profilePictureUrl = null),
        content = content,
        createdAt = Instant.fromEpochMilliseconds(createdAtMillis),
        updatedAt = null
    )

    private companion object {
        const val HOST_ID = "host_1"
        const val GUEST_ID = "user_2"
    }
}
