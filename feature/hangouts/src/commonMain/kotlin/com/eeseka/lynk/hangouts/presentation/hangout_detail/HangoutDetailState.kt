package com.eeseka.lynk.hangouts.presentation.hangout_detail

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Stable
import com.eeseka.lynk.shared.domain.lobby.model.ConnectionState
import com.eeseka.lynk.shared.presentation.hangout.model.HangoutUi
import com.eeseka.lynk.shared.presentation.hangout.model.HangoutUserUi
import com.eeseka.lynk.shared.presentation.util.UiText
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentSetOf

@Stable
data class HangoutDetailState(
    val hangout: HangoutUi? = null,
    val currentUserId: String? = null,
    val isLoading: Boolean = false,
    val error: UiText? = null,
    val connectionState: ConnectionState = ConnectionState.DISCONNECTED,
    val presentUserIds: ImmutableSet<String> = persistentSetOf(),
    val isCompleting: Boolean = false,
    val isCancelling: Boolean = false,
    val isLeaving: Boolean = false,
    val withdrawingUserIds: ImmutableSet<String> = persistentSetOf(),

    // Invite search sheet
    val isInviteSheetOpen: Boolean = false,
    val inviteQueryState: TextFieldState = TextFieldState(),
    val inviteResult: HangoutUserUi? = null,
    val isInviteSearching: Boolean = false,
    val inviteNotFound: Boolean = false,
    val isInviting: Boolean = false
)