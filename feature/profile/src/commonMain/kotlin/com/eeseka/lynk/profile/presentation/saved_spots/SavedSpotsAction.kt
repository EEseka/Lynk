package com.eeseka.lynk.profile.presentation.saved_spots

sealed interface SavedSpotsAction {
    data class OnSpotSelected(val spotId: String) : SavedSpotsAction
    data object OnDismissSpotDetail : SavedSpotsAction
    data class OnToggleSaveSpot(val spotId: String, val isCurrentlySaved: Boolean) : SavedSpotsAction
    data object LoadNextPage : SavedSpotsAction
}
