package com.eeseka.lynk.discover.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.composables.icons.lucide.Globe
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Map
import com.composables.icons.lucide.MapPin
import com.eeseka.lynk.shared.design_system.components.buttons.LynkButton
import com.eeseka.lynk.shared.design_system.components.buttons.LynkTonalIconButton
import com.eeseka.lynk.shared.design_system.components.modals_and_overlays.LynkAdaptiveSheet
import com.eeseka.lynk.shared.design_system.components.textfields.LynkText
import com.eeseka.lynk.shared.design_system.components.util.AppHaptic
import com.eeseka.lynk.shared.design_system.components.util.rememberAppHaptic
import com.eeseka.lynk.shared.design_system.theme.extended
import com.eeseka.lynk.shared.presentation.spot.mappers.getTitle
import com.eeseka.lynk.shared.presentation.spot.model.SpotUi
import com.eeseka.lynk.shared.presentation.spot.util.DistanceCalculator
import com.eeseka.lynk.shared.presentation.spot.util.SpotPhotoUrlBuilder
import com.eeseka.lynk.shared.presentation.spot.util.getPriceLevelSymbol
import com.eeseka.lynk.shared.presentation.spot.util.rememberGoogleImageRequest
import lynk.feature.discover.generated.resources.Res
import lynk.feature.discover.generated.resources.about_this_spot
import lynk.feature.discover.generated.resources.closed
import lynk.feature.discover.generated.resources.create_hangout_here
import lynk.feature.discover.generated.resources.km
import lynk.feature.discover.generated.resources.m
import lynk.feature.discover.generated.resources.no_description_available
import lynk.feature.discover.generated.resources.open_in_google_maps
import lynk.feature.discover.generated.resources.open_now
import lynk.feature.discover.generated.resources.reviews_count
import lynk.feature.discover.generated.resources.save_spot
import lynk.feature.discover.generated.resources.unsave_spot
import lynk.feature.discover.generated.resources.visit_website
import org.jetbrains.compose.resources.stringResource

@Composable
fun SpotDetailSheet(
    spot: SpotUi,
    userLat: Double?,
    userLng: Double?,
    onCreateHangoutClick: (String) -> Unit,
    onToggleSave: (String, Boolean) -> Unit,
    onDismissRequest: () -> Unit
) {
    val hapticFeedback = rememberAppHaptic()
    var initialImageIndex by remember { mutableStateOf<Int?>(null) }
    val scrollState = rememberScrollState()
    val uriHandler = LocalUriHandler.current

    if (initialImageIndex != null) {
        FullScreenImageViewer(
            rawUrls = spot.photoUrls,
            initialIndex = initialImageIndex!!,
            onDismiss = { initialImageIndex = null }
        )
    }

    LynkAdaptiveSheet(
        onDismissRequest = onDismissRequest,
        skipBottomSheetPartiallyExpanded = false
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .verticalScroll(scrollState)
                    .padding(bottom = 16.dp)
            ) {
                if (spot.photoUrls.isNotEmpty()) {
                    if (spot.photoUrls.size == 1) {
                        val fullUrl = remember(spot.photoUrls[0]) {
                            SpotPhotoUrlBuilder.build(spot.photoUrls[0])
                        }
                        val imageRequest = rememberGoogleImageRequest(url = fullUrl ?: "")
                        if (imageRequest != null) {
                            AsyncImage(
                                model = imageRequest,
                                contentDescription = spot.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                                    .clip(MaterialTheme.shapes.medium)
                                    .clickable {
                                        hapticFeedback(AppHaptic.Selection)
                                        initialImageIndex = 0
                                    }
                            )
                        }
                    } else {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            itemsIndexed(spot.photoUrls) { index, rawUrl ->
                                val fullUrl = remember(rawUrl) {
                                    SpotPhotoUrlBuilder.build(rawUrl)
                                }

                                val imageRequest = rememberGoogleImageRequest(url = fullUrl ?: "")

                                if (imageRequest != null) {
                                    AsyncImage(
                                        model = imageRequest,
                                        contentDescription = spot.name,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(width = 280.dp, height = 200.dp)
                                            .clip(MaterialTheme.shapes.medium)
                                            .clickable {
                                                hapticFeedback(AppHaptic.Selection)
                                                initialImageIndex = index
                                            }
                                    )
                                }
                            }
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .clip(MaterialTheme.shapes.medium)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Lucide.MapPin,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LynkText(
                        text = spot.name,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    val isSaved = spot.isSaved
                    LynkTonalIconButton(
                        onClick = {
                            hapticFeedback(AppHaptic.ImpactLight)
                            onToggleSave(spot.id, isSaved)
                        },
                        containerColor = if (isSaved) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
                        contentColor = if (isSaved) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface
                    ) {
                        Icon(
                            imageVector = if (isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = stringResource(if (isSaved) Res.string.unsave_spot else Res.string.save_spot)
                        )
                    }
                }

                spot.rating?.let { rating ->
                    if (rating > 0) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RatingStars(rating = rating)

                            spot.reviewCount?.let { reviewCount ->
                                if (reviewCount > 0) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    LynkText(
                                        text = stringResource(
                                            Res.string.reviews_count,
                                            reviewCount
                                        ),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val isOpenNow = spot.isOpenNow
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (isOpenNow) MaterialTheme.colorScheme.extended.success else MaterialTheme.colorScheme.error)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    LynkText(
                        text = stringResource(if (isOpenNow) Res.string.open_now else Res.string.closed),
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isOpenNow) MaterialTheme.colorScheme.extended.success else MaterialTheme.colorScheme.error
                    )

                    LynkText(
                        text = "  ·  ",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    LynkText(
                        text = spot.category.getTitle(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    spot.priceLevel?.let { price ->
                        LynkText(
                            text = "  ·  ",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        LynkText(
                            text = getPriceLevelSymbol(price.tier),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    val kmStr = stringResource(Res.string.km)
                    val mStr = stringResource(Res.string.m)

                    val distanceString = remember(spot.latitude, spot.longitude, userLat, userLng) {
                        if (userLat != null && userLng != null) {
                            val meters = DistanceCalculator.calculateDistanceInMeters(
                                userLat = userLat,
                                userLng = userLng,
                                spotLat = spot.latitude,
                                spotLng = spot.longitude
                            )
                            if (meters > 1000) "${(meters / 1000.0).toInt()} $kmStr" else "$meters $mStr"
                        } else null
                    }

                    distanceString?.let { dist ->
                        LynkText(
                            text = "  ·  ",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        LynkText(
                            text = dist,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                spot.shortAddress?.let { address ->
                    if (address.isNotBlank()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Lucide.MapPin,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(8.dp))

                            LynkText(
                                text = address,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                val hasWebsite = !spot.websiteUrl.isNullOrBlank()
                val hasGoogleMaps = !spot.googleMapsUrl.isNullOrBlank()

                if (hasWebsite || hasGoogleMaps) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        if (hasWebsite) {
                            ExternalActionRow(
                                text = stringResource(Res.string.visit_website),
                                leadingIcon = Lucide.Globe,
                                onClick = {
                                    hapticFeedback(AppHaptic.ImpactLight)
                                    uriHandler.openUri(spot.websiteUrl!!)
                                }
                            )
                        }

                        if (hasGoogleMaps) {
                            ExternalActionRow(
                                text = stringResource(Res.string.open_in_google_maps),
                                leadingIcon = Lucide.Map,
                                onClick = {
                                    hapticFeedback(AppHaptic.ImpactLight)
                                    uriHandler.openUri(spot.googleMapsUrl!!)
                                }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (spot.tags.isNotEmpty()) {
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        spot.tags.forEach { tag ->
                            val formattedTag = tag.replace("_", " ")
                                .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }

                            val shape = MaterialTheme.shapes.small
                            Row(
                                modifier = Modifier
                                    .clip(shape)
                                    .border(
                                        width = 1.dp,
                                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                        shape = shape
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                LynkText(
                                    text = formattedTag,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                } else {
                    Spacer(modifier = Modifier.height(8.dp))
                }

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(24.dp))

                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    LynkText(
                        text = stringResource(Res.string.about_this_spot),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LynkText(
                        text = spot.description
                            ?: stringResource(Res.string.no_description_available),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            LynkButton(
                modifier = Modifier.padding(16.dp),
                text = stringResource(Res.string.create_hangout_here),
                onClick = {
                    hapticFeedback(AppHaptic.ImpactMedium)
                    onCreateHangoutClick(spot.id)
                }
            )
        }
    }
}