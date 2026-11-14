package com.eeseka.lynk.core.data.hangout

import com.eeseka.lynk.core.data.hangout.dto.HangoutUserDto
import com.eeseka.lynk.core.data.hangout.mappers.toDomain
import com.eeseka.lynk.core.data.networking.get
import com.eeseka.lynk.core.domain.hangout.HangoutParticipantService
import com.eeseka.lynk.core.domain.hangout.model.HangoutUser
import com.eeseka.lynk.core.domain.util.DataError
import com.eeseka.lynk.core.domain.util.Result
import com.eeseka.lynk.core.domain.util.map
import io.ktor.client.HttpClient

class KtorHangoutParticipantService(
    private val httpClient: HttpClient
) : HangoutParticipantService {

    override suspend fun getHangoutUserByUsername(
        query: String
    ): Result<HangoutUser, DataError.Remote> {
        return httpClient.get<HangoutUserDto>(
            route = "/participants",
            queryParams = mapOf("query" to query)
        ).map { it.toDomain() }
    }
}
