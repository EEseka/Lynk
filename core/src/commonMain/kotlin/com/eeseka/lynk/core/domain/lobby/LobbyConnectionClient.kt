package com.eeseka.lynk.core.domain.lobby

import com.eeseka.lynk.core.domain.lobby.model.ConnectionState
import com.eeseka.lynk.core.domain.lobby.model.LobbyEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface LobbyConnectionClient {
    val events: Flow<LobbyEvent>
    val connectionState: StateFlow<ConnectionState>
}