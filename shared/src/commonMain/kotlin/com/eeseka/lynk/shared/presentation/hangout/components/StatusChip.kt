package com.eeseka.lynk.shared.presentation.hangout.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
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
            color = foreground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@PreviewLightDark
@Composable
private fun StatusChipPreview() {
    LynkTheme {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            HangoutStatus.entries.forEach { status ->
                StatusChip(status = status)
            }
        }
    }
}
