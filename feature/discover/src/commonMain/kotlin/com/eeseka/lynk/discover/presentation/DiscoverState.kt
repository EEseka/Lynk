package com.eeseka.lynk.discover.presentation

import androidx.compose.foundation.text.input.TextFieldState
import com.eeseka.lynk.discover.presentation.model.GuestPromptContext
import com.eeseka.lynk.shared.domain.settings.AppTheme
import com.eeseka.lynk.shared.domain.spot.model.PriceLevel
import com.eeseka.lynk.shared.domain.spot.model.SpotCategory
import com.eeseka.lynk.shared.presentation.spot.model.SpotUi
import com.eeseka.lynk.shared.presentation.util.UiText
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class DiscoverState(
    val mapTheme: AppTheme = AppTheme.SYSTEM,

    val isGuest: Boolean = false,
    val guestPromptContext: GuestPromptContext? = null,
    val isGuestSigningOut: Boolean = false,

    // User Location
    val userLatitude: Double? = null,
    val userLongitude: Double? = null,
    // Bumped on every fix so the blue dot can replay its pulse.
    val locationFetchEpoch: Int = 0,

    // Trending Data
    val trendingSpots: ImmutableList<SpotUi> = persistentListOf(),
    val isTrendingLoading: Boolean = false,
    val trendingError: UiText? = null,

    // Detail Sheet
    val selectedSpotId: String? = null,

    // Search Sheet Data (Pagination)
    val searchTextState: TextFieldState = TextFieldState(),
    val searchResults: ImmutableList<SpotUi> = persistentListOf(),
    val isSearchLoading: Boolean = false,
    val searchError: UiText? = null,
    val searchEndReached: Boolean = false,
    val searchResetEpoch: Int = 0,

    // Search Filters
    val selectedCategory: SpotCategory? = null,
    val selectedPriceLevel: PriceLevel? = null,

    // Sheet States
    val showSearchSheet: Boolean = false,
    val hangoutCreationSpotId: String? = null
)