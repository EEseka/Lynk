package com.eeseka.lynk.discover.presentation

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Stable
import com.eeseka.lynk.shared.domain.settings.AppTheme
import com.eeseka.lynk.shared.domain.spot.model.PriceLevel
import com.eeseka.lynk.shared.domain.spot.model.SpotCategory
import com.eeseka.lynk.shared.presentation.spot.model.SpotUi
import com.eeseka.lynk.shared.presentation.util.UiText

enum class GuestPromptContext { SAVE_SPOT, CREATE_HANGOUT }

@Stable
data class DiscoverState(
    val mapTheme: AppTheme = AppTheme.SYSTEM,

    val isGuest: Boolean = false,
    val guestPromptContext: GuestPromptContext? = null,

    // User Location
    val userLatitude: Double? = null,
    val userLongitude: Double? = null,

    // Trending Data
    val trendingSpots: List<SpotUi> = emptyList(),
    val isTrendingLoading: Boolean = false,

    // Detail Sheet
    val selectedSpotId: String? = null,

    // Search Sheet Data (Pagination)
    val searchTextState: TextFieldState = TextFieldState(),
    val searchResults: List<SpotUi> = emptyList(),
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