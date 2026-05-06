package com.eeseka.lynk.discover.presentation

import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isInstanceOf
import assertk.assertions.isTrue
import com.eeseka.lynk.discover.data.FakeAppPreferences
import com.eeseka.lynk.discover.data.FakeSpotService
import com.eeseka.lynk.shared.domain.spot.model.Spot
import com.eeseka.lynk.shared.domain.spot.model.SpotCategory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
    private lateinit var appPreferences: FakeAppPreferences
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
        appPreferences = FakeAppPreferences()
        viewModel = DiscoverViewModel(spotService, appPreferences)
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
            assertThat(state.trendingSpots).isEqualTo(listOf(dummySpot))
            assertThat(state.isTrendingLoading).isFalse()
        }
    }

    @Test
    fun `location fetch error emits error event`() = runTest {
        spotService.shouldReturnError = true

        viewModel.events.test {
            viewModel.onAction(DiscoverAction.OnLocationFetched(6.5, 3.3))
            advanceUntilIdle()

            val event = awaitItem()
            assertThat(event).isInstanceOf(DiscoverEvent.Error::class)
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
            viewModel.state.value.searchTextFieldState.setTextAndPlaceCursorAtEnd("Cafe")

            // Advance time just before debounce finishes
            advanceTimeBy(400.milliseconds)
            assertThat(expectMostRecentItem().searchResults).isEmpty()

            // Advance past debounce
            advanceTimeBy(101.milliseconds)
            advanceUntilIdle()

            val finalState = expectMostRecentItem()
            assertThat(finalState.searchResults).isEqualTo(listOf(dummySpot))
            assertThat(finalState.isSearchLoading).isFalse()
        }
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
}