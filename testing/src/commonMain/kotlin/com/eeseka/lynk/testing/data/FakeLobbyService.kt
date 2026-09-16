package com.eeseka.lynk.testing.data

import com.eeseka.lynk.shared.domain.lobby.LobbyService
import com.eeseka.lynk.shared.domain.util.DataError
import com.eeseka.lynk.shared.domain.util.EmptyResult
import com.eeseka.lynk.shared.domain.util.Result

// Every call is only a message down the socket, so the fake records what was sent
class FakeLobbyService : LobbyService {
    var shouldReturnError = false
    var errorToReturn = DataError.Connection.MESSAGE_SEND_FAILED
    var enteredLobbyIds = mutableListOf<String>()
    var leftLobbyIds = mutableListOf<String>()
    var sharedLocations = mutableListOf<Pair<Double, Double>>()
    var proposedSpotIds = mutableListOf<String>()
    var removedSpotIds = mutableListOf<String>()
    var votedSpotIds = mutableListOf<String>()
    var closedVotingChosenSpotIds = mutableListOf<String?>()

    override suspend fun enterLobby(hangoutId: String): EmptyResult<DataError.Connection> {
        if (shouldReturnError) return Result.Failure(errorToReturn)
        enteredLobbyIds.add(hangoutId)
        return Result.Success(Unit)
    }

    override suspend fun leaveLobby(hangoutId: String): EmptyResult<DataError.Connection> {
        if (shouldReturnError) return Result.Failure(errorToReturn)
        leftLobbyIds.add(hangoutId)
        return Result.Success(Unit)
    }

    override suspend fun shareLocation(
        hangoutId: String,
        latitude: Double,
        longitude: Double
    ): EmptyResult<DataError.Connection> {
        if (shouldReturnError) return Result.Failure(errorToReturn)
        sharedLocations.add(latitude to longitude)
        return Result.Success(Unit)
    }

    override suspend fun proposeSpot(hangoutId: String, spotId: String): EmptyResult<DataError.Connection> {
        if (shouldReturnError) return Result.Failure(errorToReturn)
        proposedSpotIds.add(spotId)
        return Result.Success(Unit)
    }

    override suspend fun removeSpot(hangoutId: String, spotId: String): EmptyResult<DataError.Connection> {
        if (shouldReturnError) return Result.Failure(errorToReturn)
        removedSpotIds.add(spotId)
        return Result.Success(Unit)
    }

    override suspend fun castVote(hangoutId: String, spotId: String): EmptyResult<DataError.Connection> {
        if (shouldReturnError) return Result.Failure(errorToReturn)
        votedSpotIds.add(spotId)
        return Result.Success(Unit)
    }

    override suspend fun closeVoting(
        hangoutId: String,
        chosenSpotId: String?
    ): EmptyResult<DataError.Connection> {
        if (shouldReturnError) return Result.Failure(errorToReturn)
        closedVotingChosenSpotIds.add(chosenSpotId)
        return Result.Success(Unit)
    }
}
