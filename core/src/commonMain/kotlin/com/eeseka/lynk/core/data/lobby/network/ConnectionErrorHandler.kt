package com.eeseka.lynk.core.data.lobby.network

import com.eeseka.lynk.core.domain.lobby.model.ConnectionState

expect class ConnectionErrorHandler() {
    fun getConnectionStateForError(cause: Throwable): ConnectionState
    fun transformException(exception: Throwable): Throwable
    fun isRetriableError(cause: Throwable): Boolean
}
