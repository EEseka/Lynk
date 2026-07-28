package com.eeseka.lynk.hangouts.presentation.hangout_detail

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Stable
import com.eeseka.lynk.hangouts.presentation.hangout_detail.model.SearchTab
import com.eeseka.lynk.shared.domain.location.LocationCoordinates
import com.eeseka.lynk.shared.domain.lobby.model.ConnectionState
import com.eeseka.lynk.shared.presentation.hangout.model.HangoutUi
import com.eeseka.lynk.shared.presentation.hangout.model.HangoutUserUi
import com.eeseka.lynk.shared.presentation.spot.model.SpotUi
import com.eeseka.lynk.shared.presentation.util.UiText

@Stable
data class HangoutDetailState(
    val hangout: HangoutUi? = null,
    val currentUserId: String? = null,
    val isLoading: Boolean = false,
    val error: UiText? = null,
    val connectionState: ConnectionState = ConnectionState.DISCONNECTED,
    val presentUserIds: Set<String> = emptySet(),
    val isCompleting: Boolean = false,
    val isCancelling: Boolean = false,
    val isLeaving: Boolean = false,
    val withdrawingUserIds: Set<String> = emptySet(),

    // Invite search sheet
    val isInviteSheetOpen: Boolean = false,
    val inviteQueryState: TextFieldState = TextFieldState(),
    val inviteResult: HangoutUserUi? = null,
    val isInviteSearching: Boolean = false,
    val inviteNotFound: Boolean = false,
    val isInviting: Boolean = false,

    // Voting
    val candidates: List<SpotUi> = emptyList(),
    val votes: Map<String, String> = emptyMap(), // userId -> spotId
    val center: LocationCoordinates? = null,
    val myLocation: LocationCoordinates? = null,
    val tiedSpotIds: List<String> = emptyList(),
    val isClosingVoting: Boolean = false,

    // Propose-spot sheet
    val isProposeSpotSheetOpen: Boolean = false,
    val activeProposeSpotSheetSearchTab: SearchTab = SearchTab.ALL_SPOTS,
    val proposeSpotSheetSearchTextState: TextFieldState = TextFieldState(),

    val trendingSpots: List<SpotUi> = emptyList(),
    val isTrendingLoading: Boolean = false,

    val spotSearchResults: List<SpotUi> = emptyList(),
    val isSpotSearchLoading: Boolean = false,
    val spotSearchError: UiText? = null,
    val spotSearchEndReached: Boolean = false,
    val spotSearchResetEpoch: Int = 0,

    val favoriteSpotSearchResults: List<SpotUi> = emptyList(),
    val isFavoriteSpotSearchLoading: Boolean = false,
    val favoriteSpotSearchError: UiText? = null,
    val favoriteSpotSearchEndReached: Boolean = false,
    val favoriteSearchResetEpoch: Int = 0,

    val proposingSpotIds: Set<String> = emptySet(),
    val removingSpotIds: Set<String> = emptySet()
)