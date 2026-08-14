package com.eeseka.lynk.shared.data.lobby.network

import com.eeseka.lynk.shared.domain.lobby.model.ConnectionState

expect class ConnectionErrorHandler() {
    fun getConnectionStateForError(cause: Throwable): ConnectionState
    fun transformException(exception: Throwable): Throwable
    fun isRetriableError(cause: Throwable): Boolean
}
