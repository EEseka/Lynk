package com.eeseka.lynk.hangouts.presentation.hangouts_list_detail

sealed interface HangoutsListDetailEvent {
    // One-shot signal telling the list pane to re-fetch: a hangout was created, left, or changed live in its lobby
    data object RefreshList : HangoutsListDetailEvent
}