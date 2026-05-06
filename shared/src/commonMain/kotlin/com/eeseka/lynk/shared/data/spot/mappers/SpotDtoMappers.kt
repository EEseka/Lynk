package com.eeseka.lynk.shared.data.spot.mappers

import com.eeseka.lynk.shared.data.spot.dto.PaginatedSpotsDto
import com.eeseka.lynk.shared.data.spot.dto.SpotDto
import com.eeseka.lynk.shared.domain.spot.model.PaginatedSpots
import com.eeseka.lynk.shared.domain.spot.model.Spot
import kotlin.time.Instant

fun SpotDto.toDomain(): Spot {
    return Spot(
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
        isSaved = isSaved,
        savedAt = savedAt?.let { Instant.parse(it) }
    )
}

fun PaginatedSpotsDto.toDomain(): PaginatedSpots {
    return PaginatedSpots(
        spots = spots.map { it.toDomain() },
        nextPageToken = nextPageToken
    )
}