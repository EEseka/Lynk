package com.eeseka.lynk.main_shell.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eeseka.lynk.shared.domain.auth.SessionStorage
import com.eeseka.lynk.shared.domain.auth.model.User
import com.eeseka.lynk.shared.domain.notification.NotificationService
import com.eeseka.lynk.shared.domain.util.onSuccess
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainShellViewModel(
    private val notificationService: NotificationService,
    private val sessionStorage: SessionStorage
) : ViewModel() {

    private val _state = MutableStateFlow(MainShellState())
    private var hasLoadedInitialData = false
    private var lastSeenUnreadCount = 0

    val state = _state
        .onStart {
            if (!hasLoadedInitialData) {
                fetchUnreadCount()
                hasLoadedInitialData = true
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = MainShellState()
        )

    fun onAction(action: MainShellAction) {
        when (action) {
            MainShellAction.RefreshUnreadCount -> fetchUnreadCount()
            MainShellAction.HangoutsTabSeen -> markHangoutsTabSeen()
        }
    }

    private fun fetchUnreadCount() {
        viewModelScope.launch {
            val isGuest = sessionStorage.observeAuthInfo().firstOrNull()?.user is User.Guest
            if (isGuest) return@launch

            notificationService.getUnreadCount()
                .onSuccess { count ->
                    val unreadCount = count.toInt()

                    lastSeenUnreadCount = minOf(lastSeenUnreadCount, unreadCount)

                    _state.update {
                        it.copy(
                            unreadNotificationCount = unreadCount,
                            hasUnseenNotifications = unreadCount > lastSeenUnreadCount
                        )
                    }
                }
        }
    }

    private fun markHangoutsTabSeen() {
        lastSeenUnreadCount = state.value.unreadNotificationCount
        _state.update { it.copy(hasUnseenNotifications = false) }
    }
}