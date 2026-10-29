package com.eeseka.lynk.assistant.presentation

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Stable
import com.eeseka.lynk.assistant.presentation.model.AssistantMessageUi
import com.eeseka.lynk.shared.domain.assistant.model.AssistantStatusKind
import com.eeseka.lynk.shared.domain.hangout.model.HangoutStatus
import com.eeseka.lynk.shared.domain.location.LocationCoordinates
import com.eeseka.lynk.shared.presentation.spot.model.SpotUi
import com.eeseka.lynk.shared.presentation.util.UiText
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf
import kotlin.time.Instant

@Stable
data class AssistantState(
    // A hangout chat has a hangout id; a Discover chat has the map area it was opened on
    val hangoutId: String? = null,
    val searchCenter: LocationCoordinates? = null,
    val areaName: String? = null,
    val discoverChatId: String? = null,

    // The hangout the chat is about
    val hangoutName: String? = null,
    val hangoutStatus: HangoutStatus? = null,
    val isAttending: Boolean? = null,
    val ballotSpotIds: ImmutableSet<String> = persistentSetOf(),
    val center: LocationCoordinates? = null,
    val suggestingSpotIds: ImmutableSet<String> = persistentSetOf(),

    val messages: ImmutableList<AssistantMessageUi> = persistentListOf(),
    val isLoading: Boolean = false,
    val isEndReached: Boolean = false,
    val loadError: UiText? = null,
    val messagesResetEpoch: Int = 0,

    val messageTextState: TextFieldState = TextFieldState(),
    val isAnswering: Boolean = false,
    val answerStatus: AssistantStatusKind? = null,
    val isClearingChat: Boolean = false,

    // Today's messages, shared by every chat
    val messagesLeft: Int? = null,
    val messageLimit: Int? = null,
    val usageResetsAt: Instant? = null,

    // Spot sheet
    val selectedSpot: SpotUi? = null,
    val loadingSpotId: String? = null
)
