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
import lynk.feature.hangouts.generated.resources.Res
import lynk.feature.hangouts.generated.resources.detail_group_declined
import lynk.feature.hangouts.generated.resources.detail_group_going
import lynk.feature.hangouts.generated.resources.detail_group_pending
import lynk.feature.hangouts.generated.resources.detail_withdraw
import lynk.feature.hangouts.generated.resources.participants_sheet_title
import org.jetbrains.compose.resources.stringResource

@Composable
fun ParticipantsSheet(
    participants: List<HangoutParticipantUi>,
    isHost: Boolean,
    onDismiss: () -> Unit,
    onWithdraw: (String) -> Unit,
    withdrawingUserIds: Set<String>,
    presentUserIds: Set<String> = emptySet(),
    modifier: Modifier = Modifier
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
            modifier = modifier
        )
    }
}

@Composable
private fun ParticipantsSheetContent(
    participants: List<HangoutParticipantUi>,
    isHost: Boolean,
    onWithdraw: (String) -> Unit,
    withdrawingUserIds: Set<String>,
    presentUserIds: Set<String>,
    modifier: Modifier = Modifier
) {
    val going = participants.filter { it.rsvpStatus == RsvpStatus.ATTENDING }
    val pending = participants.filter { it.rsvpStatus == RsvpStatus.PENDING }
    val declined = participants.filter { it.rsvpStatus == RsvpStatus.DECLINED }

    Column(modifier = modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
        LynkText(
            text = stringResource(Res.string.participants_sheet_title),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )

        val goingTitle = stringResource(Res.string.detail_group_going)
        val pendingTitle = stringResource(Res.string.detail_group_pending)
        val declinedTitle = stringResource(Res.string.detail_group_declined)

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            participantGroup(
                title = goingTitle,
                participants = going,
                presentUserIds = presentUserIds,
                showHeader = isHost
            )

            if (isHost && pending.isNotEmpty()) {
                participantGroup(
                    title = pendingTitle,
                    participants = pending,
                    trailing = { participant ->
                        WithdrawControl(
                            isLoading = participant.userId in withdrawingUserIds,
                            onClick = { onWithdraw(participant.userId) }
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
    items(items = participants, key = { it.userId }) { participant ->
        Box(modifier = Modifier.animateItem()) {
            ParticipantCard(
                participant = participant,
                isOnline = participant.userId in presentUserIds,
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

private fun previewParticipant(
    id: String,
    name: String,
    username: String,
    initials: String,
    status: RsvpStatus
) = HangoutParticipantUi(
    userId = id,
    username = username,
    displayName = name,
    initials = initials,
    profilePictureUrl = null,
    rsvpStatus = status,
    hasPaid = false
)

private val previewRoster = listOf(
    previewParticipant("1", "Eseka Emmanuel", "e.eseka", "EE", RsvpStatus.ATTENDING),
    previewParticipant("2", "Ada Obi", "ada", "AO", RsvpStatus.ATTENDING),
    previewParticipant("3", "Tunde Bello", "tunde", "TB", RsvpStatus.ATTENDING),
    previewParticipant("4", "Chidi Okafor", "chidi", "CO", RsvpStatus.PENDING),
    previewParticipant("5", "Zainab Musa", "zainab", "ZM", RsvpStatus.PENDING),
    previewParticipant("6", "Femi Ade", "femi", "FA", RsvpStatus.DECLINED)
)

@Composable
private fun ParticipantsSheetPreview(
    isHost: Boolean,
    withdrawingUserIds: Set<String> = emptySet()
) {
    LynkTheme {
        ParticipantsSheetContent(
            participants = previewRoster,
            isHost = isHost,
            onWithdraw = {},
            withdrawingUserIds = withdrawingUserIds,
            presentUserIds = setOf("1", "3"),
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface)
                .padding(vertical = 20.dp)
        )
    }
}

@PreviewLightDark
@Composable
private fun ParticipantsSheetHostPreview() = ParticipantsSheetPreview(isHost = true)

@PreviewLightDark
@Composable
private fun ParticipantsSheetAttendeePreview() = ParticipantsSheetPreview(isHost = false)

@PreviewLightDark
@Composable
private fun ParticipantsSheetWithdrawingPreview() = ParticipantsSheetPreview(isHost = true, withdrawingUserIds = setOf("4"))