package com.eeseka.lynk.shared.data.spot.mappers

import com.eeseka.lynk.shared.data.spot.dto.PaginatedSpotsDto
import com.eeseka.lynk.shared.data.spot.dto.SpotAiSummaryDto
import com.eeseka.lynk.shared.data.spot.dto.SpotAmenitiesDto
import com.eeseka.lynk.shared.data.spot.dto.SpotDto
import com.eeseka.lynk.shared.data.spot.dto.SpotOpeningHoursDto
import com.eeseka.lynk.shared.data.spot.dto.SpotParkingDto
import com.eeseka.lynk.shared.data.spot.dto.SpotPaymentDto
import com.eeseka.lynk.shared.data.spot.dto.SpotPriceRangeDto
import com.eeseka.lynk.shared.domain.spot.model.PaginatedSpots
import com.eeseka.lynk.shared.domain.spot.model.Spot
import com.eeseka.lynk.shared.domain.spot.model.SpotAiSummary
import com.eeseka.lynk.shared.domain.spot.model.SpotAmenities
import com.eeseka.lynk.shared.domain.spot.model.SpotOpeningHours
import com.eeseka.lynk.shared.domain.spot.model.SpotParking
import com.eeseka.lynk.shared.domain.spot.model.SpotPayment
import com.eeseka.lynk.shared.domain.spot.model.SpotPriceRange

fun SpotDto.toDomain(): Spot {
    return Spot(
        id = id,
        name = name,
        typeLabel = typeLabel,
        description = description,
        generativeSummary = generativeSummary?.toDomain(),
        reviewSummary = reviewSummary?.toDomain(),
        photoUrls = photoUrls,
        category = category,
        priceLevel = priceLevel,
        priceRange = priceRange?.toDomain(),
        rating = rating,
        reviewCount = reviewCount,
        businessStatus = businessStatus,
        openingHours = openingHours?.toDomain(),
        amenities = amenities?.toDomain(),
        parking = parking?.toDomain(),
        payment = payment?.toDomain(),
        shortAddress = shortAddress,
        latitude = latitude,
        longitude = longitude,
        phoneNumber = phoneNumber,
        websiteUrl = websiteUrl,
        googleMapsUrl = googleMapsUrl,
        directionsUrl = directionsUrl,
        isSaved = isSaved,
        savedAt = savedAt
    )
}

fun PaginatedSpotsDto.toDomain(): PaginatedSpots {
    return PaginatedSpots(
        spots = spots.map { it.toDomain() },
        nextPageToken = nextPageToken
    )
}

private fun SpotAiSummaryDto.toDomain() = SpotAiSummary(
    text = text,
    disclosure = disclosure
)

private fun SpotPriceRangeDto.toDomain() = SpotPriceRange(
    currencyCode = currencyCode,
    startAmount = startAmount,
    endAmount = endAmount
)

private fun SpotOpeningHoursDto.toDomain() = SpotOpeningHours(
    isOpenNow = isOpenNow,
    weekdayDescriptions = weekdayDescriptions,
    nextOpenTime = nextOpenTime,
    nextCloseTime = nextCloseTime,
    utcOffsetMinutes = utcOffsetMinutes
)

private fun SpotAmenitiesDto.toDomain() = SpotAmenities(
    isGoodForGroups = isGoodForGroups,
    isReservable = isReservable,
    hasLiveMusic = hasLiveMusic,
    hasOutdoorSeating = hasOutdoorSeating,
    servesCocktails = servesCocktails,
    isGoodForWatchingSports = isGoodForWatchingSports
)

private fun SpotParkingDto.toDomain() = SpotParking(
    hasFreeLot = hasFreeLot,
    hasPaidLot = hasPaidLot,
    hasFreeStreet = hasFreeStreet,
    hasPaidStreet = hasPaidStreet,
    hasValet = hasValet,
    hasFreeGarage = hasFreeGarage,
    hasPaidGarage = hasPaidGarage
)

private fun SpotPaymentDto.toDomain() = SpotPayment(
    acceptsCreditCards = acceptsCreditCards,
    acceptsDebitCards = acceptsDebitCards,
    acceptsCashOnly = acceptsCashOnly,
    acceptsNfc = acceptsNfc
)
