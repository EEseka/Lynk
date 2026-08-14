package com.eeseka.lynk.create_hangout.data

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
            query == null || spot.name.contains(query, ignoreCase = true)
        }
        return Result.Success(PaginatedSpots(spots = filtered, nextPageToken = null))
    }

    override suspend fun getSpotDetails(spotId: String): Result<Spot, DataError.Remote> =
        Result.Failure(DataError.Remote.SERVER_ERROR)

    override suspend fun saveSpot(spotId: String): EmptyResult<DataError.Remote> =
        Result.Failure(DataError.Remote.SERVER_ERROR)

    override suspend fun unsaveSpot(spotId: String): EmptyResult<DataError.Remote> =
        Result.Failure(DataError.Remote.SERVER_ERROR)

    override suspend fun getSavedSpots(
        query: String?,
        before: String?
    ): Result<List<Spot>, DataError.Remote> {
        if (shouldReturnError) return Result.Failure(DataError.Remote.SERVER_ERROR)
        val filtered = if (query.isNullOrBlank()) savedSpotsList
        else savedSpotsList.filter { it.name.contains(query, ignoreCase = true) }
        return Result.Success(filtered)
    }
}