package com.eeseka.lynk.hangouts.presentation.hangouts_list

import com.eeseka.lynk.hangouts.presentation.model.HangoutStatusFilter
import com.eeseka.lynk.shared.domain.hangout.model.HangoutVibe

sealed interface HangoutsListAction {
    data class OnSelectHangout(val hangoutId: String?) : HangoutsListAction
    data class OnStatusFilterSelected(val filter: HangoutStatusFilter) : HangoutsListAction
    data class OnVibeSelected(val vibe: HangoutVibe?) : HangoutsListAction
    data object LoadNextPage : HangoutsListAction
    data object OnNotificationPermissionDenied : HangoutsListAction
    data object Refresh : HangoutsListAction
    data object SignOutGuest : HangoutsListAction
}