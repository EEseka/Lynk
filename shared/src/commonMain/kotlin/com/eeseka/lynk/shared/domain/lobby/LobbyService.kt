package com.eeseka.lynk.shared.domain.lobby

import com.eeseka.lynk.shared.domain.util.DataError
import com.eeseka.lynk.shared.domain.util.EmptyResult

interface LobbyService {

    /** Presence: tell the server this socket is now viewing [hangoutId]'s lobby (green dot). */
    suspend fun enterLobby(hangoutId: String): EmptyResult<DataError.Connection>

    /** Presence: this socket stopped viewing [hangoutId]'s lobby. */
    suspend fun leaveLobby(hangoutId: String): EmptyResult<DataError.Connection>

    /** One-off device location so the server can average a group center for this hangout. */
    suspend fun shareLocation(
        hangoutId: String,
        latitude: Double,
        longitude: Double
    ): EmptyResult<DataError.Connection>

    /** Where this user is right now, for anyone with this hangout's lobby open. */
    suspend fun shareLiveLocation(
        hangoutId: String,
        latitude: Double,
        longitude: Double
    ): EmptyResult<DataError.Connection>

    /** Takes this user off the hangout's map. */
    suspend fun stopLiveLocation(hangoutId: String): EmptyResult<DataError.Connection>

    /** Add a spot to the candidate list for voting, with an optional [note] on why. */
    suspend fun proposeSpot(hangoutId: String, spotId: String, note: String?): EmptyResult<DataError.Connection>

    /** The host takes any spot off the candidate list, everyone else only the ones they suggested. */
    suspend fun removeSpot(hangoutId: String, spotId: String): EmptyResult<DataError.Connection>

    /** Cast (or move) this user's single vote to [spotId]. */
    suspend fun castVote(hangoutId: String, spotId: String): EmptyResult<DataError.Connection>

    /**
     * Host-only: end the voting round. [chosenSpotId] stays null normally (current leader
     * wins); it's set only to break a tie among co-leaders.
     */
    suspend fun closeVoting(
        hangoutId: String,
        chosenSpotId: String? = null
    ): EmptyResult<DataError.Connection>
}
