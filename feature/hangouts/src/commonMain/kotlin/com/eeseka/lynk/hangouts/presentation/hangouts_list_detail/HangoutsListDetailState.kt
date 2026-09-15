package com.eeseka.lynk.hangouts.presentation.hangouts_list_detail

import androidx.compose.runtime.Immutable
import com.eeseka.lynk.shared.presentation.hangout.model.HangoutUi

@Immutable
data class HangoutsListDetailState(
    val selectedHangoutId: String? = null,
    val sheetState: SheetState = SheetState.Hidden
)

sealed interface SheetState {
    data object Hidden : SheetState
    data object CreateHangout : SheetState
    data class EditHangout(val hangout: HangoutUi) : SheetState
}