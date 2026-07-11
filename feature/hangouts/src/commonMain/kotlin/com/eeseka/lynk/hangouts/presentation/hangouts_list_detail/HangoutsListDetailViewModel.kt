package com.eeseka.lynk.hangouts.presentation.hangouts_list_detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HangoutsListDetailViewModel : ViewModel() {
    private val eventChannel = Channel<HangoutsListDetailEvent>()
    val events = eventChannel.receiveAsFlow()

    private val _state = MutableStateFlow(HangoutsListDetailState())
    val state = _state
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

            HangoutsListDetailAction.OnDismissCurrentSheet -> {
                _state.update {
                    it.copy(
                        sheetState = SheetState.Hidden
                    )
                }
            }

            HangoutsListDetailAction.RefreshList -> {
                viewModelScope.launch {
                    eventChannel.send(HangoutsListDetailEvent.RefreshList)
                }
            }
        }
    }
}