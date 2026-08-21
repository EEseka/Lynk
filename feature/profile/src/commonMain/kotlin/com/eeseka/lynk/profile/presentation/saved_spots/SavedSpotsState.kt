package com.eeseka.lynk.profile.presentation.saved_spots

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Stable
import com.eeseka.lynk.shared.presentation.spot.model.SpotUi

@Stable
data class SavedSpotsState(
    val spots: List<SpotUi> = emptyList(),
    val searchTextState: TextFieldState = TextFieldState(),
    val isLoading: Boolean = false,
    val isEndReached: Boolean = false,
    val searchResetEpoch: Int = 0,
    val selectedSpotId: String? = null
)