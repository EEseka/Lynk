package com.eeseka.lynk.hangouts.presentation.hangouts_list_detail

sealed interface HangoutsListDetailEvent {
    // One-shot signal telling the list pane to re-fetch (e.g. after a hangout is created or updated)
    data object RefreshList : HangoutsListDetailEvent
}