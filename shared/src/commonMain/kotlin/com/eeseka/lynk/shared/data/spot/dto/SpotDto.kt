package com.eeseka.lynk.shared.data.spot.dto

import com.eeseka.lynk.shared.domain.spot.model.PriceLevel
import com.eeseka.lynk.shared.domain.spot.model.SpotCategory
import kotlinx.serialization.Serializable

@Serializable
data class SpotDto(
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
    val savedAt: String?
)