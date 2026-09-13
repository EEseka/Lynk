package com.eeseka.lynk.hangouts.presentation.hangout_detail

import androidx.compose.foundation.text.input.clearText
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eeseka.lynk.shared.design_system.components.modals_and_overlays.LynkFlashType
import com.eeseka.lynk.shared.domain.auth.SessionStorage
import com.eeseka.lynk.shared.domain.hangout.HangoutDetailRepository
import com.eeseka.lynk.shared.domain.hangout.HangoutParticipantService
import com.eeseka.lynk.shared.domain.hangout.HangoutService
import com.eeseka.lynk.shared.domain.hangout.model.HangoutStatus
import com.eeseka.lynk.shared.domain.hangout.model.RsvpStatus
import com.eeseka.lynk.shared.domain.lobby.LobbyConnectionClient
import com.eeseka.lynk.shared.domain.lobby.LobbyService
import com.eeseka.lynk.shared.domain.lobby.model.ConnectionState
import com.eeseka.lynk.shared.domain.lobby.model.LobbyEvent
import com.eeseka.lynk.shared.domain.spot.SpotService
import com.eeseka.lynk.shared.domain.util.DataError
import com.eeseka.lynk.shared.domain.util.onFailure
import com.eeseka.lynk.shared.domain.util.onSuccess
import com.eeseka.lynk.shared.presentation.hangout.mappers.toHangoutUi
import com.eeseka.lynk.shared.presentation.hangout.mappers.toHangoutUserUi
import com.eeseka.lynk.shared.presentation.util.UiText
import com.eeseka.lynk.shared.presentation.util.toUiText
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toImmutableSet
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import lynk.feature.hangouts.generated.resources.Res
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
import kotlin.time.Duration.Companion.milliseconds

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class HangoutDetailViewModel(
    private val hangoutService: HangoutService,
    private val hangoutDetailRepository: HangoutDetailRepository,
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
    private var enteredHangoutId: String? = null // Which hangout's lobby this socket is currently marked "present" in (server-side).

    private var saveSpotJob: Job? = null

    val state = _state
        .onStart {
            if (!hasLoadedInitialData) {
                val authInfo = sessionStorage.observeAuthInfo().firstOrNull()
                _state.update { it.copy(currentUserId = authInfo?.user?.id) }
                observeHangout()
                observeConnectionState()
                observeInviteSearch()
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
            is HangoutDetailAction.OnToggleSaveSpot -> toggleSaveSpot(spotId = action.spotId, isCurrentlySaved = action.isCurrentlySaved)
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
                    _state.update { it.copy(presentUserIds = event.presentUserIds.toImmutableSet()) }
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
                eventChannel.send(HangoutDetailEvent.Error(event.toUiText()))
            }

            // The ballot events belong to HangoutVotingViewModel.
            else -> Unit
        }
    }

    private suspend fun refreshDetailAndShowSnackbar(
        match: Boolean,
        snackbar: Pair<UiText, LynkFlashType>?
    ) {
        if (!match) return
        _hangoutId.value?.let { hangoutDetailRepository.refreshHangout(it) }
        snackbar?.let { (message, type) ->
            eventChannel.send(HangoutDetailEvent.LobbyAnnouncement(message, type))
        }
    }

    private fun observeConnectionState() {
        connectionClient
            .connectionState
            .onEach { connectionState ->
                _state.update { it.copy(connectionState = connectionState) }
                if (connectionState == ConnectionState.CONNECTED) {
                    _hangoutId.value?.let { hangoutDetailRepository.refreshHangout(it) }
                }
            }.launchIn(viewModelScope)
    }

    private fun observeLobbyPresence() {
        combine(_hangoutId, connectionClient.connectionState) { hangoutId, connectionState ->
            if (connectionState != ConnectionState.CONNECTED) {
                enteredHangoutId = null
                _state.update { it.copy(presentUserIds = persistentSetOf()) }
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

        // A different hangout means a different lobby, so the old lobby's presence goes with it —
        // and so do the invite sheet and every host action still in flight for the old id.
        dismissInviteSheet()
        _state.update {
            it.copy(
                presentUserIds = persistentSetOf(),
                withdrawingUserIds = persistentSetOf(),
                isInviting = false,
                isCompleting = false,
                isCancelling = false,
                isLeaving = false,
                error = null,
                isLoading = hangoutId != null
            )
        }
        hangoutId?.let(::loadHangout)
    }

    private fun loadHangout(hangoutId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            hangoutDetailRepository
                .refreshHangout(hangoutId)
                .onSuccess {
                    // A late answer for a hangout the user already left must not end the next one's loading.
                    if (hangoutId != _hangoutId.value) return@onSuccess
                    _state.update { it.copy(isLoading = false, error = null) }
                }
                .onFailure { error ->
                    if (hangoutId != _hangoutId.value) return@onFailure
                    _state.update { it.copy(isLoading = false, error = error.toUiText()) }
                }
        }
    }

    private fun observeHangout() {
        _hangoutId
            .flatMapLatest { hangoutId ->
                hangoutId?.let(hangoutDetailRepository::observeHangout) ?: flowOf(null)
            }
            .onEach { hangout ->
                _state.update { it.copy(hangout = hangout?.toHangoutUi()) }

                val isUpcoming = hangout?.status == HangoutStatus.VOTING || hangout?.status == HangoutStatus.SCHEDULED
                if (hangout != null && state.value.isInviteSheetOpen && !isUpcoming) {
                    dismissInviteSheet()
                }
            }
            .launchIn(viewModelScope)
    }

    private fun completeHangout() {
        val hangoutId = _hangoutId.value ?: return
        viewModelScope.launch {
            _state.update { it.copy(isCompleting = true) }
            hangoutService
                .completeHangout(hangoutId)
                .onSuccess {
                    hangoutDetailRepository.refreshHangout(hangoutId)
                    eventChannel.send(HangoutDetailEvent.HangoutCompleted)
                }
                .onFailure { error ->
                    eventChannel.send(HangoutDetailEvent.Error(error.toUiText()))
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
                    hangoutDetailRepository.refreshHangout(hangoutId)
                    eventChannel.send(HangoutDetailEvent.HangoutCancelled)
                }
                .onFailure { error ->
                    eventChannel.send(HangoutDetailEvent.Error(error.toUiText()))
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
                    eventChannel.send(HangoutDetailEvent.HangoutLeft)
                    eventChannel.send(HangoutDetailEvent.NavigateBack)
                }
                .onFailure { error ->
                    eventChannel.send(HangoutDetailEvent.Error(error.toUiText()))
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
                    hangoutDetailRepository.refreshHangout(hangoutId)
                    eventChannel.send(HangoutDetailEvent.InviteSent)
                }
                .onFailure { error ->
                    eventChannel.send(HangoutDetailEvent.Error(error.toUiText()))
                }
            _state.update { it.copy(isInviting = false) }
        }
    }

    private fun withdrawParticipantInvite(userId: String) {
        val hangoutId = _hangoutId.value ?: return
        viewModelScope.launch {
            _state.update {
                it.copy(withdrawingUserIds = (it.withdrawingUserIds + userId).toImmutableSet())
            }
            hangoutService
                .removeParticipant(hangoutId, userId)
                .onSuccess {
                    hangoutDetailRepository.refreshHangout(hangoutId)
                    eventChannel.send(HangoutDetailEvent.InviteWithdrawn)
                }
                .onFailure { error ->
                    eventChannel.send(HangoutDetailEvent.Error(error.toUiText()))
                }
            _state.update {
                it.copy(withdrawingUserIds = (it.withdrawingUserIds - userId).toImmutableSet())
            }
        }
    }

    private fun observeInviteSearch() {
        snapshotFlow { _state.value.inviteQueryState.text.toString() }
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
                            eventChannel.send(HangoutDetailEvent.Error(error.toUiText()))
                        }
                    }
            }.launchIn(viewModelScope)
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
                eventChannel.send(HangoutDetailEvent.Error(error.toUiText()))
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
}