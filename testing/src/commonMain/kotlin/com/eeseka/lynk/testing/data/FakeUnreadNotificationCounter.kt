package com.eeseka.lynk.testing.data

import com.eeseka.lynk.shared.domain.notification.UnreadNotificationCounter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class FakeUnreadNotificationCounter : UnreadNotificationCounter {
    var refreshCount = 0

    override val count = MutableStateFlow(0L)

    override suspend fun refresh() {
        refreshCount++
    }

    override fun decrement() {
        count.update { current -> (current - 1).coerceAtLeast(0L) }
    }

    override fun clear() {
        count.value = 0L
    }
}
