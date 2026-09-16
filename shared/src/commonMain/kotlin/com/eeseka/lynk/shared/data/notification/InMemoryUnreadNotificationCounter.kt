package com.eeseka.lynk.shared.data.notification

import com.eeseka.lynk.shared.domain.lifecycle.AppLifecycleObserver
import com.eeseka.lynk.shared.domain.auth.SessionStorage
import com.eeseka.lynk.shared.domain.auth.model.User
import com.eeseka.lynk.shared.domain.notification.NotificationService
import com.eeseka.lynk.shared.domain.notification.UnreadNotificationCounter
import com.eeseka.lynk.shared.domain.util.onSuccess
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update

class InMemoryUnreadNotificationCounter(
    private val notificationService: NotificationService,
    private val sessionStorage: SessionStorage,
    appLifecycleObserver: AppLifecycleObserver,
    applicationScope: CoroutineScope
) : UnreadNotificationCounter {

    private val _count = MutableStateFlow(0L)
    override val count: StateFlow<Long> = _count.asStateFlow()

    init {
        appLifecycleObserver.isInForeground
            .distinctUntilChanged()
            .onEach { isInForeground ->
                if (isInForeground) {
                    refresh()
                }
            }
            .launchIn(applicationScope)

        sessionStorage
            .observeAuthInfo()
            .map { authInfo -> authInfo?.user?.id }
            .distinctUntilChanged()
            .onEach {
                clear()
                refresh()
            }
            .launchIn(applicationScope)
    }

    override suspend fun refresh() {
        val authInfo = sessionStorage.observeAuthInfo().firstOrNull()
        if (authInfo?.user !is User.Authenticated) return

        notificationService.getUnreadCount()
            .onSuccess { unreadCount -> _count.value = unreadCount }
    }

    override fun decrement() {
        _count.update { current -> (current - 1).coerceAtLeast(0L) }
    }

    override fun clear() {
        _count.value = 0L
    }
}
