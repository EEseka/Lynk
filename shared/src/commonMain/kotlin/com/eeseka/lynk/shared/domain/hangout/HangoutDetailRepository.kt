package com.eeseka.lynk.shared.domain.hangout

import com.eeseka.lynk.shared.domain.hangout.model.Hangout
import com.eeseka.lynk.shared.domain.util.DataError
import com.eeseka.lynk.shared.domain.util.EmptyResult
import kotlinx.coroutines.flow.Flow

interface HangoutDetailRepository {

    /**
     * The last copy fetched for this hangout, or null until a fetch succeeds.
     * Everything showing the same hangout observes this one copy, so a refresh reaches all of them.
     */
    fun observeHangout(hangoutId: String): Flow<Hangout?>

    suspend fun refreshHangout(hangoutId: String): EmptyResult<DataError.Remote>
}
