package com.eeseka.lynk.hangouts.presentation.hangout_detail.voting.components

import androidx.compose.animation.animateBounds
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.LookaheadScope
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.MapPin
import com.composables.icons.lucide.Plus
import com.composables.icons.lucide.Scale
import com.eeseka.lynk.hangouts.presentation.hangout_detail.components.DetailSection
import com.eeseka.lynk.hangouts.presentation.hangout_detail.components.PlaceholderCard
import com.eeseka.lynk.shared.design_system.components.buttons.LynkButton
import com.eeseka.lynk.shared.design_system.components.buttons.LynkButtonStyle
import com.eeseka.lynk.shared.design_system.components.textfields.LynkText
import com.eeseka.lynk.shared.design_system.components.util.AppHaptic
import com.eeseka.lynk.shared.design_system.components.util.rememberAppHaptic
import com.eeseka.lynk.shared.design_system.theme.LynkTheme
import com.eeseka.lynk.shared.domain.spot.model.SpotCategory
import com.eeseka.lynk.shared.presentation.spot.model.SpotUi
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.persistentSetOf
import lynk.feature.hangouts.generated.resources.Res
import lynk.feature.hangouts.generated.resources.voting_close
import lynk.feature.hangouts.generated.resources.voting_closing
import lynk.feature.hangouts.generated.resources.voting_empty_message
import lynk.feature.hangouts.generated.resources.voting_empty_title
import lynk.feature.hangouts.generated.resources.voting_propose
import lynk.feature.hangouts.generated.resources.voting_tie_banner
import lynk.feature.hangouts.generated.resources.voting_tie_banner_guest
import lynk.feature.hangouts.generated.resources.voting_title
import org.jetbrains.compose.resources.stringResource

@Composable
fun VotingSection(
    candidates: ImmutableList<SpotUi>,
    votes: ImmutableMap<String, String>,
    removingSpotIds: ImmutableSet<String>,
    currentUserId: String?,
    isHost: Boolean,
    tiedSpotIds: ImmutableList<String>,
    isClosingVoting: Boolean,
    onCastVote: (String) -> Unit,
    onRemoveSpot: (String) -> Unit,
    onProposeClick: () -> Unit,
    onCloseVoting: () -> Unit,
    onBreakTie: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val counts = remember(votes) { votes.values.groupingBy { it }.eachCount() }
    val maxCount = counts.values.maxOrNull() ?: 0
    val totalVotes = votes.size
    val myVote = currentUserId?.let { votes[it] }
    val tie = tiedSpotIds.isNotEmpty()
    val orderedCandidates = remember(candidates, counts) {
        candidates.sortedByDescending { counts[it.id] ?: 0 }
    }

    val hapticFeedback = rememberAppHaptic()

    DetailSection(
        title = stringResource(Res.string.voting_title),
        trailing = {
            Row(
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .clip(CircleShape)
                    .clickable {
                        hapticFeedback(AppHaptic.ImpactLight)
                        onProposeClick()
                    }
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Lucide.Plus,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(16.dp)
                )
                LynkText(
                    text = stringResource(Res.string.voting_propose),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        },
        modifier = modifier
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (tie) {
                PlaceholderCard(
                    icon = Lucide.Scale,
                    title = stringResource(
                        if (isHost) Res.string.voting_tie_banner
                        else Res.string.voting_tie_banner_guest
                    ),
                    message = null,
                    iconTint = MaterialTheme.colorScheme.tertiary
                )
            }

            if (candidates.isEmpty()) {
                PlaceholderCard(
                    icon = Lucide.MapPin,
                    title = stringResource(Res.string.voting_empty_title),
                    message = stringResource(Res.string.voting_empty_message),
                    iconTint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                // LookaheadScope + animateBounds slides each card to its new rank when the leaderboard reorders
                LookaheadScope {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        orderedCandidates.forEach { spot ->
                            key(spot.id) {
                                val voteCount = counts[spot.id] ?: 0
                                CandidateCard(
                                    spotName = spot.name,
                                    spotAddress = spot.shortAddress,
                                    photoUrls = spot.photoUrls,
                                    voteCount = voteCount,
                                    totalVotes = totalVotes,
                                    isMyVote = myVote == spot.id,
                                    isLeading = voteCount > 0 && voteCount == maxCount,
                                    isTiebreakTarget = tie && isHost && spot.id in tiedSpotIds,
                                    canRemove = isHost && !tie,  // Host can pull a spot off the ballot, but not mid-tiebreak.
                                    isRemoving = spot.id in removingSpotIds,
                                    onRemove = { onRemoveSpot(spot.id) },
                                    onClick = {
                                        when {
                                            tie && isHost && spot.id in tiedSpotIds -> {
                                                hapticFeedback(AppHaptic.ImpactMedium)
                                                onBreakTie(spot.id)
                                            }

                                            tie -> Unit // locked while the host breaks the tie, so no buzz either
                                            else -> {
                                                hapticFeedback(AppHaptic.ImpactMedium)
                                                onCastVote(spot.id)
                                            }
                                        }
                                    },
                                    modifier = Modifier.animateBounds(this@LookaheadScope)
                                )
                            }
                        }
                    }
                }

                if (isHost && !tie) {
                    LynkButton(
                        text = stringResource(Res.string.voting_close),
                        onClick = {
                            hapticFeedback(AppHaptic.ImpactMedium)
                            onCloseVoting()
                        },
                        style = LynkButtonStyle.SECONDARY,
                        enabled = totalVotes > 0,
                        isLoading = isClosingVoting,
                        loadingText = stringResource(Res.string.voting_closing),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
    }
}

private fun previewSpot(id: String, name: String, address: String) = SpotUi(
    id = id,
    name = name,
    description = null,
    photoUrls = persistentListOf(),
    category = SpotCategory.RESTAURANT,
    tags = persistentListOf(),
    priceLevel = null,
    rating = 4.5,
    reviewCount = 120,
    isOpenNow = true,
    shortAddress = address,
    latitude = 6.4,
    longitude = 3.4,
    websiteUrl = null,
    googleMapsUrl = null,
    isSaved = false
)

private val previewCandidates = persistentListOf(
    previewSpot("s1", "The Rooftop Lounge", "12 Admiralty Way, Lekki"),
    previewSpot("s2", "Nomad Beach Bar", "8 Elegushi Rd, Lekki"),
    previewSpot("s3", "Craft & Co", "3 Karimu Kotun St, VI")
)

@Composable
private fun VotingSectionPreview(
    candidates: ImmutableList<SpotUi> = previewCandidates,
    votes: ImmutableMap<String, String> = persistentMapOf("0" to "s1", "1" to "s1", "2" to "s2", "me" to "s3"),
    isHost: Boolean = false,
    tiedSpotIds: ImmutableList<String> = persistentListOf(),
    removingSpotIds: ImmutableSet<String> = persistentSetOf(),
    isClosingVoting: Boolean = false
) {
    LynkTheme {
        VotingSection(
            candidates = candidates,
            votes = votes,
            removingSpotIds = removingSpotIds,
            currentUserId = "me",
            isHost = isHost,
            tiedSpotIds = tiedSpotIds,
            isClosingVoting = isClosingVoting,
            onCastVote = {},
            onRemoveSpot = {},
            onProposeClick = {},
            onCloseVoting = {},
            onBreakTie = {},
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        )
    }
}

@PreviewLightDark
@Composable
private fun VotingSectionAttendeePreview() = VotingSectionPreview()

@PreviewLightDark
@Composable
private fun VotingSectionHostPreview() = VotingSectionPreview(isHost = true)

@PreviewLightDark
@Composable
private fun VotingSectionEmptyPreview() = VotingSectionPreview(
    candidates = persistentListOf(),
    votes = persistentMapOf()
)

@PreviewLightDark
@Composable
private fun VotingSectionTiePreview() = VotingSectionPreview(
    votes = persistentMapOf("0" to "s1", "1" to "s2"),
    isHost = true,
    tiedSpotIds = persistentListOf("s1", "s2")
)

@PreviewLightDark
@Composable
private fun VotingSectionClosingPreview() = VotingSectionPreview(
    isHost = true,
    isClosingVoting = true
)
