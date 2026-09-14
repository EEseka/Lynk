package com.eeseka.lynk.hangouts.presentation.hangout_detail.voting.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.UsersRound
import com.eeseka.lynk.shared.design_system.components.layouts.LynkCard
import com.eeseka.lynk.shared.design_system.components.progress_indicator.LynkProgressIndicator
import com.eeseka.lynk.shared.design_system.components.textfields.LynkText
import com.eeseka.lynk.shared.design_system.components.util.AppHaptic
import com.eeseka.lynk.shared.design_system.components.util.rememberAppHaptic
import com.eeseka.lynk.shared.design_system.theme.LynkTheme
import com.eeseka.lynk.shared.presentation.preview.previewSpots
import com.eeseka.lynk.shared.presentation.spot.mappers.getTitle
import com.eeseka.lynk.shared.presentation.spot.model.SpotUi
import com.eeseka.lynk.shared.presentation.spot.util.SpotPhotoUrlBuilder
import com.eeseka.lynk.shared.presentation.spot.util.getPriceLevelSymbol
import com.eeseka.lynk.shared.presentation.spot.util.rememberGoogleImageRequest
import com.eeseka.lynk.shared.presentation.spot.util.rememberSpotDistanceLabel
import lynk.feature.hangouts.generated.resources.Res
import lynk.feature.hangouts.generated.resources.propose_proposed
import lynk.feature.hangouts.generated.resources.propose_suggest_action
import org.jetbrains.compose.resources.stringResource

private enum class SuggestActionState { Proposing, Added, Suggest }

@Composable
fun SuggestSpotListItem(
    spot: SpotUi,
    originLat: Double?,
    originLng: Double?,
    isProposing: Boolean,
    alreadyAdded: Boolean,
    onSuggest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val primaryPhotoUrl = remember(spot.photoUrls) {
        SpotPhotoUrlBuilder.getPrimaryPhotoUrl(spot.photoUrls)
    }

    val distanceString = rememberSpotDistanceLabel(
        userLatitude = originLat,
        userLongitude = originLng,
        spotLatitude = spot.latitude,
        spotLongitude = spot.longitude
    )

    val imageRequest = rememberGoogleImageRequest(url = primaryPhotoUrl ?: "")

    LynkCard(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                if (imageRequest != null) {
                    AsyncImage(
                        model = imageRequest,
                        contentDescription = spot.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                LynkText(
                    text = spot.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                val metadata = buildString {
                    append(spot.category.getTitle())
                    spot.priceLevel?.let {
                        append(" • ")
                        append(getPriceLevelSymbol(it.tier))
                    }
                }
                LynkText(
                    text = metadata,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (distanceString != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Lucide.UsersRound,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        LynkText(
                            text = distanceString,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            SuggestControl(
                isProposing = isProposing,
                alreadyAdded = alreadyAdded,
                onSuggest = onSuggest
            )
        }
    }
}

@Composable
private fun SuggestControl(
    isProposing: Boolean,
    alreadyAdded: Boolean,
    onSuggest: () -> Unit
) {
    val hapticFeedback = rememberAppHaptic()

    val action = when {
        isProposing -> SuggestActionState.Proposing
        alreadyAdded -> SuggestActionState.Added
        else -> SuggestActionState.Suggest
    }

    AnimatedContent(
        targetState = action,
        transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(200)) },
        label = "suggest_action"
    ) { target ->
        when (target) {
            SuggestActionState.Proposing -> {
                LynkProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            SuggestActionState.Added -> {
                LynkText(
                    text = stringResource(Res.string.propose_proposed),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            SuggestActionState.Suggest -> {
                LynkText(
                    text = stringResource(Res.string.propose_suggest_action),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .clip(CircleShape)
                        .clickable {
                            hapticFeedback(AppHaptic.ImpactMedium)
                            onSuggest()
                        }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun SuggestSpotListItemPreview() {
    val (nok, jazzhole, rooftop) = previewSpots
    LynkTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface)
                .padding(16.dp)
        ) {
            SuggestSpotListItem(nok, originLat = 6.44, originLng = 3.42, isProposing = false, alreadyAdded = false, onSuggest = {})
            SuggestSpotListItem(jazzhole, originLat = null, originLng = null, isProposing = false, alreadyAdded = false, onSuggest = {})
            SuggestSpotListItem(rooftop, originLat = 6.44, originLng = 3.42, isProposing = true, alreadyAdded = false, onSuggest = {})
            SuggestSpotListItem(nok, originLat = 6.44, originLng = 3.42, isProposing = false, alreadyAdded = true, onSuggest = {})
        }
    }
}