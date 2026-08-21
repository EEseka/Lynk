package com.eeseka.lynk.create_hangout.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.composables.icons.lucide.MapPin
import com.eeseka.lynk.shared.design_system.components.layouts.LynkCard
import com.eeseka.lynk.shared.design_system.components.textfields.LynkText
import com.eeseka.lynk.shared.design_system.theme.LynkTheme
import com.eeseka.lynk.shared.domain.spot.model.PriceLevel
import com.eeseka.lynk.shared.domain.spot.model.Spot
import com.eeseka.lynk.shared.domain.spot.model.SpotCategory
import com.eeseka.lynk.shared.presentation.spot.mappers.getTitle
import com.eeseka.lynk.shared.presentation.spot.util.DistanceCalculator
import com.eeseka.lynk.shared.presentation.spot.util.SpotPhotoUrlBuilder
import com.eeseka.lynk.shared.presentation.spot.util.getPriceLevelSymbol
import com.eeseka.lynk.shared.presentation.spot.util.rememberGoogleImageRequest
import lynk.feature.create_hangout.generated.resources.Res
import lynk.feature.create_hangout.generated.resources.km
import lynk.feature.create_hangout.generated.resources.m
import org.jetbrains.compose.resources.stringResource

@Composable
fun SpotPickerListItem(
    spotName: String,
    spotPhotos: List<String>,
    spotLatitude: Double,
    spotLongitude: Double,
    spotCategory: SpotCategory,
    spotPriceLevel: PriceLevel?,
    userLat: Double?,
    userLng: Double?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val primaryPhotoUrl = remember(spotPhotos) {
        SpotPhotoUrlBuilder.getPrimaryPhotoUrl(spotPhotos)
    }

    val km = stringResource(Res.string.km)
    val m = stringResource(Res.string.m)

    val distanceString = remember(spotLatitude, spotLongitude, userLat, userLng) {
        if (userLat != null && userLng != null) {
            val meters = DistanceCalculator.calculateDistanceInMeters(
                userLat = userLat,
                userLng = userLng,
                spotLat = spotLatitude,
                spotLng = spotLongitude
            )
            if (meters > 1000) "${(meters / 1000.0).toInt()} $km" else "$meters $m"
        } else null
    }

    val imageRequest = rememberGoogleImageRequest(url = primaryPhotoUrl ?: "")

    LynkCard(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick
    ) {
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
                        contentDescription = spotName,
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
                    text = spotName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                val metadata = buildString {
                    append(spotCategory.getTitle())
                    if (spotPriceLevel != null) {
                        append(" • ")
                        append(getPriceLevelSymbol(spotPriceLevel.tier))
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
                            imageVector = Lucide.MapPin,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        LynkText(
                            text = distanceString,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@PreviewLightDark
@Composable
fun SpotPickerListItemPreview() {
    LynkTheme { 
        SpotPickerListItem(
            spotName = "Mega Chicken Substation",
            spotPhotos = listOf("https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?w=800&q=80"),
            spotLatitude = 6.443,
            spotLongitude = 2.455,
            spotCategory = SpotCategory.RESTAURANT,
            spotPriceLevel = PriceLevel.MODERATE,
            userLat = 3.33,
            userLng = 2.344,
            onClick = { }
        )
    }
}