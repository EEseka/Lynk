package com.eeseka.lynk.hangouts.presentation.hangouts_list_detail

import com.eeseka.lynk.hangouts.presentation.model.HangoutStatusFilter

sealed interface HangoutsListDetailEvent {
    // One-shot signal telling the list pane to re-fetch: a hangout was created, left, or changed live in its lobby
    data object RefreshList : HangoutsListDetailEvent

    // A hangout opened from outside the list: the list moves to the tab it is on
    data class ShowHangoutInList(val statusFilter: HangoutStatusFilter) : HangoutsListDetailEvent
}
