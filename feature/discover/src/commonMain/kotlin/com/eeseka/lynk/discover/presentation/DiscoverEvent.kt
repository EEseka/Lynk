package com.eeseka.lynk.discover.presentation

import com.eeseka.lynk.shared.presentation.util.UiText

sealed interface DiscoverEvent {
    data class Error(val error: UiText) : DiscoverEvent
    data object NoTrendingSpotsInArea : DiscoverEvent
    data object NoSearchResults : DiscoverEvent
    data object NoTopSpotsInArea : DiscoverEvent
}