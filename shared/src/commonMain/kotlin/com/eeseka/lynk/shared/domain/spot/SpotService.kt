package com.eeseka.lynk.shared.domain.spot

import com.eeseka.lynk.shared.domain.spot.model.PaginatedSpots
import com.eeseka.lynk.shared.domain.spot.model.PriceLevel
import com.eeseka.lynk.shared.domain.spot.model.Spot
import com.eeseka.lynk.shared.domain.spot.model.SpotCategory
import com.eeseka.lynk.shared.domain.util.DataError
import com.eeseka.lynk.shared.domain.util.EmptyResult
import com.eeseka.lynk.shared.domain.util.Result

interface SpotService {

    suspend fun getTrendingSpots(
        latitude: Double,
        longitude: Double,
        limit: Int = 20
    ): Result<List<Spot>, DataError.Remote>

    suspend fun searchSpots(
        latitude: Double,
        longitude: Double,
        query: String? = null,
        category: SpotCategory? = null,
        priceLevel: PriceLevel? = null,
        radiusInMeters: Int = 5000,
        nextPageToken: String? = null // For Google Places pagination
    ): Result<PaginatedSpots, DataError.Remote>

    suspend fun getSpotDetails(spotId: String): Result<Spot, DataError.Remote>

    suspend fun saveSpot(spotId: String): EmptyResult<DataError.Remote>

    suspend fun unsaveSpot(spotId: String): EmptyResult<DataError.Remote>

    suspend fun getSavedSpots(
        query: String? = null,
        before: String? = null
    ): Result<List<Spot>, DataError.Remote>
}