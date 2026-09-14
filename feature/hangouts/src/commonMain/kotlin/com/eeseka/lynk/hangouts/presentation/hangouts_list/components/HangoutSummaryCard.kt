package com.eeseka.lynk.hangouts.presentation.hangouts_list.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import com.composables.icons.lucide.Calendar
import com.composables.icons.lucide.Crown
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Users
import com.eeseka.lynk.shared.design_system.components.layouts.LynkCard
import com.eeseka.lynk.shared.design_system.components.layouts.LynkCardStyle
import com.eeseka.lynk.shared.design_system.components.textfields.LynkText
import com.eeseka.lynk.shared.design_system.theme.LynkTheme
import com.eeseka.lynk.shared.design_system.theme.extended
import com.eeseka.lynk.shared.domain.hangout.model.HangoutStatus
import com.eeseka.lynk.shared.presentation.hangout.components.StatusChip
import com.eeseka.lynk.shared.presentation.hangout.mappers.getIcon
import com.eeseka.lynk.shared.presentation.hangout.model.HangoutSummaryUi
import com.eeseka.lynk.shared.presentation.preview.previewHangoutSummaries
import lynk.feature.hangouts.generated.resources.Res
import lynk.feature.hangouts.generated.resources.host_badge_description
import lynk.feature.hangouts.generated.resources.participants_attended_format
import lynk.feature.hangouts.generated.resources.participants_attended_max_format
import lynk.feature.hangouts.generated.resources.participants_format
import lynk.feature.hangouts.generated.resources.participants_max_format
import org.jetbrains.compose.resources.stringResource

@Composable
fun HangoutSummaryCard(
    hangout: HangoutSummaryUi,
    scheduledDate: String,
    isSelected: Boolean,
    isHost: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme

    val participantsText = when (hangout.status) {
        HangoutStatus.COMPLETED -> {
            hangout.maxAttendees?.let { maxAttendees ->
                stringResource(Res.string.participants_attended_max_format, hangout.participantCount, maxAttendees)
            } ?: stringResource(Res.string.participants_attended_format, hangout.participantCount)
        }

        HangoutStatus.CANCELLED -> null

        else -> {
            hangout.maxAttendees?.let { maxAttendees ->
                stringResource(Res.string.participants_max_format, hangout.participantCount, maxAttendees)
            } ?: stringResource(Res.string.participants_format, hangout.participantCount)
        }
    }

    LynkCard(
        onClick = onClick,
        style = if (isSelected) LynkCardStyle.OUTLINED else LynkCardStyle.FILLED,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(scheme.surfaceVariant)
                        .border(1.dp, scheme.outlineVariant, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = hangout.vibe.getIcon(),
                        contentDescription = null,
                        tint = scheme.onSurfaceVariant
                    )
                }

                if (isHost) {
                    val cardColor = if (isSelected) scheme.surface else scheme.surfaceContainerHighest

                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(scheme.extended.gold)
                            .border(2.dp, cardColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Lucide.Crown,
                            contentDescription = stringResource(Res.string.host_badge_description),
                            tint = scheme.extended.onGold,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LynkText(
                        text = hangout.name,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f).padding(end = 8.dp)
                    )
                    StatusChip(status = hangout.status)
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Lucide.Calendar,
                        contentDescription = null,
                        tint = scheme.onSurfaceVariant,
                        modifier = Modifier.size(12.dp)
                    )
                    LynkText(
                        text = scheduledDate,
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant
                    )
                }

                participantsText?.let {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Lucide.Users,
                            contentDescription = null,
                            tint = scheme.onSurfaceVariant,
                            modifier = Modifier.size(12.dp)
                        )
                        LynkText(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = scheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun HangoutSummaryCardPreview() {
    LynkTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            previewHangoutSummaries.forEachIndexed { index, hangout ->
                HangoutSummaryCard(
                    hangout = hangout,
                    scheduledDate = "Mon 15 Jun · 8:00 PM",
                    isSelected = index == 1,
                    isHost = index == 0,
                    onClick = {}
                )
            }
        }
    }
}
