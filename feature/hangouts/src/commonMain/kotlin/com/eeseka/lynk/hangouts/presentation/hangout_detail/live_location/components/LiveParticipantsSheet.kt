package com.eeseka.lynk.hangouts.presentation.hangout_detail.live_location.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.eeseka.lynk.hangouts.presentation.hangout_detail.components.ParticipantCard
import com.eeseka.lynk.hangouts.presentation.hangout_detail.live_location.model.LiveParticipantUi
import com.eeseka.lynk.shared.design_system.components.modals_and_overlays.LynkAdaptiveSheet
import com.eeseka.lynk.shared.design_system.components.textfields.LynkText
import com.eeseka.lynk.shared.design_system.theme.LynkTheme
import com.eeseka.lynk.shared.domain.hangout.model.RsvpStatus
import com.eeseka.lynk.shared.presentation.hangout.model.HangoutParticipantUi
import com.eeseka.lynk.shared.presentation.preview.PREVIEW_HOST_ID
import com.eeseka.lynk.shared.presentation.preview.previewUsers
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import lynk.feature.hangouts.generated.resources.Res
import lynk.feature.hangouts.generated.resources.map_shared_just_now
import lynk.feature.hangouts.generated.resources.map_shared_minutes_ago
import org.jetbrains.compose.resources.stringResource

@Composable
fun LiveParticipantsSheet(
    people: ImmutableList<LiveParticipantUi>,
    hostId: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    LynkAdaptiveSheet(
        onDismissRequest = onDismiss,
        modifier = modifier
    ) {
        LiveParticipantsSheetContent(
            people = people,
            hostId = hostId
        )
    }
}

@Composable
private fun LiveParticipantsSheetContent(
    people: ImmutableList<LiveParticipantUi>,
    hostId: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        people.forEach { person ->
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                ParticipantCard(
                    participant = person.participant,
                    showHostBadge = person.participant.user.userId == hostId,
                    showHereBadge = person.participant.hasArrived
                )
                LynkText(
                    text = if (person.minutesSinceShared < 1) {
                        stringResource(Res.string.map_shared_just_now)
                    } else {
                        stringResource(Res.string.map_shared_minutes_ago, person.minutesSinceShared)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 56.dp)
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun LiveParticipantsSheetContentPreview() {
    LynkTheme {
        LiveParticipantsSheetContent(
            people = previewUsers(2).mapIndexed { index, user ->
                LiveParticipantUi(
                    participant = HangoutParticipantUi(
                        user = user,
                        rsvpStatus = RsvpStatus.ATTENDING,
                        hasPaid = false,
                        hasArrived = index == 0
                    ),
                    latitude = 6.4433,
                    longitude = 3.4555,
                    minutesSinceShared = index * 2
                )
            }.toImmutableList(),
            hostId = PREVIEW_HOST_ID,
            modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainerLow)
        )
    }
}
