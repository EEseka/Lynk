package com.eeseka.lynk.shared.domain.hangout

import com.eeseka.lynk.shared.domain.hangout.model.HangoutPhotoStats
import com.eeseka.lynk.shared.domain.util.DataError
import com.eeseka.lynk.shared.domain.util.EmptyResult
import kotlinx.coroutines.flow.Flow

interface HangoutPhotoStatsRepository {

    /**
     * The last counts fetched for this hangout's album, or null until a fetch succeeds.
     * Everything showing the same album observes this one copy, so a refresh reaches all of them.
     */
    fun observePhotoStats(hangoutId: String): Flow<HangoutPhotoStats?>

    suspend fun refreshPhotoStats(hangoutId: String): EmptyResult<DataError.Remote>
}
