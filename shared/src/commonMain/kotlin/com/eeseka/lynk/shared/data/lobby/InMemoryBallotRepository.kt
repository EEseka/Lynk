package com.eeseka.lynk.shared.data.lobby

import com.eeseka.lynk.shared.domain.auth.SessionStorage
import com.eeseka.lynk.shared.domain.lobby.BallotRepository
import com.eeseka.lynk.shared.domain.spot.model.Spot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update

class InMemoryBallotRepository(
    sessionStorage: SessionStorage,
    applicationScope: CoroutineScope
) : BallotRepository {

    // Kept per hangout id, so one hangout's ballot can never show on another.
    private val candidates = MutableStateFlow<Map<String, List<Spot>>>(emptyMap())

    init {
        // This outlives sign-out, so the next user must never be shown the last user's ballots.
        sessionStorage
            .observeAuthInfo()
            .map { authInfo -> authInfo?.user?.id }
            .distinctUntilChanged()
            .onEach { candidates.value = emptyMap() }
            .launchIn(applicationScope)
    }

    override fun observeCandidates(hangoutId: String): Flow<List<Spot>> {
        return candidates
            .map { it[hangoutId].orEmpty() }
            .distinctUntilChanged()
    }

    override fun replaceCandidates(hangoutId: String, candidates: List<Spot>) {
        this.candidates.update { it + (hangoutId to candidates) }
    }

    override fun addCandidate(hangoutId: String, spot: Spot) {
        candidates.update { ballots ->
            val ballot = ballots[hangoutId].orEmpty()
            if (ballot.any { it.id == spot.id }) ballots
            else ballots + (hangoutId to ballot + spot)
        }
    }

    override fun removeCandidate(hangoutId: String, spotId: String) {
        candidates.update { ballots ->
            val ballot = ballots[hangoutId] ?: return@update ballots
            ballots + (hangoutId to ballot.filterNot { it.id == spotId })
        }
    }
}
