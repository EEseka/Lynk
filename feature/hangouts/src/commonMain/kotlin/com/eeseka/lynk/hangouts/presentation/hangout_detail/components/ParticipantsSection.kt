package com.eeseka.lynk.hangouts.presentation.hangout_detail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.ChevronRight
import com.composables.icons.lucide.Lucide
import com.eeseka.lynk.shared.design_system.components.textfields.LynkText
import com.eeseka.lynk.shared.design_system.components.util.AppHaptic
import com.eeseka.lynk.shared.design_system.components.util.rememberAppHaptic
import com.eeseka.lynk.shared.design_system.theme.LynkTheme
import com.eeseka.lynk.shared.domain.hangout.model.RsvpStatus
import com.eeseka.lynk.shared.presentation.hangout.model.HangoutParticipantUi
import com.eeseka.lynk.shared.presentation.hangout.model.HangoutUserUi
import lynk.feature.hangouts.generated.resources.Res
import lynk.feature.hangouts.generated.resources.detail_going
import lynk.feature.hangouts.generated.resources.detail_going_count
import lynk.feature.hangouts.generated.resources.detail_going_count_max
import lynk.feature.hangouts.generated.resources.detail_see_all
import org.jetbrains.compose.resources.stringResource

@Composable
fun ParticipantsSection(
    participants: List<HangoutParticipantUi>,
    participantCount: Int,
    maxAttendees: Int?,
    presentUserIds: Set<String>,
    onSeeAllClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val goingText = maxAttendees?.let { max ->
        stringResource(Res.string.detail_going_count_max, participantCount, max)
    } ?: stringResource(Res.string.detail_going_count, participantCount)

    val attending = participants.filter { it.rsvpStatus == RsvpStatus.ATTENDING }
    val hapticFeedback = rememberAppHaptic()

    DetailSection(
        title = stringResource(Res.string.detail_going),
        trailing = {
            LynkText(
                text = goingText,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ParticipantStack(
                participants = attending,
                presentUserIds = presentUserIds,
                modifier = Modifier.weight(1f, fill = false)
            )

            if (attending.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .clip(CircleShape)
                        .clickable {
                            hapticFeedback(AppHaptic.ImpactLight)
                            onSeeAllClick()
                        }
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LynkText(
                        text = stringResource(Res.string.detail_see_all),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                    Icon(
                        imageVector = Lucide.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

private fun previewParticipants(count: Int) = List(count) { index ->
    HangoutParticipantUi(
        user = HangoutUserUi(
            userId = "$index",
            username = "user$index",
            displayName = "Guest $index",
            initials = "G$index",
            profilePictureUrl = null
        ),
        rsvpStatus = RsvpStatus.ATTENDING,
        hasPaid = false
    )
}

@Composable
private fun ParticipantsSectionPreview(
    participants: List<HangoutParticipantUi> = previewParticipants(6),
    participantCount: Int = 6,
    maxAttendees: Int? = 10,
    presentUserIds: Set<String> = setOf("0", "2")
) {
    LynkTheme {
        ParticipantsSection(
            participants = participants,
            participantCount = participantCount,
            maxAttendees = maxAttendees,
            presentUserIds = presentUserIds,
            onSeeAllClick = {},
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        )
    }
}

@PreviewLightDark
@Composable
private fun ParticipantsSectionDefaultPreview() = ParticipantsSectionPreview()

@PreviewLightDark
@Composable
private fun ParticipantsSectionNoCapPreview() = ParticipantsSectionPreview(maxAttendees = null)

@PreviewLightDark
@Composable
private fun ParticipantsSectionFullPreview() = ParticipantsSectionPreview(
    participants = previewParticipants(10),
    participantCount = 10,
    maxAttendees = 10
)

@PreviewLightDark
@Composable
private fun ParticipantsSectionEmptyPreview() = ParticipantsSectionPreview(
    participants = emptyList(),
    participantCount = 0,
    presentUserIds = emptySet()
)
