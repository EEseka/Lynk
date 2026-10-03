package com.eeseka.lynk.testing.data

import com.eeseka.lynk.shared.domain.hangout.HangoutDetailRepository
import com.eeseka.lynk.shared.domain.hangout.model.Hangout
import com.eeseka.lynk.shared.domain.util.DataError
import com.eeseka.lynk.shared.domain.util.EmptyResult
import com.eeseka.lynk.shared.domain.util.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeHangoutDetailRepository : HangoutDetailRepository {
    val refreshedHangoutIds = mutableListOf<String>()

    override fun observeHangout(hangoutId: String): Flow<Hangout?> = flowOf(null)

    override suspend fun refreshHangout(hangoutId: String): EmptyResult<DataError.Remote> {
        refreshedHangoutIds.add(hangoutId)
        return Result.Success(Unit)
    }
}
