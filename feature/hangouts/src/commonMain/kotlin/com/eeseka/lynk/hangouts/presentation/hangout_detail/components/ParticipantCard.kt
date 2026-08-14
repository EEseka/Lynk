package com.eeseka.lynk.hangouts.presentation.hangout_detail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Crown
import com.composables.icons.lucide.Lucide
import com.eeseka.lynk.shared.design_system.components.textfields.LynkText
import com.eeseka.lynk.shared.design_system.theme.LynkTheme
import com.eeseka.lynk.shared.design_system.theme.extended
import com.eeseka.lynk.shared.domain.hangout.model.RsvpStatus
import com.eeseka.lynk.shared.presentation.hangout.model.HangoutParticipantUi
import com.eeseka.lynk.shared.presentation.hangout.model.HangoutUserUi
import lynk.feature.hangouts.generated.resources.Res
import lynk.feature.hangouts.generated.resources.payment_host_badge
import lynk.feature.hangouts.generated.resources.payment_paid_badge
import org.jetbrains.compose.resources.stringResource

@Composable
fun ParticipantCard(
    participant: HangoutParticipantUi,
    isOnline: Boolean = false,
    showPaidBadge: Boolean = false,
    showHostBadge: Boolean = false,
    trailing: @Composable (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ParticipantAvatar(
            displayName = participant.user.displayName,
            initials = participant.user.initials,
            profilePictureUrl = participant.user.profilePictureUrl,
            isOnline = isOnline,
            size = 44.dp
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            LynkText(
                text = participant.user.displayName,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            LynkText(
                text = "@${participant.user.username}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (showHostBadge) {
            HostBadge()
        }
        if (showPaidBadge) {
            PaidBadge()
        }
        trailing?.invoke()
    }
}

@Composable
private fun HostBadge(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.extended.gold)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Lucide.Crown,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.extended.onGold,
            modifier = Modifier.size(12.dp)
        )
        LynkText(
            text = stringResource(Res.string.payment_host_badge),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.extended.onGold
        )
    }
}

@Composable
private fun PaidBadge(modifier: Modifier = Modifier) {
    LynkText(
        text = stringResource(Res.string.payment_paid_badge),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.extended.onSuccessContainer,
        modifier = modifier
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.extended.successContainer)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    )
}

@Composable
private fun ParticipantCardPreview(
    displayName: String = "Eseka Emmanuel",
    username: String = "e.eseka",
    initials: String = "EE",
    hasPaid: Boolean = true,
    isOnline: Boolean = true,
    showPaidBadge: Boolean = false,
    showHostBadge: Boolean = false
) {
    LynkTheme {
        ParticipantCard(
            participant = HangoutParticipantUi(
                user = HangoutUserUi(
                    userId = "0",
                    username = username,
                    displayName = displayName,
                    initials = initials,
                    profilePictureUrl = null
                ),
                rsvpStatus = RsvpStatus.ATTENDING,
                hasPaid = hasPaid
            ),
            isOnline = isOnline,
            showPaidBadge = showPaidBadge,
            showHostBadge = showHostBadge,
            modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface)
        )
    }
}

@PreviewLightDark
@Composable
private fun ParticipantCardPaidPreview() = ParticipantCardPreview(showPaidBadge = true)

@PreviewLightDark
@Composable
private fun ParticipantCardHostPreview() = ParticipantCardPreview(
    displayName = "Ada Obi",
    username = "the.host",
    initials = "AO",
    showHostBadge = true
)