package com.eeseka.lynk.hangouts.presentation.hangouts_list_detail

sealed interface HangoutsListDetailAction {
    data class OnSelectHangout(val hangoutId: String?): HangoutsListDetailAction
    data object OnCreateHangoutClick: HangoutsListDetailAction
    data object OnDismissCurrentSheet: HangoutsListDetailAction
    data object RefreshList: HangoutsListDetailAction
}