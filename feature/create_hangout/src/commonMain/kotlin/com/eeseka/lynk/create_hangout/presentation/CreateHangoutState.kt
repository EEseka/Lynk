package com.eeseka.lynk.create_hangout.presentation

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Stable
import com.eeseka.lynk.create_hangout.presentation.model.HangoutFormMode
import com.eeseka.lynk.create_hangout.presentation.model.SearchTab
import com.eeseka.lynk.shared.domain.hangout.model.HangoutVibe
import com.eeseka.lynk.shared.presentation.hangout.model.HangoutUi
import com.eeseka.lynk.shared.presentation.spot.model.SpotUi
import com.eeseka.lynk.shared.presentation.util.UiText
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

@Stable
data class CreateHangoutState(
    val mode: HangoutFormMode = HangoutFormMode.CREATE,
    val originalHangout: HangoutUi? = null,

    val currentStep: Int = 1,
    val activeSearchTab: SearchTab = SearchTab.ALL_SPOTS,

    val hangoutNameTextState: TextFieldState = TextFieldState(),
    val hangoutNameError: UiText? = null,

    val hangoutDescriptionTextState: TextFieldState = TextFieldState(),
    val hangoutDescriptionError: UiText? = null,

    val hangoutVibe: HangoutVibe = HangoutVibe.CHILL,

    val hangoutDate: LocalDate? = null,
    val hangoutDateError: UiText? = null,

    val hangoutTime: LocalTime? = null,
    val hangoutTimeError: UiText? = null,

    val maxAttendees: Int? = null,
    val minAttendees: Int = 2,

    val canProceedToStepTwo: Boolean = false,

    val isVotingMode: Boolean = true,
    val selectedSpot: SpotUi? = null,

    val userLatitude: Double? = null,
    val userLongitude: Double? = null,

    val spotSearchTextState: TextFieldState = TextFieldState(),

    val trendingSpots: List<SpotUi> = emptyList(),
    val isTrendingLoading: Boolean = false,

    val favoriteSpotSearchResults: List<SpotUi> = emptyList(),
    val isFavoriteSpotSearchLoading: Boolean = false,
    val favoriteSpotSearchError: UiText? = null,
    val favoriteSpotSearchEndReached: Boolean = false,
    val favoriteSearchResetEpoch: Int = 0,

    val spotSearchResults: List<SpotUi> = emptyList(),
    val isSpotSearchLoading: Boolean = false,
    val spotSearchError: UiText? = null,
    val spotSearchEndReached: Boolean = false,
    val spotSearchResetEpoch: Int = 0,

    val canProceedToStepThree: Boolean = false,

    val isSubmitting: Boolean = false,
    val submitError: UiText? = null, // I am not using snackbar here, so I need this state to display the form error if it fails.
    val canSubmit: Boolean = false
)