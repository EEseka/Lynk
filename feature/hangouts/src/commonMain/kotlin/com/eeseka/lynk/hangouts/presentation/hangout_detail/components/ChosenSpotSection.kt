package com.eeseka.lynk.hangouts.presentation.hangout_detail.components

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
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.composables.icons.lucide.Copy
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.MapPin
import com.composables.icons.lucide.Star
import com.eeseka.lynk.shared.design_system.components.layouts.LynkCard
import com.eeseka.lynk.shared.design_system.components.layouts.LynkCardStyle
import com.eeseka.lynk.shared.design_system.components.textfields.LynkText
import com.eeseka.lynk.shared.design_system.components.util.AppHaptic
import com.eeseka.lynk.shared.design_system.components.util.rememberAppHaptic
import com.eeseka.lynk.shared.design_system.theme.LynkTheme
import com.eeseka.lynk.shared.design_system.theme.extended
import com.eeseka.lynk.shared.domain.spot.model.SpotCategory
import com.eeseka.lynk.shared.presentation.spot.model.SpotUi
import com.eeseka.lynk.shared.presentation.spot.util.SpotPhotoUrlBuilder
import com.eeseka.lynk.shared.presentation.spot.util.rememberGoogleImageRequest
import lynk.feature.hangouts.generated.resources.Res
import lynk.feature.hangouts.generated.resources.detail_chosen_spot
import lynk.feature.hangouts.generated.resources.detail_get_a_ride
import lynk.feature.hangouts.generated.resources.detail_no_spot_message
import lynk.feature.hangouts.generated.resources.detail_no_spot_title
import org.jetbrains.compose.resources.stringResource

@Composable
fun ChosenSpotSection(
    chosenSpot: SpotUi?,
    onSpotClick: () -> Unit,
    canCopyAddress: Boolean = false,
    onCopyAddressClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val hapticFeedback = rememberAppHaptic()

    DetailSection(
        title = stringResource(Res.string.detail_chosen_spot),
        trailing = if (canCopyAddress) {
            {
                Row(
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .clip(CircleShape)
                        .clickable {
                            hapticFeedback(AppHaptic.ImpactLight)
                            onCopyAddressClick()
                        }
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LynkText(
                        text = stringResource(Res.string.detail_get_a_ride),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                    Icon(
                        imageVector = Lucide.Copy,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        } else null,
        modifier = modifier
    ) {
        if (chosenSpot != null) {
            ChosenSpotCard(
                spotName = chosenSpot.name,
                spotAddress = chosenSpot.shortAddress,
                photoUrls = chosenSpot.photoUrls,
                rating = chosenSpot.rating,
                onClick = onSpotClick
            )
        } else {
            PlaceholderCard(
                icon = Lucide.MapPin,
                title = stringResource(Res.string.detail_no_spot_title),
                message = stringResource(Res.string.detail_no_spot_message),
                iconTint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ChosenSpotCard(
    spotName: String,
    spotAddress: String?,
    photoUrls: List<String>,
    rating: Double?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hapticFeedback = rememberAppHaptic()
    val scheme = MaterialTheme.colorScheme
    val primaryPhotoUrl = remember(photoUrls) {
        SpotPhotoUrlBuilder.getPrimaryPhotoUrl(photoUrls)
    }
    val imageRequest = rememberGoogleImageRequest(url = primaryPhotoUrl ?: "")
    // Tapping opens the full spot sheet — the card only has room for name, address and rating.
    LynkCard(
        style = LynkCardStyle.OUTLINED,
        modifier = modifier.fillMaxWidth(),
        onClick = {
            hapticFeedback(AppHaptic.ImpactLight)
            onClick()
        }
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
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(scheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                // Photo when there is one; the pin icon is the fallback.
                if (imageRequest != null) {
                    AsyncImage(
                        model = imageRequest,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Lucide.MapPin,
                        contentDescription = null,
                        tint = scheme.onSecondaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                LynkText(
                    text = spotName,
                    style = MaterialTheme.typography.titleSmall
                )
                spotAddress?.let {
                    LynkText(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant
                    )
                }
            }
            rating?.let { ratingValue ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Lucide.Star,
                        contentDescription = null,
                        tint = scheme.extended.gold,
                        modifier = Modifier.size(16.dp)
                    )
                    LynkText(
                        text = ratingValue.toString(),
                        style = MaterialTheme.typography.labelLarge,
                        color = scheme.onSurface
                    )
                }
            }
        }
    }
}

private val previewSpot = SpotUi(
    id = "s1",
    name = "The Rooftop Lounge",
    description = "Skyline views",
    photoUrls = emptyList(),
    category = SpotCategory.RESTAURANT,
    tags = emptyList(),
    priceLevel = null,
    rating = 4.6,
    reviewCount = 214,
    isOpenNow = true,
    shortAddress = "12 Admiralty Way, Lekki",
    latitude = 6.4,
    longitude = 3.4,
    websiteUrl = null,
    googleMapsUrl = null,
    isSaved = false
)

@Composable
private fun ChosenSpotSectionPreview(
    chosenSpot: SpotUi? = previewSpot,
    canCopyAddress: Boolean = false
) {
    LynkTheme {
        ChosenSpotSection(
            chosenSpot = chosenSpot,
            onSpotClick = {},
            canCopyAddress = canCopyAddress,
            onCopyAddressClick = {},
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface)
                .padding(16.dp)
        )
    }
}

@PreviewLightDark
@Composable
private fun ChosenSpotSectionDefaultPreview() = ChosenSpotSectionPreview()

@PreviewLightDark
@Composable
private fun ChosenSpotSectionNoRatingPreview() = ChosenSpotSectionPreview(
    chosenSpot = previewSpot.copy(rating = null, shortAddress = null)
)

@PreviewLightDark
@Composable
private fun ChosenSpotSectionEmptyPreview() = ChosenSpotSectionPreview(chosenSpot = null)

@PreviewLightDark
@Composable
private fun ChosenSpotSectionCopyAddressPreview() =
    ChosenSpotSectionPreview(canCopyAddress = true)
