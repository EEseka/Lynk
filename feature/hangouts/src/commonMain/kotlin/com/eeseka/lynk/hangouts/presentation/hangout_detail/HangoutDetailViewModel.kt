package com.eeseka.lynk.hangouts.presentation.hangout_detail

import androidx.compose.foundation.text.input.clearText
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eeseka.lynk.hangouts.presentation.hangout_detail.model.SearchTab
import com.eeseka.lynk.shared.design_system.components.modals_and_overlays.LynkFlashType
import com.eeseka.lynk.shared.domain.auth.SessionStorage
import com.eeseka.lynk.shared.domain.hangout.HangoutParticipantService
import com.eeseka.lynk.shared.domain.hangout.HangoutService
import com.eeseka.lynk.shared.domain.hangout.model.HangoutStatus
import com.eeseka.lynk.shared.domain.hangout.model.RsvpStatus
import com.eeseka.lynk.shared.domain.lobby.LobbyConnectionClient
import com.eeseka.lynk.shared.domain.lobby.LobbyService
import com.eeseka.lynk.shared.domain.lobby.model.ConnectionState
import com.eeseka.lynk.shared.domain.lobby.model.LobbyEvent
import com.eeseka.lynk.shared.domain.location.LocationCoordinates
import com.eeseka.lynk.shared.domain.spot.SpotService
import com.eeseka.lynk.shared.domain.spot.model.Spot
import com.eeseka.lynk.shared.domain.util.DataError
import com.eeseka.lynk.shared.domain.util.DataErrorException
import com.eeseka.lynk.shared.domain.util.Paginator
import com.eeseka.lynk.shared.domain.util.map
import com.eeseka.lynk.shared.domain.util.onFailure
import com.eeseka.lynk.shared.domain.util.onSuccess
import com.eeseka.lynk.shared.presentation.hangout.mappers.toHangoutUi
import com.eeseka.lynk.shared.presentation.hangout.mappers.toHangoutUserUi
import com.eeseka.lynk.shared.presentation.hangout.model.HangoutUi
import com.eeseka.lynk.shared.presentation.spot.mappers.toSpotUi
import com.eeseka.lynk.shared.presentation.util.UiText
import com.eeseka.lynk.shared.presentation.util.toUiText
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import lynk.feature.hangouts.generated.resources.Res
import lynk.feature.hangouts.generated.resources.detail_cancelled_message
import lynk.feature.hangouts.generated.resources.detail_completed_message
import lynk.feature.hangouts.generated.resources.detail_invited_message
import lynk.feature.hangouts.generated.resources.detail_left_message
import lynk.feature.hangouts.generated.resources.detail_withdrawn_message
import lynk.feature.hangouts.generated.resources.event_cancelled
import lynk.feature.hangouts.generated.resources.event_completed
import lynk.feature.hangouts.generated.resources.event_invited
import lynk.feature.hangouts.generated.resources.event_left
import lynk.feature.hangouts.generated.resources.event_rsvp_in
import lynk.feature.hangouts.generated.resources.event_rsvp_out
import lynk.feature.hangouts.generated.resources.event_updated
import lynk.feature.hangouts.generated.resources.event_withdrawn
import lynk.feature.hangouts.generated.resources.spot_suggested_message
import lynk.feature.hangouts.generated.resources.voting_tie_flash
import kotlin.time.Duration.Companion.milliseconds

class HangoutDetailViewModel(
    private val hangoutService: HangoutService,
    private val participantService: HangoutParticipantService,
    private val spotService: SpotService,
    private val connectionClient: LobbyConnectionClient,
    private val lobbyService: LobbyService,
    private val sessionStorage: SessionStorage
) : ViewModel() {
    private val eventChannel = Channel<HangoutDetailEvent>()
    val events = eventChannel.receiveAsFlow()

    private val _state = MutableStateFlow(HangoutDetailState())
    private var hasLoadedInitialData = false

    private val _hangoutId = MutableStateFlow<String?>(null)
    private var enteredHangoutId: String? =
        null // Which hangout's lobby this socket is currently marked "present" in (server-side).

    private var spotSearchPaginator: Paginator<String?, Spot>? = null
    private var currentNextPageToken: String? = null

    private var favoriteSpotSearchPaginator: Paginator<String?, Spot>? = null

    private var saveSpotJob: Job? = null

    val state = _state
        .onStart {
            if (!hasLoadedInitialData) {
                val authInfo = sessionStorage.observeAuthInfo().firstOrNull()
                _state.update { it.copy(currentUserId = authInfo?.user?.id) }
                observeConnectionState()
                observeInviteSearch()
                observeTrendingSpots()
                observeProposeSpotSheetSearchFilters()
                observeLobbyEvents()
                observeLobbyPresence()
                hasLoadedInitialData = true
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = HangoutDetailState()
        )

    fun onAction(action: HangoutDetailAction) {
        when (action) {
            is HangoutDetailAction.OnSelectHangout -> selectHangout(action.hangoutId)
            HangoutDetailAction.OnRetryClick -> _hangoutId.value?.let(::loadHangout)
            HangoutDetailAction.OnCompleteHangoutConfirmed -> completeHangout()
            HangoutDetailAction.OnCancelHangoutConfirmed -> cancelHangout()
            HangoutDetailAction.OnLeaveHangoutConfirmed -> leaveHangout()
            is HangoutDetailAction.OnWithdrawParticipantInvite -> withdrawParticipantInvite(action.userId)
            HangoutDetailAction.OnInviteClick -> _state.update { it.copy(isInviteSheetOpen = true) }
            HangoutDetailAction.OnDismissInviteSheet -> dismissInviteSheet()
            is HangoutDetailAction.OnInviteUser -> inviteParticipant(action.userId)
            is HangoutDetailAction.OnCastVote -> castVote(action.spotId)
            HangoutDetailAction.OnCloseVotingClick -> closeVoting(null)
            is HangoutDetailAction.OnBreakTie -> closeVoting(action.spotId)
            is HangoutDetailAction.OnShareLocation -> shareLocation(
                action.latitude,
                action.longitude
            )

            HangoutDetailAction.OnProposeSpotClick -> _state.update { it.copy(isProposeSpotSheetOpen = true) }
            HangoutDetailAction.OnDismissProposeSpotSheet -> dismissProposeSpotSheet()
            is HangoutDetailAction.OnTabSelected -> _state.update {
                it.copy(
                    activeProposeSpotSheetSearchTab = action.tab
                )
            }

            HangoutDetailAction.LoadNextSpotPage -> loadNextSpotSearchPage()
            HangoutDetailAction.LoadNextFavoriteSpotPage -> loadNextFavoriteSpotSearchPage()
            is HangoutDetailAction.OnProposeSpot -> proposeSpot(action.spotId)
            is HangoutDetailAction.OnRemoveSpot -> removeSpot(action.spotId)
            is HangoutDetailAction.OnToggleSaveSpot -> toggleSaveSpot(
                spotId = action.spotId,
                isCurrentlySaved = action.isCurrentlySaved
            )
        }
    }

    private fun observeLobbyEvents() {
        connectionClient.events
            .onEach { event -> handleLobbyEvent(event) }
            .launchIn(viewModelScope)
    }

    private suspend fun handleLobbyEvent(event: LobbyEvent) {
        val currentHangoutId = _hangoutId.value ?: return
        val isHost = state.value.hangout?.hostId == state.value.currentUserId

        when (event) {
            is LobbyEvent.PresenceUpdate -> {
                if (event.hangoutId == currentHangoutId) {
                    _state.update { it.copy(presentUserIds = event.presentUserIds) }
                }
            }

            is LobbyEvent.ParticipantInvited -> refreshDetailAndShowSnackbar(
                match = event.hangoutId == currentHangoutId,
                // Host triggered this via REST and already saw a confirmation.
                snackbar = if (isHost) null else UiText.Resource(
                    Res.string.event_invited,
                    arrayOf(event.displayName)
                ) to LynkFlashType.Info
            )

            is LobbyEvent.ParticipantInviteWithdrawn -> refreshDetailAndShowSnackbar(
                match = event.hangoutId == currentHangoutId,
                snackbar = if (isHost) null else UiText.Resource(
                    Res.string.event_withdrawn,
                    arrayOf(event.displayName)
                ) to LynkFlashType.Info
            )

            is LobbyEvent.ParticipantLeft -> refreshDetailAndShowSnackbar(
                match = event.hangoutId == currentHangoutId,
                snackbar = UiText.Resource(
                    Res.string.event_left,
                    arrayOf(event.displayName)
                ) to LynkFlashType.Info
            )

            is LobbyEvent.RsvpUpdated -> refreshDetailAndShowSnackbar(
                match = event.hangoutId == currentHangoutId,
                snackbar = when (event.rsvpStatus) {
                    RsvpStatus.ATTENDING -> UiText.Resource(
                        Res.string.event_rsvp_in,
                        arrayOf(event.displayName)
                    ) to LynkFlashType.Success

                    else -> UiText.Resource(
                        Res.string.event_rsvp_out,
                        arrayOf(event.displayName)
                    ) to LynkFlashType.Info
                }
            )

            is LobbyEvent.HangoutUpdated -> refreshDetailAndShowSnackbar(
                match = event.hangoutId == currentHangoutId,
                snackbar = if (isHost) null else UiText.Resource(
                    Res.string.event_updated,
                    arrayOf(event.hostDisplayName)
                ) to LynkFlashType.Info
            )

            is LobbyEvent.HangoutCompleted -> refreshDetailAndShowSnackbar(
                match = event.hangoutId == currentHangoutId,
                snackbar = if (isHost) null else UiText.Resource(
                    Res.string.event_completed,
                    arrayOf(event.hostDisplayName)
                ) to LynkFlashType.Success
            )

            is LobbyEvent.HangoutCancelled -> refreshDetailAndShowSnackbar(
                match = event.hangoutId == currentHangoutId,
                snackbar = if (isHost) null else UiText.Resource(
                    Res.string.event_cancelled,
                    arrayOf(event.hostDisplayName)
                ) to LynkFlashType.Warning
            )

            is LobbyEvent.LobbyError -> {
                _state.update { it.clearPendingVotingActions() }
                eventChannel.send(
                    HangoutDetailEvent.ShowMessage(
                        event.toUiText(), LynkFlashType.Error
                    )
                )
            }

            is LobbyEvent.VotingSnapshot -> {
                if (event.hangoutId == currentHangoutId) {
                    val latitude = event.latitude
                    val longitude = event.longitude

                    _state.update {
                        it.copy(
                            candidates = event.candidates.map { spot -> spot.toSpotUi() },
                            votes = event.votes,
                            tiedSpotIds = emptyList(),
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
                            else state.copy(candidates = state.candidates + event.spot.toSpotUi())
                        withCandidate.copy(
                            proposingSpotIds = withCandidate.proposingSpotIds - event.spot.id
                        )
                    }
                    if (wasOurs) {
                        eventChannel.send(
                            HangoutDetailEvent.ShowMessage(
                                UiText.Resource(Res.string.spot_suggested_message),
                                LynkFlashType.Success
                            )
                        )
                    }
                }
            }

            is LobbyEvent.CandidateRemoved -> {
                if (event.hangoutId == currentHangoutId) {
                    _state.update { state ->
                        state.copy(
                            candidates = state.candidates.filterNot { it.id == event.spotId },
                            votes = state.votes.filterValues { it != event.spotId },
                            removingSpotIds = state.removingSpotIds - event.spotId
                        )
                    }
                }
            }

            is LobbyEvent.VoteTally -> {
                if (event.hangoutId == currentHangoutId) {
                    // A fresh tally means the ballot moved (vote change or a removed candidate),
                    // so any previously announced tie is stale — clear it and re-evaluate on next close.
                    _state.update { it.copy(votes = event.votes, tiedSpotIds = emptyList()) }
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
                        it.copy(tiedSpotIds = event.tiedSpotIds, isClosingVoting = false)
                    }
                    if (isHost) {
                        eventChannel.send(
                            HangoutDetailEvent.ShowMessage(
                                UiText.Resource(Res.string.voting_tie_flash),
                                LynkFlashType.Warning
                            )
                        )
                    }
                }
            }
        }
    }

    private suspend fun refreshDetailAndShowSnackbar(
        match: Boolean,
        snackbar: Pair<UiText, LynkFlashType>?
    ) {
        if (!match) return
        _hangoutId.value?.let { reloadIfCurrent(it) }
        snackbar?.let { (message, type) ->
            eventChannel.send(HangoutDetailEvent.ShowMessage(message, type))
        }
    }

    private fun observeConnectionState() {
        connectionClient
            .connectionState
            .onEach { connectionState ->
                _state.update { it.copy(connectionState = connectionState) }
            }.launchIn(viewModelScope)
    }

    private fun observeLobbyPresence() {
        combine(_hangoutId, connectionClient.connectionState) { hangoutId, connectionState ->
            if (connectionState != ConnectionState.CONNECTED) {
                enteredHangoutId = null
                _state.update {
                    it.copy(presentUserIds = emptySet()).clearPendingVotingActions()
                }
                return@combine
            }
            when {
                hangoutId == null -> {
                    enteredHangoutId?.let { lobbyService.leaveLobby(it) }
                    enteredHangoutId = null
                }

                hangoutId != enteredHangoutId -> {
                    enteredHangoutId?.let { lobbyService.leaveLobby(it) }
                    lobbyService.enterLobby(hangoutId)
                    enteredHangoutId = hangoutId
                }
            }
        }.launchIn(viewModelScope)
    }

    private fun selectHangout(hangoutId: String?) {
        if (hangoutId == _hangoutId.value) return
        _hangoutId.update { hangoutId }

        // A different hangout means a different lobby — drop presence, voting and the
        // location-dependent spot search (trending is recomputed around the new group).
        _state.update {
            it.copy(
                presentUserIds = emptySet(),
                candidates = emptyList(),
                votes = emptyMap(),
                center = null,
                tiedSpotIds = emptyList(),
                trendingSpots = emptyList(),
                spotSearchResults = emptyList(),
                favoriteSpotSearchResults = emptyList()
            )
        }

        if (hangoutId == null) {
            _state.update { it.copy(hangout = null, error = null, isLoading = false) }
        } else {
            _state.update { it.copy(hangout = null, error = null) }
            loadHangout(hangoutId)
        }
    }

    private fun loadHangout(hangoutId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            hangoutService
                .getHangoutDetails(hangoutId)
                .onSuccess { hangout ->
                    // Guard against a stale response after the user switched hangouts.
                    if (hangoutId != _hangoutId.value) return@onSuccess
                    applyLoadedHangout(hangout.toHangoutUi(), clearLoading = true)
                }
                .onFailure { error ->
                    if (hangoutId != _hangoutId.value) return@onFailure
                    _state.update { it.copy(isLoading = false, error = error.toUiText()) }
                }
        }
    }

    private fun completeHangout() {
        val hangoutId = _hangoutId.value ?: return
        viewModelScope.launch {
            _state.update { it.copy(isCompleting = true) }
            hangoutService
                .completeHangout(hangoutId)
                .onSuccess {
                    reloadIfCurrent(hangoutId)
                    eventChannel.send(
                        HangoutDetailEvent.ShowMessage(
                            UiText.Resource(Res.string.detail_completed_message),
                            LynkFlashType.Success
                        )
                    )
                }
                .onFailure { error ->
                    eventChannel.send(
                        HangoutDetailEvent.ShowMessage(
                            error.toUiText(),
                            LynkFlashType.Error
                        )
                    )
                }
            _state.update { it.copy(isCompleting = false) }
        }
    }

    private fun cancelHangout() {
        val hangoutId = _hangoutId.value ?: return
        viewModelScope.launch {
            _state.update { it.copy(isCancelling = true) }
            hangoutService
                .cancelHangout(hangoutId)
                .onSuccess {
                    reloadIfCurrent(hangoutId)
                    eventChannel.send(
                        HangoutDetailEvent.ShowMessage(
                            UiText.Resource(Res.string.detail_cancelled_message),
                            LynkFlashType.Success
                        )
                    )
                }
                .onFailure { error ->
                    eventChannel.send(
                        HangoutDetailEvent.ShowMessage(
                            error.toUiText(),
                            LynkFlashType.Error
                        )
                    )
                }
            _state.update { it.copy(isCancelling = false) }
        }
    }

    private fun leaveHangout() {
        val hangoutId = _hangoutId.value ?: return
        viewModelScope.launch {
            _state.update { it.copy(isLeaving = true) }
            hangoutService
                .leaveHangout(hangoutId)
                .onSuccess {
                    // No reload — a non-attendee can't fetch the detail (403), so close it.
                    eventChannel.send(
                        HangoutDetailEvent.ShowMessage(
                            UiText.Resource(Res.string.detail_left_message),
                            LynkFlashType.Success
                        )
                    )
                    eventChannel.send(HangoutDetailEvent.NavigateBack)
                }
                .onFailure { error ->
                    eventChannel.send(
                        HangoutDetailEvent.ShowMessage(
                            error.toUiText(),
                            LynkFlashType.Error
                        )
                    )
                }
            _state.update { it.copy(isLeaving = false) }
        }
    }

    private fun dismissInviteSheet() {
        _state.value.inviteQueryState.clearText()
        _state.update {
            it.copy(
                isInviteSheetOpen = false,
                inviteResult = null,
                inviteNotFound = false,
                isInviteSearching = false
            )
        }
    }

    private fun inviteParticipant(userId: String) {
        val hangoutId = _hangoutId.value ?: return
        viewModelScope.launch {
            _state.update { it.copy(isInviting = true) }
            hangoutService
                .inviteParticipant(hangoutId, userId)
                .onSuccess {
                    reloadIfCurrent(hangoutId)
                    eventChannel.send(
                        HangoutDetailEvent.ShowMessage(
                            UiText.Resource(Res.string.detail_invited_message),
                            LynkFlashType.Success
                        )
                    )
                }
                .onFailure { error ->
                    eventChannel.send(
                        HangoutDetailEvent.ShowMessage(
                            error.toUiText(),
                            LynkFlashType.Error
                        )
                    )
                }
            _state.update { it.copy(isInviting = false) }
        }
    }

    private fun withdrawParticipantInvite(userId: String) {
        val hangoutId = _hangoutId.value ?: return
        viewModelScope.launch {
            _state.update { it.copy(withdrawingUserIds = it.withdrawingUserIds + userId) }
            hangoutService
                .removeParticipant(hangoutId, userId)
                .onSuccess {
                    reloadIfCurrent(hangoutId)
                    eventChannel.send(
                        HangoutDetailEvent.ShowMessage(
                            UiText.Resource(Res.string.detail_withdrawn_message),
                            LynkFlashType.Success
                        )
                    )
                }
                .onFailure { error ->
                    eventChannel.send(
                        HangoutDetailEvent.ShowMessage(
                            error.toUiText(),
                            LynkFlashType.Error
                        )
                    )
                }
            _state.update { it.copy(withdrawingUserIds = it.withdrawingUserIds - userId) }
        }
    }

    @OptIn(FlowPreview::class)
    private fun observeInviteSearch() {
        snapshotFlow { _state.value.inviteQueryState.text.toString() }
            .debounce { query -> if (query.isBlank()) 0.milliseconds else 500.milliseconds }
            .distinctUntilChanged()
            .onEach { query ->
                val trimmed = query.trim()
                if (trimmed.isBlank()) {
                    _state.update {
                        it.copy(
                            inviteResult = null,
                            inviteNotFound = false
                        )
                    }
                    return@onEach
                }

                _state.update { it.copy(isInviteSearching = true, inviteNotFound = false) }
                participantService
                    .getHangoutUserByUsername(trimmed)
                    .onSuccess { user ->
                        _state.update {
                            it.copy(
                                inviteResult = user.toHangoutUserUi(),
                                inviteNotFound = false,
                                isInviteSearching = false
                            )
                        }
                    }
                    .onFailure { error ->
                        val notFound = error == DataError.Remote.NOT_FOUND
                        _state.update {
                            it.copy(
                                inviteResult = null,
                                inviteNotFound = notFound,
                                isInviteSearching = false
                            )
                        }
                        if (!notFound) {
                            eventChannel.send(
                                HangoutDetailEvent.ShowMessage(
                                    error.toUiText(),
                                    LynkFlashType.Error
                                )
                            )
                        }
                    }
            }.launchIn(viewModelScope)
    }

    private fun castVote(spotId: String) {
        val hangoutId = _hangoutId.value ?: return
        viewModelScope.launch {
            lobbyService.castVote(hangoutId, spotId)
                .onFailure { error ->
                    eventChannel.send(
                        HangoutDetailEvent.ShowMessage(
                            error.toUiText(),
                            LynkFlashType.Error
                        )
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
                        HangoutDetailEvent.ShowMessage(
                            error.toUiText(),
                            LynkFlashType.Error
                        )
                    )
                }
        }
    }

    private fun proposeSpot(spotId: String) {
        val hangoutId = _hangoutId.value ?: return
        viewModelScope.launch {
            _state.update { it.copy(proposingSpotIds = it.proposingSpotIds + spotId) }
            lobbyService.proposeSpot(hangoutId, spotId)
                .onFailure { error ->
                    _state.update { it.copy(proposingSpotIds = it.proposingSpotIds - spotId) }
                    eventChannel.send(
                        HangoutDetailEvent.ShowMessage(
                            error.toUiText(),
                            LynkFlashType.Error
                        )
                    )
                }
        }
    }

    private fun removeSpot(spotId: String) {
        val hangoutId = _hangoutId.value ?: return
        viewModelScope.launch {
            _state.update { it.copy(removingSpotIds = it.removingSpotIds + spotId) }
            lobbyService.removeSpot(hangoutId, spotId)
                .onFailure { error ->
                    _state.update { it.copy(removingSpotIds = it.removingSpotIds - spotId) }
                    eventChannel.send(
                        HangoutDetailEvent.ShowMessage(
                            error.toUiText(),
                            LynkFlashType.Error
                        )
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
                spotSearchResults = emptyList(),
                isSpotSearchLoading = false,
                spotSearchError = null,
                spotSearchEndReached = false,
                favoriteSpotSearchResults = emptyList(),
                isFavoriteSpotSearchLoading = false,
                favoriteSpotSearchError = null,
                favoriteSpotSearchEndReached = false
            )
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeTrendingSpots() {
        state
            .map { it.center ?: it.myLocation }
            .distinctUntilChanged()
            .mapLatest { origin ->
                if (origin == null) return@mapLatest
                _state.update { it.copy(isTrendingLoading = true) }
                spotService.getTrendingSpots(origin.latitude, origin.longitude)
                    .onSuccess { spots ->
                        _state.update {
                            it.copy(
                                isTrendingLoading = false,
                                trendingSpots = spots.map { spot -> spot.toSpotUi() }
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

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    private fun observeProposeSpotSheetSearchFilters() {
        val searchQueryFlow =
            snapshotFlow { _state.value.proposeSpotSheetSearchTextState.text.toString() }
                .debounce { query -> if (query.isBlank()) 0.milliseconds else 500.milliseconds }
                .distinctUntilChanged()

        val tabFlow = state.map { it.activeProposeSpotSheetSearchTab }.distinctUntilChanged()

        combine(searchQueryFlow, tabFlow) { query, activeTab ->
            query to activeTab
        }.mapLatest { (query, activeTab) ->
            when (activeTab) {
                SearchTab.FAVORITES -> {
                    setupFavoriteSpotSearchPaginator(query.takeIf { it.isNotBlank() })
                    _state.update {
                        it.copy(
                            favoriteSpotSearchResults = emptyList(),
                            favoriteSpotSearchEndReached = false,
                            favoriteSearchResetEpoch = it.favoriteSearchResetEpoch + 1
                        )
                    }
                    favoriteSpotSearchPaginator?.loadNextItems()
                }

                SearchTab.ALL_SPOTS -> {
                    if (query.isBlank()) {
                        _state.update {
                            it.copy(
                                spotSearchResults = emptyList(),
                                spotSearchEndReached = false,
                                isSpotSearchLoading = false,
                                spotSearchResetEpoch = it.spotSearchResetEpoch + 1
                            )
                        }
                    } else {
                        val origin = state.value.center ?: state.value.myLocation
                        if (origin != null) {
                            setupSpotSearchPaginator(
                                origin.latitude,
                                origin.longitude,
                                query
                            )
                            _state.update {
                                it.copy(
                                    spotSearchResults = emptyList(),
                                    spotSearchEndReached = false,
                                    spotSearchResetEpoch = it.spotSearchResetEpoch + 1
                                )
                            }
                            spotSearchPaginator?.loadNextItems()
                        }
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
                        spotSearchResults = it.spotSearchResults + newSpots.map { newSpot -> newSpot.toSpotUi() },
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
                        favoriteSpotSearchResults = it.favoriteSpotSearchResults + favoriteSpots.map { favoriteSpot -> favoriteSpot.toSpotUi() },
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

    private fun toggleSaveSpot(spotId: String, isCurrentlySaved: Boolean) {
        updateChosenSpotSaveState(spotId, !isCurrentlySaved)

        saveSpotJob?.cancel()
        saveSpotJob = viewModelScope.launch {
            delay(300.milliseconds)

            val result = if (isCurrentlySaved) {
                spotService.unsaveSpot(spotId)
            } else {
                spotService.saveSpot(spotId)
            }

            result.onFailure { error ->
                updateChosenSpotSaveState(spotId, isCurrentlySaved)
                eventChannel.send(
                    HangoutDetailEvent.ShowMessage(error.toUiText(), LynkFlashType.Error)
                )
            }
        }
    }

    private fun updateChosenSpotSaveState(spotId: String, isSaved: Boolean) {
        _state.update { currentState ->
            val chosenSpot = currentState.hangout?.chosenSpot
            if (chosenSpot?.id != spotId) return@update currentState
            currentState.copy(
                hangout = currentState.hangout.copy(chosenSpot = chosenSpot.copy(isSaved = isSaved))
            )
        }
    }

    private suspend fun reloadIfCurrent(hangoutId: String) {
        if (hangoutId != _hangoutId.value) return
        hangoutService.getHangoutDetails(hangoutId)
            .onSuccess { hangout ->
                if (hangoutId != _hangoutId.value) return@onSuccess
                applyLoadedHangout(hangout.toHangoutUi())
            }
    }

    // Keep the voting data only if we were voting and still are — on any other status change the
    // server has already thrown its copy away, so ours is stale and the open sheets are dead too.
    private fun applyLoadedHangout(hangout: HangoutUi, clearLoading: Boolean = false) {
        _state.update { current ->
            val previous = current.hangout
            val sameVotingRound = previous != null &&
                    previous.status == HangoutStatus.VOTING &&
                    hangout.status == HangoutStatus.VOTING

            val next = if (previous == null || sameVotingRound) {
                current.copy(hangout = hangout)
            } else {
                current.copy(
                    hangout = hangout,
                    candidates = emptyList(),
                    votes = emptyMap(),
                    tiedSpotIds = emptyList(),
                    center = null
                ).clearPendingVotingActions()
            }

            if (clearLoading) next.copy(isLoading = false, error = null) else next
        }

        // Suggesting a spot once voting closed, or inviting once the roster locked, both bounce
        // off the server — a sheet left open over a moved-on hangout can only produce an error.
        if (state.value.isProposeSpotSheetOpen && hangout.status != HangoutStatus.VOTING) {
            dismissProposeSpotSheet()
        }
        // Same window the invite button uses: the roster only stays open before the hangout starts.
        val isUpcoming =
            hangout.status == HangoutStatus.VOTING || hangout.status == HangoutStatus.SCHEDULED
        if (state.value.isInviteSheetOpen && !isUpcoming) {
            dismissInviteSheet()
        }
    }

    // Suggesting, removing and closing voting all wait for the server to answer over the
    // socket. Wherever that answer can no longer arrive, all three have to stop waiting
    // together — otherwise a spinner turns forever.
    private fun HangoutDetailState.clearPendingVotingActions() = copy(
        proposingSpotIds = emptySet(),
        removingSpotIds = emptySet(),
        isClosingVoting = false
    )
}