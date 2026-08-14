package com.eeseka.lynk.hangouts.presentation.hangout_detail

import androidx.compose.foundation.text.input.clearText
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eeseka.lynk.hangouts.presentation.hangout_detail.model.DeadlineChangeIntent
import com.eeseka.lynk.hangouts.presentation.hangout_detail.model.PaymentQuoteUi
import com.eeseka.lynk.hangouts.presentation.hangout_detail.model.SearchTab
import com.eeseka.lynk.hangouts.presentation.mappers.toBankUi
import com.eeseka.lynk.hangouts.presentation.util.toNairaString
import com.eeseka.lynk.shared.design_system.components.modals_and_overlays.LynkFlashType
import com.eeseka.lynk.shared.domain.auth.SessionStorage
import com.eeseka.lynk.shared.domain.hangout.HangoutConstants.MIN_COST_PER_PERSON_KOBO
import com.eeseka.lynk.shared.domain.hangout.HangoutParticipantService
import com.eeseka.lynk.shared.domain.hangout.HangoutService
import com.eeseka.lynk.shared.domain.hangout.model.HangoutStatus
import com.eeseka.lynk.shared.domain.hangout.model.RsvpStatus
import com.eeseka.lynk.shared.domain.lobby.LobbyConnectionClient
import com.eeseka.lynk.shared.domain.lobby.LobbyService
import com.eeseka.lynk.shared.domain.lobby.model.ConnectionState
import com.eeseka.lynk.shared.domain.lobby.model.LobbyEvent
import com.eeseka.lynk.shared.domain.location.LocationCoordinates
import com.eeseka.lynk.shared.domain.payment.PaymentConstants.NUBAN_LENGTH
import com.eeseka.lynk.shared.domain.payment.PaymentService
import com.eeseka.lynk.shared.domain.payment.model.DeadlineDecision
import com.eeseka.lynk.shared.domain.payment.model.PaymentStatus
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
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
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
import lynk.feature.hangouts.generated.resources.event_non_payer_removed
import lynk.feature.hangouts.generated.resources.event_payment_received
import lynk.feature.hangouts.generated.resources.event_payout_failed
import lynk.feature.hangouts.generated.resources.event_payout_sent
import lynk.feature.hangouts.generated.resources.event_rsvp_in
import lynk.feature.hangouts.generated.resources.event_rsvp_out
import lynk.feature.hangouts.generated.resources.event_updated
import lynk.feature.hangouts.generated.resources.event_withdrawn
import lynk.feature.hangouts.generated.resources.payment_account_check_limit
import lynk.feature.hangouts.generated.resources.payment_account_not_found
import lynk.feature.hangouts.generated.resources.payment_deadline_after_hangout
import lynk.feature.hangouts.generated.resources.payment_deadline_changed
import lynk.feature.hangouts.generated.resources.payment_deadline_in_past
import lynk.feature.hangouts.generated.resources.payment_decision_saved
import lynk.feature.hangouts.generated.resources.payment_enabled_message
import lynk.feature.hangouts.generated.resources.payment_not_completed
import lynk.feature.hangouts.generated.resources.payment_payout_queued
import lynk.feature.hangouts.generated.resources.payment_share_too_low
import lynk.feature.hangouts.generated.resources.payment_still_processing
import lynk.feature.hangouts.generated.resources.spot_suggested_message
import lynk.feature.hangouts.generated.resources.voting_tie_flash
import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Instant

class HangoutDetailViewModel(
    private val hangoutService: HangoutService,
    private val participantService: HangoutParticipantService,
    private val spotService: SpotService,
    private val connectionClient: LobbyConnectionClient,
    private val lobbyService: LobbyService,
    private val paymentService: PaymentService,
    private val sessionStorage: SessionStorage
) : ViewModel() {
    private val eventChannel = Channel<HangoutDetailEvent>()
    val events = eventChannel.receiveAsFlow()

    private val _state = MutableStateFlow(HangoutDetailState())
    private var hasLoadedInitialData = false

    private val _hangoutId = MutableStateFlow<String?>(null)
    private var enteredHangoutId: String? = null // Which hangout's lobby this socket is currently marked "present" in (server-side).

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
                observeBankSearch()
                observeAccountResolution()
                observeEnablePaymentsForm()
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
            is HangoutDetailAction.OnCollectPaymentsToggled -> toggleCollectPayments(action.isOn)
            HangoutDetailAction.OnPaymentDeadlinePickerClick -> _state.update {
                it.copy(isPaymentDeadlinePickerOpen = true)
            }
            HangoutDetailAction.OnDismissPaymentDeadlinePicker -> _state.update {
                it.copy(isPaymentDeadlinePickerOpen = false)
            }
            is HangoutDetailAction.OnPaymentDeadlineSelected -> selectPaymentDeadline(action.date)
            HangoutDetailAction.OnEnablePaymentsConfirmed -> enablePayments()
            HangoutDetailAction.OnChangeDeadlineClick -> _state.update {
                it.copy(pendingDeadlineChange = DeadlineChangeIntent.CHANGE)
            }
            HangoutDetailAction.OnDismissDeadlinePicker -> _state.update {
                it.copy(pendingDeadlineChange = null)
            }
            is HangoutDetailAction.OnNewDeadlineSelected -> submitNewDeadline(action.date)
            HangoutDetailAction.OnDeadlineDecisionClick -> _state.update {
                it.copy(isDeadlineDecisionSheetOpen = true)
            }
            HangoutDetailAction.OnDismissDeadlineDecisionSheet -> _state.update {
                it.copy(isDeadlineDecisionSheetOpen = false)
            }
            is HangoutDetailAction.OnDeadlineDecisionSelected -> selectDeadlineDecision(action.decision)
            HangoutDetailAction.OnRetryPayoutClick -> retryPayout()
            HangoutDetailAction.OnPayClick -> initializePayment()
            HangoutDetailAction.OnDismissPayConfirmSheet -> dismissPayConfirmSheet()
            HangoutDetailAction.OnConfirmPayment -> openPaymentPage()
            HangoutDetailAction.OnCheckPaymentClick -> verifyPayment()
            HangoutDetailAction.OnDismissPaymentCheckout -> dismissPaymentCheckout()
            HangoutDetailAction.OnBankPickerClick -> _state.update { it.copy(isBankPickerOpen = true) }
            HangoutDetailAction.OnDismissBankPicker -> dismissBankPicker()
            is HangoutDetailAction.OnBankSelected -> selectBank(action.bankCode)
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

            is LobbyEvent.PaymentReceived -> refreshDetailAndShowSnackbar(
                match = event.hangoutId == currentHangoutId,
                // The payer just came back from Paystack and knows perfectly well that they paid.
                snackbar = if (event.userId == state.value.currentUserId) null else UiText.Resource(
                    Res.string.event_payment_received,
                    arrayOf(event.displayName)
                ) to LynkFlashType.Success
            )

            is LobbyEvent.NonPayerRemoved -> refreshDetailAndShowSnackbar(
                match = event.hangoutId == currentHangoutId,
                snackbar = UiText.Resource(
                    Res.string.event_non_payer_removed,
                    arrayOf(event.displayName)
                ) to LynkFlashType.Warning
            )

            is LobbyEvent.PaymentDeadlineResolved -> refreshDetailAndShowSnackbar(
                match = event.hangoutId == currentHangoutId,
                snackbar = null
            )

            // Only ever delivered to the host, so this needs no isHost guard.
            is LobbyEvent.PayoutOutcome -> refreshDetailAndShowSnackbar(
                match = event.hangoutId == currentHangoutId,
                snackbar = if (event.succeeded) {
                    UiText.Resource(Res.string.event_payout_sent) to LynkFlashType.Success
                } else {
                    UiText.Resource(Res.string.event_payout_failed) to LynkFlashType.Error
                }
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
                if (connectionState == ConnectionState.CONNECTED) {
                    _hangoutId.value?.let { reloadIfCurrent(it) }
                }
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
        // location-dependent spot search (trending is recomputed around the new group), along with
        // any payment this screen was still waiting on.
        _state.update {
            it.copy(
                isAwaitingPaymentReturn = false,
                paymentCheckoutUrl = null,
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
            .distinctUntilChanged()
            .debounce { query -> if (query.isBlank()) 0.milliseconds else 500.milliseconds }
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
                .distinctUntilChanged()
                .debounce { query -> if (query.isBlank()) 0.milliseconds else 500.milliseconds }

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

    private fun toggleCollectPayments(isOn: Boolean) {
        if (!isOn) {
            resetCollectPaymentsForm()
            return
        }
        _state.update { it.copy(isCollectPaymentsOn = true) }
        // 262 banks in one unpaginated call, so it is fetched once when the form first opens.
        if (state.value.allBanks.isEmpty()) loadBanks()
    }

    private fun selectPaymentDeadline(date: LocalDate) {
        _state.update {
            it.copy(
                paymentDeadlineDate = date,
                isPaymentDeadlinePickerOpen = false
            )
        }
    }

    private fun submitNewDeadline(date: LocalDate) {
        val hangoutId = _hangoutId.value ?: return
        val intent = state.value.pendingDeadlineChange ?: return
        val scheduledAt = state.value.hangout?.scheduledAt ?: return

        date.deadlineError()?.let { error ->
            viewModelScope.launch {
                eventChannel.send(HangoutDetailEvent.ShowMessage(error, LynkFlashType.Error))
            }
            return
        }

        val deadline = date.toDeadlineInstant(scheduledAt)
        _state.update { it.copy(pendingDeadlineChange = null) }

        viewModelScope.launch {
            val result = when (intent) {
                DeadlineChangeIntent.CHANGE -> paymentService.changeDeadline(hangoutId, deadline)
                DeadlineChangeIntent.EXTEND -> paymentService.decideAtDeadline(
                    hangoutId = hangoutId,
                    decision = DeadlineDecision.EXTEND,
                    newDeadline = deadline
                )
            }
            result
                .onSuccess {
                    reloadIfCurrent(hangoutId)
                    eventChannel.send(
                        HangoutDetailEvent.ShowMessage(
                            UiText.Resource(Res.string.payment_deadline_changed),
                            LynkFlashType.Success
                        )
                    )
                }
                .onFailure { error ->
                    eventChannel.send(
                        HangoutDetailEvent.ShowMessage(error.toUiText(), LynkFlashType.Error)
                    )
                }
        }
    }

    private fun selectDeadlineDecision(decision: DeadlineDecision) {
        if (decision == DeadlineDecision.EXTEND) {
            _state.update {
                it.copy(
                    isDeadlineDecisionSheetOpen = false,
                    pendingDeadlineChange = DeadlineChangeIntent.EXTEND
                )
            }
            return
        }

        val hangoutId = _hangoutId.value ?: return
        viewModelScope.launch {
            paymentService
                .decideAtDeadline(hangoutId = hangoutId, decision = decision)
                .onSuccess {
                    reloadIfCurrent(hangoutId)
                    eventChannel.send(
                        HangoutDetailEvent.ShowMessage(
                            UiText.Resource(Res.string.payment_decision_saved),
                            LynkFlashType.Success
                        )
                    )
                }
                .onFailure { error ->
                    eventChannel.send(
                        HangoutDetailEvent.ShowMessage(error.toUiText(), LynkFlashType.Error)
                    )
                }
        }
    }

    private fun dismissBankPicker() {
        _state.value.bankSearchState.clearText()
        _state.update { it.copy(isBankPickerOpen = false) }
    }

    private fun selectBank(bankCode: String) {
        val bank = state.value.allBanks.firstOrNull { it.code == bankCode } ?: return
        _state.value.bankSearchState.clearText()
        _state.update {
            it.copy(
                selectedBank = bank,
                isBankPickerOpen = false,
                // The old name belonged to the old bank, so it cannot stand while the new one resolves.
                resolvedAccountName = null,
                accountResolutionError = null
            )
        }
    }

    private fun loadBanks() {
        viewModelScope.launch {
            _state.update { it.copy(isLoadingBanks = true, bankLoadError = null) }
            paymentService
                .getBanks()
                .onSuccess { banks ->
                    val bankUis = banks.map { bank -> bank.toBankUi() }
                    _state.update {
                        it.copy(
                            allBanks = bankUis,
                            bankResults = bankUis,
                            isLoadingBanks = false
                        )
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(bankLoadError = error.toUiText(), isLoadingBanks = false)
                    }
                }
        }
    }

    @OptIn(FlowPreview::class)
    private fun observeBankSearch() {
        snapshotFlow { _state.value.bankSearchState.text.toString() }
            .distinctUntilChanged()
            .debounce { query -> if (query.isBlank()) 0.milliseconds else 200.milliseconds }
            .onEach { query ->
                val trimmed = query.trim()
                _state.update { state ->
                    state.copy(
                        bankResults = if (trimmed.isBlank()) {
                            state.allBanks
                        } else {
                            state.allBanks.filter { it.name.contains(trimmed, ignoreCase = true) }
                        }
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    private fun observeAccountResolution() {
        combine(
            snapshotFlow { _state.value.accountNumberState.text.toString() },
            state.map { it.selectedBank?.code }.distinctUntilChanged()
        ) { accountNumber, bankCode ->
            accountNumber.trim() to bankCode
        }
        .distinctUntilChanged()
        .debounce(800.milliseconds)
        .mapLatest { (accountNumber, bankCode) ->
            if (bankCode == null || accountNumber.length != NUBAN_LENGTH) {
                _state.update {
                    it.copy(
                        resolvedAccountName = null,
                        accountResolutionError = null,
                        isResolvingAccount = false
                    )
                }
                return@mapLatest
            }

            _state.update {
                it.copy(
                    isResolvingAccount = true,
                    resolvedAccountName = null,
                    accountResolutionError = null
                )
            }
            paymentService
                .resolveBankAccount(accountNumber = accountNumber, bankCode = bankCode)
                .onSuccess { account ->
                    _state.update {
                        it.copy(
                            resolvedAccountName = account.accountName,
                            isResolvingAccount = false
                        )
                    }
                }
                .onFailure { error ->
                    val resolutionError = when (error) {
                        DataError.Remote.BAD_REQUEST -> {
                            UiText.Resource(Res.string.payment_account_not_found)
                        }

                        DataError.Remote.TOO_MANY_REQUESTS -> {
                            UiText.Resource(Res.string.payment_account_check_limit)
                        }

                        else -> error.toUiText()
                    }

                    _state.update {
                        it.copy(
                            accountResolutionError = resolutionError,
                            isResolvingAccount = false
                        )
                    }
                }
        }
        .launchIn(viewModelScope)
    }

    private fun observeEnablePaymentsForm() {
        combine(
            snapshotFlow { _state.value.totalCostState.text.toString() },
            state.map {
                Triple(it.paymentDeadlineDate, it.selectedBank?.code, it.resolvedAccountName)
            }.distinctUntilChanged(),
            state.map { currentState ->
                currentState.hangout?.participants?.count {
                    it.rsvpStatus == RsvpStatus.ATTENDING
                } ?: 0
            }.distinctUntilChanged()
        ) { totalCost, (deadline, bankCode, accountName), attendingCount ->
            val totalCostKobo = totalCost.toKoboOrNull()
            val deadlineError = deadline?.deadlineError()

            val isShareTooLow = totalCostKobo != null &&
                    attendingCount > 0 &&
                    totalCostKobo / attendingCount < MIN_COST_PER_PERSON_KOBO

            _state.update {
                it.copy(
                    canEnablePayments = totalCostKobo != null &&
                            !isShareTooLow &&
                            deadline != null &&
                            deadlineError == null &&
                            bankCode != null &&
                            accountName != null,
                    totalCostError = if (isShareTooLow) {
                        UiText.Resource(Res.string.payment_share_too_low)
                    } else null,
                    paymentDeadlineError = deadlineError
                )
            }
        }
        .launchIn(viewModelScope)
    }

    private fun enablePayments() {
        val hangoutId = _hangoutId.value ?: return
        val currentState = state.value
        val scheduledAt = currentState.hangout?.scheduledAt ?: return
        val totalCostKobo = currentState.totalCostState.text.toKoboOrNull() ?: return
        val deadline = currentState.paymentDeadlineDate?.toDeadlineInstant(scheduledAt) ?: return
        val bankCode = currentState.selectedBank?.code ?: return
        val accountNumber = currentState.accountNumberState.text.toString().trim()

        viewModelScope.launch {
            _state.update { it.copy(isEnablingPayments = true) }
            paymentService
                .enablePayments(
                    hangoutId = hangoutId,
                    totalCostKobo = totalCostKobo,
                    paymentDeadline = deadline,
                    accountNumber = accountNumber,
                    bankCode = bankCode
                )
                .onSuccess {
                    reloadIfCurrent(hangoutId)
                    resetCollectPaymentsForm()
                    eventChannel.send(
                        HangoutDetailEvent.ShowMessage(
                            UiText.Resource(Res.string.payment_enabled_message),
                            LynkFlashType.Success
                        )
                    )
                }
                .onFailure { error ->
                    eventChannel.send(
                        HangoutDetailEvent.ShowMessage(error.toUiText(), LynkFlashType.Error)
                    )
                }
            _state.update { it.copy(isEnablingPayments = false) }
        }
    }

    private fun resetCollectPaymentsForm() {
        _state.value.totalCostState.clearText()
        _state.value.accountNumberState.clearText()
        _state.value.bankSearchState.clearText()
        _state.update {
            it.copy(
                isCollectPaymentsOn = false,
                isPaymentDeadlinePickerOpen = false,
                isBankPickerOpen = false,
                totalCostError = null,
                paymentDeadlineDate = null,
                paymentDeadlineError = null,
                selectedBank = null,
                resolvedAccountName = null,
                accountResolutionError = null
            )
        }
    }

    private fun retryPayout() {
        val hangoutId = _hangoutId.value ?: return
        viewModelScope.launch {
            _state.update { it.copy(isRetryingPayout = true) }
            paymentService
                .retryPayout(hangoutId)
                .onSuccess {
                    reloadIfCurrent(hangoutId)
                    eventChannel.send(
                        HangoutDetailEvent.ShowMessage(
                            UiText.Resource(Res.string.payment_payout_queued),
                            LynkFlashType.Success
                        )
                    )
                }
                .onFailure { error ->
                    eventChannel.send(
                        HangoutDetailEvent.ShowMessage(error.toUiText(), LynkFlashType.Error)
                    )
                }
            _state.update { it.copy(isRetryingPayout = false) }
        }
    }

    private fun initializePayment() {
        val hangoutId = _hangoutId.value ?: return
        viewModelScope.launch {
            _state.update { it.copy(isInitializingPayment = true) }
            paymentService
                .initializePayment(hangoutId)
                .onSuccess { initialization ->
                    _state.update {
                        it.copy(
                            paymentQuote = PaymentQuoteUi(
                                shareLabel = initialization.netAmountKobo.toNairaString(),
                                chargeLabel = initialization.amountKobo.toNairaString(),
                                authorizationUrl = initialization.authorizationUrl
                            )
                        )
                    }
                }
                .onFailure { error ->
                    eventChannel.send(
                        HangoutDetailEvent.ShowMessage(error.toUiText(), LynkFlashType.Error)
                    )
                }
            _state.update { it.copy(isInitializingPayment = false) }
        }
    }

    private fun openPaymentPage() {
        val url = state.value.paymentQuote?.authorizationUrl ?: return
        dismissPayConfirmSheet()
        _state.update { it.copy(paymentCheckoutUrl = url, isAwaitingPaymentReturn = true) }
    }

    private fun dismissPaymentCheckout() {
        _state.update { it.copy(paymentCheckoutUrl = null) }
        verifyPayment()
    }

    private fun verifyPayment() {
        val hangoutId = _hangoutId.value ?: return
        viewModelScope.launch {
            _state.update { it.copy(isVerifyingPayment = true) }
            paymentService
                .verifyPayment(hangoutId)
                .onSuccess { status ->
                    when (status) {
                        PaymentStatus.SUCCESS -> {
                            reloadIfCurrent(hangoutId)
                            _state.update { it.copy(isAwaitingPaymentReturn = false) }
                        }
                        PaymentStatus.PENDING -> eventChannel.send(
                            HangoutDetailEvent.ShowMessage(
                                UiText.Resource(Res.string.payment_still_processing),
                                LynkFlashType.Info
                            )
                        )
                        PaymentStatus.FAILED, PaymentStatus.ABANDONED -> {
                            _state.update { it.copy(isAwaitingPaymentReturn = false) }
                            eventChannel.send(
                                HangoutDetailEvent.ShowMessage(
                                    UiText.Resource(Res.string.payment_not_completed),
                                    LynkFlashType.Error
                                )
                            )
                        }
                    }
                }
                .onFailure { error ->
                    eventChannel.send(
                        HangoutDetailEvent.ShowMessage(error.toUiText(), LynkFlashType.Error)
                    )
                }
            _state.update { it.copy(isVerifyingPayment = false) }
        }
    }

    // The quote belongs to one attempt at paying, so it leaves with the sheet.
    private fun dismissPayConfirmSheet() {
        _state.update { it.copy(paymentQuote = null) }
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

    private fun CharSequence.toKoboOrNull(): Long? {
        val cleaned = filter { it.isDigit() || it == '.' }.toString()
        // Two points is a typo, and there is no honest amount to guess from it.
        if (cleaned.count { it == '.' } > 1) return null

        val naira = cleaned.substringBefore('.').ifEmpty { "0" }.toLongOrNull() ?: return null
        val kobo =
            cleaned.substringAfter('.', "").take(2).padEnd(2, '0').toLongOrNull() ?: return null

        if (naira > (Long.MAX_VALUE - kobo) / 100) return null

        val totalKobo = naira * 100 + kobo
        return if (totalKobo <= 0L) null else totalKobo
    }

    private fun LocalDate.toDeadlineInstant(scheduledAt: Instant): Instant {
        val endOfDay = LocalDateTime(this, LocalTime(23, 59))
            .toInstant(TimeZone.currentSystemDefault())
        return if (endOfDay > scheduledAt) scheduledAt else endOfDay
    }

    private fun LocalDate.deadlineError(): UiText? {
        val scheduledAt = state.value.hangout?.scheduledAt ?: return null
        val timeZone = TimeZone.currentSystemDefault()
        val today = Clock.System.now().toLocalDateTime(timeZone).date
        val lastAllowedDate = scheduledAt.toLocalDateTime(timeZone).date

        return when {
            this < today -> UiText.Resource(Res.string.payment_deadline_in_past)
            this > lastAllowedDate -> UiText.Resource(Res.string.payment_deadline_after_hangout)
            else -> null
        }
    }
}