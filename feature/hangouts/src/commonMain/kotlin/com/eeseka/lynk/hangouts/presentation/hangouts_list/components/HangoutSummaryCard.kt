package com.eeseka.lynk.hangouts.presentation.hangouts_list.components

import androidx.compose.foundation.background
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
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Users
import com.eeseka.lynk.hangouts.presentation.util.toHangoutDisplayDate
import com.eeseka.lynk.shared.design_system.components.layouts.LynkCard
import com.eeseka.lynk.shared.design_system.components.layouts.LynkCardStyle
import com.eeseka.lynk.shared.design_system.components.textfields.LynkText
import com.eeseka.lynk.shared.design_system.theme.LynkTheme
import com.eeseka.lynk.shared.design_system.theme.extended
import com.eeseka.lynk.shared.domain.hangout.model.HangoutStatus
import com.eeseka.lynk.shared.domain.hangout.model.HangoutVibe
import com.eeseka.lynk.shared.presentation.hangout.mappers.getIcon
import com.eeseka.lynk.shared.presentation.hangout.mappers.getTitle
import com.eeseka.lynk.shared.presentation.hangout.model.HangoutSummaryUi
import lynk.feature.hangouts.generated.resources.Res
import lynk.feature.hangouts.generated.resources.participants_attended_format
import lynk.feature.hangouts.generated.resources.participants_attended_max_format
import lynk.feature.hangouts.generated.resources.participants_format
import lynk.feature.hangouts.generated.resources.participants_max_format
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Instant

@Composable
fun HangoutSummaryCard(
    hangout: HangoutSummaryUi,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme

    val (statusBackground, statusForeground) = when (hangout.status) {
        HangoutStatus.ONGOING -> scheme.extended.success to scheme.extended.onSuccess
        HangoutStatus.SCHEDULED -> scheme.secondary to scheme.onSecondary
        HangoutStatus.VOTING -> scheme.tertiary to scheme.onTertiary
        HangoutStatus.COMPLETED -> scheme.surfaceVariant to scheme.onSurfaceVariant
        HangoutStatus.CANCELLED -> scheme.error to scheme.onError
    }

    val participantsText = when (hangout.status) {
        HangoutStatus.COMPLETED -> {
            hangout.maxAttendees?.let { maxAttendees ->
                stringResource(
                    Res.string.participants_attended_max_format,
                    hangout.participantCount,
                    maxAttendees
                )
            } ?: stringResource(Res.string.participants_attended_format, hangout.participantCount)
        }

        HangoutStatus.CANCELLED -> null

        else -> {
            hangout.maxAttendees?.let { maxAttendees ->
                stringResource(
                    Res.string.participants_max_format,
                    hangout.participantCount,
                    maxAttendees
                )
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
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(scheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = hangout.vibe.getIcon(),
                    contentDescription = null,
                    tint = scheme.onSurfaceVariant
                )
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
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(statusBackground)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        LynkText(
                            text = hangout.status.getTitle(),
                            style = MaterialTheme.typography.labelSmall,
                            color = statusForeground
                        )
                    }
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
                        text = hangout.scheduledAt.toHangoutDisplayDate(),
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
        HangoutSummaryCard(
            hangout = HangoutSummaryUi(
                id = "1",
                hostId = "user1",
                name = "Gaming Night at Ikeja Arcade",
                vibe = HangoutVibe.GAMING,
                status = HangoutStatus.VOTING,
                scheduledAt = Instant.fromEpochSeconds(1_750_000_000),
                maxAttendees = 10,
                participantCount = 4,
                createdAt = Instant.fromEpochSeconds(1_749_000_000)
            ),
            isSelected = false,
            onClick = {},
            modifier = Modifier.background(MaterialTheme.colorScheme.background)
        )
    }
}

@PreviewLightDark
@Composable
private fun HangoutSummaryCardSelectedPreview() {
    LynkTheme {
        HangoutSummaryCard(
            hangout = HangoutSummaryUi(
                id = "2",
                hostId = "user1",
                name = "Friday Drinks",
                vibe = HangoutVibe.DRINKS,
                status = HangoutStatus.SCHEDULED,
                scheduledAt = Instant.fromEpochSeconds(1_750_000_000),
                maxAttendees = null,
                participantCount = 7,
                createdAt = Instant.fromEpochSeconds(1_749_000_000)
            ),
            isSelected = true,
            onClick = {},
            modifier = Modifier.background(MaterialTheme.colorScheme.background)
        )
    }
}