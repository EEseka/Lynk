package com.eeseka.lynk.create_hangout.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Users
import com.eeseka.lynk.shared.design_system.components.layouts.LynkCard
import com.eeseka.lynk.shared.design_system.components.textfields.LynkText
import com.eeseka.lynk.shared.design_system.theme.LynkTheme
import com.eeseka.lynk.shared.domain.hangout.model.HangoutVibe
import com.eeseka.lynk.shared.presentation.hangout.mappers.getIcon
import com.eeseka.lynk.shared.presentation.hangout.mappers.getTitle
import lynk.feature.create_hangout.generated.resources.Res
import lynk.feature.create_hangout.generated.resources.description_label
import lynk.feature.create_hangout.generated.resources.group_size_label
import lynk.feature.create_hangout.generated.resources.group_vote
import lynk.feature.create_hangout.generated.resources.location_label
import lynk.feature.create_hangout.generated.resources.name_label
import lynk.feature.create_hangout.generated.resources.no_location_selected
import lynk.feature.create_hangout.generated.resources.people
import lynk.feature.create_hangout.generated.resources.review_and_confirm_description
import lynk.feature.create_hangout.generated.resources.unlimited
import lynk.feature.create_hangout.generated.resources.unnamed_hangout
import lynk.feature.create_hangout.generated.resources.vibe
import lynk.feature.create_hangout.generated.resources.when_label
import org.jetbrains.compose.resources.stringResource

@Composable
fun CreateHangoutStepThree(
    name: String,
    description: String,
    vibe: HangoutVibe,
    scheduledLabel: String,
    maxAttendees: Int?,
    isVotingMode: Boolean,
    selectedSpotName: String?,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        LynkText(
            text = stringResource(Res.string.review_and_confirm_description),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        LynkCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                //  Name
                SummaryRow(
                    label = stringResource(Res.string.name_label),
                    animationDelay = 0
                ) {
                    LynkText(
                        text = name.takeIf { it.isNotBlank() }
                            ?: stringResource(Res.string.unnamed_hangout),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Description
                if (description.isNotBlank()) {
                    SummaryDivider()
                    SummaryRow(
                        label = stringResource(Res.string.description_label),
                        animationDelay = 50
                    ) {
                        LynkText(
                            text = description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                SummaryDivider()

                // Vibe
                SummaryRow(
                    label = stringResource(Res.string.vibe),
                    animationDelay = 100
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = vibe.getIcon(),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        LynkText(
                            text = vibe.getTitle(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                SummaryDivider()

                // Date & Time
                SummaryRow(
                    label = stringResource(Res.string.when_label),
                    animationDelay = 150
                ) {
                    LynkText(
                        text = scheduledLabel,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium
                    )
                }

                SummaryDivider()

                // Max People
                SummaryRow(
                    label = stringResource(Res.string.group_size_label),
                    animationDelay = 200
                ) {
                    val people = stringResource(Res.string.people)

                    LynkText(
                        text = maxAttendees?.let { "$it $people" }
                            ?: stringResource(Res.string.unlimited),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium
                    )
                }

                SummaryDivider()

                // Location / Voting
                SummaryRow(
                    label = stringResource(Res.string.location_label),
                    animationDelay = 250
                ) {
                    if (isVotingMode) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Lucide.Users,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            LynkText(
                                text = stringResource(Res.string.group_vote),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        LynkText(
                            text = selectedSpotName
                                ?: stringResource(Res.string.no_location_selected),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun SummaryRow(
    label: String,
    animationDelay: Int,
    content: @Composable () -> Unit
) {
    val entranceState = remember { MutableTransitionState(false).apply { targetState = true } }

    AnimatedVisibility(
        visibleState = entranceState,
        enter = slideInVertically(
            initialOffsetY = { 20 },
            animationSpec = tween(durationMillis = 400, delayMillis = animationDelay)
        ) + fadeIn(animationSpec = tween(durationMillis = 400, delayMillis = animationDelay))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            LynkText(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f).padding(end = 16.dp)
            )

            Box(
                modifier = Modifier.weight(2f),
                contentAlignment = Alignment.TopEnd
            ) {
                content()
            }
        }
    }
}

@Composable
private fun SummaryDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 20.dp),
        thickness = 1.dp,
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    )
}

@PreviewLightDark
@Composable
private fun CreateHangoutStepThreeVotingPreview() {
    LynkTheme {
        CreateHangoutStepThree(
            name = "Suya Night 🔥",
            description = "Friday night chills with the guys.",
            vibe = HangoutVibe.CHILL,
            scheduledLabel = "Wed 20 May · 8:00 PM",
            maxAttendees = null,
            isVotingMode = true,
            selectedSpotName = null,
            modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainerLow)
        )
    }
}

@PreviewLightDark
@Composable
private fun CreateHangoutStepThreeSpotSelectedPreview() {
    LynkTheme {
        CreateHangoutStepThree(
            name = "Beach Day 🏖️",
            description = "",
            vibe = HangoutVibe.ACTIVE,
            scheduledLabel = "Fri 12 Jun · 10:30 AM",
            maxAttendees = 12,
            isVotingMode = false,
            selectedSpotName = "Landmark Beach Resort",
            modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainerLow)
        )
    }
}
