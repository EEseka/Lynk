package com.eeseka.lynk.shared.domain.hangout

import com.eeseka.lynk.shared.domain.hangout.model.HangoutUser
import com.eeseka.lynk.shared.domain.util.DataError
import com.eeseka.lynk.shared.domain.util.Result

interface HangoutParticipantService {
    suspend fun getHangoutUserByUsername(query: String): Result<HangoutUser, DataError.Remote>
}
