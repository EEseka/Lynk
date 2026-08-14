package com.eeseka.lynk.hangouts.presentation.hangout_detail.components

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
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Calendar
import com.composables.icons.lucide.Crown
import com.composables.icons.lucide.Lucide
import com.eeseka.lynk.shared.design_system.components.textfields.LynkText
import com.eeseka.lynk.shared.design_system.theme.LynkTheme
import com.eeseka.lynk.shared.design_system.theme.extended
import com.eeseka.lynk.shared.domain.hangout.model.HangoutStatus
import com.eeseka.lynk.shared.domain.hangout.model.HangoutVibe
import com.eeseka.lynk.shared.presentation.hangout.mappers.getIcon
import com.eeseka.lynk.shared.presentation.hangout.mappers.getTitle
import lynk.feature.hangouts.generated.resources.Res
import lynk.feature.hangouts.generated.resources.host_badge_description
import org.jetbrains.compose.resources.stringResource

@Composable
fun HangoutHero(
    name: String,
    vibe: HangoutVibe,
    status: HangoutStatus,
    scheduledDate: String,
    isHost: Boolean,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        LynkText(
            text = name,
            style = MaterialTheme.typography.headlineSmall,
            color = scheme.onBackground
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(scheme.surfaceVariant)
                        .border(1.dp, scheme.outlineVariant, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = vibe.getIcon(),
                        contentDescription = null,
                        tint = scheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                }
                if (isHost) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(scheme.extended.gold)
                            .border(2.dp, scheme.surface, CircleShape),
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
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LynkText(
                        text = vibe.getTitle(),
                        style = MaterialTheme.typography.titleMedium,
                        color = scheme.onBackground
                    )
                    StatusChip(status = status)
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Lucide.Calendar,
                        contentDescription = null,
                        tint = scheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    LynkText(
                        text = scheduledDate,
                        style = MaterialTheme.typography.bodyMedium,
                        color = scheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun HangoutHeroPreview(
    name: String = "Rooftop Party in Lekki",
    vibe: HangoutVibe = HangoutVibe.PARTY,
    status: HangoutStatus = HangoutStatus.SCHEDULED,
    isHost: Boolean = true
) {
    LynkTheme {
        HangoutHero(
            name = name,
            vibe = vibe,
            status = status,
            scheduledDate = "Sat, 12 Oct · 8:00 PM",
            isHost = isHost,
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        )
    }
}

@PreviewLightDark
@Composable
private fun HangoutHeroHostPreview() = HangoutHeroPreview()

@PreviewLightDark
@Composable
private fun HangoutHeroAttendeePreview() = HangoutHeroPreview(isHost = false)

@PreviewLightDark
@Composable
private fun HangoutHeroVotingPreview() = HangoutHeroPreview(
    vibe = HangoutVibe.CHILL,
    status = HangoutStatus.VOTING
)

@PreviewLightDark
@Composable
private fun HangoutHeroLongNamePreview() = HangoutHeroPreview(
    name = "Sunday Afternoon Rooftop Listening Party and Small Chops Tasting",
    status = HangoutStatus.ONGOING
)
