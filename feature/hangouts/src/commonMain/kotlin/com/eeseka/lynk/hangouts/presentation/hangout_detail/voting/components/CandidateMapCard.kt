package com.eeseka.lynk.hangouts.presentation.hangout_detail.voting.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Check
import com.composables.icons.lucide.Crown
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.MapPin
import com.composables.icons.lucide.Navigation
import com.composables.icons.lucide.Scale
import com.eeseka.lynk.shared.design_system.components.buttons.LynkButton
import com.eeseka.lynk.shared.design_system.components.buttons.LynkButtonStyle
import com.eeseka.lynk.shared.design_system.components.images.LynkAsyncImage
import com.eeseka.lynk.shared.design_system.components.layouts.LynkCard
import com.eeseka.lynk.shared.design_system.components.layouts.LynkCardStyle
import com.eeseka.lynk.shared.design_system.components.textfields.LynkText
import com.eeseka.lynk.shared.design_system.components.util.AppHaptic
import com.eeseka.lynk.shared.design_system.components.util.rememberAppHaptic
import com.eeseka.lynk.shared.design_system.theme.LynkTheme
import com.eeseka.lynk.shared.design_system.theme.extended
import com.eeseka.lynk.shared.presentation.hangout.components.ParticipantStack
import com.eeseka.lynk.shared.presentation.hangout.model.HangoutUserUi
import com.eeseka.lynk.shared.presentation.preview.previewUsers
import com.eeseka.lynk.shared.presentation.spot.util.SpotPhotoUrlBuilder
import com.eeseka.lynk.shared.presentation.spot.util.rememberGoogleImageRequest
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import lynk.feature.hangouts.generated.resources.Res
import lynk.feature.hangouts.generated.resources.voting_from_middle
import lynk.feature.hangouts.generated.resources.voting_leading
import lynk.feature.hangouts.generated.resources.voting_no_votes
import lynk.feature.hangouts.generated.resources.voting_pick_winner
import lynk.feature.hangouts.generated.resources.voting_tie_locked
import lynk.feature.hangouts.generated.resources.voting_vote_here
import lynk.feature.hangouts.generated.resources.voting_your_vote
import org.jetbrains.compose.resources.stringResource

@Composable
fun CandidateMapCard(
    spotName: String,
    photoUrls: ImmutableList<String>,
    distanceFromMiddle: String?,
    voters: ImmutableList<HangoutUserUi>,
    isMyVote: Boolean,
    isLeading: Boolean,
    isTiebreakTarget: Boolean,
    isTieLocked: Boolean,
    onClick: () -> Unit,
    onVoteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val hapticFeedback = rememberAppHaptic()

    val borderColor by animateColorAsState(
        when {
            isTiebreakTarget -> scheme.tertiary
            isLeading -> scheme.extended.gold
            isMyVote -> scheme.primary
            else -> scheme.outlineVariant
        }
    )
    val borderWidth by animateDpAsState(
        if (isMyVote || isTiebreakTarget || isLeading) 2.dp else 1.dp
    )

    val primaryPhotoUrl = remember(photoUrls) {
        SpotPhotoUrlBuilder.getPrimaryPhotoUrl(photoUrls)
    }
    val imageRequest = rememberGoogleImageRequest(url = primaryPhotoUrl ?: "")

    LynkCard(
        onClick = {
            hapticFeedback(AppHaptic.ImpactLight)
            onClick()
        },
        style = LynkCardStyle.OUTLINED,
        modifier = modifier
            .fillMaxWidth()
            .border(width = borderWidth, color = borderColor, shape = MaterialTheme.shapes.large)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(MaterialTheme.shapes.medium)
                        .background(scheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    if (imageRequest != null) {
                        LynkAsyncImage(
                            model = imageRequest,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.size(72.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Lucide.MapPin,
                            contentDescription = null,
                            tint = scheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isLeading) {
                            Icon(
                                imageVector = Lucide.Crown,
                                contentDescription = stringResource(Res.string.voting_leading),
                                tint = scheme.extended.gold,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        LynkText(
                            text = spotName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    distanceFromMiddle?.let { distance ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Lucide.Navigation,
                                contentDescription = null,
                                tint = scheme.onSurfaceVariant,
                                modifier = Modifier.size(12.dp)
                            )
                            LynkText(
                                text = stringResource(Res.string.voting_from_middle, distance),
                                style = MaterialTheme.typography.bodySmall,
                                color = scheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Box(
                        modifier = Modifier.heightIn(min = 24.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (voters.isEmpty()) {
                            LynkText(
                                text = stringResource(Res.string.voting_no_votes),
                                style = MaterialTheme.typography.bodySmall,
                                color = scheme.onSurfaceVariant
                            )
                        } else {
                            ParticipantStack(
                                users = voters,
                                avatarSize = 24.dp
                            )
                        }
                    }
                }
            }

            when {
                isTiebreakTarget -> LynkButton(
                    text = stringResource(Res.string.voting_pick_winner),
                    onClick = {
                        hapticFeedback(AppHaptic.ImpactMedium)
                        onVoteClick()
                    },
                    style = LynkButtonStyle.SECONDARY
                )

                isTieLocked -> VoteStatusRow(
                    icon = Lucide.Scale,
                    text = stringResource(Res.string.voting_tie_locked),
                    color = scheme.onSurfaceVariant
                )

                isMyVote -> VoteStatusRow(
                    icon = Lucide.Check,
                    text = stringResource(Res.string.voting_your_vote),
                    color = scheme.primary
                )

                else -> LynkButton(
                    text = stringResource(Res.string.voting_vote_here),
                    onClick = {
                        hapticFeedback(AppHaptic.ImpactMedium)
                        onVoteClick()
                    },
                    style = LynkButtonStyle.SECONDARY
                )
            }
        }
    }
}

@Composable
private fun VoteStatusRow(
    icon: ImageVector,
    text: String,
    color: Color
) {
    Box(contentAlignment = Alignment.Center) {
        LynkButton(
            text = text,
            onClick = {},
            enabled = false,
            modifier = Modifier
                .graphicsLayer { alpha = 0f }
                .clearAndSetSemantics { }
        )
        Row(
            modifier = Modifier.matchParentSize(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            LynkText(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                color = color
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun CandidateMapCardPreview() {
    LynkTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            PreviewCandidateMapCard()
            PreviewCandidateMapCard(isMyVote = true, isLeading = true, voterCount = 3)
            PreviewCandidateMapCard(isTiebreakTarget = true, voterCount = 2)
            PreviewCandidateMapCard(isTieLocked = true, voterCount = 0)
        }
    }
}

@Composable
private fun PreviewCandidateMapCard(
    isMyVote: Boolean = false,
    isLeading: Boolean = false,
    isTiebreakTarget: Boolean = false,
    isTieLocked: Boolean = false,
    voterCount: Int = 1
) {
    CandidateMapCard(
        spotName = "Nok by Alara",
        photoUrls = persistentListOf(),
        distanceFromMiddle = "1.2 km",
        voters = previewUsers(voterCount),
        isMyVote = isMyVote,
        isLeading = isLeading,
        isTiebreakTarget = isTiebreakTarget,
        isTieLocked = isTieLocked,
        onClick = {},
        onVoteClick = {}
    )
}
