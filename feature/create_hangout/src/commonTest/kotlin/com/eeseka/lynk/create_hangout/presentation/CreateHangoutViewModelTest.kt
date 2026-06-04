package com.eeseka.lynk.create_hangout.presentation

import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import assertk.assertions.isTrue
import com.eeseka.lynk.create_hangout.data.FakeHangoutService
import com.eeseka.lynk.create_hangout.data.FakeSpotService
import com.eeseka.lynk.create_hangout.domain.SearchTab
import com.eeseka.lynk.shared.domain.spot.model.Spot
import com.eeseka.lynk.shared.domain.spot.model.SpotCategory
import com.eeseka.lynk.shared.domain.hangout.model.HangoutStatus
import com.eeseka.lynk.shared.domain.hangout.model.HangoutVibe
import com.eeseka.lynk.shared.presentation.hangout.model.HangoutUi
import com.eeseka.lynk.shared.presentation.spot.model.SpotUi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class CreateHangoutViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var hangoutService: FakeHangoutService
    private lateinit var spotService: FakeSpotService
    private lateinit var viewModel: CreateHangoutViewModel

    private val futureDate = LocalDate(2030, 12, 31)
    private val futureTime = LocalTime(18, 0)
    private val futureInstant = Instant.fromEpochMilliseconds(4102444800000L) // 2100-01-01

    private val dummySpot = Spot(
        id = "spot_1", name = "The Lounge", category = SpotCategory.CAFE,
        latitude = 6.5, longitude = 3.3, isSaved = false,
        photoUrls = emptyList(), rating = 4.5, reviewCount = 100,
        isOpenNow = true, shortAddress = "VI, Lagos",
        websiteUrl = null, googleMapsUrl = null, priceLevel = null,
        description = null, tags = emptyList(), savedAt = null
    )

    private val dummySpotUi = SpotUi(
        id = "spot_1", name = "The Lounge", category = SpotCategory.CAFE,
        latitude = 6.5, longitude = 3.3, isSaved = false,
        photoUrls = emptyList(), rating = 4.5, reviewCount = 100,
        isOpenNow = true, shortAddress = "VI, Lagos",
        websiteUrl = null, googleMapsUrl = null, priceLevel = null,
        description = null, tags = emptyList()
    )

    private val dummyHangoutUi = HangoutUi(
        id = "hangout_1", hostId = "host_1",
        name = "Existing Hangout", description = "Fun times",
        vibe = HangoutVibe.FOOD, status = HangoutStatus.SCHEDULED,
        scheduledAt = futureInstant, maxAttendees = 10,
        participantCount = 3, chosenSpot = dummySpotUi,
        totalCost = null, costPerPerson = null,
        participants = emptyList(), createdAt = futureInstant
    )

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        hangoutService = FakeHangoutService()
        spotService = FakeSpotService()
        viewModel = CreateHangoutViewModel(hangoutService, spotService)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun setValidStepOneInputs() {
        viewModel.state.value.hangoutNameTextState.setTextAndPlaceCursorAtEnd("Hangout Night")
        viewModel.onAction(CreateHangoutAction.OnDateSelected(futureDate))
        viewModel.onAction(CreateHangoutAction.OnTimeSelected(futureTime))
    }


    @Test
    fun `OnLocationFetched updates coordinates and loads trending spots`() = runTest {
        spotService.trendingSpotsList = mutableListOf(dummySpot)

        viewModel.state.test {
            awaitItem()
            viewModel.onAction(CreateHangoutAction.OnLocationFetched(6.5, 3.3))
            advanceUntilIdle()

            val state = expectMostRecentItem()
            assertThat(state.userLatitude).isEqualTo(6.5)
            assertThat(state.userLongitude).isEqualTo(3.3)
            assertThat(state.trendingSpots.size).isEqualTo(1)
            assertThat(state.isTrendingLoading).isFalse()
        }
    }

    @Test
    fun `trending fetch failure is silent - loading clears and no event emitted`() = runTest {
        spotService.shouldReturnError = true

        viewModel.events.test {
            viewModel.onAction(CreateHangoutAction.OnLocationFetched(6.5, 3.3))
            advanceUntilIdle()

            expectNoEvents()
        }

        assertThat(viewModel.state.value.isTrendingLoading).isFalse()
        assertThat(viewModel.state.value.trendingSpots).isEqualTo(emptyList())
    }

    @Test
    fun `second OnLocationFetched does not re-fetch if trending already loaded`() = runTest {
        spotService.trendingSpotsList = mutableListOf(dummySpot)
        viewModel.onAction(CreateHangoutAction.OnLocationFetched(6.5, 3.3))
        advanceUntilIdle()

        spotService.trendingSpotsList = mutableListOf(dummySpot, dummySpot.copy(id = "spot_2"))
        viewModel.onAction(CreateHangoutAction.OnLocationFetched(7.0, 4.0))
        advanceUntilIdle()

        assertThat(viewModel.state.value.trendingSpots.size).isEqualTo(1)
    }


    @Test
    fun `all step 1 fields valid enables canProceedToStepTwo`() = runTest {
        viewModel.state.test {
            awaitItem()
            setValidStepOneInputs()
            advanceUntilIdle()

            assertThat(expectMostRecentItem().canProceedToStepTwo).isTrue()
        }
    }

    @Test
    fun `blank name blocks canProceedToStepTwo`() = runTest {
        viewModel.state.test {
            awaitItem()
            viewModel.onAction(CreateHangoutAction.OnDateSelected(futureDate))
            viewModel.onAction(CreateHangoutAction.OnTimeSelected(futureTime))
            advanceUntilIdle()

            assertThat(expectMostRecentItem().canProceedToStepTwo).isFalse()
        }
    }

    @Test
    fun `missing date blocks canProceedToStepTwo`() = runTest {
        viewModel.state.test {
            awaitItem()
            viewModel.state.value.hangoutNameTextState.setTextAndPlaceCursorAtEnd("Night Out")
            viewModel.onAction(CreateHangoutAction.OnTimeSelected(futureTime))
            advanceUntilIdle()

            assertThat(expectMostRecentItem().canProceedToStepTwo).isFalse()
        }
    }

    @Test
    fun `missing time blocks canProceedToStepTwo`() = runTest {
        viewModel.state.test {
            awaitItem()
            viewModel.state.value.hangoutNameTextState.setTextAndPlaceCursorAtEnd("Night Out")
            viewModel.onAction(CreateHangoutAction.OnDateSelected(futureDate))
            // No time set
            advanceUntilIdle()

            assertThat(expectMostRecentItem().canProceedToStepTwo).isFalse()
        }
    }


    @Test
    fun `OnNextStep with past date surfaces hangoutDateError and stays on step 1`() = runTest {
        viewModel.state.test {
            awaitItem()
            viewModel.state.value.hangoutNameTextState.setTextAndPlaceCursorAtEnd("Night Out")
            viewModel.onAction(CreateHangoutAction.OnDateSelected(LocalDate(2020, 1, 1)))
            viewModel.onAction(CreateHangoutAction.OnTimeSelected(futureTime))
            advanceUntilIdle()

            viewModel.onAction(CreateHangoutAction.OnNextStep)
            advanceUntilIdle()

            val state = expectMostRecentItem()
            assertThat(state.hangoutDateError).isNotNull()
            assertThat(state.currentStep).isEqualTo(1)
        }
    }

    @Test
    fun `OnNextStep with too long description surfaces hangoutDescriptionError and stays on step 1`() = runTest {
        viewModel.state.test {
            awaitItem()
            viewModel.state.value.hangoutNameTextState.setTextAndPlaceCursorAtEnd("Night Out")
            viewModel.state.value.hangoutDescriptionTextState.setTextAndPlaceCursorAtEnd("a".repeat(501))
            viewModel.onAction(CreateHangoutAction.OnDateSelected(futureDate))
            viewModel.onAction(CreateHangoutAction.OnTimeSelected(futureTime))
            advanceUntilIdle()

            viewModel.onAction(CreateHangoutAction.OnNextStep)
            advanceUntilIdle()

            val state = expectMostRecentItem()
            assertThat(state.hangoutDescriptionError).isNotNull()
            assertThat(state.currentStep).isEqualTo(1)
        }
    }

    @Test
    fun `OnNextStep with valid step 1 inputs advances to step 2`() = runTest {
        viewModel.state.test {
            awaitItem()
            setValidStepOneInputs()
            advanceUntilIdle()

            viewModel.onAction(CreateHangoutAction.OnNextStep)
            advanceUntilIdle()

            assertThat(expectMostRecentItem().currentStep).isEqualTo(2)
        }
    }


    @Test
    fun `voting mode always allows canProceedToStepThree`() = runTest {
        viewModel.state.test {
            awaitItem()
            viewModel.onAction(CreateHangoutAction.OnLocationModeChanged(isVotingMode = true))
            advanceUntilIdle()

            assertThat(expectMostRecentItem().canProceedToStepThree).isTrue()
        }
    }

    @Test
    fun `non-voting mode with no spot blocks canProceedToStepThree`() = runTest {
        viewModel.state.test {
            awaitItem()
            viewModel.onAction(CreateHangoutAction.OnLocationModeChanged(isVotingMode = false))
            advanceUntilIdle()

            assertThat(expectMostRecentItem().canProceedToStepThree).isFalse()
        }
    }

    @Test
    fun `selecting a spot in non-voting mode enables canProceedToStepThree`() = runTest {
        viewModel.state.test {
            awaitItem()
            viewModel.onAction(CreateHangoutAction.OnLocationModeChanged(isVotingMode = false))
            viewModel.onAction(CreateHangoutAction.OnSpotSelected(dummySpotUi))
            advanceUntilIdle()

            val state = expectMostRecentItem()
            assertThat(state.selectedSpot).isEqualTo(dummySpotUi)
            assertThat(state.canProceedToStepThree).isTrue()
        }
    }


    @Test
    fun `search query after debounce populates spotSearchResults`() = runTest {
        spotService.searchSpotsList = mutableListOf(dummySpot)
        viewModel.onAction(CreateHangoutAction.OnLocationFetched(6.5, 3.3))

        viewModel.state.test {
            awaitItem()
            viewModel.state.value.spotSearchTextState.setTextAndPlaceCursorAtEnd("Lounge")

            advanceTimeBy(400.milliseconds)
            assertThat(expectMostRecentItem().spotSearchResults).isEqualTo(emptyList())

            advanceTimeBy(101.milliseconds)
            advanceUntilIdle()

            assertThat(expectMostRecentItem().spotSearchResults.size).isEqualTo(1)
        }
    }

    @Test
    fun `clearing search query clears spotSearchResults`() = runTest {
        spotService.searchSpotsList = mutableListOf(dummySpot)
        viewModel.onAction(CreateHangoutAction.OnLocationFetched(6.5, 3.3))

        viewModel.state.value.spotSearchTextState.setTextAndPlaceCursorAtEnd("Lounge")
        advanceTimeBy(501.milliseconds)
        advanceUntilIdle()

        viewModel.onAction(CreateHangoutAction.OnSearchQueryCleared)
        advanceTimeBy(501.milliseconds)
        advanceUntilIdle()

        assertThat(viewModel.state.value.spotSearchResults).isEqualTo(emptyList())
    }

    @Test
    fun `switching to FAVORITES tab immediately loads all saved spots`() = runTest {
        spotService.savedSpotsList = mutableListOf(dummySpot)

        viewModel.state.test {
            awaitItem()
            viewModel.onAction(CreateHangoutAction.OnSearchTabSelected(SearchTab.FAVORITES))
            advanceUntilIdle()

            val state = expectMostRecentItem()
            assertThat(state.favoriteSpotSearchResults.size).isEqualTo(1)
            assertThat(state.isFavoriteSpotSearchLoading).isFalse()
        }
    }


    @Test
    fun `successful create emits Success event with hangout id`() = runTest {
        setValidStepOneInputs()
        viewModel.onAction(CreateHangoutAction.OnLocationModeChanged(isVotingMode = true))
        advanceUntilIdle()

        viewModel.events.test {
            viewModel.onAction(CreateHangoutAction.OnSubmitClick)
            advanceUntilIdle()

            val event = awaitItem()
            assertThat(event).isInstanceOf(CreateHangoutEvent.Success::class)
        }
    }

    @Test
    fun `failed create sets submitError and clears isSubmitting`() = runTest {
        hangoutService.shouldReturnError = true
        setValidStepOneInputs()
        viewModel.onAction(CreateHangoutAction.OnLocationModeChanged(isVotingMode = true))
        advanceUntilIdle()

        viewModel.state.test {
            awaitItem()
            viewModel.onAction(CreateHangoutAction.OnSubmitClick)
            advanceUntilIdle()

            val state = expectMostRecentItem()
            assertThat(state.submitError).isNotNull()
            assertThat(state.isSubmitting).isFalse()
        }
    }

    @Test
    fun `create with voting mode passes null spotId to service`() = runTest {
        setValidStepOneInputs()
        viewModel.onAction(CreateHangoutAction.OnLocationModeChanged(isVotingMode = true))
        advanceUntilIdle()

        viewModel.onAction(CreateHangoutAction.OnSubmitClick)
        advanceUntilIdle()

        assertThat(hangoutService.lastCreatedSpotId).isNull()
    }

    @Test
    fun `create with selected spot passes spotId to service`() = runTest {
        setValidStepOneInputs()
        viewModel.onAction(CreateHangoutAction.OnLocationModeChanged(isVotingMode = false))
        viewModel.onAction(CreateHangoutAction.OnSpotSelected(dummySpotUi))
        advanceUntilIdle()

        viewModel.onAction(CreateHangoutAction.OnSubmitClick)
        advanceUntilIdle()

        assertThat(hangoutService.lastCreatedSpotId).isEqualTo("spot_1")
    }


    @Test
    fun `InitCreateMode with spot sets isVotingMode false and selectedSpot`() = runTest {
        viewModel.state.test {
            awaitItem()
            viewModel.onAction(CreateHangoutAction.InitCreateMode(dummySpotUi))
            advanceUntilIdle()

            val state = expectMostRecentItem()
            assertThat(state.isVotingMode).isFalse()
            assertThat(state.selectedSpot).isEqualTo(dummySpotUi)
        }
    }

    @Test
    fun `InitCreateMode without spot sets isVotingMode true`() = runTest {
        viewModel.state.test {
            awaitItem()
            viewModel.onAction(CreateHangoutAction.InitCreateMode(null))
            advanceUntilIdle()

            val state = expectMostRecentItem()
            assertThat(state.isVotingMode).isTrue()
            assertThat(state.selectedSpot).isNull()
        }
    }

    @Test
    fun `InitEditMode pre-fills all form fields from existing hangout`() = runTest {
        viewModel.state.test {
            awaitItem()
            viewModel.onAction(CreateHangoutAction.InitEditMode(dummyHangoutUi))
            advanceUntilIdle()

            val state = expectMostRecentItem()
            assertThat(state.hangoutNameTextState.text.toString()).isEqualTo("Existing Hangout")
            assertThat(state.hangoutDescriptionTextState.text.toString()).isEqualTo("Fun times")
            assertThat(state.hangoutVibe).isEqualTo(HangoutVibe.FOOD)
            assertThat(state.maxAttendees).isEqualTo(10)
            assertThat(state.selectedSpot).isEqualTo(dummySpotUi)
            assertThat(state.isVotingMode).isFalse()
            assertThat(state.hangoutDate).isNotNull()
            assertThat(state.hangoutTime).isNotNull()
        }
    }


    @Test
    fun `IncrementAttendees from null sets to 2`() = runTest {
        viewModel.state.test {
            awaitItem()
            viewModel.onAction(CreateHangoutAction.IncrementAttendees)
            val state = expectMostRecentItem()
            assertThat(state.maxAttendees).isEqualTo(2)
        }
    }

    @Test
    fun `IncrementAttendees at max 50 does not exceed limit`() = runTest {
        repeat(49) { viewModel.onAction(CreateHangoutAction.IncrementAttendees) }
        viewModel.onAction(CreateHangoutAction.IncrementAttendees)
        advanceUntilIdle()

        assertThat(viewModel.state.value.maxAttendees).isEqualTo(50)
    }

    @Test
    fun `DecrementAttendees from 2 sets to null`() = runTest {
        viewModel.onAction(CreateHangoutAction.IncrementAttendees) // null → 2
        viewModel.state.test {
            awaitItem()
            viewModel.onAction(CreateHangoutAction.DecrementAttendees)
            val state = expectMostRecentItem()
            assertThat(state.maxAttendees).isNull()
        }
    }

    @Test
    fun `DecrementAttendees when null does nothing`() = runTest {
        assertThat(viewModel.state.value.maxAttendees).isNull()
        viewModel.onAction(CreateHangoutAction.DecrementAttendees)
        advanceUntilIdle()
        assertThat(viewModel.state.value.maxAttendees).isNull()
    }

    @Test
    fun `OnPreviousStep from step 2 returns to step 1`() = runTest {
        setValidStepOneInputs()
        viewModel.onAction(CreateHangoutAction.OnNextStep)
        advanceUntilIdle()

        viewModel.state.test {
            awaitItem()
            viewModel.onAction(CreateHangoutAction.OnPreviousStep)
            assertThat(expectMostRecentItem().currentStep).isEqualTo(1)
        }
    }

    @Test
    fun `OnPreviousStep on step 1 stays at step 1`() = runTest {
        viewModel.state.test {
            awaitItem()
            viewModel.onAction(CreateHangoutAction.OnPreviousStep)
            assertThat(expectMostRecentItem().currentStep).isEqualTo(1)
        }
    }
}