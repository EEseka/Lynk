package com.eeseka.lynk.hangouts.presentation.util

import com.eeseka.lynk.shared.domain.lobby.model.ConnectionState
import com.eeseka.lynk.shared.presentation.util.UiText
import lynk.feature.hangouts.generated.resources.Res
import lynk.feature.hangouts.generated.resources.connection_connecting
import lynk.feature.hangouts.generated.resources.connection_network_error
import lynk.feature.hangouts.generated.resources.connection_offline
import lynk.feature.hangouts.generated.resources.connection_online
import lynk.feature.hangouts.generated.resources.connection_unknown_error

fun ConnectionState.toUiText(): UiText {
    val resource = when (this) {
        ConnectionState.DISCONNECTED -> Res.string.connection_offline
        ConnectionState.CONNECTING -> Res.string.connection_connecting
        ConnectionState.CONNECTED -> Res.string.connection_online
        ConnectionState.ERROR_NETWORK -> Res.string.connection_network_error
        ConnectionState.ERROR_UNKNOWN -> Res.string.connection_unknown_error
    }
    return UiText.Resource(resource)
}
