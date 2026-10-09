package com.eeseka.lynk.hangouts.presentation.hangouts_list_detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eeseka.lynk.hangouts.presentation.mappers.toHangoutStatusFilter
import com.eeseka.lynk.hangouts.presentation.model.HangoutStatusFilter
import com.eeseka.lynk.shared.domain.hangout.HangoutDetailRepository
import com.eeseka.lynk.shared.domain.lobby.LobbyConnectionClient
import com.eeseka.lynk.shared.domain.lobby.model.LobbyEvent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HangoutsListDetailViewModel(
    private val connectionClient: LobbyConnectionClient,
    private val hangoutDetailRepository: HangoutDetailRepository
) : ViewModel() {
    private val eventChannel = Channel<HangoutsListDetailEvent>()
    val events = eventChannel.receiveAsFlow()

    private var hasLoadedInitialData = false

    // A hangout opened from outside the list, waiting to load so the list can move to its tab
    private val _hangoutIdToShowInList = MutableStateFlow<String?>(null)

    private val _state = MutableStateFlow(HangoutsListDetailState())
    val state = _state
        .onStart {
            if (!hasLoadedInitialData) {
                observeLobbyEvents()
                observeHangoutToShowInList()
                hasLoadedInitialData = true
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = HangoutsListDetailState()
        )

    fun onAction(action: HangoutsListDetailAction) {
        when (action) {
            is HangoutsListDetailAction.OnSelectHangout -> selectHangout(action.hangoutId)
            is HangoutsListDetailAction.OnSelectHangoutAndShowInList -> selectHangoutAndShowInList(action.hangoutId)

            HangoutsListDetailAction.OnCreateHangoutClick -> {
                _state.update { it.copy(sheetState = SheetState.CreateHangout) }
            }

            is HangoutsListDetailAction.OnEditHangoutClick -> {
                _state.update { it.copy(sheetState = SheetState.EditHangout(action.hangout)) }
            }

            HangoutsListDetailAction.OnDismissCurrentSheet -> {
                _state.update { it.copy(sheetState = SheetState.Hidden) }
            }

            HangoutsListDetailAction.RefreshList -> refreshList()
        }
    }

    // Any other pick drops a hangout still waiting to be shown, so its late load never moves the list
    private fun selectHangout(hangoutId: String?) {
        _hangoutIdToShowInList.update { null }
        _state.update { it.copy(selectedHangoutId = hangoutId) }
    }

    private fun selectHangoutAndShowInList(hangoutId: String) {
        _hangoutIdToShowInList.update { hangoutId }
        _state.update { it.copy(selectedHangoutId = hangoutId) }
    }

    // The detail pane loads the hangout anyway, so its status costs no extra request
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeHangoutToShowInList() {
        _hangoutIdToShowInList
            .flatMapLatest { hangoutId ->
                if (hangoutId == null) emptyFlow()
                else hangoutDetailRepository.observeHangout(hangoutId).filterNotNull().take(1)
            }
            .onEach { hangout ->
                // Cleared once shown, so opening the same hangout from outside again still moves the list
                _hangoutIdToShowInList.update { null }
                showHangoutInList(hangout.status.toHangoutStatusFilter())
            }
            .launchIn(viewModelScope)
    }

    private fun showHangoutInList(statusFilter: HangoutStatusFilter) {
        viewModelScope.launch {
            eventChannel.send(HangoutsListDetailEvent.ShowHangoutInList(statusFilter))
        }
    }

    private fun observeLobbyEvents() {
        connectionClient.events
            .onEach { event ->
                val affectsList = when (event) {
                    is LobbyEvent.NonPayerRemoved,
                    is LobbyEvent.ParticipantLeft,
                    is LobbyEvent.RsvpUpdated,
                    is LobbyEvent.HangoutUpdated,
                    is LobbyEvent.HangoutStarted,
                    is LobbyEvent.HangoutCompleted,
                    is LobbyEvent.HangoutCancelled -> true
                    else -> false
                }
                if (affectsList) refreshList()
            }
            .launchIn(viewModelScope)
    }

    private fun refreshList() {
        viewModelScope.launch {
            eventChannel.send(HangoutsListDetailEvent.RefreshList)
        }
    }
}
