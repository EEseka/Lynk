package com.eeseka.lynk.assistant.presentation.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Check
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.MapPin
import com.eeseka.lynk.assistant.presentation.model.SpotSuggestState
import com.eeseka.lynk.shared.design_system.components.images.LynkAsyncImage
import com.eeseka.lynk.shared.design_system.components.layouts.LynkCard
import com.eeseka.lynk.shared.design_system.components.layouts.LynkCardStyle
import com.eeseka.lynk.shared.design_system.components.progress_indicator.LynkProgressIndicator
import com.eeseka.lynk.shared.design_system.components.textfields.LynkText
import com.eeseka.lynk.shared.design_system.theme.LynkTheme
import com.eeseka.lynk.shared.domain.spot.model.PriceLevel
import com.eeseka.lynk.shared.domain.spot.model.SpotCategory
import com.eeseka.lynk.shared.presentation.preview.previewSpots
import com.eeseka.lynk.shared.presentation.spot.mappers.getTitle
import com.eeseka.lynk.shared.presentation.spot.model.SpotUi
import com.eeseka.lynk.shared.presentation.spot.util.SpotPhotoUrlBuilder
import com.eeseka.lynk.shared.presentation.spot.util.getPriceLevelSymbol
import com.eeseka.lynk.shared.presentation.spot.util.rememberGoogleImageRequest
import kotlinx.collections.immutable.ImmutableList
import lynk.feature.assistant.generated.resources.Res
import lynk.feature.assistant.generated.resources.ballot_full
import lynk.feature.assistant.generated.resources.on_the_ballot
import lynk.feature.assistant.generated.resources.suggest
import org.jetbrains.compose.resources.stringResource

@Composable
fun AssistantSpotCard(
    spotName: String,
    spotPhotos: ImmutableList<String>,
    spotCategory: SpotCategory,
    spotPriceLevel: PriceLevel?,
    reason: String,
    suggestState: SpotSuggestState?,
    isLoading: Boolean,
    onClick: () -> Unit,
    onSuggestClick: () -> Unit,
    onPhotoLoadFailed: () -> Unit,
    modifier: Modifier = Modifier
) {
    val primaryPhotoUrl = remember(spotPhotos) {
        SpotPhotoUrlBuilder.getPrimaryPhotoUrl(spotPhotos)
    }
    val imageRequest = rememberGoogleImageRequest(url = primaryPhotoUrl ?: "")
    val contentAlpha by animateFloatAsState(
        targetValue = if (isLoading) 0.4f else 1f,
        label = "assistant_spot_card_alpha"
    )

    LynkCard(
        style = LynkCardStyle.FILLED,
        onClick = onClick.takeIf { !isLoading },
        modifier = modifier.fillMaxWidth()
    ) {
        Box(contentAlignment = Alignment.Center) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer { alpha = contentAlpha }
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(MaterialTheme.shapes.small)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        if (imageRequest != null) {
                            LynkAsyncImage(
                                model = imageRequest,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                onLoadFailed = onPhotoLoadFailed,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Icon(
                                imageVector = Lucide.MapPin,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        LynkText(
                            text = spotName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        val metadata = buildString {
                            append(spotCategory.getTitle())
                            spotPriceLevel?.let {
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
                    }

                    suggestState?.let {
                        SuggestControl(
                            suggestState = it,
                            enabled = !isLoading,
                            onSuggestClick = onSuggestClick
                        )
                    }
                }

                LynkText(
                    text = reason,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            CardLoadingSpinner(isLoading = isLoading)
        }
    }
}

@Composable
private fun SuggestControl(
    suggestState: SpotSuggestState,
    enabled: Boolean,
    onSuggestClick: () -> Unit
) {
    AnimatedContent(
        targetState = suggestState,
        transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(200)) },
        label = "assistant_suggest_action"
    ) { target ->
        when (target) {
            SpotSuggestState.SUGGESTING -> {
                LynkProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp
                )
            }

            SpotSuggestState.ON_BALLOT -> {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Lucide.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    LynkText(
                        text = stringResource(Res.string.on_the_ballot),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            SpotSuggestState.BALLOT_FULL -> {
                LynkText(
                    text = stringResource(Res.string.ballot_full),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            SpotSuggestState.SUGGEST -> {
                LynkText(
                    text = stringResource(Res.string.suggest),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .clip(CircleShape)
                        .clickable(enabled = enabled, onClick = onSuggestClick)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun CardLoadingSpinner(isLoading: Boolean) {
    AnimatedVisibility(
        visible = isLoading,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        LynkProgressIndicator(modifier = Modifier.size(24.dp))
    }
}

@PreviewLightDark
@Composable
private fun AssistantSpotCardPreview() {
    val (nok, jazzhole, rooftop) = previewSpots
    LynkTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            PreviewSpotCard(nok, suggestState = SpotSuggestState.SUGGEST, isLoading = false)
            PreviewSpotCard(jazzhole, suggestState = SpotSuggestState.SUGGESTING, isLoading = false)
            PreviewSpotCard(rooftop, suggestState = SpotSuggestState.ON_BALLOT, isLoading = false)
            PreviewSpotCard(nok, suggestState = SpotSuggestState.BALLOT_FULL, isLoading = false)
            PreviewSpotCard(jazzhole, suggestState = SpotSuggestState.SUGGEST, isLoading = true)
            PreviewSpotCard(rooftop, suggestState = null, isLoading = false)
        }
    }
}

@Composable
private fun PreviewSpotCard(
    spot: SpotUi,
    suggestState: SpotSuggestState?,
    isLoading: Boolean
) {
    AssistantSpotCard(
        spotName = spot.name,
        spotPhotos = spot.photoUrls,
        spotCategory = spot.category,
        spotPriceLevel = spot.priceLevel,
        reason = "Relaxed rooftop seating that suits a chill night, and it stays open until 2 AM on Fridays.",
        suggestState = suggestState,
        isLoading = isLoading,
        onClick = {},
        onSuggestClick = {},
        onPhotoLoadFailed = {}
    )
}
