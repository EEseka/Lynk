package com.eeseka.lynk.main_shell.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eeseka.lynk.shared.domain.auth.SessionStorage
import com.eeseka.lynk.shared.domain.auth.model.User
import com.eeseka.lynk.shared.domain.notification.UnreadNotificationCounter
import com.eeseka.lynk.shared.domain.settings.AppPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainShellViewModel(
    private val unreadNotificationCounter: UnreadNotificationCounter,
    private val sessionStorage: SessionStorage,
    private val appPreferences: AppPreferences
) : ViewModel() {

    private val _state = MutableStateFlow(MainShellState())
    private var hasLoadedInitialData = false
    private var lastSeenUnreadCount = 0
    private var isOnHangoutsTab = false

    val state = _state
        .onStart {
            if (!hasLoadedInitialData) {
                observeUnreadCount()
                observeCanReceiveNotifications()
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
            MainShellAction.OnNotificationPermissionDenied -> disablePushNotifications()
            is MainShellAction.OnHangoutsTabActiveChanged -> setHangoutsTabActive(action.isActive)
            is MainShellAction.OnHangoutDetailPaneFullScreenChanged -> {
                _state.update { it.copy(isHangoutDetailPaneFullScreen = action.isFullScreen) }
            }
        }
    }

    private fun observeCanReceiveNotifications() {
        sessionStorage.observeAuthInfo()
            .onEach { authInfo ->
                val user = authInfo?.user
                _state.update { it.copy(canReceiveNotifications = user != null && user !is User.Guest) }
            }
            .launchIn(viewModelScope)
    }

    private fun disablePushNotifications() {
        viewModelScope.launch {
            appPreferences.setPushNotificationsEnabled(false)
        }
    }

    private fun observeUnreadCount() {
        unreadNotificationCounter.count
            .onEach { count ->
                val unreadCount = count.toInt()

                lastSeenUnreadCount = if (isOnHangoutsTab) unreadCount
                else minOf(lastSeenUnreadCount, unreadCount)

                _state.update {
                    it.copy(
                        unreadNotificationCount = unreadCount,
                        hasUnseenNotifications = unreadCount > lastSeenUnreadCount
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    private fun setHangoutsTabActive(isActive: Boolean) {
        isOnHangoutsTab = isActive
        if (!isActive) return

        lastSeenUnreadCount = state.value.unreadNotificationCount
        _state.update { it.copy(hasUnseenNotifications = false) }

        viewModelScope.launch {
            unreadNotificationCounter.refresh()
        }
    }
}