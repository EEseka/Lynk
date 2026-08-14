package com.eeseka.lynk.hangouts.presentation.hangout_detail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.eeseka.lynk.shared.design_system.components.textfields.LynkText
import com.eeseka.lynk.shared.design_system.theme.LynkTheme
import com.eeseka.lynk.shared.design_system.theme.extended
import com.eeseka.lynk.shared.domain.hangout.model.HangoutStatus
import com.eeseka.lynk.shared.presentation.hangout.mappers.getTitle

@Composable
fun StatusChip(
    status: HangoutStatus,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val (background, foreground) = when (status) {
        HangoutStatus.ONGOING -> scheme.extended.success to scheme.extended.onSuccess
        HangoutStatus.SCHEDULED -> scheme.secondary to scheme.onSecondary
        HangoutStatus.VOTING -> scheme.tertiary to scheme.onTertiary
        HangoutStatus.COMPLETED -> scheme.surfaceVariant to scheme.onSurfaceVariant
        HangoutStatus.CANCELLED -> scheme.error to scheme.onError
    }
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(background)
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        LynkText(
            text = status.getTitle(),
            style = MaterialTheme.typography.labelSmall,
            color = foreground
        )
    }
}

@Composable
private fun StatusChipPreview(status: HangoutStatus) {
    LynkTheme {
        StatusChip(
            status = status,
            modifier = Modifier.background(MaterialTheme.colorScheme.background)
        )
    }
}

@PreviewLightDark
@Composable
private fun StatusChipVotingPreview() = StatusChipPreview(HangoutStatus.VOTING)

@PreviewLightDark
@Composable
private fun StatusChipScheduledPreview() = StatusChipPreview(HangoutStatus.SCHEDULED)

@PreviewLightDark
@Composable
private fun StatusChipOngoingPreview() = StatusChipPreview(HangoutStatus.ONGOING)

@PreviewLightDark
@Composable
private fun StatusChipCompletedPreview() = StatusChipPreview(HangoutStatus.COMPLETED)

@PreviewLightDark
@Composable
private fun StatusChipCancelledPreview() = StatusChipPreview(HangoutStatus.CANCELLED)
