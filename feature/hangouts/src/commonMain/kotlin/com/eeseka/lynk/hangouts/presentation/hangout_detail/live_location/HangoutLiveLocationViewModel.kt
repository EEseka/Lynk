package com.eeseka.lynk.hangouts.presentation.hangout_detail.live_location

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eeseka.lynk.hangouts.presentation.mappers.toLiveLocationUi
import com.eeseka.lynk.shared.domain.lobby.LobbyConnectionClient
import com.eeseka.lynk.shared.domain.lobby.LobbyService
import com.eeseka.lynk.shared.domain.lobby.model.LobbyEvent
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HangoutLiveLocationViewModel(
    private val connectionClient: LobbyConnectionClient,
    private val lobbyService: LobbyService
) : ViewModel() {

    private val _state = MutableStateFlow(HangoutLiveLocationState())
    private var hasLoadedInitialData = false

    private var currentHangoutId: String? = null

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
            initialValue = HangoutLiveLocationState()
        )

    fun onAction(action: HangoutLiveLocationAction) {
        when (action) {
            is HangoutLiveLocationAction.OnSelectHangout -> selectHangout(action.hangoutId)
            is HangoutLiveLocationAction.OnSharingChange -> changeSharing(action.isSharing)
            is HangoutLiveLocationAction.OnLocationChange -> shareLocation(action.latitude, action.longitude)
        }
    }

    private fun selectHangout(hangoutId: String?) {
        if (hangoutId == currentHangoutId) return

        changeSharing(false)
        currentHangoutId = hangoutId
        _state.update { it.copy(liveLocations = persistentMapOf()) }
    }

    private fun observeLobbyEvents() {
        connectionClient.events
            .onEach { event -> handleLobbyEvent(event) }
            .launchIn(viewModelScope)
    }

    private fun handleLobbyEvent(event: LobbyEvent) {
        when (event) {
            is LobbyEvent.LiveLocationSnapshot -> {
                if (event.hangoutId == currentHangoutId) {
                    _state.update { state ->
                        state.copy(
                            liveLocations = event.liveLocations
                                .associate { it.userId to it.toLiveLocationUi() }
                                .toImmutableMap()
                        )
                    }
                }
            }

            is LobbyEvent.LiveLocationUpdated -> {
                if (event.hangoutId == currentHangoutId) {
                    _state.update { state ->
                        state.copy(
                            liveLocations = (state.liveLocations + (event.liveLocation.userId to event.liveLocation.toLiveLocationUi()))
                                .toImmutableMap()
                        )
                    }
                }
            }

            is LobbyEvent.LiveLocationStopped -> {
                if (event.hangoutId == currentHangoutId) {
                    _state.update { state ->
                        state.copy(liveLocations = (state.liveLocations - event.userId).toImmutableMap())
                    }
                }
            }

            else -> Unit
        }
    }

    private fun changeSharing(isSharing: Boolean) {
        val wasSharing = _state.value.isSharingLocation
        if (isSharing == wasSharing) return

        _state.update { it.copy(isSharingLocation = isSharing) }

        val hangoutId = currentHangoutId ?: return
        if (wasSharing) {
            viewModelScope.launch { lobbyService.stopLiveLocation(hangoutId) }
        }
    }

    private fun shareLocation(latitude: Double, longitude: Double) {
        val hangoutId = currentHangoutId ?: return
        if (!_state.value.isSharingLocation) return

        viewModelScope.launch { lobbyService.shareLiveLocation(hangoutId, latitude, longitude) }
    }
}
