package com.eeseka.lynk.shared.presentation.spot.model

import androidx.compose.runtime.Immutable
import com.eeseka.lynk.shared.domain.spot.model.BusinessStatus
import com.eeseka.lynk.shared.domain.spot.model.PriceLevel
import com.eeseka.lynk.shared.domain.spot.model.SpotAiSummary
import com.eeseka.lynk.shared.domain.spot.model.SpotCategory
import com.eeseka.lynk.shared.domain.spot.model.SpotPriceRange
import kotlinx.collections.immutable.ImmutableList
import kotlin.time.Instant

@Immutable
data class SpotUi(
    val id: String,
    val name: String,
    val typeLabel: String?,
    val description: String?,
    val aiSummary: SpotAiSummary?,
    val photoUrls: ImmutableList<String>,
    val category: SpotCategory,
    val priceLevel: PriceLevel?,
    val priceRange: SpotPriceRange?,
    val rating: Double?,
    val reviewCount: Int?,
    val businessStatus: BusinessStatus?,
    val isOpenNow: Boolean?,
    val nextOpenTime: Instant?,
    val nextCloseTime: Instant?,
    val weekHours: ImmutableList<SpotDayHoursUi>,
    val highlights: ImmutableList<SpotHighlight>,
    val shortAddress: String?,
    val latitude: Double,
    val longitude: Double,
    val phoneNumber: String?,
    val websiteUrl: String?,
    val googleMapsUrl: String?,
    val directionsUrl: String?,
    val isSaved: Boolean
)
