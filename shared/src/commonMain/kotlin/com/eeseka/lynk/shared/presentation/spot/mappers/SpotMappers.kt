package com.eeseka.lynk.shared.presentation.spot.mappers

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.composables.icons.lucide.Building2
import com.composables.icons.lucide.Coffee
import com.composables.icons.lucide.Compass
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Music
import com.composables.icons.lucide.Utensils
import com.composables.icons.lucide.Wine
import com.eeseka.lynk.shared.domain.spot.model.PriceLevel
import com.eeseka.lynk.shared.domain.spot.model.Spot
import com.eeseka.lynk.shared.domain.spot.model.SpotCategory
import com.eeseka.lynk.shared.presentation.spot.model.SpotUi
import lynk.shared.generated.resources.Res
import lynk.shared.generated.resources.activity
import lynk.shared.generated.resources.cafe
import lynk.shared.generated.resources.cheap
import lynk.shared.generated.resources.club
import lynk.shared.generated.resources.expensive
import lynk.shared.generated.resources.lounge
import lynk.shared.generated.resources.luxury
import lynk.shared.generated.resources.moderate
import lynk.shared.generated.resources.other
import lynk.shared.generated.resources.restaurant
import org.jetbrains.compose.resources.stringResource

@Composable
fun SpotCategory.getTitle(): String {
    return stringResource(
        when (this) {
            SpotCategory.LOUNGE -> Res.string.lounge
            SpotCategory.CAFE -> Res.string.cafe
            SpotCategory.CLUB -> Res.string.club
            SpotCategory.RESTAURANT -> Res.string.restaurant
            SpotCategory.ACTIVITY -> Res.string.activity
            SpotCategory.OTHER -> Res.string.other
        }
    )
}

fun SpotCategory.getIcon(): ImageVector {
    return when (this) {
        SpotCategory.LOUNGE -> Lucide.Wine
        SpotCategory.CAFE -> Lucide.Coffee
        SpotCategory.CLUB -> Lucide.Music
        SpotCategory.RESTAURANT -> Lucide.Utensils
        SpotCategory.ACTIVITY -> Lucide.Compass
        SpotCategory.OTHER -> Lucide.Building2
    }
}

@Composable
fun PriceLevel.getTitle(): String {
    return stringResource(
        when (this) {
            PriceLevel.CHEAP -> Res.string.cheap
            PriceLevel.MODERATE -> Res.string.moderate
            PriceLevel.EXPENSIVE -> Res.string.expensive
            PriceLevel.LUXURY -> Res.string.luxury
        }
    )
}

fun Spot.toSpotUi() = SpotUi(
    id = id,
    name = name,
    description = description,
    photoUrls = photoUrls,
    category = category,
    tags = tags,
    priceLevel = priceLevel,
    rating = rating,
    reviewCount = reviewCount,
    isOpenNow = isOpenNow,
    shortAddress = shortAddress,
    latitude = latitude,
    longitude = longitude,
    websiteUrl = websiteUrl,
    googleMapsUrl = googleMapsUrl,
    isSaved = isSaved
)