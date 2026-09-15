package com.eeseka.lynk.shared.domain.notification

import kotlinx.coroutines.flow.StateFlow

interface UnreadNotificationCounter {
    val count: StateFlow<Long>

    suspend fun refresh()

    fun decrement()

    fun clear()
}
