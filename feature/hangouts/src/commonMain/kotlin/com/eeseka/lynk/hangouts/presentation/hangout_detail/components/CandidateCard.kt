package com.eeseka.lynk.hangouts.presentation.hangout_detail.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.composables.icons.lucide.Check
import com.composables.icons.lucide.Crown
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.MapPin
import com.composables.icons.lucide.Trash2
import com.eeseka.lynk.shared.design_system.components.layouts.LynkCard
import com.eeseka.lynk.shared.design_system.components.layouts.LynkCardStyle
import com.eeseka.lynk.shared.design_system.components.progress_indicator.LynkProgressIndicator
import com.eeseka.lynk.shared.design_system.components.textfields.LynkText
import com.eeseka.lynk.shared.design_system.components.util.AppHaptic
import com.eeseka.lynk.shared.design_system.components.util.rememberAppHaptic
import com.eeseka.lynk.shared.design_system.theme.LynkTheme
import com.eeseka.lynk.shared.design_system.theme.extended
import com.eeseka.lynk.shared.presentation.spot.util.SpotPhotoUrlBuilder
import com.eeseka.lynk.shared.presentation.spot.util.rememberGoogleImageRequest
import lynk.feature.hangouts.generated.resources.Res
import lynk.feature.hangouts.generated.resources.voting_leading
import lynk.feature.hangouts.generated.resources.voting_remove_spot
import lynk.feature.hangouts.generated.resources.voting_your_pick
import org.jetbrains.compose.resources.stringResource

@Composable
fun CandidateCard(
    spotName: String,
    spotAddress: String?,
    photoUrls: List<String>,
    voteCount: Int,
    totalVotes: Int,
    isMyVote: Boolean,
    isLeading: Boolean,
    isTiebreakTarget: Boolean,
    canRemove: Boolean = false,
    isRemoving: Boolean = false,
    onClick: () -> Unit,
    onRemove: () -> Unit = {},
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
    val fraction by animateFloatAsState(
        if (totalVotes > 0) voteCount.toFloat() / totalVotes else 0f
    )
    val barColor by animateColorAsState(
        when {
            isTiebreakTarget -> scheme.tertiary
            isLeading -> scheme.extended.gold
            isMyVote -> scheme.primary
            else -> scheme.secondary
        }
    )

    val primaryPhotoUrl = remember(photoUrls) {
        SpotPhotoUrlBuilder.getPrimaryPhotoUrl(photoUrls)
    }
    val imageRequest = rememberGoogleImageRequest(url = primaryPhotoUrl ?: "")

    LynkCard(
        onClick = {
            hapticFeedback(AppHaptic.ImpactMedium)
            onClick()
        },
        style = LynkCardStyle.OUTLINED,
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = borderWidth,
                color = borderColor,
                shape = MaterialTheme.shapes.large
            )
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(180.dp)) {
            // Photo hero (falls back to a tinted placeholder icon).
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(scheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (imageRequest != null) {
                    AsyncImage(
                        model = imageRequest,
                        contentDescription = spotName,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Lucide.MapPin,
                        contentDescription = null,
                        tint = scheme.onSurfaceVariant,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            // Dark scrim so overlaid text stays legible on any photo.
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            0.35f to Color.Transparent,
                            1f to Color.Black.copy(alpha = 0.72f)
                        )
                    )
            )

            // Top row: leader crown (start) + remove/vote badge (end).
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isLeading) {
                    LeadingBadge()
                }
                Spacer(Modifier.weight(1f))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (canRemove) {
                        RemoveButton(isRemoving = isRemoving, onRemove = onRemove)
                    }
                    VoteBadge(voteCount = voteCount, isMyVote = isMyVote)
                }
            }

            // Bottom block: your-pick chip, name, address, momentum bar.
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (isMyVote) YourPickChip()

                LynkText(
                    text = spotName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                spotAddress?.let {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Lucide.MapPin,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(12.dp)
                        )
                        LynkText(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Momentum bar — share of the vote.
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.24f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction)
                            .height(4.dp)
                            .clip(CircleShape)
                            .background(barColor)
                    )
                }
            }
        }
    }
}

@Composable
private fun VoteBadge(
    voteCount: Int,
    isMyVote: Boolean
) {
    val scheme = MaterialTheme.colorScheme
    val bgColor by animateColorAsState(
        if (isMyVote) scheme.primary else Color.Black.copy(alpha = 0.5f)
    )
    val contentColor by animateColorAsState(
        if (isMyVote) scheme.onPrimary else Color.White
    )
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(bgColor)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isMyVote) {
            Icon(
                imageVector = Lucide.Check,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(16.dp)
            )
        }

        AnimatedContent(
            targetState = voteCount,
            transitionSpec = {
                (slideInVertically { it } + fadeIn(tween(200))) togetherWith
                    (slideOutVertically { -it } + fadeOut(tween(200)))
            },
            label = "vote_count"
        ) { count ->
            LynkText(
                text = count.toString(),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
        }
    }
}

@Composable
private fun RemoveButton(
    isRemoving: Boolean,
    onRemove: () -> Unit
) {
    val hapticFeedback = rememberAppHaptic()
    Box(
        modifier = Modifier
            .minimumInteractiveComponentSize()
            .clip(CircleShape)
            .clickable(enabled = !isRemoving) {
                hapticFeedback(AppHaptic.ImpactMedium)
                onRemove()
            }
            .padding(8.dp)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.5f))
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isRemoving) {
            LynkProgressIndicator(
                modifier = Modifier.size(16.dp),
                color = Color.White
            )
        } else {
            Icon(
                imageVector = Lucide.Trash2,
                contentDescription = stringResource(Res.string.voting_remove_spot),
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun YourPickChip() {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(scheme.primary)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Lucide.Check,
            contentDescription = null,
            tint = scheme.onPrimary,
            modifier = Modifier.size(12.dp)
        )
        LynkText(
            text = stringResource(Res.string.voting_your_pick),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = scheme.onPrimary
        )
    }
}

@Composable
private fun LeadingBadge() {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(scheme.extended.gold)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Lucide.Crown,
            contentDescription = null,
            tint = scheme.extended.onGold,
            modifier = Modifier.size(12.dp)
        )
        LynkText(
            text = stringResource(Res.string.voting_leading),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = scheme.extended.onGold
        )
    }
}

@Composable
private fun CandidateCardPreview(
    spotName: String = "Mama Cass Restaurant",
    spotAddress: String? = "Victoria Island",
    voteCount: Int = 2,
    totalVotes: Int = 5,
    isMyVote: Boolean = false,
    isLeading: Boolean = false,
    isTiebreakTarget: Boolean = false,
    canRemove: Boolean = false,
    isRemoving: Boolean = false
) {
    LynkTheme {
        CandidateCard(
            spotName = spotName,
            spotAddress = spotAddress,
            photoUrls = emptyList(),
            voteCount = voteCount,
            totalVotes = totalVotes,
            isMyVote = isMyVote,
            isLeading = isLeading,
            isTiebreakTarget = isTiebreakTarget,
            canRemove = canRemove,
            isRemoving = isRemoving,
            onRemove = {},
            onClick = {},
            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
        )
    }
}

@PreviewLightDark
@Composable
private fun CandidateCardDefaultPreview() = CandidateCardPreview()

@PreviewLightDark
@Composable
private fun CandidateCardMyVotePreview() = CandidateCardPreview(
    isMyVote = true,
    voteCount = 3,
    totalVotes = 5
)

@PreviewLightDark
@Composable
private fun CandidateCardLeadingPreview() = CandidateCardPreview(
    spotName = "The Good Beach Lounge",
    spotAddress = "Lekki Phase 1",
    isLeading = true,
    voteCount = 4,
    totalVotes = 6
)

@PreviewLightDark
@Composable
private fun CandidateCardLeadingAndMinePreview() = CandidateCardPreview(
    spotName = "The Good Beach Lounge",
    spotAddress = "Lekki Phase 1",
    isMyVote = true,
    isLeading = true,
    voteCount = 4,
    totalVotes = 6
)

@PreviewLightDark
@Composable
private fun CandidateCardTiebreakPreview() = CandidateCardPreview(
    spotName = "Terra Culture",
    isTiebreakTarget = true,
    voteCount = 3,
    totalVotes = 6
)

@PreviewLightDark
@Composable
private fun CandidateCardHostRemovablePreview() = CandidateCardPreview(
    spotName = "Cafe Neo",
    canRemove = true,
    voteCount = 1,
    totalVotes = 5
)

@PreviewLightDark
@Composable
private fun CandidateCardRemovingPreview() = CandidateCardPreview(
    spotName = "Cafe Neo",
    canRemove = true,
    isRemoving = true,
    voteCount = 1,
    totalVotes = 5
)