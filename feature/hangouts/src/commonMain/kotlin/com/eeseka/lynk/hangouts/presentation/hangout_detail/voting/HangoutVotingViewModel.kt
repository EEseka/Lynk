package com.eeseka.lynk.hangouts.presentation.hangout_detail.voting

import androidx.compose.foundation.text.input.clearText
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eeseka.lynk.hangouts.presentation.hangout_detail.voting.model.SearchTab
import com.eeseka.lynk.shared.domain.auth.SessionStorage
import com.eeseka.lynk.shared.domain.hangout.HangoutDetailRepository
import com.eeseka.lynk.shared.domain.hangout.model.HangoutStatus
import com.eeseka.lynk.shared.domain.lobby.LobbyConnectionClient
import com.eeseka.lynk.shared.domain.lobby.LobbyService
import com.eeseka.lynk.shared.domain.lobby.model.ConnectionState
import com.eeseka.lynk.shared.domain.lobby.model.LobbyEvent
import com.eeseka.lynk.shared.domain.location.LocationCoordinates
import com.eeseka.lynk.shared.domain.spot.SpotService
import com.eeseka.lynk.shared.domain.spot.model.Spot
import com.eeseka.lynk.shared.domain.util.DataErrorException
import com.eeseka.lynk.shared.domain.util.Paginator
import com.eeseka.lynk.shared.domain.util.map
import com.eeseka.lynk.shared.domain.util.onFailure
import com.eeseka.lynk.shared.domain.util.onSuccess
import com.eeseka.lynk.shared.presentation.spot.mappers.toSpotUi
import com.eeseka.lynk.shared.presentation.util.toUiText
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.collections.immutable.toImmutableSet
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class HangoutVotingViewModel(
    private val hangoutDetailRepository: HangoutDetailRepository,
    private val spotService: SpotService,
    private val connectionClient: LobbyConnectionClient,
    private val lobbyService: LobbyService,
    private val sessionStorage: SessionStorage
) : ViewModel() {
    private val eventChannel = Channel<HangoutVotingEvent>()
    val events = eventChannel.receiveAsFlow()

    private val _state = MutableStateFlow(HangoutVotingState())
    private var hasLoadedInitialData = false

    private val _hangoutId = MutableStateFlow<String?>(null)

    private val hangout = _hangoutId
        .flatMapLatest { hangoutId ->
            hangoutId?.let(hangoutDetailRepository::observeHangout) ?: flowOf(null)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = null
        )

    private var currentUserId: String? = null
    private var previousStatus: HangoutStatus? = null // The status the ballot was last checked against.

    private var spotSearchPaginator: Paginator<String?, Spot>? = null
    private var currentNextPageToken: String? = null

    private var favoriteSpotSearchPaginator: Paginator<String?, Spot>? = null

    val state = _state
        .onStart {
            if (!hasLoadedInitialData) {
                currentUserId = sessionStorage.observeAuthInfo().firstOrNull()?.user?.id
                observeVotingRound()
                observeConnectionState()
                observeLobbyEvents()
                observeTrendingSpots()
                observeProposeSpotSheetSearchFilters()
                hasLoadedInitialData = true
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = HangoutVotingState()
        )

    fun onAction(action: HangoutVotingAction) {
        when (action) {
            is HangoutVotingAction.OnSelectHangout -> selectHangout(action.hangoutId)
            is HangoutVotingAction.OnCastVote -> castVote(action.spotId)
            HangoutVotingAction.OnCloseVotingClick -> closeVoting(null)
            is HangoutVotingAction.OnBreakTie -> closeVoting(action.spotId)
            is HangoutVotingAction.OnShareLocation -> shareLocation(action.latitude, action.longitude)
            HangoutVotingAction.OnProposeSpotClick -> _state.update { it.copy(isProposeSpotSheetOpen = true) }
            HangoutVotingAction.OnDismissProposeSpotSheet -> dismissProposeSpotSheet()
            is HangoutVotingAction.OnTabSelected -> _state.update { it.copy(activeProposeSpotSheetSearchTab = action.tab) }
            HangoutVotingAction.LoadNextSpotPage -> loadNextSpotSearchPage()
            HangoutVotingAction.LoadNextFavoriteSpotPage -> loadNextFavoriteSpotSearchPage()
            is HangoutVotingAction.OnProposeSpot -> proposeSpot(action.spotId)
            is HangoutVotingAction.OnRemoveSpot -> removeSpot(action.spotId)
        }
    }

    private fun selectHangout(hangoutId: String?) {
        if (hangoutId == _hangoutId.value) return

        previousStatus = null
        dismissProposeSpotSheet()
        _state.update {
            it.copy(
                candidates = persistentListOf(),
                votes = persistentMapOf(),
                center = null,
                tiedSpotIds = persistentListOf(),
                trendingSpots = persistentListOf(),
                isTrendingLoading = false
            ).clearPendingVotingActions()
        }
        _hangoutId.update { hangoutId }
    }

    private fun observeVotingRound() {
        hangout
            .onEach { currentHangout ->
                if (currentHangout == null) {
                    previousStatus = null
                    return@onEach
                }

                val sameVotingRound = previousStatus == HangoutStatus.VOTING && currentHangout.status == HangoutStatus.VOTING
                if (previousStatus != null && !sameVotingRound) {
                    _state.update {
                        it.copy(
                            candidates = persistentListOf(),
                            votes = persistentMapOf(),
                            tiedSpotIds = persistentListOf(),
                            center = null
                        ).clearPendingVotingActions()
                    }
                }
                previousStatus = currentHangout.status

                if (state.value.isProposeSpotSheetOpen && currentHangout.status != HangoutStatus.VOTING) {
                    dismissProposeSpotSheet()
                }
            }
            .launchIn(viewModelScope)
    }

    private fun observeConnectionState() {
        connectionClient
            .connectionState
            .onEach { connectionState ->
                if (connectionState != ConnectionState.CONNECTED) {
                    _state.update { it.clearPendingVotingActions() }
                }
            }
            .launchIn(viewModelScope)
    }

    private fun observeLobbyEvents() {
        connectionClient.events
            .onEach { event -> handleLobbyEvent(event) }
            .launchIn(viewModelScope)
    }

    private suspend fun handleLobbyEvent(event: LobbyEvent) {
        val currentHangoutId = _hangoutId.value ?: return
        val isHost = hangout.value?.hostId == currentUserId

        when (event) {
            is LobbyEvent.LobbyError -> _state.update { it.clearPendingVotingActions() }

            is LobbyEvent.VotingSnapshot -> {
                if (event.hangoutId == currentHangoutId) {
                    val latitude = event.latitude
                    val longitude = event.longitude

                    _state.update {
                        it.copy(
                            candidates = event.candidates.map { spot -> spot.toSpotUi() }.toImmutableList(),
                            votes = event.votes.toImmutableMap(),
                            tiedSpotIds = persistentListOf(),
                            center = if (latitude != null && longitude != null) {
                                LocationCoordinates(latitude, longitude)
                            } else null
                        )
                    }
                }
            }

            is LobbyEvent.CandidateAdded -> {
                if (event.hangoutId == currentHangoutId) {
                    val wasOurs = state.value.proposingSpotIds.contains(event.spot.id)
                    _state.update { state ->
                        val withCandidate =
                            if (state.candidates.any { it.id == event.spot.id }) state
                            else state.copy(
                                candidates = (state.candidates + event.spot.toSpotUi()).toImmutableList()
                            )
                        withCandidate.copy(
                            proposingSpotIds = (withCandidate.proposingSpotIds - event.spot.id).toImmutableSet()
                        )
                    }
                    if (wasOurs) {
                        eventChannel.send(HangoutVotingEvent.SpotSuggested)
                    }
                }
            }

            is LobbyEvent.CandidateRemoved -> {
                if (event.hangoutId == currentHangoutId) {
                    _state.update { state ->
                        state.copy(
                            candidates = state.candidates.filterNot { it.id == event.spotId }.toImmutableList(),
                            votes = state.votes.filterValues { it != event.spotId }.toImmutableMap(),
                            removingSpotIds = (state.removingSpotIds - event.spotId).toImmutableSet()
                        )
                    }
                }
            }

            is LobbyEvent.VoteTally -> {
                if (event.hangoutId == currentHangoutId) {
                    // A fresh tally means the ballot moved (vote change or a removed candidate),
                    // so any previously announced tie is stale — clear it and re-evaluate on next close.
                    _state.update {
                        it.copy(votes = event.votes.toImmutableMap(), tiedSpotIds = persistentListOf())
                    }
                }
            }

            is LobbyEvent.CenterUpdate -> {
                if (event.hangoutId == currentHangoutId) {
                    _state.update {
                        it.copy(center = LocationCoordinates(event.latitude, event.longitude))
                    }
                }
            }

            is LobbyEvent.VotingTie -> {
                if (event.hangoutId == currentHangoutId) {
                    _state.update {
                        it.copy(
                            tiedSpotIds = event.tiedSpotIds.toImmutableList(),
                            isClosingVoting = false
                        )
                    }
                    if (isHost) {
                        eventChannel.send(HangoutVotingEvent.VotingTied)
                    }
                }
            }

            else -> Unit
        }
    }

    private fun castVote(spotId: String) {
        val hangoutId = _hangoutId.value ?: return
        viewModelScope.launch {
            lobbyService.castVote(hangoutId, spotId)
                .onFailure { error ->
                    eventChannel.send(
                        HangoutVotingEvent.Error(error.toUiText())
                    )
                }
        }
    }

    private fun closeVoting(chosenSpotId: String?) {
        val hangoutId = _hangoutId.value ?: return
        viewModelScope.launch {
            _state.update { it.copy(isClosingVoting = true) }
            lobbyService.closeVoting(hangoutId, chosenSpotId)
                .onFailure { error ->
                    _state.update { it.copy(isClosingVoting = false) }
                    eventChannel.send(
                        HangoutVotingEvent.Error(error.toUiText())
                    )
                }
        }
    }

    private fun proposeSpot(spotId: String) {
        val hangoutId = _hangoutId.value ?: return
        viewModelScope.launch {
            _state.update {
                it.copy(proposingSpotIds = (it.proposingSpotIds + spotId).toImmutableSet())
            }
            lobbyService.proposeSpot(hangoutId, spotId)
                .onFailure { error ->
                    _state.update {
                        it.copy(proposingSpotIds = (it.proposingSpotIds - spotId).toImmutableSet())
                    }
                    eventChannel.send(
                        HangoutVotingEvent.Error(error.toUiText())
                    )
                }
        }
    }

    private fun removeSpot(spotId: String) {
        val hangoutId = _hangoutId.value ?: return
        viewModelScope.launch {
            _state.update {
                it.copy(removingSpotIds = (it.removingSpotIds + spotId).toImmutableSet())
            }
            lobbyService.removeSpot(hangoutId, spotId)
                .onFailure { error ->
                    _state.update {
                        it.copy(removingSpotIds = (it.removingSpotIds - spotId).toImmutableSet())
                    }
                    eventChannel.send(
                        HangoutVotingEvent.Error(error.toUiText())
                    )
                }
        }
    }

    private fun shareLocation(latitude: Double, longitude: Double) {
        val hangoutId = _hangoutId.value ?: return
        _state.update {
            it.copy(
                myLocation = LocationCoordinates(latitude, longitude)
            )
        }
        viewModelScope.launch {
            lobbyService.shareLocation(hangoutId, latitude, longitude)
        }
    }

    private fun dismissProposeSpotSheet() {
        _state.value.proposeSpotSheetSearchTextState.clearText()
        _state.update {
            it.copy(
                isProposeSpotSheetOpen = false,
                activeProposeSpotSheetSearchTab = SearchTab.ALL_SPOTS,
                spotSearchResults = persistentListOf(),
                isSpotSearchLoading = false,
                spotSearchError = null,
                spotSearchEndReached = false,
                favoriteSpotSearchResults = persistentListOf(),
                isFavoriteSpotSearchLoading = false,
                favoriteSpotSearchError = null,
                favoriteSpotSearchEndReached = false
            )
        }
    }

    private fun observeTrendingSpots() {
        combine(_hangoutId, state.map { it.center ?: it.myLocation }) { hangoutId, origin ->
            hangoutId to origin
        }
            .distinctUntilChanged()
            .mapLatest { (hangoutId, origin) ->
                if (hangoutId == null || origin == null) {
                    _state.update { it.copy(isTrendingLoading = false) }
                    return@mapLatest
                }
                _state.update { it.copy(isTrendingLoading = true) }
                spotService.getTrendingSpots(origin.latitude, origin.longitude)
                    .onSuccess { spots ->
                        _state.update {
                            it.copy(
                                isTrendingLoading = false,
                                trendingSpots = spots.map { spot -> spot.toSpotUi() }.toImmutableList()
                            )
                        }
                    }
                    .onFailure { _ ->
                        // Trending is a nicety for the zero-state; on failure just let them search.
                        _state.update { it.copy(isTrendingLoading = false) }
                    }
            }
            .launchIn(viewModelScope)
    }

    private fun observeProposeSpotSheetSearchFilters() {
        val searchQueryFlow =
            snapshotFlow { _state.value.proposeSpotSheetSearchTextState.text.toString() }
                .debounce { query -> if (query.isBlank()) 0.milliseconds else 500.milliseconds }
        val tabFlow = state.map { it.activeProposeSpotSheetSearchTab }.distinctUntilChanged()
        val hasOriginFlow = state.map { it.center != null || it.myLocation != null }.distinctUntilChanged()

        combine(searchQueryFlow, tabFlow, hasOriginFlow) { query, activeTab, _ ->
            query to activeTab
        }.mapLatest { (query, activeTab) ->
            when (activeTab) {
                SearchTab.FAVORITES -> {
                    setupFavoriteSpotSearchPaginator(query.takeIf { it.isNotBlank() })
                    _state.update {
                        it.copy(
                            favoriteSpotSearchResults = persistentListOf(),
                            favoriteSpotSearchEndReached = false,
                            favoriteSpotSearchError = null,
                            favoriteSearchResetEpoch = it.favoriteSearchResetEpoch + 1
                        )
                    }
                    favoriteSpotSearchPaginator?.loadNextItems()
                }

                SearchTab.ALL_SPOTS -> {
                    val origin = state.value.center ?: state.value.myLocation
                    if (query.isBlank() || origin == null) {
                        _state.update {
                            it.copy(
                                spotSearchResults = persistentListOf(),
                                spotSearchEndReached = false,
                                isSpotSearchLoading = false,
                                spotSearchError = null,
                                spotSearchResetEpoch = it.spotSearchResetEpoch + 1
                            )
                        }
                    } else {
                        setupSpotSearchPaginator(origin.latitude, origin.longitude, query)
                        _state.update {
                            it.copy(
                                spotSearchResults = persistentListOf(),
                                spotSearchEndReached = false,
                                spotSearchError = null,
                                spotSearchResetEpoch = it.spotSearchResetEpoch + 1
                            )
                        }
                        spotSearchPaginator?.loadNextItems()
                    }
                }
            }
        }.launchIn(viewModelScope)
    }

    private fun setupSpotSearchPaginator(lat: Double, lng: Double, query: String) {
        currentNextPageToken = null

        spotSearchPaginator = Paginator(
            initialKey = null,
            onLoadUpdated = { isLoading ->
                _state.update { it.copy(isSpotSearchLoading = isLoading) }
            },
            onRequest = { nextPageToken ->
                spotService.searchSpots(
                    latitude = lat,
                    longitude = lng,
                    query = query,
                    nextPageToken = nextPageToken
                ).map { paginatedSpots ->
                    currentNextPageToken = paginatedSpots.nextPageToken
                    paginatedSpots.spots
                }
            },
            getNextKey = { _ ->
                currentNextPageToken
            },
            onError = { throwable ->
                if (throwable is DataErrorException) {
                    _state.update {
                        it.copy(
                            spotSearchError = throwable.error.toUiText()
                        )
                    }
                }
            },
            onSuccess = { newSpots, newKey ->
                _state.update {
                    it.copy(
                        spotSearchResults = (it.spotSearchResults + newSpots.map { newSpot -> newSpot.toSpotUi() }).toImmutableList(),
                        spotSearchEndReached = newKey == null,
                        spotSearchError = null
                    )
                }
            }
        )
    }

    private fun setupFavoriteSpotSearchPaginator(searchQuery: String? = null) {
        favoriteSpotSearchPaginator = Paginator(
            initialKey = null,
            onLoadUpdated = { isLoading ->
                _state.update { it.copy(isFavoriteSpotSearchLoading = isLoading) }
            },
            onRequest = { beforeTimestamp ->
                spotService.getSavedSpots(searchQuery, beforeTimestamp)
            },
            getNextKey = { spots ->
                spots.mapNotNull { it.savedAt }.minOrNull()?.toString()
            },
            onError = { throwable ->
                if (throwable is DataErrorException) {
                    _state.update {
                        it.copy(
                            favoriteSpotSearchError = throwable.error.toUiText()
                        )
                    }
                }
            },
            onSuccess = { favoriteSpots, _ ->
                _state.update {
                    it.copy(
                        favoriteSpotSearchResults = (it.favoriteSpotSearchResults + favoriteSpots.map { favoriteSpot -> favoriteSpot.toSpotUi() }).toImmutableList(),
                        favoriteSpotSearchEndReached = favoriteSpots.isEmpty(),
                        favoriteSpotSearchError = null
                    )
                }
            }
        )
    }

    private fun loadNextSpotSearchPage() {
        viewModelScope.launch {
            spotSearchPaginator?.loadNextItems()
        }
    }

    private fun loadNextFavoriteSpotSearchPage() {
        viewModelScope.launch {
            favoriteSpotSearchPaginator?.loadNextItems()
        }
    }

    // Suggesting, removing and closing voting all wait for the server to answer over the
    // socket. Wherever that answer can no longer arrive, all three have to stop waiting
    // together — otherwise a spinner turns forever.
    private fun HangoutVotingState.clearPendingVotingActions() = copy(
        proposingSpotIds = persistentSetOf(),
        removingSpotIds = persistentSetOf(),
        isClosingVoting = false
    )
}
