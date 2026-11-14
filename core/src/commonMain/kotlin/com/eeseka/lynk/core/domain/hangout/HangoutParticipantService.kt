package com.eeseka.lynk.core.domain.hangout

import com.eeseka.lynk.core.domain.hangout.model.HangoutUser
import com.eeseka.lynk.core.domain.util.DataError
import com.eeseka.lynk.core.domain.util.Result

interface HangoutParticipantService {
    suspend fun getHangoutUserByUsername(query: String): Result<HangoutUser, DataError.Remote>
}
