package com.eeseka.lynk.discover.data

import com.eeseka.lynk.shared.domain.spot.SpotService
import com.eeseka.lynk.shared.domain.spot.model.PaginatedSpots
import com.eeseka.lynk.shared.domain.spot.model.PriceLevel
import com.eeseka.lynk.shared.domain.spot.model.Spot
import com.eeseka.lynk.shared.domain.spot.model.SpotCategory
import com.eeseka.lynk.shared.domain.util.DataError
import com.eeseka.lynk.shared.domain.util.EmptyResult
import com.eeseka.lynk.shared.domain.util.Result

class FakeSpotService : SpotService {
    var shouldReturnError = false
    var trendingSpotsList = mutableListOf<Spot>()
    var searchSpotsList = mutableListOf<Spot>()
    var savedSpotsList = mutableListOf<Spot>()
    var savedSpots = mutableSetOf<String>()

    override suspend fun getTrendingSpots(
        latitude: Double,
        longitude: Double,
        limit: Int
    ): Result<List<Spot>, DataError.Remote> {
        if (shouldReturnError) return Result.Failure(DataError.Remote.SERVER_ERROR)
        return Result.Success(trendingSpotsList.take(limit))
    }

    override suspend fun searchSpots(
        latitude: Double,
        longitude: Double,
        query: String?,
        category: SpotCategory?,
        priceLevel: PriceLevel?,
        radiusInMeters: Int,
        nextPageToken: String?
    ): Result<PaginatedSpots, DataError.Remote> {
        if (shouldReturnError) return Result.Failure(DataError.Remote.SERVER_ERROR)

        val filtered = searchSpotsList.filter { spot ->
            val matchesQuery = query == null || spot.name.contains(query, ignoreCase = true)
            val matchesCategory = category == null || spot.category == category
            val matchesPrice = priceLevel == null || spot.priceLevel == priceLevel
            matchesQuery && matchesCategory && matchesPrice
        }

        return Result.Success(PaginatedSpots(spots = filtered, nextPageToken = null))
    }

    override suspend fun getSpotDetails(spotId: String): Result<Spot, DataError.Remote> {
        if (shouldReturnError) return Result.Failure(DataError.Remote.SERVER_ERROR)

        val spot = trendingSpotsList.find { it.id == spotId }
            ?: searchSpotsList.find { it.id == spotId }

        return if (spot != null) {
            Result.Success(spot)
        } else {
            Result.Failure(DataError.Remote.NOT_FOUND)
        }
    }

    override suspend fun saveSpot(spotId: String): EmptyResult<DataError.Remote> {
        if (shouldReturnError) return Result.Failure(DataError.Remote.SERVER_ERROR)
        savedSpots.add(spotId)
        return Result.Success(Unit)
    }

    override suspend fun unsaveSpot(spotId: String): EmptyResult<DataError.Remote> {
        if (shouldReturnError) return Result.Failure(DataError.Remote.SERVER_ERROR)
        savedSpots.remove(spotId)
        return Result.Success(Unit)
    }

    override suspend fun getSavedSpots(before: String?): Result<List<Spot>, DataError.Remote> {
        if (shouldReturnError) return Result.Failure(DataError.Remote.SERVER_ERROR)
        return Result.Success(savedSpotsList)
    }
}