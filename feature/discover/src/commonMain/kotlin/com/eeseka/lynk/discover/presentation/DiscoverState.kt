package com.eeseka.lynk.discover.presentation

import androidx.compose.foundation.text.input.TextFieldState
import com.eeseka.lynk.shared.domain.settings.AppTheme
import com.eeseka.lynk.shared.domain.spot.model.PriceLevel
import com.eeseka.lynk.shared.domain.spot.model.Spot
import com.eeseka.lynk.shared.domain.spot.model.SpotCategory
import com.eeseka.lynk.shared.presentation.util.UiText

data class DiscoverState(
    val mapTheme: AppTheme = AppTheme.SYSTEM,

    // User Location
    val userLatitude: Double? = null,
    val userLongitude: Double? = null,

    // Trending Data
    val trendingSpots: List<Spot> = emptyList(),
    val isTrendingLoading: Boolean = false,

    // Detail Sheet
    val selectedSpotId: String? = null,

    // Search Sheet Data (Pagination)
    val searchTextFieldState: TextFieldState = TextFieldState(),
    val searchResults: List<Spot> = emptyList(),
    val isSearchLoading: Boolean = false,
    val searchError: UiText? = null,
    val searchEndReached: Boolean = false,
    val searchResetEpoch: Int = 0,

    // Search Filters
    val selectedCategory: SpotCategory? = null,
    val selectedPriceLevel: PriceLevel? = null
)