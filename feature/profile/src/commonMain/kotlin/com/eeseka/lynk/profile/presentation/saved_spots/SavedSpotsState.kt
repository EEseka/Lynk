package com.eeseka.lynk.profile.presentation.saved_spots

import androidx.compose.foundation.text.input.TextFieldState
import com.eeseka.lynk.shared.presentation.spot.model.SpotUi
import com.eeseka.lynk.shared.presentation.util.UiText
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class SavedSpotsState(
    val spots: ImmutableList<SpotUi> = persistentListOf(),
    val searchTextState: TextFieldState = TextFieldState(),
    val isLoading: Boolean = false,
    val isEndReached: Boolean = false,
    val searchResetEpoch: Int = 0,
    val selectedSpotId: String? = null,
    val loadError: UiText? = null
)