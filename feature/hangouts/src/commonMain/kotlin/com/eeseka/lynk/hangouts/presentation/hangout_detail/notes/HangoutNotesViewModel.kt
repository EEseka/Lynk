package com.eeseka.lynk.hangouts.presentation.hangout_detail.notes

import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eeseka.lynk.hangouts.presentation.mappers.toHangoutNoteUi
import com.eeseka.lynk.shared.domain.hangout.HangoutNoteService
import com.eeseka.lynk.shared.domain.hangout.model.HangoutNote
import com.eeseka.lynk.shared.domain.hangout.model.HangoutNoteAudience
import com.eeseka.lynk.shared.domain.lobby.LobbyConnectionClient
import com.eeseka.lynk.shared.domain.lobby.model.ConnectionState
import com.eeseka.lynk.shared.domain.lobby.model.LobbyEvent
import com.eeseka.lynk.shared.domain.util.DataError
import com.eeseka.lynk.shared.domain.util.DataErrorException
import com.eeseka.lynk.shared.domain.util.Paginator
import com.eeseka.lynk.shared.domain.util.onFailure
import com.eeseka.lynk.shared.domain.util.onSuccess
import com.eeseka.lynk.shared.presentation.util.UiText
import com.eeseka.lynk.shared.presentation.util.toUiText
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableSet
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import lynk.feature.hangouts.generated.resources.Res
import lynk.feature.hangouts.generated.resources.notes_note_gone

class HangoutNotesViewModel(
    private val hangoutNoteService: HangoutNoteService,
    private val connectionClient: LobbyConnectionClient
) : ViewModel() {
    private val eventChannel = Channel<HangoutNotesEvent>()
    val events = eventChannel.receiveAsFlow()

    private val _state = MutableStateFlow(HangoutNotesState())
    private var hasLoadedInitialData = false

    private var currentHangoutId: String? = null
    private var notesPaginator: Paginator<String?, HangoutNote>? = null

    val state = _state
        .onStart {
            if (!hasLoadedInitialData) {
                observeCanPostNote()
                observeLobbyEvents()
                observeReconnections()
                hasLoadedInitialData = true
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = HangoutNotesState()
        )

    private val noteTextFlow = snapshotFlow { _state.value.noteState.text.toString() }
        .map { it.trim() }
        .distinctUntilChanged()

    private val savedNoteTextFlow = _state.map { currentState ->
        currentState.notes.find { it.id == currentState.editingNoteId }?.content.orEmpty()
    }.distinctUntilChanged()

    private val isNoteDirtyFlow = combine(
        noteTextFlow,
        savedNoteTextFlow
    ) { noteText, savedNoteText ->
        noteText.isNotEmpty() && noteText != savedNoteText
    }.distinctUntilChanged()

    private val isPostingNoteFlow = _state.map { it.isPostingNote }.distinctUntilChanged()

    fun onAction(action: HangoutNotesAction) {
        when (action) {
            is HangoutNotesAction.OnSelectHangout -> selectHangout(action.hangoutId)
            HangoutNotesAction.OnAddNoteClick -> openComposer(editingNoteId = null)
            is HangoutNotesAction.OnEditNoteClick -> openComposer(editingNoteId = action.noteId)
            is HangoutNotesAction.OnAudienceChange -> _state.update { it.copy(audience = action.audience) }
            HangoutNotesAction.OnPostNoteClick -> postNote()
            HangoutNotesAction.OnDismissComposer -> dismissComposer()
            HangoutNotesAction.OnSeeAllNotesClick -> _state.update { it.copy(isAllNotesShowing = true) }
            HangoutNotesAction.OnDismissAllNotes -> _state.update { it.copy(isAllNotesShowing = false) }
            HangoutNotesAction.LoadNextPage -> loadNextPage()
            is HangoutNotesAction.OnDeleteNoteClick -> _state.update { it.copy(pendingDeleteNoteId = action.noteId) }
            HangoutNotesAction.OnDismissDeleteDialog -> _state.update { it.copy(pendingDeleteNoteId = null) }
            HangoutNotesAction.OnDeleteNoteConfirmed -> deleteNote()
        }
    }

    private fun selectHangout(hangoutId: String?) {
        if (hangoutId == currentHangoutId) return
        currentHangoutId = hangoutId

        _state.value.noteState.clearText()
        _state.update {
            it.copy(
                notes = persistentListOf(),
                noteCount = 0,
                isLoadingNotes = false,
                isEndReached = false,
                isComposerShowing = false,
                editingNoteId = null,
                audience = HangoutNoteAudience.EVERYONE,
                isPostingNote = false,
                isAllNotesShowing = false,
                pendingDeleteNoteId = null,
                deletingNoteIds = persistentSetOf()
            )
        }
        hangoutId?.let(::loadNotes)
    }

    private fun loadNotes(hangoutId: String) {
        setupNotesPaginator(hangoutId = hangoutId, initialKey = null)
        loadNextPage()
        loadNoteCount(hangoutId)
    }

    private fun observeCanPostNote() {
        combine(isNoteDirtyFlow, isPostingNoteFlow) { isNoteDirty, isPostingNote ->
            _state.update { it.copy(canPostNote = !isPostingNote && isNoteDirty) }
        }.launchIn(viewModelScope)
    }

    private fun observeLobbyEvents() {
        connectionClient.events
            .onEach { event ->
                val hangoutId = currentHangoutId ?: return@onEach
                if (event is LobbyEvent.NotesChanged && event.hangoutId == hangoutId) {
                    refreshHeldNotes(hangoutId)
                }
            }
            .launchIn(viewModelScope)
    }

    private fun observeReconnections() {
        connectionClient.connectionState
            .filter { it == ConnectionState.CONNECTED }
            .drop(1)
            .onEach { currentHangoutId?.let(::refreshHeldNotes) }
            .launchIn(viewModelScope)
    }

    private fun openComposer(editingNoteId: String?) {
        val savedText = _state.value.notes.find { it.id == editingNoteId }?.content.orEmpty()
        _state.value.noteState.setTextAndPlaceCursorAtEnd(savedText)
        _state.update {
            it.copy(
                isComposerShowing = true,
                editingNoteId = editingNoteId,
                audience = HangoutNoteAudience.EVERYONE,
                isAllNotesShowing = false
            )
        }
    }

    private fun dismissComposer() {
        _state.value.noteState.clearText()
        _state.update { it.copy(isComposerShowing = false, editingNoteId = null) }
    }

    private fun postNote() {
        val currentState = _state.value
        val hangoutId = currentHangoutId ?: return
        if (currentState.isPostingNote) return

        val content = currentState.noteState.text.toString().trim()
        val editingNoteId = currentState.editingNoteId

        viewModelScope.launch {
            _state.update { it.copy(isPostingNote = true) }
            val result = if (editingNoteId == null) {
                hangoutNoteService.addNote(
                    hangoutId = hangoutId,
                    content = content,
                    audience = currentState.audience
                )
            } else {
                hangoutNoteService.editNote(
                    hangoutId = hangoutId,
                    noteId = editingNoteId,
                    content = content
                )
            }
            result
                .onSuccess {
                    dismissComposer()
                    refreshHeldNotes(hangoutId)
                    eventChannel.send(if (editingNoteId == null) HangoutNotesEvent.NotePosted else HangoutNotesEvent.NoteEdited)
                }
                .onFailure { error ->
                    if (editingNoteId != null && error == DataError.Remote.NOT_FOUND) {
                        dismissComposer()
                        removeNote(editingNoteId)
                        eventChannel.send(HangoutNotesEvent.Error(UiText.Resource(Res.string.notes_note_gone)))
                        return@onFailure
                    }

                    eventChannel.send(HangoutNotesEvent.Error(error.toUiText()))
                }
            _state.update { it.copy(isPostingNote = false) }
        }
    }

    private fun deleteNote() {
        val hangoutId = currentHangoutId ?: return
        val noteId = _state.value.pendingDeleteNoteId ?: return

        viewModelScope.launch {
            _state.update {
                it.copy(pendingDeleteNoteId = null, deletingNoteIds = (it.deletingNoteIds + noteId).toImmutableSet())
            }
            hangoutNoteService
                .deleteNote(hangoutId = hangoutId, noteId = noteId)
                .onSuccess {
                    removeNote(noteId)
                    eventChannel.send(HangoutNotesEvent.NoteDeleted)
                }
                .onFailure { error ->
                    if (error == DataError.Remote.NOT_FOUND) {
                        removeNote(noteId)
                        eventChannel.send(HangoutNotesEvent.NoteDeleted)
                        return@onFailure
                    }

                    eventChannel.send(HangoutNotesEvent.Error(error.toUiText()))
                }
            _state.update { it.copy(deletingNoteIds = (it.deletingNoteIds - noteId).toImmutableSet()) }
        }
    }

    private fun removeNote(noteId: String) {
        _state.update { currentState ->
            currentState.copy(
                notes = currentState.notes.filterNot { it.id == noteId }.toImmutableList(),
                noteCount = (currentState.noteCount - 1).coerceAtLeast(0)
            )
        }
    }

    private fun refreshHeldNotes(hangoutId: String) {
        viewModelScope.launch {
            hangoutNoteService
                .getNotes(hangoutId = hangoutId, before = null)
                .onSuccess { newestNotes ->
                    if (hangoutId != currentHangoutId) return@onSuccess
                    setupNotesPaginator(
                        hangoutId = hangoutId,
                        initialKey = newestNotes.minOfOrNull { it.createdAt }?.toString()
                    )
                    _state.update {
                        it.copy(
                            notes = newestNotes.map { note -> note.toHangoutNoteUi() }.toImmutableList(),
                            isEndReached = newestNotes.isEmpty(),
                            notesResetEpoch = it.notesResetEpoch + 1
                        )
                    }
                }
        }
        loadNoteCount(hangoutId)
    }

    private fun setupNotesPaginator(hangoutId: String, initialKey: String?) {
        notesPaginator?.close()
        notesPaginator = Paginator(
            initialKey = initialKey,
            onLoadUpdated = { isLoading ->
                _state.update { it.copy(isLoadingNotes = isLoading) }
            },
            onRequest = { beforeTimestamp ->
                hangoutNoteService.getNotes(hangoutId = hangoutId, before = beforeTimestamp)
            },
            getNextKey = { notes ->
                notes.minOfOrNull { it.createdAt }?.toString()
            },
            onError = { throwable ->
                if (throwable is DataErrorException) {
                    eventChannel.send(HangoutNotesEvent.Error(throwable.error.toUiText()))
                }
            },
            onSuccess = { olderNotes, _ ->
                _state.update {
                    it.copy(
                        notes = (it.notes + olderNotes.map { note -> note.toHangoutNoteUi() }).toImmutableList(),
                        isEndReached = olderNotes.isEmpty()
                    )
                }
            }
        )
    }

    private fun loadNextPage() {
        viewModelScope.launch {
            notesPaginator?.loadNextItems()
        }
    }

    private fun loadNoteCount(hangoutId: String) {
        viewModelScope.launch {
            hangoutNoteService
                .getNoteCount(hangoutId)
                .onSuccess { noteCount ->
                    if (hangoutId != currentHangoutId) return@onSuccess
                    _state.update { it.copy(noteCount = noteCount) }
                }
        }
    }
}
