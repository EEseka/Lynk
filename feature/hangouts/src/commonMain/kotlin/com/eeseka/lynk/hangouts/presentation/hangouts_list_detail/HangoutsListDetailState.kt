package com.eeseka.lynk.hangouts.presentation.hangouts_list_detail

import androidx.compose.runtime.Stable

@Stable
data class HangoutsListDetailState(
    val selectedHangoutId: String? = null,
    val sheetState: SheetState = SheetState.Hidden
)

sealed interface SheetState {
    data object Hidden : SheetState
    data object CreateHangout : SheetState
}