package com.eeseka.lynk.hangouts.presentation.hangout_detail.live_location

sealed interface HangoutLiveLocationAction {
    data class OnSelectHangout(val hangoutId: String?) : HangoutLiveLocationAction
    data class OnSharingChange(val isSharing: Boolean) : HangoutLiveLocationAction
    data class OnLocationChange(val latitude: Double, val longitude: Double) : HangoutLiveLocationAction
}
