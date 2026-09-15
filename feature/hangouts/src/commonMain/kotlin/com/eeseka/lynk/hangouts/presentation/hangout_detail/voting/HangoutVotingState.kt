package com.eeseka.lynk.hangouts.presentation.hangout_detail.voting

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Stable
import com.eeseka.lynk.hangouts.presentation.hangout_detail.voting.model.SearchTab
import com.eeseka.lynk.shared.domain.location.LocationCoordinates
import com.eeseka.lynk.shared.presentation.spot.model.SpotUi
import com.eeseka.lynk.shared.presentation.util.UiText
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.persistentSetOf

@Stable
data class HangoutVotingState(
    val candidates: ImmutableList<SpotUi> = persistentListOf(),
    val votes: ImmutableMap<String, String> = persistentMapOf(), // userId -> spotId
    val center: LocationCoordinates? = null,
    val myLocation: LocationCoordinates? = null,
    val tiedSpotIds: ImmutableList<String> = persistentListOf(),
    val isClosingVoting: Boolean = false,

    // Propose-spot sheet
    val isProposeSpotSheetOpen: Boolean = false,
    val activeProposeSpotSheetSearchTab: SearchTab = SearchTab.ALL_SPOTS,
    val proposeSpotSheetSearchTextState: TextFieldState = TextFieldState(),

    val trendingSpots: ImmutableList<SpotUi> = persistentListOf(),
    val isTrendingLoading: Boolean = false,

    val spotSearchResults: ImmutableList<SpotUi> = persistentListOf(),
    val isSpotSearchLoading: Boolean = false,
    val spotSearchError: UiText? = null,
    val spotSearchEndReached: Boolean = false,
    val spotSearchResetEpoch: Int = 0,

    val favoriteSpotSearchResults: ImmutableList<SpotUi> = persistentListOf(),
    val isFavoriteSpotSearchLoading: Boolean = false,
    val favoriteSpotSearchError: UiText? = null,
    val favoriteSpotSearchEndReached: Boolean = false,
    val favoriteSearchResetEpoch: Int = 0,

    val proposingSpotIds: ImmutableSet<String> = persistentSetOf(),
    val removingSpotIds: ImmutableSet<String> = persistentSetOf()
)
