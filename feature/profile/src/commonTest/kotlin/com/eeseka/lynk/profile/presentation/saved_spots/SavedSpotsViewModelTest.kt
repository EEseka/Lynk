package com.eeseka.lynk.profile.presentation.saved_spots

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import assertk.assertions.isTrue
import com.eeseka.lynk.shared.domain.spot.model.Spot
import com.eeseka.lynk.shared.domain.spot.model.SpotCategory
import com.eeseka.lynk.testing.collectInBackground
import com.eeseka.lynk.testing.data.FakeSpotService
import com.eeseka.lynk.testing.typeText
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
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class SavedSpotsViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var spotService: FakeSpotService
    private lateinit var viewModel: SavedSpotsViewModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        spotService = FakeSpotService()
        spotService.savedSpotsList = mutableListOf(
            savedSpot(id = "spot_1", name = "The Lounge"),
            savedSpot(id = "spot_2", name = "Rooftop Bar")
        )
        spotService.savedSpots = mutableSetOf("spot_1", "spot_2")
        viewModel = SavedSpotsViewModel(spotService)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `opening saved spots loads them`() = runTest {
        collectInBackground(viewModel.state)
        advanceUntilIdle()

        assertThat(viewModel.state.value.spots.map { it.id }).containsExactly("spot_1", "spot_2")
        assertThat(viewModel.state.value.isLoading).isFalse()
    }

    @Test
    fun `a page with nothing saved earlier marks the end`() = runTest {
        collectInBackground(viewModel.state)
        advanceUntilIdle()

        viewModel.onAction(SavedSpotsAction.LoadNextPage)
        advanceUntilIdle()

        assertThat(viewModel.state.value.isEndReached).isTrue()
        assertThat(viewModel.state.value.spots.size).isEqualTo(2)
    }

    @Test
    fun `searching narrows the saved spots after the debounce`() = runTest {
        collectInBackground(viewModel.state)
        advanceUntilIdle()

        viewModel.state.value.searchTextState.typeText("roof")
        advanceTimeBy(400.milliseconds)
        assertThat(viewModel.state.value.spots.size).isEqualTo(2)

        advanceTimeBy(101.milliseconds)
        advanceUntilIdle()
        assertThat(viewModel.state.value.spots.map { it.id }).containsExactly("spot_2")
    }

    @Test
    fun `a failed first load shows an error state and retry loads the spots`() = runTest {
        spotService.shouldReturnError = true
        collectInBackground(viewModel.state)
        advanceUntilIdle()
        assertThat(viewModel.state.value.loadError).isNotNull()

        spotService.shouldReturnError = false
        viewModel.onAction(SavedSpotsAction.OnRetryClick)
        advanceUntilIdle()

        assertThat(viewModel.state.value.loadError).isNull()
        assertThat(viewModel.state.value.spots.size).isEqualTo(2)
    }

    @Test
    fun `a failed later page sends a message and keeps the spots`() = runTest {
        collectInBackground(viewModel.state)
        advanceUntilIdle()

        viewModel.events.test {
            spotService.shouldReturnError = true
            viewModel.onAction(SavedSpotsAction.LoadNextPage)
            advanceUntilIdle()

            assertThat(awaitItem()).isInstanceOf(SavedSpotsEvent.Error::class)
            assertThat(viewModel.state.value.spots.size).isEqualTo(2)
        }
    }

    @Test
    fun `unsaving a spot shows at once and reaches the server after a short wait`() = runTest {
        collectInBackground(viewModel.state)
        advanceUntilIdle()

        viewModel.onAction(SavedSpotsAction.OnToggleSaveSpot("spot_1", isCurrentlySaved = true))
        assertThat(viewModel.state.value.spots.first { it.id == "spot_1" }.isSaved).isFalse()
        assertThat(spotService.savedSpots.contains("spot_1")).isTrue()

        advanceTimeBy(301.milliseconds)
        advanceUntilIdle()

        assertThat(spotService.savedSpots.contains("spot_1")).isFalse()
    }

    @Test
    fun `an unsave the server refuses is rolled back with a message`() = runTest {
        collectInBackground(viewModel.state)
        advanceUntilIdle()
        spotService.shouldReturnError = true

        viewModel.events.test {
            viewModel.onAction(SavedSpotsAction.OnToggleSaveSpot("spot_1", isCurrentlySaved = true))
            advanceTimeBy(301.milliseconds)
            advanceUntilIdle()

            assertThat(awaitItem()).isInstanceOf(SavedSpotsEvent.Error::class)
            assertThat(viewModel.state.value.spots.first { it.id == "spot_1" }.isSaved).isTrue()
        }
    }

    @Test
    fun `tapping again before the wait is over cancels the first tap`() = runTest {
        collectInBackground(viewModel.state)
        advanceUntilIdle()

        viewModel.onAction(SavedSpotsAction.OnToggleSaveSpot("spot_1", isCurrentlySaved = true))
        advanceTimeBy(200.milliseconds)
        viewModel.onAction(SavedSpotsAction.OnToggleSaveSpot("spot_1", isCurrentlySaved = false))

        // The first tap would have unsaved the spot at 300ms; it was canceled, so the spot is still saved
        advanceTimeBy(150.milliseconds)
        assertThat(spotService.savedSpots.contains("spot_1")).isTrue()

        advanceTimeBy(200.milliseconds)
        advanceUntilIdle()
        assertThat(spotService.savedSpots.contains("spot_1")).isTrue()
        assertThat(viewModel.state.value.spots.first { it.id == "spot_1" }.isSaved).isTrue()
    }

    @Test
    fun `selecting a spot opens its detail and dismissing closes it`() = runTest {
        collectInBackground(viewModel.state)

        viewModel.onAction(SavedSpotsAction.OnSpotSelected("spot_1"))
        assertThat(viewModel.state.value.selectedSpotId).isEqualTo("spot_1")

        viewModel.onAction(SavedSpotsAction.OnDismissSpotDetail)
        assertThat(viewModel.state.value.selectedSpotId).isNull()
    }

    private fun savedSpot(id: String, name: String) = Spot(
        id = id, name = name, category = SpotCategory.CAFE,
        latitude = 6.5, longitude = 3.3, isSaved = true,
        photoUrls = emptyList(), rating = 4.5, reviewCount = 100,
        isOpenNow = true, shortAddress = "VI, Lagos",
        websiteUrl = null, googleMapsUrl = null, priceLevel = null,
        description = null, tags = emptyList(),
        savedAt = Instant.fromEpochMilliseconds(4102444800000L)
    )
}
