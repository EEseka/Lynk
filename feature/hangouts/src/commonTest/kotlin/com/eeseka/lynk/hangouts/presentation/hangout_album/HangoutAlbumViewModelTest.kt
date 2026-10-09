package com.eeseka.lynk.hangouts.presentation.hangout_album

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
import com.eeseka.lynk.shared.data.hangout.InMemoryHangoutDetailRepository
import com.eeseka.lynk.shared.data.hangout.InMemoryHangoutPhotoStatsRepository
import com.eeseka.lynk.shared.domain.auth.model.AuthInfo
import com.eeseka.lynk.shared.domain.auth.model.AuthProvider
import com.eeseka.lynk.shared.domain.auth.model.User
import com.eeseka.lynk.shared.domain.hangout.model.HangoutPhoto
import com.eeseka.lynk.shared.domain.hangout.model.HangoutUser
import com.eeseka.lynk.shared.domain.hangout.model.HangoutVibe
import com.eeseka.lynk.shared.domain.lobby.model.LobbyEvent
import com.eeseka.lynk.testing.collectInBackground
import com.eeseka.lynk.testing.data.FakeHangoutPhotoService
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
class HangoutAlbumViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var hangoutService: FakeHangoutService
    private lateinit var hangoutPhotoService: FakeHangoutPhotoService
    private lateinit var sessionStorage: FakeSessionStorage
    private lateinit var connectionClient: FakeLobbyConnectionClient

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        hangoutService = FakeHangoutService()
        hangoutPhotoService = FakeHangoutPhotoService()
        sessionStorage = FakeSessionStorage()
        connectionClient = FakeLobbyConnectionClient()
        // Newest first, like the server
        hangoutPhotoService.photos = mutableListOf(
            photo("p3", createdAtMillis = 3_000),
            photo("p2", createdAtMillis = 2_000),
            photo("p1", createdAtMillis = 1_000)
        )
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loads the album and knows who the host is`() = runTest {
        val viewModel = createViewModel()

        assertThat(viewModel.state.value.photos.map { it.id }).containsExactly("p3", "p2", "p1")
        assertThat(viewModel.state.value.hostId).isEqualTo(HOST_ID)
    }

    @Test
    fun `opens the viewer on the photo tapped on the detail screen`() = runTest {
        val viewModel = createViewModel(initialPhotoId = "p2")

        assertThat(viewModel.state.value.viewerIndex).isEqualTo(1)
    }

    @Test
    fun `updates the caption trimmed and shows it`() = runTest {
        val viewModel = createViewModel()
        viewModel.onAction(HangoutAlbumAction.OnEditCaptionClick("p2"))
        viewModel.state.value.captionState.typeText("  Suya run  ")

        viewModel.events.test {
            viewModel.onAction(HangoutAlbumAction.OnSaveCaptionClick)
            assertThat(awaitItem()).isEqualTo(HangoutAlbumEvent.CaptionUpdated)
        }
        assertThat(viewModel.state.value.photos.first { it.id == "p2" }.caption).isEqualTo("Suya run")
        assertThat(viewModel.state.value.captionEditPhotoId).isNull()
    }

    @Test
    fun `save wakes only when the caption differs from the saved one`() = runTest {
        val viewModel = createViewModel()
        viewModel.onAction(HangoutAlbumAction.OnEditCaptionClick("p2"))
        assertThat(viewModel.state.value.canSaveCaption).isFalse()

        viewModel.state.value.captionState.typeText("Suya run")
        advanceUntilIdle()
        assertThat(viewModel.state.value.canSaveCaption).isTrue()

        viewModel.state.value.captionState.typeText("   ")
        advanceUntilIdle()
        assertThat(viewModel.state.value.canSaveCaption).isFalse()
    }

    @Test
    fun `removes a deleted photo and keeps the viewer on the one that slid into place`() = runTest {
        val viewModel = createViewModel(initialPhotoId = "p2")

        viewModel.onAction(HangoutAlbumAction.OnDeleteClick("p2"))
        viewModel.events.test {
            viewModel.onAction(HangoutAlbumAction.OnDeleteConfirmed)
            assertThat(awaitItem()).isEqualTo(HangoutAlbumEvent.PhotoDeleted)
        }

        assertThat(hangoutPhotoService.deletedPhotoIds).containsExactly("p2")
        assertThat(viewModel.state.value.photos.map { it.id }).containsExactly("p3", "p1")
        assertThat(viewModel.state.value.viewerIndex).isEqualTo(1)
    }

    @Test
    fun `selecting the same hangout again does not load the album twice`() = runTest {
        val viewModel = createViewModel()
        val hangoutId = viewModel.state.value.hangoutId!!

        // What the screen's LaunchedEffect does when it comes back
        viewModel.onAction(HangoutAlbumAction.OnSelectHangout(hangoutId, initialPhotoId = null))
        advanceUntilIdle()

        assertThat(viewModel.state.value.photos.map { it.id }).containsExactly("p3", "p2", "p1")
    }

    @Test
    fun `marks the photo as deleting until the server answers`() = runTest {
        val viewModel = createViewModel(initialPhotoId = "p2")
        hangoutPhotoService.deleteResponseDelay = 1.seconds

        viewModel.onAction(HangoutAlbumAction.OnDeleteClick("p2"))
        viewModel.events.test {
            viewModel.onAction(HangoutAlbumAction.OnDeleteConfirmed)
            runCurrent()

            assertThat(viewModel.state.value.deletingPhotoId).isEqualTo("p2")

            assertThat(awaitItem()).isEqualTo(HangoutAlbumEvent.PhotoDeleted)
            advanceUntilIdle()

            assertThat(viewModel.state.value.deletingPhotoId).isNull()
        }
    }

    @Test
    fun `a photo already deleted from another device is removed without an error`() = runTest {
        val viewModel = createViewModel(initialPhotoId = "p2")
        hangoutPhotoService.deletePhoto(hangoutId = viewModel.state.value.hangoutId!!, photoId = "p2")

        viewModel.onAction(HangoutAlbumAction.OnDeleteClick("p2"))
        viewModel.events.test {
            viewModel.onAction(HangoutAlbumAction.OnDeleteConfirmed)

            assertThat(awaitItem()).isEqualTo(HangoutAlbumEvent.PhotoDeleted)
            assertThat(viewModel.state.value.photos.map { it.id }).containsExactly("p3", "p1")
        }
    }

    @Test
    fun `a photo removed on another phone leaves the album`() = runTest {
        val viewModel = createViewModel()

        connectionClient.sendEvent(LobbyEvent.PhotoDeleted(viewModel.state.value.hangoutId!!, "p2"))

        assertThat(viewModel.state.value.photos.map { it.id }).containsExactly("p3", "p1")
    }

    @Test
    fun `offers photos added on another phone and shows them when asked`() = runTest {
        val viewModel = createViewModel()
        hangoutPhotoService.photos.add(0, photo("p4", createdAtMillis = 4_000))

        viewModel.events.test {
            connectionClient.sendEvent(
                LobbyEvent.PhotosAdded(viewModel.state.value.hangoutId!!, uploaderIds = setOf("bola"))
            )
            assertThat(awaitItem()).isEqualTo(HangoutAlbumEvent.NewPhotosAdded)
        }
        // Nothing moves until the user asks
        assertThat(viewModel.state.value.photos.map { it.id }).containsExactly("p3", "p2", "p1")

        viewModel.onAction(HangoutAlbumAction.OnNewPhotosClick)
        advanceUntilIdle()

        assertThat(viewModel.state.value.photos.map { it.id }).containsExactly("p4", "p3", "p2", "p1")
    }

    @Test
    fun `showing new photos from the open viewer keeps it on the same photo`() = runTest {
        val viewModel = createViewModel(initialPhotoId = "p2")
        hangoutPhotoService.photos.add(0, photo("p4", createdAtMillis = 4_000))
        hangoutPhotoService.photosResponseDelay = 1.seconds

        viewModel.onAction(HangoutAlbumAction.OnNewPhotosClick)
        runCurrent()

        // An empty album with the viewer still open is what crashed
        assertThat(viewModel.state.value.viewerIndex).isNull()

        advanceUntilIdle()

        val photoIds = viewModel.state.value.photos.map { it.id }
        assertThat(photoIds[viewModel.state.value.viewerIndex!!]).isEqualTo("p2")
    }

    @Test
    fun `offers photos I added myself from another phone`() = runTest {
        val viewModel = createViewModel()

        viewModel.events.test {
            connectionClient.sendEvent(
                LobbyEvent.PhotosAdded(viewModel.state.value.hangoutId!!, uploaderIds = setOf(HOST_ID))
            )
            assertThat(awaitItem()).isEqualTo(HangoutAlbumEvent.NewPhotosAdded)
        }
    }

    @Test
    fun `closes the viewer when the last photo is removed`() = runTest {
        hangoutPhotoService.photos = mutableListOf(photo("p1", createdAtMillis = 1_000))
        val viewModel = createViewModel(initialPhotoId = "p1")

        viewModel.onAction(HangoutAlbumAction.OnDeleteClick("p1"))
        viewModel.onAction(HangoutAlbumAction.OnDeleteConfirmed)
        advanceUntilIdle()

        assertThat(viewModel.state.value.viewerIndex).isNull()
    }

    @Test
    fun `fetches fresh links when a photo fails to load with an old one`() = runTest {
        // Links that already expired
        hangoutPhotoService.photos = mutableListOf(photo("p1", createdAtMillis = 1_000, urlsExpireAtMillis = 0))
        val viewModel = createViewModel(initialPhotoId = "p1")
        hangoutPhotoService.photos = mutableListOf(photo("p1", createdAtMillis = 1_000, urlsExpireAtMillis = FAR_FUTURE))

        viewModel.onAction(HangoutAlbumAction.OnPhotoLoadFailed)
        advanceUntilIdle()

        assertThat(viewModel.state.value.photos.single().urlsExpireAt).isEqualTo(Instant.fromEpochMilliseconds(FAR_FUTURE))
        assertThat(viewModel.state.value.viewerIndex).isEqualTo(0)
    }

    @Test
    fun `fetches fresh links even while the next page is still loading`() = runTest {
        hangoutPhotoService.photos = mutableListOf(photo("p1", createdAtMillis = 1_000, urlsExpireAtMillis = 0))
        val viewModel = createViewModel(initialPhotoId = "p1")
        hangoutPhotoService.photos = mutableListOf(photo("p1", createdAtMillis = 1_000, urlsExpireAtMillis = FAR_FUTURE))
        hangoutPhotoService.photosResponseDelay = 1.seconds

        viewModel.onAction(HangoutAlbumAction.LoadNextPage)
        runCurrent()
        viewModel.onAction(HangoutAlbumAction.OnPhotoLoadFailed)
        advanceUntilIdle()

        assertThat(viewModel.state.value.photos.single().urlsExpireAt).isEqualTo(Instant.fromEpochMilliseconds(FAR_FUTURE))
    }

    @Test
    fun `reports a photo that could not be saved`() = runTest {
        val viewModel = createViewModel()

        viewModel.events.test {
            viewModel.onAction(HangoutAlbumAction.OnPhotoSaved(isSaved = false))
            assertThat(awaitItem()).isInstanceOf(HangoutAlbumEvent.Error::class)
        }
    }

    @Test
    fun `a tap while selecting picks and unpicks a photo instead of opening it`() = runTest {
        val viewModel = createViewModel()

        viewModel.onAction(HangoutAlbumAction.OnSelectClick)
        viewModel.onAction(HangoutAlbumAction.OnPhotoClick("p2"))
        viewModel.onAction(HangoutAlbumAction.OnPhotoClick("p3"))
        viewModel.onAction(HangoutAlbumAction.OnPhotoClick("p2"))

        val state = viewModel.state.value
        assertThat(state.isSelecting).isTrue()
        assertThat(state.selectedPhotoIds).containsOnly("p3")
        assertThat(state.viewerIndex).isNull()
    }

    @Test
    fun `a long press starts selecting with that photo picked`() = runTest {
        val viewModel = createViewModel()

        viewModel.onAction(HangoutAlbumAction.OnPhotoLongClick("p1"))

        assertThat(viewModel.state.value.isSelecting).isTrue()
        assertThat(viewModel.state.value.selectedPhotoIds).containsOnly("p1")
    }

    @Test
    fun `cancelling leaves select mode with nothing picked`() = runTest {
        val viewModel = createViewModel()
        viewModel.onAction(HangoutAlbumAction.OnPhotoLongClick("p1"))

        viewModel.onAction(HangoutAlbumAction.OnCancelSelectionClick)

        assertThat(viewModel.state.value.isSelecting).isFalse()
        assertThat(viewModel.state.value.selectedPhotoIds).isEmpty()
    }

    @Test
    fun `refuses a pick past the limit`() = runTest {
        hangoutPhotoService.photos = (1..HangoutAlbumViewModel.MAX_SELECTED_PHOTOS + 1)
            .map { index -> photo("p$index", createdAtMillis = index * 1_000L) }
            .reversed()
            .toMutableList()
        val viewModel = createViewModel()
        viewModel.onAction(HangoutAlbumAction.OnSelectClick)
        (1..HangoutAlbumViewModel.MAX_SELECTED_PHOTOS).forEach { index ->
            viewModel.onAction(HangoutAlbumAction.OnPhotoClick("p$index"))
        }

        viewModel.onAction(HangoutAlbumAction.OnPhotoClick("p${HangoutAlbumViewModel.MAX_SELECTED_PHOTOS + 1}"))

        assertThat(viewModel.state.value.selectedPhotoIds.size).isEqualTo(HangoutAlbumViewModel.MAX_SELECTED_PHOTOS)
    }

    @Test
    fun `dragging across photos picks every photo between`() = runTest {
        val viewModel = createViewModel()

        viewModel.onAction(HangoutAlbumAction.OnDragSelectStart("p3"))
        viewModel.onAction(HangoutAlbumAction.OnDragSelectOver("p1"))

        assertThat(viewModel.state.value.isSelecting).isTrue()
        assertThat(viewModel.state.value.selectedPhotoIds).containsOnly("p3", "p2", "p1")
    }

    @Test
    fun `dragging back gives photos back what they were before the drag`() = runTest {
        val viewModel = createViewModel()
        viewModel.onAction(HangoutAlbumAction.OnPhotoLongClick("p1"))

        viewModel.onAction(HangoutAlbumAction.OnDragSelectStart("p3"))
        viewModel.onAction(HangoutAlbumAction.OnDragSelectOver("p2"))
        viewModel.onAction(HangoutAlbumAction.OnDragSelectOver("p3"))

        assertThat(viewModel.state.value.selectedPhotoIds).containsOnly("p3", "p1")
    }

    @Test
    fun `a drag that starts on a picked photo unpicks the photos it crosses`() = runTest {
        val viewModel = createViewModel()
        viewModel.onAction(HangoutAlbumAction.OnDragSelectStart("p3"))
        viewModel.onAction(HangoutAlbumAction.OnDragSelectOver("p1"))
        viewModel.onAction(HangoutAlbumAction.OnDragSelectEnd)

        viewModel.onAction(HangoutAlbumAction.OnDragSelectStart("p2"))
        viewModel.onAction(HangoutAlbumAction.OnDragSelectOver("p1"))

        assertThat(viewModel.state.value.selectedPhotoIds).containsOnly("p3")
    }

    @Test
    fun `a drag stops picking at the limit and keeps the photos nearest where it began`() = runTest {
        hangoutPhotoService.photos = (1..HangoutAlbumViewModel.MAX_SELECTED_PHOTOS + 1)
            .map { index -> photo("p$index", createdAtMillis = index * 1_000L) }
            .reversed()
            .toMutableList()
        val viewModel = createViewModel()
        val newestId = "p${HangoutAlbumViewModel.MAX_SELECTED_PHOTOS + 1}"

        viewModel.onAction(HangoutAlbumAction.OnDragSelectStart(newestId))
        viewModel.onAction(HangoutAlbumAction.OnDragSelectOver("p1"))

        val selectedPhotoIds = viewModel.state.value.selectedPhotoIds
        assertThat(selectedPhotoIds.size).isEqualTo(HangoutAlbumViewModel.MAX_SELECTED_PHOTOS)
        assertThat(newestId in selectedPhotoIds).isTrue()
        assertThat("p1" in selectedPhotoIds).isFalse()
    }

    @Test
    fun `saving every picked photo confirms it and leaves select mode`() = runTest {
        val viewModel = createViewModel()
        viewModel.onAction(HangoutAlbumAction.OnPhotoLongClick("p1"))
        viewModel.onAction(HangoutAlbumAction.OnPhotoClick("p2"))

        viewModel.events.test {
            viewModel.onAction(HangoutAlbumAction.OnSelectedPhotosSaved(savedCount = 2, photoCount = 2))
            assertThat(awaitItem()).isEqualTo(HangoutAlbumEvent.PhotoSaved)
        }
        assertThat(viewModel.state.value.isSelecting).isFalse()
    }

    @Test
    fun `saving only some still leaves select mode so a retry cannot save those twice`() = runTest {
        val viewModel = createViewModel()
        viewModel.onAction(HangoutAlbumAction.OnPhotoLongClick("p1"))
        viewModel.onAction(HangoutAlbumAction.OnPhotoClick("p2"))

        viewModel.events.test {
            viewModel.onAction(HangoutAlbumAction.OnSelectedPhotosSaved(savedCount = 1, photoCount = 2))
            assertThat(awaitItem()).isInstanceOf(HangoutAlbumEvent.Error::class)
        }
        assertThat(viewModel.state.value.isSelecting).isFalse()
    }

    @Test
    fun `saving none keeps the photos picked to try again`() = runTest {
        val viewModel = createViewModel()
        viewModel.onAction(HangoutAlbumAction.OnPhotoLongClick("p1"))

        viewModel.events.test {
            viewModel.onAction(HangoutAlbumAction.OnSelectedPhotosSaved(savedCount = 0, photoCount = 1))
            assertThat(awaitItem()).isInstanceOf(HangoutAlbumEvent.Error::class)
        }
        assertThat(viewModel.state.value.selectedPhotoIds).containsOnly("p1")
    }

    @Test
    fun `a photo removed while picked is unpicked`() = runTest {
        val viewModel = createViewModel()
        viewModel.onAction(HangoutAlbumAction.OnPhotoLongClick("p2"))

        connectionClient.sendEvent(LobbyEvent.PhotoDeleted(viewModel.state.value.hangoutId!!, "p2"))

        assertThat(viewModel.state.value.selectedPhotoIds).isEmpty()
    }

    // The real in-memory stores fed by the fake services, holding the hangout the album belongs to and its counts
    private suspend fun TestScope.createViewModel(initialPhotoId: String? = null): HangoutAlbumViewModel {
        val hangoutId = createHangout()
        val detailRepository = InMemoryHangoutDetailRepository(hangoutService, sessionStorage, backgroundScope)
        val photoStatsRepository = InMemoryHangoutPhotoStatsRepository(hangoutPhotoService, sessionStorage, backgroundScope)
        runCurrent()
        val viewModel = HangoutAlbumViewModel(
            hangoutPhotoService,
            detailRepository,
            photoStatsRepository,
            connectionClient,
            sessionStorage
        )
        collectInBackground(viewModel.state)
        viewModel.onAction(HangoutAlbumAction.OnSelectHangout(hangoutId, initialPhotoId))
        advanceUntilIdle()
        return viewModel
    }

    private suspend fun createHangout(): String {
        sessionStorage.set(
            AuthInfo(
                accessToken = "access",
                refreshToken = "refresh",
                user = User.Authenticated(
                    id = HOST_ID,
                    provider = AuthProvider.GOOGLE,
                    email = "host@test.com",
                    displayName = "Ada",
                    username = "ada"
                )
            )
        )
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

    private fun photo(
        id: String,
        createdAtMillis: Long,
        urlsExpireAtMillis: Long = FAR_FUTURE
    ) = HangoutPhoto(
        id = id,
        uploader = HangoutUser(userId = HOST_ID, username = "ada", displayName = "Ada", profilePictureUrl = null),
        caption = null,
        fullUrl = "https://storage.test/$id/full",
        thumbnailUrl = "https://storage.test/$id/thumb",
        urlsExpireAt = Instant.fromEpochMilliseconds(urlsExpireAtMillis),
        createdAt = Instant.fromEpochMilliseconds(createdAtMillis)
    )

    private companion object {
        const val HOST_ID = "host_1"
        const val FAR_FUTURE = 4102444800000L
    }
}
