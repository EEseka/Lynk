package com.eeseka.lynk.notifications.presentation.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eeseka.lynk.shared.domain.notification.NotificationService
import com.eeseka.lynk.shared.domain.notification.model.Notification
import com.eeseka.lynk.shared.domain.notification.model.NotificationType
import com.eeseka.lynk.shared.domain.util.DataErrorException
import com.eeseka.lynk.shared.domain.util.Paginator
import com.eeseka.lynk.shared.domain.util.onFailure
import com.eeseka.lynk.shared.domain.util.onSuccess
import com.eeseka.lynk.shared.presentation.notification.mappers.toNotificationUi
import com.eeseka.lynk.shared.presentation.util.toUiText
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class NotificationsViewModel(
    private val notificationService: NotificationService
) : ViewModel() {

    private val eventChannel = Channel<NotificationsEvent>()
    val events = eventChannel.receiveAsFlow()

    private val _state = MutableStateFlow(NotificationsState())
    private var hasLoadedInitialData = false

    private var notificationsPaginator: Paginator<String?, Notification>? = null

    val state = _state
        .onStart {
            if (!hasLoadedInitialData) {
                setupNotificationsPaginator()
                notificationsPaginator?.loadNextItems()
                hasLoadedInitialData = true
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = NotificationsState()
        )

    fun onAction(action: NotificationsAction) {
        when (action) {
            is NotificationsAction.OnNotificationClick -> openNotification(
                notificationId = action.notificationId,
                hangoutId = action.hangoutId,
                type = action.type
            )
            is NotificationsAction.OnOpenInvitePreview -> _state.update { it.copy(previewHangoutId = action.hangoutId) }
            NotificationsAction.OnMarkAllReadClick -> markAllAsRead()
            NotificationsAction.OnDismissInvitePreview -> _state.update { it.copy(previewHangoutId = null) }
            NotificationsAction.LoadNextPage -> loadNextPage()
        }
    }

    private fun openNotification(
        notificationId: String,
        hangoutId: String,
        type: NotificationType
    ) {
        val isAlreadyRead = state.value.notifications
            .any { it.id == notificationId && it.isRead }

        if (!isAlreadyRead) {
            markAsRead(notificationId)
        }

        when (type) {
            NotificationType.PARTICIPANT_INVITED -> _state.update { it.copy(previewHangoutId = hangoutId) }
            NotificationType.INVITE_CANCELLED, NotificationType.REMOVED_FOR_NON_PAYMENT -> Unit
            else -> viewModelScope.launch {
                eventChannel.send(NotificationsEvent.NavigateToHangout(hangoutId))
            }
        }
    }

    private fun markAsRead(notificationId: String) {
        updateNotificationReadState(notificationId, isRead = true)

        viewModelScope.launch {
            notificationService.markAsRead(notificationId)
                .onFailure {
                    updateNotificationReadState(notificationId, isRead = false)
                }
        }
    }

    private fun updateNotificationReadState(notificationId: String, isRead: Boolean) {
        _state.update { currentState ->
            currentState.copy(
                notifications = currentState.notifications.map {
                    if (it.id == notificationId) it.copy(isRead = isRead) else it
                }
            )
        }
    }

    private fun markAllAsRead() {
        _state.update { it.copy(isMarkingAllRead = true) }

        viewModelScope.launch {
            notificationService.markAllAsRead()
                .onSuccess {
                    _state.update { currentState ->
                        currentState.copy(
                            isMarkingAllRead = false,
                            notifications = currentState.notifications.map { it.copy(isRead = true) }
                        )
                    }
                }
                .onFailure { error ->
                    _state.update { it.copy(isMarkingAllRead = false) }
                    eventChannel.send(NotificationsEvent.Error(error.toUiText()))
                }
        }
    }

    private fun setupNotificationsPaginator() {
        notificationsPaginator = Paginator(
            initialKey = null,
            onLoadUpdated = { isLoading ->
                _state.update { it.copy(isLoading = isLoading) }
            },
            onRequest = { beforeTimestamp ->
                notificationService.getNotifications(before = beforeTimestamp)
            },
            getNextKey = { notifications ->
                notifications.minOfOrNull { it.createdAt }?.toString()
            },
            onError = { throwable ->
                if (throwable is DataErrorException) {
                    eventChannel.send(NotificationsEvent.Error(throwable.error.toUiText()))
                }
            },
            onSuccess = { newNotifications, _ ->
                _state.update {
                    it.copy(
                        notifications = it.notifications + newNotifications.map { notification -> notification.toNotificationUi() },
                        isEndReached = newNotifications.isEmpty()
                    )
                }
            }
        )
    }

    private fun loadNextPage() {
        viewModelScope.launch {
            notificationsPaginator?.loadNextItems()
        }
    }
}