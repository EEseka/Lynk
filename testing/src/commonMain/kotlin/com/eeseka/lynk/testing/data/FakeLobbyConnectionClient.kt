package com.eeseka.lynk.testing.data

import com.eeseka.lynk.shared.domain.lobby.LobbyConnectionClient
import com.eeseka.lynk.shared.domain.lobby.model.ConnectionState
import com.eeseka.lynk.shared.domain.lobby.model.LobbyEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeLobbyConnectionClient : LobbyConnectionClient {
    override val connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)

    private val eventFlow = MutableSharedFlow<LobbyEvent>(extraBufferCapacity = 64)

    override val events: Flow<LobbyEvent> = eventFlow

    // Stands in for the server pushing an event down the socket
    suspend fun sendEvent(event: LobbyEvent) {
        eventFlow.emit(event)
    }
}
