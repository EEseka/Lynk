package com.eeseka.lynk.notifications.presentation.invite_preview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eeseka.lynk.shared.domain.hangout.HangoutService
import com.eeseka.lynk.shared.domain.hangout.model.RsvpStatus
import com.eeseka.lynk.shared.domain.util.DataError
import com.eeseka.lynk.shared.domain.util.onFailure
import com.eeseka.lynk.shared.domain.util.onSuccess
import com.eeseka.lynk.shared.presentation.hangout.mappers.toHangoutPreviewUi
import com.eeseka.lynk.shared.presentation.util.toUiText
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class InvitePreviewViewModel(
    private val hangoutService: HangoutService
) : ViewModel() {

    private val eventChannel = Channel<InvitePreviewEvent>()
    val events = eventChannel.receiveAsFlow()

    private val _state = MutableStateFlow(InvitePreviewState())
    val state = _state.asStateFlow()

    fun onAction(action: InvitePreviewAction) {
        when (action) {
            is InvitePreviewAction.Init -> loadPreview(action.hangoutId)
            InvitePreviewAction.OnAcceptClick -> respond(RsvpStatus.ATTENDING)
            InvitePreviewAction.OnDeclineClick -> respond(RsvpStatus.DECLINED)
        }
    }

    private fun loadPreview(hangoutId: String) {
        _state.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            hangoutService.getHangoutPreview(hangoutId)
                .onSuccess { preview ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            hangoutPreview = preview.toHangoutPreviewUi()
                        )
                    }
                }
                .onFailure { error ->
                    _state.update { it.copy(isLoading = false) }

                    when (error) {
                        DataError.Remote.FORBIDDEN -> eventChannel.send(InvitePreviewEvent.AlreadyAnswered(hangoutId))
                        DataError.Remote.NOT_FOUND -> eventChannel.send(InvitePreviewEvent.InviteWithdrawn)
                        else -> eventChannel.send(InvitePreviewEvent.Error(error.toUiText()))
                    }
                }
        }
    }

    private fun respond(rsvpStatus: RsvpStatus) {
        val hangoutId = state.value.hangoutPreview?.id ?: return

        _state.update { it.copy(respondingTo = rsvpStatus) }

        viewModelScope.launch {
            hangoutService.updateRsvp(hangoutId = hangoutId, rsvpStatus = rsvpStatus)
                .onSuccess {
                    _state.update { it.copy(respondingTo = null) }

                    if (rsvpStatus == RsvpStatus.ATTENDING) {
                        eventChannel.send(InvitePreviewEvent.Accepted(hangoutId))
                    } else {
                        eventChannel.send(InvitePreviewEvent.Dismissed)
                    }
                }
                .onFailure { error ->
                    _state.update { it.copy(respondingTo = null) }

                    when (error) {
                        DataError.Remote.NOT_FOUND -> eventChannel.send(InvitePreviewEvent.InviteWithdrawn)
                        else -> eventChannel.send(InvitePreviewEvent.Error(error.toUiText()))
                    }
                }
        }
    }
}