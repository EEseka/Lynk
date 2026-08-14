package com.eeseka.lynk.shared.presentation.spot.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.MapPin
import com.composables.icons.lucide.Star
import com.eeseka.lynk.shared.design_system.components.layouts.LynkCard
import com.eeseka.lynk.shared.design_system.components.textfields.LynkText
import com.eeseka.lynk.shared.design_system.theme.LynkTheme
import com.eeseka.lynk.shared.design_system.theme.extended
import com.eeseka.lynk.shared.domain.spot.model.PriceLevel
import com.eeseka.lynk.shared.domain.spot.model.SpotCategory
import com.eeseka.lynk.shared.presentation.spot.mappers.getTitle
import com.eeseka.lynk.shared.presentation.spot.util.DistanceCalculator
import com.eeseka.lynk.shared.presentation.spot.util.SpotPhotoUrlBuilder
import com.eeseka.lynk.shared.presentation.spot.util.getPriceLevelSymbol
import com.eeseka.lynk.shared.presentation.spot.util.rememberGoogleImageRequest
import lynk.shared.generated.resources.Res
import lynk.shared.generated.resources.km
import lynk.shared.generated.resources.m
import org.jetbrains.compose.resources.stringResource

@Composable
fun SpotDiscoverCard(
    spotName: String,
    spotPhotos: List<String>,
    spotLatitude: Double,
    spotLongitude: Double,
    spotCategory: SpotCategory,
    spotPriceLevel: PriceLevel?,
    spotRating: Double?,
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
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(4f / 3f)
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

                if (spotRating != null) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.5f))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Lucide.Star,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.extended.gold
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        LynkText(
                            text = spotRating.toString(),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                LynkText(
                    text = spotName,
                    style = MaterialTheme.typography.titleMedium,
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

                // Distance Footer
                if (distanceString != null) {
                    Row(
                        modifier = Modifier.padding(top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Lucide.MapPin,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        LynkText(
                            text = distanceString,
                            style = MaterialTheme.typography.labelMedium,
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
private fun SpotDiscoverCardPreview() {
    LynkTheme {
        SpotDiscoverCard(
            spotName = "Mega Chicken Substation",
            spotPhotos = listOf("https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?w=800&q=80"),
            spotLatitude = 6.443,
            spotLongitude = 2.455,
            spotCategory = SpotCategory.RESTAURANT,
            spotPriceLevel = PriceLevel.MODERATE,
            spotRating = 4.8,
            userLat = 3.33,
            userLng = 2.344,
            onClick = { }
        )
    }
}