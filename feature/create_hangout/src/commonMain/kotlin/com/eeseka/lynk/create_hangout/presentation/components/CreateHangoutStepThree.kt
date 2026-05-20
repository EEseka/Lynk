package com.eeseka.lynk.create_hangout.presentation.components

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Users
import com.eeseka.lynk.create_hangout.presentation.CreateHangoutState
import com.eeseka.lynk.shared.design_system.components.layouts.LynkCard
import com.eeseka.lynk.shared.design_system.components.textfields.LynkText
import com.eeseka.lynk.shared.design_system.theme.LynkTheme
import com.eeseka.lynk.shared.domain.hangout.model.HangoutVibe
import com.eeseka.lynk.shared.domain.spot.model.PriceLevel
import com.eeseka.lynk.shared.domain.spot.model.Spot
import com.eeseka.lynk.shared.domain.spot.model.SpotCategory
import com.eeseka.lynk.shared.presentation.hangout.mappers.getIcon
import com.eeseka.lynk.shared.presentation.hangout.mappers.getTitle
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import lynk.feature.create_hangout.generated.resources.Res
import lynk.feature.create_hangout.generated.resources.description_label
import lynk.feature.create_hangout.generated.resources.group_size_label
import lynk.feature.create_hangout.generated.resources.group_vote
import lynk.feature.create_hangout.generated.resources.location_label
import lynk.feature.create_hangout.generated.resources.name_label
import lynk.feature.create_hangout.generated.resources.no_location_selected
import lynk.feature.create_hangout.generated.resources.people
import lynk.feature.create_hangout.generated.resources.review_and_confirm_description
import lynk.feature.create_hangout.generated.resources.tbd
import lynk.feature.create_hangout.generated.resources.unlimited
import lynk.feature.create_hangout.generated.resources.unnamed_hangout
import lynk.feature.create_hangout.generated.resources.vibe
import lynk.feature.create_hangout.generated.resources.when_label
import org.jetbrains.compose.resources.stringResource

@Composable
fun CreateHangoutStepThree(
    state: CreateHangoutState,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    // Trigger for the entrance animation
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVisible = true
    }

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
                    isVisible = isVisible,
                    animationDelay = 0
                ) {
                    LynkText(
                        text = state.hangoutNameTextState.text.toString().takeIf { it.isNotBlank() }
                            ?: stringResource(Res.string.unnamed_hangout),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Description
                val desc = state.hangoutDescriptionTextState.text.toString()
                if (desc.isNotBlank()) {
                    SummaryDivider()
                    SummaryRow(
                        label = stringResource(Res.string.description_label),
                        isVisible = isVisible,
                        animationDelay = 50
                    ) {
                        LynkText(
                            text = desc,
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
                    isVisible = isVisible,
                    animationDelay = 100
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = state.hangoutVibe.getIcon(),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        LynkText(
                            text = state.hangoutVibe.getTitle(),
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
                    isVisible = isVisible,
                    animationDelay = 150
                ) {
                    val dateStr = state.hangoutDate?.toString() ?: stringResource(Res.string.tbd)

                    val timeStr = state.hangoutTime?.toString() ?: stringResource(Res.string.tbd)

                    LynkText(
                        text = "$dateStr • $timeStr",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium
                    )
                }

                SummaryDivider()

                // Max People
                SummaryRow(
                    label = stringResource(Res.string.group_size_label),
                    isVisible = isVisible,
                    animationDelay = 200
                ) {
                    val people = stringResource(Res.string.people)

                    LynkText(
                        text = state.maxAttendees?.let { "$it $people" }
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
                    isVisible = isVisible,
                    animationDelay = 250
                ) {
                    if (state.isVotingMode) {
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
                            text = state.selectedSpot?.name
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

        Spacer(modifier = Modifier.height(120.dp))
    }
}

@Composable
private fun SummaryRow(
    label: String,
    isVisible: Boolean,
    animationDelay: Int,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = isVisible,
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
            state = CreateHangoutState(
                hangoutNameTextState = TextFieldState("Suya Night \uD83D\uDD25"),
                hangoutDescriptionTextState = TextFieldState("Friday night chills with the guys."),
                hangoutVibe = HangoutVibe.CHILL,
                hangoutDate = LocalDate(2026, 5, 20),
                hangoutTime = LocalTime(20, 0),
                maxAttendees = null,
                isVotingMode = true
            ),
            modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainerLow)
        )
    }
}

@PreviewLightDark
@Composable
private fun CreateHangoutStepThreeSpotSelectedPreview() {
    LynkTheme {
        CreateHangoutStepThree(
            state = CreateHangoutState(
                hangoutNameTextState = TextFieldState("Beach Day \uD83C\uDFD6\uFE0F"),
                hangoutDescriptionTextState = TextFieldState(),
                hangoutVibe = HangoutVibe.ACTIVE,
                hangoutDate = LocalDate(2026, 6, 12),
                hangoutTime = LocalTime(10, 30),
                maxAttendees = 12,
                isVotingMode = false,
                selectedSpot = Spot(
                    id = "1",
                    name = "Landmark Beach Resort",
                    photoUrls = emptyList(),
                    latitude = 6.443,
                    longitude = 3.455,
                    category = SpotCategory.ACTIVITY,
                    priceLevel = PriceLevel.EXPENSIVE,
                    rating = 4.8,
                    reviewCount = 120,
                    isSaved = false,
                    isOpenNow = true,
                    shortAddress = "Victoria Island",
                    tags = emptyList(),
                    savedAt = null,
                    description = null,
                    websiteUrl = null,
                    googleMapsUrl = null
                )
            ),
            modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainerLow)
        )
    }
}