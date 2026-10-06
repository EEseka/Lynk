package com.eeseka.lynk.shared.presentation.spot.mappers

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.composables.icons.lucide.Banknote
import com.composables.icons.lucide.Building2
import com.composables.icons.lucide.CalendarCheck
import com.composables.icons.lucide.CalendarClock
import com.composables.icons.lucide.CircleCheck
import com.composables.icons.lucide.CircleParking
import com.composables.icons.lucide.CircleX
import com.composables.icons.lucide.Coffee
import com.composables.icons.lucide.Compass
import com.composables.icons.lucide.CreditCard
import com.composables.icons.lucide.KeyRound
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Martini
import com.composables.icons.lucide.Music
import com.composables.icons.lucide.Trees
import com.composables.icons.lucide.TriangleAlert
import com.composables.icons.lucide.Tv
import com.composables.icons.lucide.Users
import com.composables.icons.lucide.Utensils
import com.composables.icons.lucide.Wine
import com.eeseka.lynk.shared.domain.spot.model.BusinessStatus
import com.eeseka.lynk.shared.domain.spot.model.PriceLevel
import com.eeseka.lynk.shared.domain.spot.model.Spot
import com.eeseka.lynk.shared.domain.spot.model.SpotCategory
import com.eeseka.lynk.shared.presentation.spot.model.SpotDayHoursUi
import com.eeseka.lynk.shared.presentation.spot.model.SpotHighlight
import com.eeseka.lynk.shared.presentation.spot.model.SpotUi
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.datetime.TimeZone
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
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
import lynk.shared.generated.resources.spot_highlight_cash_only
import lynk.shared.generated.resources.spot_highlight_cocktails
import lynk.shared.generated.resources.spot_highlight_free_parking
import lynk.shared.generated.resources.spot_highlight_groups
import lynk.shared.generated.resources.spot_highlight_live_music
import lynk.shared.generated.resources.spot_highlight_outdoor
import lynk.shared.generated.resources.spot_highlight_paid_parking
import lynk.shared.generated.resources.spot_highlight_reservable
import lynk.shared.generated.resources.spot_highlight_sports
import lynk.shared.generated.resources.spot_highlight_takes_cards
import lynk.shared.generated.resources.spot_highlight_valet_parking
import lynk.shared.generated.resources.spot_status_closed_permanently
import lynk.shared.generated.resources.spot_status_closed_temporarily
import lynk.shared.generated.resources.spot_status_opening_soon
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

@Composable
fun BusinessStatus.getTitle(): String? {
    val title = when (this) {
        BusinessStatus.OPERATIONAL -> return null
        BusinessStatus.CLOSED_TEMPORARILY -> Res.string.spot_status_closed_temporarily
        BusinessStatus.CLOSED_PERMANENTLY -> Res.string.spot_status_closed_permanently
        BusinessStatus.FUTURE_OPENING -> Res.string.spot_status_opening_soon
    }
    return stringResource(title)
}

fun BusinessStatus.getIcon(): ImageVector {
    return when (this) {
        BusinessStatus.OPERATIONAL -> Lucide.CircleCheck
        BusinessStatus.CLOSED_TEMPORARILY -> Lucide.TriangleAlert
        BusinessStatus.CLOSED_PERMANENTLY -> Lucide.CircleX
        BusinessStatus.FUTURE_OPENING -> Lucide.CalendarClock
    }
}

@Composable
fun SpotHighlight.getTitle(): String {
    return stringResource(
        when (this) {
            SpotHighlight.GOOD_FOR_GROUPS -> Res.string.spot_highlight_groups
            SpotHighlight.RESERVABLE -> Res.string.spot_highlight_reservable
            SpotHighlight.LIVE_MUSIC -> Res.string.spot_highlight_live_music
            SpotHighlight.OUTDOOR_SEATING -> Res.string.spot_highlight_outdoor
            SpotHighlight.COCKTAILS -> Res.string.spot_highlight_cocktails
            SpotHighlight.SPORTS -> Res.string.spot_highlight_sports
            SpotHighlight.FREE_PARKING -> Res.string.spot_highlight_free_parking
            SpotHighlight.PAID_PARKING -> Res.string.spot_highlight_paid_parking
            SpotHighlight.VALET_PARKING -> Res.string.spot_highlight_valet_parking
            SpotHighlight.CASH_ONLY -> Res.string.spot_highlight_cash_only
            SpotHighlight.TAKES_CARDS -> Res.string.spot_highlight_takes_cards
        }
    )
}

fun SpotHighlight.getIcon(): ImageVector {
    return when (this) {
        SpotHighlight.GOOD_FOR_GROUPS -> Lucide.Users
        SpotHighlight.RESERVABLE -> Lucide.CalendarCheck
        SpotHighlight.LIVE_MUSIC -> Lucide.Music
        SpotHighlight.OUTDOOR_SEATING -> Lucide.Trees
        SpotHighlight.COCKTAILS -> Lucide.Martini
        SpotHighlight.SPORTS -> Lucide.Tv
        SpotHighlight.FREE_PARKING -> Lucide.CircleParking
        SpotHighlight.PAID_PARKING -> Lucide.CircleParking
        SpotHighlight.VALET_PARKING -> Lucide.KeyRound
        SpotHighlight.CASH_ONLY -> Lucide.Banknote
        SpotHighlight.TAKES_CARDS -> Lucide.CreditCard
    }
}

fun Spot.toSpotUi() = SpotUi(
    id = id,
    name = name,
    typeLabel = typeLabel,
    description = description,
    aiSummary = generativeSummary ?: reviewSummary,
    photoUrls = photoUrls.toImmutableList(),
    category = category,
    priceLevel = priceLevel,
    priceRange = priceRange,
    rating = rating,
    reviewCount = reviewCount,
    businessStatus = businessStatus,
    isOpenNow = openingHours?.isOpenNow,
    nextOpenTime = openingHours?.nextOpenTime,
    nextCloseTime = openingHours?.nextCloseTime,
    weekHours = openingHours?.weekdayDescriptions.orEmpty().toWeekHours(),
    highlights = toHighlights().toImmutableList(),
    shortAddress = shortAddress,
    latitude = latitude,
    longitude = longitude,
    phoneNumber = phoneNumber,
    websiteUrl = websiteUrl,
    googleMapsUrl = googleMapsUrl,
    directionsUrl = directionsUrl,
    isSaved = isSaved
)

// Splits Google's "Monday: 11:00 AM – 1:00 AM" lines into day and hours, Monday first, and marks today
private fun List<String>.toWeekHours(): ImmutableList<SpotDayHoursUi> {
    val todayIndex = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).dayOfWeek.isoDayNumber - 1

    return mapIndexed { index, line ->
        SpotDayHoursUi(
            day = line.substringBefore(": "),
            hours = line.substringAfter(": ", missingDelimiterValue = ""),
            isToday = index == todayIndex
        )
    }.toImmutableList()
}

private fun Spot.toHighlights(): List<SpotHighlight> {
    val hasFreeParking = parking?.hasFreeLot == true || parking?.hasFreeStreet == true || parking?.hasFreeGarage == true
    val hasPaidParking = parking?.hasPaidLot == true || parking?.hasPaidStreet == true || parking?.hasPaidGarage == true
    val isCashOnly = payment?.acceptsCashOnly == true
    val takesCards = payment?.acceptsCreditCards == true || payment?.acceptsDebitCards == true || payment?.acceptsNfc == true

    return listOfNotNull(
        SpotHighlight.GOOD_FOR_GROUPS.takeIf { amenities?.isGoodForGroups == true },
        SpotHighlight.RESERVABLE.takeIf { amenities?.isReservable == true },
        SpotHighlight.LIVE_MUSIC.takeIf { amenities?.hasLiveMusic == true },
        SpotHighlight.OUTDOOR_SEATING.takeIf { amenities?.hasOutdoorSeating == true },
        SpotHighlight.COCKTAILS.takeIf { amenities?.servesCocktails == true },
        SpotHighlight.SPORTS.takeIf { amenities?.isGoodForWatchingSports == true },
        SpotHighlight.FREE_PARKING.takeIf { hasFreeParking },
        // Free parking is the news; paid only matters when there is nothing free
        SpotHighlight.PAID_PARKING.takeIf { hasPaidParking && !hasFreeParking },
        SpotHighlight.VALET_PARKING.takeIf { parking?.hasValet == true },
        SpotHighlight.CASH_ONLY.takeIf { isCashOnly },
        // Cash only rules out cards
        SpotHighlight.TAKES_CARDS.takeIf { takesCards && !isCashOnly }
    )
}