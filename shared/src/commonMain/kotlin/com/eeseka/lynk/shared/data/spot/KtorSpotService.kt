package com.eeseka.lynk.shared.data.spot

import com.eeseka.lynk.shared.data.networking.delete
import com.eeseka.lynk.shared.data.networking.get
import com.eeseka.lynk.shared.data.networking.post
import com.eeseka.lynk.shared.data.spot.dto.PaginatedSpotsDto
import com.eeseka.lynk.shared.data.spot.dto.SpotDto
import com.eeseka.lynk.shared.data.spot.mappers.toDomain
import com.eeseka.lynk.shared.domain.spot.SpotService
import com.eeseka.lynk.shared.domain.spot.model.PaginatedSpots
import com.eeseka.lynk.shared.domain.spot.model.PriceLevel
import com.eeseka.lynk.shared.domain.spot.model.Spot
import com.eeseka.lynk.shared.domain.spot.model.SpotCategory
import com.eeseka.lynk.shared.domain.util.DataError
import com.eeseka.lynk.shared.domain.util.EmptyResult
import com.eeseka.lynk.shared.domain.util.Result
import com.eeseka.lynk.shared.domain.util.map
import io.ktor.client.HttpClient

class KtorSpotService(
    private val httpClient: HttpClient
) : SpotService {

    override suspend fun getTrendingSpots(
        latitude: Double,
        longitude: Double,
        limit: Int
    ): Result<List<Spot>, DataError.Remote> {
        return httpClient.get<List<SpotDto>>(
            route = "/spots/trending",
            queryParams = mapOf(
                "latitude" to latitude,
                "longitude" to longitude,
                "limit" to limit
            )
        ).map { trendingSpots ->
            trendingSpots.map { it.toDomain() }
        }
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
        return httpClient.get<PaginatedSpotsDto>(
            route = "/spots/search",
            queryParams = buildMap {
                put("latitude", latitude)
                put("longitude", longitude)
                put("radiusInMeters", radiusInMeters)
                query?.let { put("query", it) }
                category?.let { put("category", it) }
                priceLevel?.let { put("priceLevel", it) }
                nextPageToken?.let { put("nextPageToken", it) }
            }
        ).map { it.toDomain() }
    }

    override suspend fun getSpotDetails(spotId: String): Result<Spot, DataError.Remote> {
        return httpClient.get<SpotDto>(
            route = "/spots/$spotId"
        ).map { it.toDomain() }
    }

    override suspend fun saveSpot(spotId: String): EmptyResult<DataError.Remote> {
        return httpClient.post<Unit, Unit>(
            route = "/spots/$spotId/save",
            body = Unit
        )
    }

    override suspend fun unsaveSpot(spotId: String): EmptyResult<DataError.Remote> {
        return httpClient.delete<Unit>(
            route = "/spots/$spotId/save"
        )
    }

    override suspend fun getSavedSpots(
        query: String?,
        before: String?
    ): Result<List<Spot>, DataError.Remote> {
        return httpClient.get<List<SpotDto>>(
            route = "/spots/saved",
            queryParams = buildMap {
                put("pageSize", SpotConstants.PAGE_SIZE)
                before?.let { put("before", it) }
                query?.let { put("query", it) }
            }
        ).map { savedSpots ->
            savedSpots.map { it.toDomain() }
        }
    }
}