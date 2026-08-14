package com.eeseka.lynk.hangouts.presentation.hangouts_list_detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eeseka.lynk.shared.domain.lobby.LobbyConnectionClient
import com.eeseka.lynk.shared.domain.lobby.model.LobbyEvent
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HangoutsListDetailViewModel(
    private val connectionClient: LobbyConnectionClient
) : ViewModel() {
    private val eventChannel = Channel<HangoutsListDetailEvent>()
    val events = eventChannel.receiveAsFlow()

    private var hasLoadedInitialData = false

    private val _state = MutableStateFlow(HangoutsListDetailState())
    val state = _state
        .onStart {
            if (!hasLoadedInitialData) {
                observeLobbyEvents()
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
            is HangoutsListDetailAction.OnSelectHangout -> {
                _state.update {
                    it.copy(
                        selectedHangoutId = action.hangoutId
                    )
                }
            }

            HangoutsListDetailAction.OnCreateHangoutClick -> {
                _state.update {
                    it.copy(
                        sheetState = SheetState.CreateHangout
                    )
                }
            }

            is HangoutsListDetailAction.OnEditHangoutClick -> {
                _state.update {
                    it.copy(
                        sheetState = SheetState.EditHangout(action.hangout)
                    )
                }
            }

            HangoutsListDetailAction.OnDismissCurrentSheet -> {
                _state.update {
                    it.copy(
                        sheetState = SheetState.Hidden
                    )
                }
            }

            HangoutsListDetailAction.RefreshList -> refreshList()
        }
    }

    private fun observeLobbyEvents() {
        connectionClient.events
            .onEach { event ->
                val affectsList = when (event) {
                    is LobbyEvent.ParticipantLeft,
                    is LobbyEvent.RsvpUpdated,
                    is LobbyEvent.HangoutUpdated,
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