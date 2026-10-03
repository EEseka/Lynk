package com.eeseka.lynk.hangouts.presentation.hangout_detail.memories

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isInstanceOf
import assertk.assertions.isTrue
import com.eeseka.lynk.shared.data.hangout.InMemoryHangoutDetailRepository
import com.eeseka.lynk.shared.data.hangout.InMemoryHangoutPhotoStatsRepository
import com.eeseka.lynk.shared.domain.auth.model.AuthInfo
import com.eeseka.lynk.shared.domain.auth.model.AuthProvider
import com.eeseka.lynk.shared.domain.auth.model.User
import com.eeseka.lynk.shared.domain.hangout.model.HangoutPhoto
import com.eeseka.lynk.shared.domain.hangout.model.HangoutUser
import com.eeseka.lynk.shared.domain.hangout.model.HangoutVibe
import com.eeseka.lynk.shared.domain.hangout.model.RsvpStatus
import com.eeseka.lynk.shared.domain.lobby.model.LobbyEvent
import com.eeseka.lynk.testing.collectInBackground
import com.eeseka.lynk.testing.data.FakeHangoutPhotoRepository
import com.eeseka.lynk.testing.data.FakeHangoutPhotoService
import com.eeseka.lynk.testing.data.FakeHangoutService
import com.eeseka.lynk.testing.data.FakeLobbyConnectionClient
import com.eeseka.lynk.testing.data.FakeSessionStorage
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
class HangoutMemoriesViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var hangoutService: FakeHangoutService
    private lateinit var hangoutPhotoService: FakeHangoutPhotoService
    private lateinit var hangoutPhotoRepository: FakeHangoutPhotoRepository
    private lateinit var sessionStorage: FakeSessionStorage
    private lateinit var connectionClient: FakeLobbyConnectionClient
    private lateinit var detailRepository: InMemoryHangoutDetailRepository
    private lateinit var photoStatsRepository: InMemoryHangoutPhotoStatsRepository

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        hangoutService = FakeHangoutService()
        hangoutPhotoService = FakeHangoutPhotoService()
        hangoutPhotoRepository = FakeHangoutPhotoRepository()
        sessionStorage = FakeSessionStorage()
        connectionClient = FakeLobbyConnectionClient()
        hangoutPhotoService.photos = mutableListOf(photo("p1"))
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loads no photos until the hangout is completed`() = runTest {
        val hangoutId = createHangout(attendees = listOf(GUEST_ID))
        val viewModel = createViewModel(hangoutId)

        assertThat(viewModel.state.value.photos).isEmpty()
    }

    @Test
    fun `loads the photos once the host completes the hangout`() = runTest {
        val hangoutId = createHangout(attendees = listOf(GUEST_ID))
        val viewModel = createViewModel(hangoutId)

        completeHangout(hangoutId)

        assertThat(viewModel.state.value.photos.map { it.id }).containsExactly("p1")
    }

    @Test
    fun `shows photos added on another phone`() = runTest {
        val hangoutId = createHangout(attendees = listOf(GUEST_ID))
        completeHangout(hangoutId)
        val viewModel = createViewModel(hangoutId)
        hangoutPhotoService.photos.add(0, photo("p2"))

        connectionClient.sendEvent(LobbyEvent.PhotosAdded(hangoutId, uploaderIds = setOf(GUEST_ID)))
        advanceUntilIdle()

        assertThat(viewModel.state.value.photos.map { it.id }).containsExactly("p2", "p1")
    }

    @Test
    fun `shows the row as loading while the counts are still on their way`() = runTest {
        val hangoutId = createHangout(attendees = listOf(GUEST_ID))
        val viewModel = createViewModel(hangoutId)
        hangoutPhotoService.statsResponseDelay = 1.seconds

        hangoutService.completeHangout(hangoutId)
        detailRepository.refreshHangout(hangoutId)
        runCurrent()

        assertThat(viewModel.state.value.isLoadingPhotos).isTrue()

        advanceUntilIdle()

        assertThat(viewModel.state.value.isLoadingPhotos).isFalse()
        assertThat(viewModel.state.value.photos.map { it.id }).containsExactly("p1")
    }

    @Test
    fun `fetches fresh links when a thumbnail fails to load with an old one`() = runTest {
        val hangoutId = createHangout(attendees = listOf(GUEST_ID))
        completeHangout(hangoutId)
        // The photo seeded in setUp has links that already expired
        val viewModel = createViewModel(hangoutId)
        hangoutPhotoService.photos = mutableListOf(photo("p1", urlsExpireAtMillis = FAR_FUTURE))

        viewModel.onAction(HangoutMemoriesAction.OnPhotoLoadFailed)
        advanceUntilIdle()

        assertThat(viewModel.state.value.photos.single().urlsExpireAt).isEqualTo(Instant.fromEpochMilliseconds(FAR_FUTURE))
    }

    @Test
    fun `loads no photos when nobody else went`() = runTest {
        val hangoutId = createHangout(attendees = emptyList())
        val viewModel = createViewModel(hangoutId)

        completeHangout(hangoutId)

        assertThat(viewModel.state.value.photos).isEmpty()
    }

    @Test
    fun `loads no photos for someone who never accepted`() = runTest {
        val hangoutId = createHangout(attendees = listOf(GUEST_ID), pendingInvitees = listOf(INVITEE_ID))
        completeHangout(hangoutId)
        signIn(INVITEE_ID)

        val viewModel = createViewModel(hangoutId)

        assertThat(viewModel.state.value.photos).isEmpty()
    }

    @Test
    fun `counts the photos left from what the server says I have already added`() = runTest {
        val hangoutId = createHangout(attendees = listOf(GUEST_ID))
        completeHangout(hangoutId)
        hangoutPhotoService.photos = MutableList(30) { number -> photo("p$number") }
        hangoutPhotoService.myPhotoCount = 18

        val viewModel = createViewModel(hangoutId)

        assertThat(viewModel.state.value.photoCount).isEqualTo(30)
        assertThat(viewModel.state.value.remainingPhotoSlots).isEqualTo(2)
    }

    @Test
    fun `reloads the row when a photo is removed from the album screen`() = runTest {
        val hangoutId = createHangout(attendees = listOf(GUEST_ID))
        completeHangout(hangoutId)
        hangoutPhotoService.photos.add(0, photo("p2"))
        val viewModel = createViewModel(hangoutId)

        // What the album screen does after a delete
        hangoutPhotoService.deletePhoto(hangoutId, "p2")
        photoStatsRepository.refreshPhotoStats(hangoutId)
        advanceUntilIdle()

        assertThat(viewModel.state.value.photoCount).isEqualTo(1)
        assertThat(viewModel.state.value.photos.map { it.id }).containsExactly("p1")
    }

    @Test
    fun `adds the picked photos and says so`() = runTest {
        val hangoutId = createHangout(attendees = listOf(GUEST_ID))
        completeHangout(hangoutId)
        val viewModel = createViewModel(hangoutId)

        viewModel.events.test {
            viewModel.onAction(HangoutMemoriesAction.OnPhotosPicked(listOf("first.jpg", "second.jpg")))
            assertThat(awaitItem()).isEqualTo(HangoutMemoriesEvent.PhotosAdded)
        }
        assertThat(hangoutPhotoRepository.uploadedImagePaths).containsExactly("first.jpg", "second.jpg")
        assertThat(viewModel.state.value.isUploadingPhotos).isFalse()
    }

    @Test
    fun `reports a failed upload and still shows the photos that went in`() = runTest {
        val hangoutId = createHangout(attendees = listOf(GUEST_ID))
        completeHangout(hangoutId)
        val viewModel = createViewModel(hangoutId)
        hangoutPhotoRepository.shouldReturnError = true
        // One photo of the batch went in before another failed
        hangoutPhotoService.photos.add(0, photo("p2"))

        viewModel.events.test {
            viewModel.onAction(HangoutMemoriesAction.OnPhotosPicked(listOf("first.jpg", "second.jpg")))
            assertThat(awaitItem()).isInstanceOf(HangoutMemoriesEvent.Error::class)
        }
        assertThat(viewModel.state.value.photos.map { it.id }).containsExactly("p2", "p1")
    }

    // The real in-memory stores fed by the fake services, holding the hangout and album on screen
    private suspend fun TestScope.createViewModel(hangoutId: String): HangoutMemoriesViewModel {
        detailRepository = InMemoryHangoutDetailRepository(hangoutService, sessionStorage, backgroundScope)
        photoStatsRepository = InMemoryHangoutPhotoStatsRepository(hangoutPhotoService, sessionStorage, backgroundScope)
        runCurrent()
        val viewModel = HangoutMemoriesViewModel(
            detailRepository,
            hangoutPhotoService,
            hangoutPhotoRepository,
            photoStatsRepository,
            connectionClient,
            sessionStorage
        )
        collectInBackground(viewModel.state)
        viewModel.onAction(HangoutMemoriesAction.OnSelectHangout(hangoutId))
        detailRepository.refreshHangout(hangoutId)
        advanceUntilIdle()
        return viewModel
    }

    private suspend fun TestScope.completeHangout(hangoutId: String) {
        hangoutService.completeHangout(hangoutId)
        if (::detailRepository.isInitialized) detailRepository.refreshHangout(hangoutId)
        advanceUntilIdle()
    }

    // The host creates it, everyone in [attendees] accepts and [pendingInvitees] never answer
    private suspend fun createHangout(
        attendees: List<String>,
        pendingInvitees: List<String> = emptyList()
    ): String {
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
        val hangoutId = hangoutService.hangouts.last().id

        (attendees + pendingInvitees).forEach { hangoutService.inviteParticipant(hangoutId, it) }
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

    private fun photo(id: String, urlsExpireAtMillis: Long = 0) = HangoutPhoto(
        id = id,
        uploader = HangoutUser(userId = HOST_ID, username = "ada", displayName = "Ada", profilePictureUrl = null),
        caption = null,
        fullUrl = "https://storage.test/$id/full",
        thumbnailUrl = "https://storage.test/$id/thumb",
        urlsExpireAt = Instant.fromEpochMilliseconds(urlsExpireAtMillis),
        createdAt = Instant.fromEpochMilliseconds(0)
    )

    private companion object {
        const val HOST_ID = "host_1"
        const val GUEST_ID = "user_2"
        const val INVITEE_ID = "user_3"
        const val FAR_FUTURE = 4102444800000L
    }
}
