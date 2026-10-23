package com.eeseka.lynk.shared.domain.lobby

import com.eeseka.lynk.shared.domain.spot.model.Spot
import kotlinx.coroutines.flow.Flow

interface BallotRepository {

    /**
     * The spots on this hangout's ballot as the lobby last reported them, empty until it reports any.
     * The voting section writes what the lobby says; the Lynk chat reads the same copy, so neither asks twice.
     */
    fun observeCandidates(hangoutId: String): Flow<List<Spot>>

    fun replaceCandidates(hangoutId: String, candidates: List<Spot>)

    fun addCandidate(hangoutId: String, spot: Spot)

    fun removeCandidate(hangoutId: String, spotId: String)
}
