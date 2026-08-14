package com.eeseka.lynk.shared.domain.lobby

import com.eeseka.lynk.shared.domain.lobby.model.ConnectionState
import com.eeseka.lynk.shared.domain.lobby.model.LobbyEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface LobbyConnectionClient {
    val events: Flow<LobbyEvent>
    val connectionState: StateFlow<ConnectionState>
}