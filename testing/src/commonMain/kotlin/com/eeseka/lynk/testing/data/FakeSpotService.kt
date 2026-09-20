package com.eeseka.lynk.testing.data

import com.eeseka.lynk.shared.domain.spot.SpotService
import com.eeseka.lynk.shared.domain.spot.model.PaginatedSpots
import com.eeseka.lynk.shared.domain.spot.model.PriceLevel
import com.eeseka.lynk.shared.domain.spot.model.Spot
import com.eeseka.lynk.shared.domain.spot.model.SpotCategory
import com.eeseka.lynk.shared.domain.util.DataError
import com.eeseka.lynk.shared.domain.util.EmptyResult
import com.eeseka.lynk.shared.domain.util.Result
import kotlin.time.Instant

class FakeSpotService : SpotService {
    var shouldReturnError = false
    var errorToReturn = DataError.Remote.SERVER_ERROR
    var trendingSpotsList = mutableListOf<Spot>()
    var searchSpotsList = mutableListOf<Spot>()
    var savedSpotsList = mutableListOf<Spot>()
    var savedSpots = mutableSetOf<String>()
    var trendingRequestLocations = mutableListOf<Pair<Double, Double>>()

    override suspend fun getTrendingSpots(
        latitude: Double,
        longitude: Double,
        limit: Int
    ): Result<List<Spot>, DataError.Remote> {
        if (shouldReturnError) return Result.Failure(errorToReturn)
        trendingRequestLocations.add(latitude to longitude)
        return Result.Success(trendingSpotsList.take(limit))
    }

    // Returns everything in one page, so there is never a next page token
    override suspend fun searchSpots(
        latitude: Double,
        longitude: Double,
        query: String?,
        category: SpotCategory?,
        priceLevel: PriceLevel?,
        radiusInMeters: Int,
        nextPageToken: String?
    ): Result<PaginatedSpots, DataError.Remote> {
        if (shouldReturnError) return Result.Failure(errorToReturn)

        val filtered = searchSpotsList.filter { spot ->
            val matchesQuery = query == null || spot.name.contains(query, ignoreCase = true)
            val matchesCategory = category == null || spot.category == category
            val matchesPrice = priceLevel == null || spot.priceLevel == priceLevel
            matchesQuery && matchesCategory && matchesPrice
        }
        return Result.Success(PaginatedSpots(spots = filtered, nextPageToken = null))
    }

    override suspend fun getSpotDetails(spotId: String): Result<Spot, DataError.Remote> {
        if (shouldReturnError) return Result.Failure(errorToReturn)

        val spot = (trendingSpotsList + searchSpotsList + savedSpotsList).find { it.id == spotId }
            ?: return Result.Failure(DataError.Remote.NOT_FOUND)
        return Result.Success(spot)
    }

    override suspend fun saveSpot(spotId: String): EmptyResult<DataError.Remote> {
        if (shouldReturnError) return Result.Failure(errorToReturn)
        savedSpots.add(spotId)
        return Result.Success(Unit)
    }

    override suspend fun unsaveSpot(spotId: String): EmptyResult<DataError.Remote> {
        if (shouldReturnError) return Result.Failure(errorToReturn)
        savedSpots.remove(spotId)
        return Result.Success(Unit)
    }

    // Like the backend, "before" is a savedAt cursor: the next page holds only spots saved earlier
    override suspend fun getSavedSpots(
        query: String?,
        before: String?
    ): Result<List<Spot>, DataError.Remote> {
        if (shouldReturnError) return Result.Failure(errorToReturn)

        val filtered = savedSpotsList.filter { spot ->
            val matchesQuery = query.isNullOrBlank() || spot.name.contains(query, ignoreCase = true)
            val savedAt = spot.savedAt
            val isOnThisPage = before == null || (savedAt != null && savedAt < Instant.parse(before))
            matchesQuery && isOnThisPage
        }
        return Result.Success(filtered)
    }
}
