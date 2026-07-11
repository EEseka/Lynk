package com.eeseka.lynk.hangouts.presentation.hangouts_list

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Stable
import com.eeseka.lynk.hangouts.presentation.model.HangoutStatusFilter
import com.eeseka.lynk.shared.domain.hangout.model.HangoutVibe
import com.eeseka.lynk.shared.presentation.hangout.model.HangoutSummaryUi

@Stable
data class HangoutsListState(
    val isGuest: Boolean = false,
    val isGuestSigningOut: Boolean = false,

    val hangouts: List<HangoutSummaryUi> = emptyList(),
    val isSearchLoading: Boolean = false,
    val isSearchEndReached: Boolean = false,
    val searchResetEpoch: Int = 0,
    val selectedStatusFilter: HangoutStatusFilter = HangoutStatusFilter.UPCOMING,
    val selectedVibe: HangoutVibe? = null,
    val searchTextState: TextFieldState = TextFieldState(),

    val selectedHangoutId: String? = null
)