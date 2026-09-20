package com.eeseka.lynk.discover.presentation

import com.eeseka.lynk.discover.presentation.model.GuestPromptContext
import com.eeseka.lynk.shared.domain.spot.model.PriceLevel
import com.eeseka.lynk.shared.domain.spot.model.SpotCategory

sealed interface DiscoverAction {
    data class ShowGuestPrompt(val context: GuestPromptContext) : DiscoverAction
    data object HideGuestPrompt : DiscoverAction
    data object SignOutGuest : DiscoverAction
    data object ToggleShowSearchSheet : DiscoverAction

    data class OnHangoutCreationSelected(val spotId: String?) : DiscoverAction

    data class OnLocationFetched(val latitude: Double, val longitude: Double) : DiscoverAction
    data object OnLocationUnavailable : DiscoverAction

    data class OnSpotSelected(val spotId: String?) : DiscoverAction

    data class OnToggleSaveSpot(val spotId: String, val isCurrentlySaved: Boolean) : DiscoverAction

    // Search Sheet Actions
    data class OnCategorySelected(val category: SpotCategory?) : DiscoverAction
    data class OnPriceLevelSelected(val priceLevel: PriceLevel?) : DiscoverAction
    data object LoadNextSearchPage : DiscoverAction

    data object RetryTrending : DiscoverAction
}