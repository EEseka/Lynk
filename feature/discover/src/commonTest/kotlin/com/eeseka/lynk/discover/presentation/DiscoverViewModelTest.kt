package com.eeseka.lynk.discover.presentation

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import assertk.assertions.isTrue
import com.eeseka.lynk.discover.presentation.model.GuestPromptContext
import com.eeseka.lynk.shared.domain.auth.model.AuthInfo
import com.eeseka.lynk.shared.domain.auth.model.User
import com.eeseka.lynk.shared.domain.spot.model.PriceLevel
import com.eeseka.lynk.shared.domain.spot.model.Spot
import com.eeseka.lynk.shared.domain.spot.model.SpotCategory
import com.eeseka.lynk.testing.collectInBackground
import com.eeseka.lynk.testing.data.FakeAppPreferences
import com.eeseka.lynk.testing.data.FakeAuthService
import com.eeseka.lynk.testing.data.FakeLastKnownLocationStorage
import com.eeseka.lynk.testing.data.FakeSessionStorage
import com.eeseka.lynk.testing.data.FakeSpotService
import com.eeseka.lynk.testing.typeText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class)
class DiscoverViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var spotService: FakeSpotService
    private lateinit var sessionStorage: FakeSessionStorage
    private lateinit var authService: FakeAuthService
    private lateinit var appPreferences: FakeAppPreferences
    private lateinit var lastKnownLocationStorage: FakeLastKnownLocationStorage
    private lateinit var viewModel: DiscoverViewModel

    private val dummySpot = Spot(
        id = "1", name = "Test Cafe", category = SpotCategory.CAFE,
        latitude = 0.0, longitude = 0.0, isSaved = false,
        photoUrls = emptyList(), rating = 4.5, reviewCount = 10,
        isOpenNow = true, shortAddress = "123 Main St",
        websiteUrl = null, googleMapsUrl = null, priceLevel = null,
        description = null, tags = emptyList(), savedAt = null
    )

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        spotService = FakeSpotService()
        sessionStorage = FakeSessionStorage()
        authService = FakeAuthService()
        appPreferences = FakeAppPreferences()
        lastKnownLocationStorage = FakeLastKnownLocationStorage()
        viewModel = DiscoverViewModel(
            spotService,
            sessionStorage,
            authService,
            appPreferences,
            lastKnownLocationStorage
        )
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `fetching location successfully loads trending spots`() = runTest {
        spotService.trendingSpotsList = mutableListOf(dummySpot)

        viewModel.state.test {
            awaitItem()

            viewModel.onAction(DiscoverAction.OnLocationFetched(6.5, 3.3))

            // Advance coroutines to finish API call
            advanceUntilIdle()

            val state = expectMostRecentItem()
            assertThat(state.userLatitude).isEqualTo(6.5)
            assertThat(state.trendingSpots.size).isEqualTo(1)
            assertThat(state.trendingSpots.first().id).isEqualTo("1")
            assertThat(state.isTrendingLoading).isFalse()
        }
    }

    @Test
    fun `the same fix twice only loads trending spots once`() = runTest {
        spotService.trendingSpotsList = mutableListOf(dummySpot)
        collectInBackground(viewModel.state)

        viewModel.onAction(DiscoverAction.OnLocationFetched(6.5, 3.3))
        advanceUntilIdle()
        viewModel.onAction(DiscoverAction.OnLocationFetched(6.5, 3.3))
        advanceUntilIdle()

        assertThat(spotService.trendingRequestLocations.size).isEqualTo(1)
    }

    @Test
    fun `a newer fix loads trending spots around it`() = runTest {
        spotService.trendingSpotsList = mutableListOf(dummySpot)
        collectInBackground(viewModel.state)

        // The cached fix, then the sharper one the tracker found
        viewModel.onAction(DiscoverAction.OnLocationFetched(6.5, 3.3))
        advanceUntilIdle()
        viewModel.onAction(DiscoverAction.OnLocationFetched(9.06, 7.49))
        advanceUntilIdle()

        assertThat(spotService.trendingRequestLocations.last()).isEqualTo(9.06 to 7.49)
        assertThat(viewModel.state.value.userLatitude).isEqualTo(9.06)
    }

    @Test
    fun `an unavailable location loads trending spots around the last known one`() = runTest {
        spotService.trendingSpotsList = mutableListOf(dummySpot)
        lastKnownLocationStorage.setLastKnownLocation(6.5, 3.3)

        viewModel.state.test {
            awaitItem()

            viewModel.onAction(DiscoverAction.OnLocationUnavailable)
            advanceUntilIdle()

            val state = expectMostRecentItem()
            assertThat(state.trendingSpots.size).isEqualTo(1)
            // The map still has no blue dot, because we never actually found them.
            assertThat(state.userLatitude).isNull()
        }
    }

    @Test
    fun `an unavailable location with nothing cached leaves the list empty`() = runTest {
        spotService.trendingSpotsList = mutableListOf(dummySpot)

        viewModel.onAction(DiscoverAction.OnLocationUnavailable)
        advanceUntilIdle()

        assertThat(viewModel.state.value.trendingSpots).isEmpty()
    }

    @Test
    fun `fetching location remembers it for next time`() = runTest {
        collectInBackground(viewModel.state)

        viewModel.onAction(DiscoverAction.OnLocationFetched(6.5, 3.3))
        advanceUntilIdle()

        val lastKnownLocation = lastKnownLocationStorage.lastKnownLocation.first()
        assertThat(lastKnownLocation?.latitude).isEqualTo(6.5)
        assertThat(lastKnownLocation?.longitude).isEqualTo(3.3)
    }

    @Test
    fun `trending fetch failure sets a retryable trendingError`() = runTest {
        spotService.shouldReturnError = true

        viewModel.state.test {
            awaitItem()
            viewModel.onAction(DiscoverAction.OnLocationFetched(6.5, 3.3))
            advanceUntilIdle()

            val state = expectMostRecentItem()
            assertThat(state.trendingError).isNotNull()
            assertThat(state.isTrendingLoading).isFalse()
        }
    }

    @Test
    fun `typing in search field triggers search after 500ms debounce`() = runTest {
        spotService.searchSpotsList = mutableListOf(dummySpot)

        // Setup initial location to allow searching
        viewModel.onAction(DiscoverAction.OnLocationFetched(0.0, 0.0))

        viewModel.state.test {
            val initialState = awaitItem()
            assertThat(initialState.searchResults).isEmpty()

            // Type query
            viewModel.state.value.searchTextState.typeText("Cafe")

            // Still inside the debounce timeframe, so nothing has changed and no new state was sent
            advanceTimeBy(400.milliseconds)
            assertThat(viewModel.state.value.searchResults).isEmpty()

            // Advance past debounce
            advanceTimeBy(101.milliseconds)
            advanceUntilIdle()

            val finalState = expectMostRecentItem()
            assertThat(finalState.searchResults.size).isEqualTo(1)
            assertThat(finalState.searchResults.first().id).isEqualTo("1")
            assertThat(finalState.isSearchLoading).isFalse()
        }
    }

    @Test
    fun `search works off the last known location when there is no fix`() = runTest {
        spotService.searchSpotsList = mutableListOf(dummySpot)
        lastKnownLocationStorage.setLastKnownLocation(6.5, 3.3)
        collectInBackground(viewModel.state)

        viewModel.onAction(DiscoverAction.OnLocationUnavailable)
        viewModel.state.value.searchTextState.typeText("Cafe")
        advanceUntilIdle()

        assertThat(viewModel.state.value.searchResults.size).isEqualTo(1)
        assertThat(viewModel.state.value.userLatitude).isNull()
    }

    @Test
    fun `saving a spot optimistically updates UI before API call`() = runTest {
        spotService.trendingSpotsList = mutableListOf(dummySpot)
        viewModel.onAction(DiscoverAction.OnLocationFetched(0.0, 0.0))
        advanceUntilIdle()

        viewModel.state.test {
            val initialState = awaitItem()
            assertThat(initialState.trendingSpots.first().isSaved).isFalse()

            // Action triggers optimistic update
            viewModel.onAction(DiscoverAction.OnToggleSaveSpot("1", isCurrentlySaved = false))

            // UI should update INSTANTLY, without advancing time
            val updatedState = awaitItem()
            assertThat(updatedState.trendingSpots.first().isSaved).isTrue()

            // Advance time to allow the 300ms delay and API call to finish
            advanceTimeBy(301.milliseconds)
            advanceUntilIdle()

            // Backend recorded it
            assertThat(spotService.savedSpots.contains("1")).isTrue()
        }
    }

    @Test
    fun `retrying after a trending failure loads the spots`() = runTest {
        spotService.shouldReturnError = true
        spotService.trendingSpotsList = mutableListOf(dummySpot)
        collectInBackground(viewModel.state)
        viewModel.onAction(DiscoverAction.OnLocationFetched(6.5, 3.3))
        advanceUntilIdle()
        assertThat(viewModel.state.value.trendingError).isNotNull()

        spotService.shouldReturnError = false
        viewModel.onAction(DiscoverAction.RetryTrending)
        advanceUntilIdle()

        assertThat(viewModel.state.value.trendingError).isNull()
        assertThat(viewModel.state.value.trendingSpots.size).isEqualTo(1)
    }

    @Test
    fun `choosing a price level searches with it`() = runTest {
        spotService.searchSpotsList = mutableListOf(dummySpot, dummySpot.copy(id = "2", priceLevel = PriceLevel.EXPENSIVE))
        collectInBackground(viewModel.state)
        viewModel.onAction(DiscoverAction.OnLocationFetched(6.5, 3.3))
        advanceUntilIdle()

        viewModel.onAction(DiscoverAction.OnPriceLevelSelected(PriceLevel.EXPENSIVE))
        advanceUntilIdle()

        assertThat(viewModel.state.value.searchResults.map { it.id }).containsExactly("2")
        assertThat(viewModel.state.value.searchEndReached).isTrue()
    }

    @Test
    fun `the search sheet opens and closes`() = runTest {
        collectInBackground(viewModel.state)

        viewModel.onAction(DiscoverAction.ToggleShowSearchSheet)
        assertThat(viewModel.state.value.showSearchSheet).isTrue()

        viewModel.onAction(DiscoverAction.ToggleShowSearchSheet)
        assertThat(viewModel.state.value.showSearchSheet).isFalse()
    }

    @Test
    fun `picking a spot to build a hangout around remembers it`() = runTest {
        collectInBackground(viewModel.state)

        viewModel.onAction(DiscoverAction.OnHangoutCreationSelected("1"))

        assertThat(viewModel.state.value.hangoutCreationSpotId).isEqualTo("1")
    }

    @Test
    fun `OnCategorySelected updates selectedCategory state`() = runTest {
        viewModel.state.test {
            awaitItem()
            viewModel.onAction(DiscoverAction.OnCategorySelected(SpotCategory.CAFE))
            assertThat(expectMostRecentItem().selectedCategory).isEqualTo(SpotCategory.CAFE)

            viewModel.onAction(DiscoverAction.OnCategorySelected(null))
            assertThat(expectMostRecentItem().selectedCategory).isNull()
        }
    }

    @Test
    fun `ShowGuestPrompt sets guestPromptContext`() = runTest {
        viewModel.state.test {
            awaitItem()
            viewModel.onAction(DiscoverAction.ShowGuestPrompt(GuestPromptContext.SAVE_SPOT))
            assertThat(expectMostRecentItem().guestPromptContext).isEqualTo(GuestPromptContext.SAVE_SPOT)
        }
    }

    @Test
    fun `HideGuestPrompt clears guestPromptContext`() = runTest {
        viewModel.onAction(DiscoverAction.ShowGuestPrompt(GuestPromptContext.CREATE_HANGOUT))

        viewModel.state.test {
            awaitItem()
            viewModel.onAction(DiscoverAction.HideGuestPrompt)
            assertThat(expectMostRecentItem().guestPromptContext).isNull()
        }
    }

    @Test
    fun `SignOutGuest clears session and resets isGuestSigningOut`() = runTest {
        val guestAuthInfo = AuthInfo(
            accessToken = "access",
            refreshToken = "refresh",
            user = User.Guest(id = "guest_1")
        )
        sessionStorage.set(guestAuthInfo)

        viewModel.state.test {
            awaitItem()
            viewModel.onAction(DiscoverAction.SignOutGuest)
            advanceUntilIdle()

            val state = expectMostRecentItem()
            assertThat(state.isGuestSigningOut).isFalse()
        }

        assertThat(sessionStorage.observeAuthInfo().first()).isNull()
    }
}