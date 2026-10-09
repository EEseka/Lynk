package com.eeseka.lynk.shared.presentation.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.CalendarPlus
import com.composables.icons.lucide.Globe
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Map
import com.composables.icons.lucide.MapPin
import com.composables.icons.lucide.Navigation
import com.composables.icons.lucide.Phone
import com.composables.icons.lucide.Sparkles
import com.composables.icons.lucide.Wallet
import com.eeseka.lynk.shared.design_system.components.buttons.LynkButton
import com.eeseka.lynk.shared.design_system.components.buttons.LynkTonalIconButton
import com.eeseka.lynk.shared.design_system.components.images.LynkAsyncImage
import com.eeseka.lynk.shared.design_system.components.modals_and_overlays.LynkAdaptiveSheet
import com.eeseka.lynk.shared.design_system.components.textfields.LynkText
import com.eeseka.lynk.shared.design_system.components.util.AppHaptic
import com.eeseka.lynk.shared.design_system.components.util.rememberAppHaptic
import com.eeseka.lynk.shared.design_system.theme.LynkTheme
import com.eeseka.lynk.shared.domain.spot.model.BusinessStatus
import com.eeseka.lynk.shared.domain.spot.model.PriceLevel
import com.eeseka.lynk.shared.domain.spot.model.SpotCategory
import com.eeseka.lynk.shared.presentation.preview.previewFullSpot
import com.eeseka.lynk.shared.presentation.preview.previewSpot
import com.eeseka.lynk.shared.presentation.preview.previewWeekHours
import com.eeseka.lynk.shared.presentation.spot.components.SpotBusinessStatusPill
import com.eeseka.lynk.shared.presentation.spot.components.SpotHighlightChips
import com.eeseka.lynk.shared.presentation.spot.components.SpotInfoRow
import com.eeseka.lynk.shared.presentation.spot.components.SpotLinkRow
import com.eeseka.lynk.shared.presentation.spot.components.SpotOpenStatus
import com.eeseka.lynk.shared.presentation.spot.mappers.getTitle
import com.eeseka.lynk.shared.presentation.spot.model.SpotHighlight
import com.eeseka.lynk.shared.presentation.spot.model.SpotUi
import com.eeseka.lynk.shared.presentation.spot.util.SpotPhotoUrlBuilder
import com.eeseka.lynk.shared.presentation.spot.util.getPriceLevelSymbol
import com.eeseka.lynk.shared.presentation.spot.util.rememberGoogleImageRequest
import com.eeseka.lynk.shared.presentation.spot.util.rememberSpotDistanceLabel
import com.eeseka.lynk.shared.presentation.spot.util.toPriceRangeLabel
import com.eeseka.lynk.shared.presentation.spot.util.toWebsiteLabel
import com.eeseka.lynk.shared.presentation.util.placeTimeZone
import com.eeseka.lynk.shared.presentation.util.toUpcomingTimeLabel
import kotlinx.collections.immutable.persistentListOf
import kotlinx.datetime.TimeZone
import kotlinx.datetime.offsetAt
import kotlin.time.Clock
import lynk.shared.generated.resources.Res
import lynk.shared.generated.resources.about_this_spot
import lynk.shared.generated.resources.create_hangout_here
import lynk.shared.generated.resources.get_directions
import lynk.shared.generated.resources.open_in_google_maps
import lynk.shared.generated.resources.reviews_count
import lynk.shared.generated.resources.save_spot
import lynk.shared.generated.resources.spot_closes_at
import lynk.shared.generated.resources.spot_hours_local
import lynk.shared.generated.resources.spot_local_time
import lynk.shared.generated.resources.spot_opens_at
import lynk.shared.generated.resources.unsave_spot
import org.jetbrains.compose.resources.stringResource

@Composable
fun SpotDetailSheet(
    spot: SpotUi,
    userLat: Double?,
    userLng: Double?,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    rankLabel: String? = null,
    onCreateHangoutClick: ((String) -> Unit)? = null,
    onToggleSave: ((String, Boolean) -> Unit)? = null
) {
    var initialImageIndex by remember { mutableStateOf<Int?>(null) }

    if (initialImageIndex != null) {
        FullScreenSpotPhotoViewer(
            rawPhotoNames = spot.photoUrls,
            initialIndex = initialImageIndex!!,
            onDismiss = { initialImageIndex = null }
        )
    }

    LynkAdaptiveSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        skipBottomSheetPartiallyExpanded = false
    ) {
        SpotDetailSheetContent(
            spot = spot,
            userLat = userLat,
            userLng = userLng,
            rankLabel = rankLabel,
            onPhotoClick = { index -> initialImageIndex = index },
            onCreateHangoutClick = onCreateHangoutClick,
            onToggleSave = onToggleSave
        )
    }
}

@Composable
private fun SpotDetailSheetContent(
    spot: SpotUi,
    userLat: Double?,
    userLng: Double?,
    rankLabel: String?,
    onPhotoClick: (Int) -> Unit,
    onCreateHangoutClick: ((String) -> Unit)?,
    onToggleSave: ((String, Boolean) -> Unit)?,
    modifier: Modifier = Modifier
) {
    val hapticFeedback = rememberAppHaptic()
    val scrollState = rememberScrollState()
    val uriHandler = LocalUriHandler.current

    // The server caches spots for up to an hour, so Google's open or closed can be out of date by the time it's shown
    val now = Clock.System.now()
    val hasClosedSince = spot.nextCloseTime?.let { it <= now } == true
    val hasOpenedSince = spot.nextOpenTime?.let { it <= now } == true
    val isOpenNow = when (spot.isOpenNow) {
        true -> !hasClosedSince
        false -> hasOpenedSince
        null -> null
    }
    // On the place's clock, so "Opens 8:00 AM" matches the week's hours wherever the viewer is
    val spotTimeZone = placeTimeZone(spot.utcOffsetMinutes)
    // Said out loud when it differs from the phone's, or "Closes 8:30 PM" reads as the viewer's evening
    val isOnAnotherClock = spotTimeZone.offsetAt(now) != TimeZone.currentSystemDefault().offsetAt(now)
    val nextChangeTime = when (isOpenNow) {
        true -> spot.nextCloseTime
        false -> spot.nextOpenTime
        null -> null
    }?.takeIf { it > now }
    val nextChangeTimeLabel = nextChangeTime?.toUpcomingTimeLabel(spotTimeZone)?.let { label ->
        if (isOnAnotherClock) stringResource(Res.string.spot_local_time, label) else label
    }
    val opensOrClosesLabel = nextChangeTimeLabel?.let { label ->
        stringResource(if (isOpenNow == true) Res.string.spot_closes_at else Res.string.spot_opens_at, label)
    }
    val address = spot.shortAddress?.takeIf { it.isNotBlank() }
    val priceRangeLabel = spot.priceRange?.toPriceRangeLabel()
    val websiteUrl = spot.websiteUrl?.takeIf { it.isNotBlank() }
    val googleMapsUrl = spot.googleMapsUrl?.takeIf { it.isNotBlank() }

    Column(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = false)
                .verticalScroll(scrollState)
                .padding(bottom = 16.dp)
        ) {
            if (spot.photoUrls.isNotEmpty()) {
                if (spot.photoUrls.size == 1) {
                    val fullUrl = remember(spot.photoUrls) {
                        SpotPhotoUrlBuilder.getPrimaryPhotoUrl(spot.photoUrls)
                    }
                    val imageRequest = rememberGoogleImageRequest(url = fullUrl ?: "")
                    if (imageRequest != null) {
                        LynkAsyncImage(
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
                                    onPhotoClick(0)
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
                                LynkAsyncImage(
                                    model = imageRequest,
                                    contentDescription = spot.name,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(width = 280.dp, height = 200.dp)
                                        .clip(MaterialTheme.shapes.medium)
                                        .clickable {
                                            hapticFeedback(AppHaptic.Selection)
                                            onPhotoClick(index)
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

            val distanceLabel = rememberSpotDistanceLabel(
                userLatitude = userLat,
                userLongitude = userLng,
                spotLatitude = spot.latitude,
                spotLongitude = spot.longitude
            )
            val metaLine = listOfNotNull(
                spot.typeLabel ?: spot.category.getTitle(),
                spot.priceLevel?.let { getPriceLevelSymbol(it.tier) },
                distanceLabel
            ).joinToString("  ·  ")

            Column(
                modifier = Modifier.padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            rankLabel?.let { label ->
                                LynkText(
                                    text = label,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                            LynkText(
                                text = spot.name,
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            LynkText(
                                text = metaLine,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        onToggleSave?.let { toggleSave ->
                            Spacer(modifier = Modifier.width(16.dp))

                            val isSaved = spot.isSaved
                            LynkTonalIconButton(
                                onClick = {
                                    hapticFeedback(AppHaptic.ImpactLight)
                                    toggleSave(spot.id, isSaved)
                                },
                                containerColor = if (isSaved) MaterialTheme.colorScheme.secondaryContainer else Color.Unspecified,
                                contentColor = if (isSaved) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface
                            ) {
                                AnimatedContent(
                                    targetState = isSaved,
                                    transitionSpec = {
                                        (scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy), initialScale = 0.6f) + fadeIn()) togetherWith (scaleOut(targetScale = 0.6f) + fadeOut())
                                    }
                                ) { isSavedIcon ->
                                    Icon(
                                        imageVector = if (isSavedIcon) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                                        contentDescription = stringResource(if (isSavedIcon) Res.string.unsave_spot else Res.string.save_spot)
                                    )
                                }
                            }
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
                        }
                    }

                    if (spot.businessStatus != null && spot.businessStatus != BusinessStatus.OPERATIONAL) {
                        SpotBusinessStatusPill(
                            status = spot.businessStatus,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    } else {
                        isOpenNow?.let { isOpen ->
                            SpotOpenStatus(
                                isOpenNow = isOpen,
                                opensOrClosesLabel = opensOrClosesLabel,
                                weekHours = spot.weekHours,
                                weekHoursNote = if (isOnAnotherClock) stringResource(Res.string.spot_hours_local) else null
                            )
                        }
                    }
                }

                if (address != null || priceRangeLabel != null) {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        address?.let { label ->
                            SpotInfoRow(text = label, leadingIcon = Lucide.MapPin)
                        }
                        priceRangeLabel?.let { label ->
                            SpotInfoRow(text = label, leadingIcon = Lucide.Wallet)
                        }
                    }
                }

                if (spot.highlights.isNotEmpty()) {
                    SpotHighlightChips(
                        highlights = spot.highlights,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                if (spot.directionsUrl != null || spot.phoneNumber != null || websiteUrl != null || googleMapsUrl != null) {
                    Column {
                        spot.directionsUrl?.let { url ->
                            SpotLinkRow(
                                text = stringResource(Res.string.get_directions),
                                leadingIcon = Lucide.Navigation,
                                onClick = {
                                    hapticFeedback(AppHaptic.ImpactLight)
                                    uriHandler.openUri(url)
                                }
                            )
                        }
                        spot.phoneNumber?.let { number ->
                            SpotLinkRow(
                                text = number,
                                leadingIcon = Lucide.Phone,
                                onClick = {
                                    hapticFeedback(AppHaptic.ImpactLight)
                                    uriHandler.openUri("tel:${number.filterNot { it.isWhitespace() }}")
                                }
                            )
                        }
                        websiteUrl?.let { url ->
                            SpotLinkRow(
                                text = url.toWebsiteLabel(),
                                leadingIcon = Lucide.Globe,
                                onClick = {
                                    hapticFeedback(AppHaptic.ImpactLight)
                                    uriHandler.openUri(url)
                                }
                            )
                        }
                        googleMapsUrl?.let { url ->
                            SpotLinkRow(
                                text = stringResource(Res.string.open_in_google_maps),
                                leadingIcon = Lucide.Map,
                                onClick = {
                                    hapticFeedback(AppHaptic.ImpactLight)
                                    uriHandler.openUri(url)
                                }
                            )
                        }
                    }
                }

                val aboutText = spot.description ?: spot.aiSummary?.text
                if (aboutText != null) {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            LynkText(
                                text = stringResource(Res.string.about_this_spot),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            LynkText(
                                text = aboutText,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (spot.description == null && spot.aiSummary != null) {
                                val disclosureColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Lucide.Sparkles,
                                        contentDescription = null,
                                        tint = disclosureColor,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    LynkText(
                                        text = spot.aiSummary.disclosure,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = disclosureColor
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        onCreateHangoutClick?.let { createHangout ->
            LynkButton(
                modifier = Modifier.padding(16.dp),
                text = stringResource(Res.string.create_hangout_here),
                onClick = {
                    hapticFeedback(AppHaptic.ImpactMedium)
                    createHangout(spot.id)
                },
                leadingIcon = {
                    Icon(
                        imageVector = Lucide.CalendarPlus,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                }
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun SpotDetailSheetFullPreview() = SpotDetailSheetContentPreview(
    spot = previewFullSpot,
    rankLabel = "#3 in Lagos"
)

@PreviewLightDark
@Composable
private fun SpotDetailSheetPartialPreview() = SpotDetailSheetContentPreview(
    spot = previewSpot(
        category = SpotCategory.LOUNGE,
        priceLevel = PriceLevel.EXPENSIVE,
        isOpenNow = false
    ).copy(
        description = "Rooftop lounge with skyline views over Lagos Lagoon.",
        businessStatus = BusinessStatus.CLOSED_TEMPORARILY,
        weekHours = previewWeekHours,
        highlights = persistentListOf(SpotHighlight.PAID_PARKING, SpotHighlight.CASH_ONLY),
        phoneNumber = "+234 810 361 3662"
    )
)

@PreviewLightDark
@Composable
private fun SpotDetailSheetMinimalPreview() = SpotDetailSheetContentPreview(
    spot = previewSpot(
        priceLevel = null,
        rating = null,
        reviewCount = null,
        isOpenNow = null,
        shortAddress = null
    ),
    userLat = null,
    userLng = null
)

@Composable
private fun SpotDetailSheetContentPreview(
    spot: SpotUi,
    userLat: Double? = 6.4433,
    userLng: Double? = 3.4555,
    rankLabel: String? = null
) {
    LynkTheme {
        SpotDetailSheetContent(
            spot = spot,
            userLat = userLat,
            userLng = userLng,
            rankLabel = rankLabel,
            onPhotoClick = {},
            onCreateHangoutClick = {},
            onToggleSave = { _, _ -> },
            modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainerLow)
        )
    }
}
