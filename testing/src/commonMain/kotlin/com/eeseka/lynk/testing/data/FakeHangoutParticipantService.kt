package com.eeseka.lynk.testing.data

import com.eeseka.lynk.core.domain.hangout.HangoutParticipantService
import com.eeseka.lynk.core.domain.hangout.model.HangoutUser
import com.eeseka.lynk.core.domain.util.DataError
import com.eeseka.lynk.core.domain.util.Result

class FakeHangoutParticipantService : HangoutParticipantService {
    var shouldReturnError = false
    var errorToReturn = DataError.Remote.SERVER_ERROR
    var users = mutableListOf<HangoutUser>()

    override suspend fun getHangoutUserByUsername(query: String): Result<HangoutUser, DataError.Remote> {
        if (shouldReturnError) return Result.Failure(errorToReturn)

        val user = users.find { it.username == query }
            ?: return Result.Failure(DataError.Remote.NOT_FOUND)
        return Result.Success(user)
    }
}
