package com.eeseka.lynk.testing.data

import com.eeseka.lynk.shared.domain.hangout.HangoutNoteService
import com.eeseka.lynk.shared.domain.hangout.model.HangoutNote
import com.eeseka.lynk.shared.domain.hangout.model.HangoutNoteAudience
import com.eeseka.lynk.shared.domain.hangout.model.HangoutUser
import com.eeseka.lynk.shared.domain.util.DataError
import com.eeseka.lynk.shared.domain.util.EmptyResult
import com.eeseka.lynk.shared.domain.util.Result
import kotlinx.coroutines.delay
import kotlin.time.Duration
import kotlin.time.Instant

class FakeHangoutNoteService : HangoutNoteService {
    var shouldReturnError = false
    var errorToReturn = DataError.Remote.SERVER_ERROR
    var notes = mutableListOf<HangoutNote>()
    var author = HangoutUser(userId = "user_2", username = "bola", displayName = "Bola", profilePictureUrl = null)

    var pageSize = 20
    val postedAudiences = mutableListOf<HangoutNoteAudience>()
    val deletedNoteIds = mutableListOf<String>()
    var deleteResponseDelay = Duration.ZERO

    private var nextNoteNumber = 1

    override suspend fun getNotes(
        hangoutId: String,
        before: String?
    ): Result<List<HangoutNote>, DataError.Remote> {
        if (shouldReturnError) return Result.Failure(errorToReturn)

        val page = notes
            .filter { note -> before == null || note.createdAt < Instant.parse(before) }
            .take(pageSize)
        return Result.Success(page)
    }

    override suspend fun getNoteCount(hangoutId: String): Result<Long, DataError.Remote> {
        if (shouldReturnError) return Result.Failure(errorToReturn)
        return Result.Success(notes.size.toLong())
    }

    override suspend fun addNote(
        hangoutId: String,
        content: String,
        audience: HangoutNoteAudience
    ): Result<HangoutNote, DataError.Remote> {
        if (shouldReturnError) return Result.Failure(errorToReturn)

        val note = HangoutNote(
            id = "note_${nextNoteNumber}",
            author = author,
            content = content,
            // Newer than anything already there, like the server's clock
            createdAt = Instant.fromEpochMilliseconds(1_000_000L + nextNoteNumber++),
            updatedAt = null
        )
        notes.add(0, note)
        postedAudiences.add(audience)
        return Result.Success(note)
    }

    override suspend fun editNote(
        hangoutId: String,
        noteId: String,
        content: String
    ): EmptyResult<DataError.Remote> {
        if (shouldReturnError) return Result.Failure(errorToReturn)

        val index = notes.indexOfFirst { it.id == noteId }
        if (index == -1) return Result.Failure(DataError.Remote.NOT_FOUND)
        notes[index] = notes[index].copy(content = content, updatedAt = Instant.fromEpochMilliseconds(1))
        return Result.Success(Unit)
    }

    override suspend fun deleteNote(
        hangoutId: String,
        noteId: String
    ): EmptyResult<DataError.Remote> {
        delay(deleteResponseDelay)
        if (shouldReturnError) return Result.Failure(errorToReturn)

        val wasRemoved = notes.removeAll { it.id == noteId }
        if (!wasRemoved) return Result.Failure(DataError.Remote.NOT_FOUND)
        deletedNoteIds.add(noteId)
        return Result.Success(Unit)
    }
}
