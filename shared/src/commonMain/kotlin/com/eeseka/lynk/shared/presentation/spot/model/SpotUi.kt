package com.eeseka.lynk.shared.presentation.spot.model

import androidx.compose.runtime.Immutable
import com.eeseka.lynk.shared.domain.spot.model.PriceLevel
import com.eeseka.lynk.shared.domain.spot.model.SpotCategory
import kotlinx.collections.immutable.ImmutableList

@Immutable
data class SpotUi(
    val id: String,
    val name: String,
    val description: String?,
    val photoUrls: ImmutableList<String>,
    val category: SpotCategory,
    val tags: ImmutableList<String>,
    val priceLevel: PriceLevel?,
    val rating: Double?,
    val reviewCount: Int?,
    val isOpenNow: Boolean,
    val shortAddress: String?,
    val latitude: Double,
    val longitude: Double,
    val websiteUrl: String?,
    val googleMapsUrl: String?,
    val isSaved: Boolean
)