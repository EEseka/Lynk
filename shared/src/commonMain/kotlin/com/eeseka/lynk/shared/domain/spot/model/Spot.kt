package com.eeseka.lynk.shared.domain.spot.model

import kotlin.time.Instant

data class Spot(
    val id: String,
    val name: String,
    val description: String?,
    val photoUrls: List<String>,
    val category: SpotCategory,
    val tags: List<String>,
    val priceLevel: PriceLevel?,
    val rating: Double?,
    val reviewCount: Int?,
    val isOpenNow: Boolean,
    val shortAddress: String?,
    val latitude: Double,
    val longitude: Double,
    val websiteUrl: String?,
    val googleMapsUrl: String?,
    val isSaved: Boolean,
    val savedAt: Instant?
)