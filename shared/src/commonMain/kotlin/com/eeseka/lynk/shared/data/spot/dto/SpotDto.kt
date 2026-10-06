package com.eeseka.lynk.shared.data.spot.dto

import com.eeseka.lynk.shared.domain.spot.model.BusinessStatus
import com.eeseka.lynk.shared.domain.spot.model.PriceLevel
import com.eeseka.lynk.shared.domain.spot.model.SpotCategory
import kotlin.time.Instant
import kotlinx.serialization.Serializable

@Serializable
data class SpotDto(
    val id: String,
    val name: String,
    val typeLabel: String?,
    val description: String?,
    val generativeSummary: SpotAiSummaryDto?,
    val reviewSummary: SpotAiSummaryDto?,
    val photoUrls: List<String>,
    val category: SpotCategory,
    val priceLevel: PriceLevel?,
    val priceRange: SpotPriceRangeDto?,
    val rating: Double?,
    val reviewCount: Int?,
    val businessStatus: BusinessStatus?,
    val openingHours: SpotOpeningHoursDto?,
    val amenities: SpotAmenitiesDto?,
    val parking: SpotParkingDto?,
    val payment: SpotPaymentDto?,
    val shortAddress: String?,
    val latitude: Double,
    val longitude: Double,
    val phoneNumber: String?,
    val websiteUrl: String?,
    val googleMapsUrl: String?,
    val directionsUrl: String?,
    val isSaved: Boolean,
    val savedAt: Instant?
)
