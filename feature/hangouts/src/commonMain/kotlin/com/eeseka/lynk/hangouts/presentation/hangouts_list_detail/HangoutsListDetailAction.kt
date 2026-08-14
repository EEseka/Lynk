package com.eeseka.lynk.hangouts.presentation.hangouts_list_detail

import com.eeseka.lynk.shared.presentation.hangout.model.HangoutUi

sealed interface HangoutsListDetailAction {
    data class OnSelectHangout(val hangoutId: String?) : HangoutsListDetailAction
    data object OnCreateHangoutClick : HangoutsListDetailAction
    data class OnEditHangoutClick(val hangout: HangoutUi) : HangoutsListDetailAction
    data object OnDismissCurrentSheet : HangoutsListDetailAction
    data object RefreshList : HangoutsListDetailAction
}