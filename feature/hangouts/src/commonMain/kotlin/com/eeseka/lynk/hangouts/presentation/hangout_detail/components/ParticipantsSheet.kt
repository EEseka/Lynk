package com.eeseka.lynk.hangouts.presentation.hangout_detail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.eeseka.lynk.shared.design_system.components.modals_and_overlays.LynkAdaptiveSheet
import com.eeseka.lynk.shared.design_system.components.progress_indicator.LynkProgressIndicator
import com.eeseka.lynk.shared.design_system.components.textfields.LynkText
import com.eeseka.lynk.shared.design_system.components.util.AppHaptic
import com.eeseka.lynk.shared.design_system.components.util.rememberAppHaptic
import com.eeseka.lynk.shared.design_system.theme.LynkTheme
import com.eeseka.lynk.shared.domain.hangout.model.RsvpStatus
import com.eeseka.lynk.shared.presentation.hangout.model.HangoutParticipantUi
import com.eeseka.lynk.shared.presentation.preview.PREVIEW_HOST_ID
import com.eeseka.lynk.shared.presentation.preview.previewParticipants
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toImmutableList
import lynk.feature.hangouts.generated.resources.Res
import lynk.feature.hangouts.generated.resources.detail_group_declined
import lynk.feature.hangouts.generated.resources.detail_group_going
import lynk.feature.hangouts.generated.resources.detail_group_pending
import lynk.feature.hangouts.generated.resources.detail_withdraw
import lynk.feature.hangouts.generated.resources.participants_sheet_title
import org.jetbrains.compose.resources.stringResource

@Composable
fun ParticipantsSheet(
    participants: ImmutableList<HangoutParticipantUi>,
    isHost: Boolean,
    onDismiss: () -> Unit,
    onWithdraw: (String) -> Unit,
    withdrawingUserIds: ImmutableSet<String>,
    modifier: Modifier = Modifier,
    presentUserIds: ImmutableSet<String> = persistentSetOf(),
    arePaymentsOn: Boolean = false,
    hostId: String? = null
) {
    LynkAdaptiveSheet(
        onDismissRequest = onDismiss,
        skipBottomSheetPartiallyExpanded = false,
    ) {
        ParticipantsSheetContent(
            participants = participants,
            isHost = isHost,
            onWithdraw = onWithdraw,
            withdrawingUserIds = withdrawingUserIds,
            presentUserIds = presentUserIds,
            arePaymentsOn = arePaymentsOn,
            hostId = hostId,
            modifier = modifier.weight(1f, fill = false)
        )
    }
}

@Composable
private fun ParticipantsSheetContent(
    participants: ImmutableList<HangoutParticipantUi>,
    isHost: Boolean,
    onWithdraw: (String) -> Unit,
    withdrawingUserIds: ImmutableSet<String>,
    presentUserIds: ImmutableSet<String>,
    arePaymentsOn: Boolean,
    hostId: String?,
    modifier: Modifier = Modifier
) {
    val going = participants.filter { it.rsvpStatus == RsvpStatus.ATTENDING }
    val pending = participants.filter { it.rsvpStatus == RsvpStatus.PENDING }
    val declined = participants.filter { it.rsvpStatus == RsvpStatus.DECLINED }

    Column(modifier = modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
        LynkText(
            text = stringResource(Res.string.participants_sheet_title),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        val goingTitle = stringResource(Res.string.detail_group_going)
        val pendingTitle = stringResource(Res.string.detail_group_pending)
        val declinedTitle = stringResource(Res.string.detail_group_declined)

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            participantGroup(
                title = goingTitle,
                participants = going,
                presentUserIds = presentUserIds,
                showHeader = isHost,
                arePaymentsOn = arePaymentsOn,
                hostId = hostId
            )

            if (isHost && pending.isNotEmpty()) {
                participantGroup(
                    title = pendingTitle,
                    participants = pending,
                    trailing = { participant ->
                        WithdrawControl(
                            isLoading = participant.user.userId in withdrawingUserIds,
                            onClick = { onWithdraw(participant.user.userId) }
                        )
                    }
                )
            }

            if (isHost && declined.isNotEmpty()) {
                participantGroup(
                    title = declinedTitle,
                    participants = declined
                )
            }
        }
    }
}

private fun LazyListScope.participantGroup(
    title: String,
    participants: List<HangoutParticipantUi>,
    presentUserIds: Set<String> = emptySet(),
    showHeader: Boolean = true,
    arePaymentsOn: Boolean = false,
    hostId: String? = null,
    trailing: (@Composable (HangoutParticipantUi) -> Unit)? = null
) {
    if (participants.isEmpty()) return

    if (showHeader) {
        item(key = "header-$title") {
            LynkText(
                text = "$title · ${participants.size}",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
    items(items = participants, key = { it.user.userId }) { participant ->
        Box(modifier = Modifier.animateItem()) {
            ParticipantCard(
                participant = participant,
                isOnline = participant.user.userId in presentUserIds,
                showPaidBadge = arePaymentsOn &&
                        participant.hasPaid &&
                        participant.user.userId != hostId,
                showHostBadge = participant.user.userId == hostId,
                trailing = trailing?.let { { it(participant) } },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun WithdrawControl(
    isLoading: Boolean,
    onClick: () -> Unit
) {
    val hapticFeedback = rememberAppHaptic()

    if (isLoading) {
        LynkProgressIndicator(
            modifier = Modifier.size(16.dp),
            color = MaterialTheme.colorScheme.error
        )
    } else {
        LynkText(
            text = stringResource(Res.string.detail_withdraw),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.error,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .minimumInteractiveComponentSize()
                .clip(CircleShape)
                .clickable {
                    hapticFeedback(AppHaptic.ImpactMedium)
                    onClick()
                }
                .padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

// The host is paid by the server but never badged; the guests split across going, pending and declined.
private val previewRoster = previewParticipants(count = 6, paidGuestCount = 1)
    .mapIndexed { index, participant ->
        participant.copy(
            rsvpStatus = when (index) {
                in 0..2 -> RsvpStatus.ATTENDING
                3, 4 -> RsvpStatus.PENDING
                else -> RsvpStatus.DECLINED
            }
        )
    }
    .toImmutableList()

@PreviewLightDark
@Composable
private fun ParticipantsSheetHostPreview() = ParticipantsSheetPreview(isHost = true)

@PreviewLightDark
@Composable
private fun ParticipantsSheetAttendeePreview() = ParticipantsSheetPreview(isHost = false)

@Composable
private fun ParticipantsSheetPreview(isHost: Boolean) {
    LynkTheme {
        ParticipantsSheetContent(
            participants = previewRoster,
            isHost = isHost,
            onWithdraw = {},
            withdrawingUserIds = persistentSetOf(),
            presentUserIds = persistentSetOf(PREVIEW_HOST_ID, "user-2"),
            arePaymentsOn = true,
            hostId = PREVIEW_HOST_ID,
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface)
                .padding(vertical = 20.dp)
        )
    }
}
