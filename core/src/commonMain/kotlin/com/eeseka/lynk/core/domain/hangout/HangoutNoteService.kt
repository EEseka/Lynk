package com.eeseka.lynk.core.domain.hangout

import com.eeseka.lynk.core.domain.hangout.model.HangoutNote
import com.eeseka.lynk.core.domain.hangout.model.HangoutNoteAudience
import com.eeseka.lynk.core.domain.util.DataError
import com.eeseka.lynk.core.domain.util.EmptyResult
import com.eeseka.lynk.core.domain.util.Result

interface HangoutNoteService {
    suspend fun getNotes(
        hangoutId: String,
        before: String?
    ): Result<List<HangoutNote>, DataError.Remote>

    suspend fun getNoteCount(hangoutId: String): Result<Long, DataError.Remote>

    suspend fun addNote(
        hangoutId: String,
        content: String,
        audience: HangoutNoteAudience
    ): Result<HangoutNote, DataError.Remote>

    suspend fun editNote(
        hangoutId: String,
        noteId: String,
        content: String
    ): EmptyResult<DataError.Remote>

    suspend fun deleteNote(
        hangoutId: String,
        noteId: String
    ): EmptyResult<DataError.Remote>
}
