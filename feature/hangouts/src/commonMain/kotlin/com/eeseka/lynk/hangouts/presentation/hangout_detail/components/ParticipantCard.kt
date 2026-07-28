package com.eeseka.lynk.hangouts.presentation.hangout_detail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.eeseka.lynk.shared.design_system.components.textfields.LynkText
import com.eeseka.lynk.shared.design_system.theme.LynkTheme
import com.eeseka.lynk.shared.domain.hangout.model.RsvpStatus
import com.eeseka.lynk.shared.presentation.hangout.model.HangoutParticipantUi

@Composable
fun ParticipantCard(
    participant: HangoutParticipantUi,
    isOnline: Boolean = false,
    trailing: @Composable (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ParticipantAvatar(
            displayName = participant.displayName,
            initials = participant.initials,
            profilePictureUrl = participant.profilePictureUrl,
            isOnline = isOnline,
            size = 44.dp
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            LynkText(
                text = participant.displayName,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            LynkText(
                text = "@${participant.username}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        trailing?.invoke()
    }
}

@PreviewLightDark
@Composable
fun ParticipantCardPreview() {
    LynkTheme {
        ParticipantCard(
            participant = HangoutParticipantUi(
                userId = "0",
                username = "e.eseka",
                displayName = "Eseka Emmanuel",
                initials = "EE",
                profilePictureUrl = null,
                rsvpStatus = RsvpStatus.ATTENDING,
                hasPaid = false
            ),
            isOnline = true,
            modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface)
        )
    }
}