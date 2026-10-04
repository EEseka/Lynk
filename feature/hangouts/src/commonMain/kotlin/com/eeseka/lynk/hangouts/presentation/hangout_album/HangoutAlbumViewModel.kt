package com.eeseka.lynk.hangouts.presentation.hangout_album

import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eeseka.lynk.hangouts.presentation.mappers.toHangoutPhotoUi
import com.eeseka.lynk.shared.domain.auth.SessionStorage
import com.eeseka.lynk.shared.domain.hangout.HangoutDetailRepository
import com.eeseka.lynk.shared.domain.hangout.HangoutPhotoService
import com.eeseka.lynk.shared.domain.hangout.HangoutPhotoStatsRepository
import com.eeseka.lynk.shared.domain.hangout.model.HangoutPhoto
import com.eeseka.lynk.shared.domain.lobby.LobbyConnectionClient
import com.eeseka.lynk.shared.domain.lobby.model.LobbyEvent
import com.eeseka.lynk.shared.domain.util.DataError
import com.eeseka.lynk.shared.domain.util.DataErrorException
import com.eeseka.lynk.shared.domain.util.Paginator
import com.eeseka.lynk.shared.domain.util.onFailure
import com.eeseka.lynk.shared.domain.util.onSuccess
import com.eeseka.lynk.shared.presentation.util.UiText
import com.eeseka.lynk.shared.presentation.util.toUiText
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import lynk.feature.hangouts.generated.resources.Res
import lynk.feature.hangouts.generated.resources.album_save_failed
import lynk.feature.hangouts.generated.resources.album_share_failed
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes

class HangoutAlbumViewModel(
    private val hangoutPhotoService: HangoutPhotoService,
    private val hangoutDetailRepository: HangoutDetailRepository,
    private val hangoutPhotoStatsRepository: HangoutPhotoStatsRepository,
    private val connectionClient: LobbyConnectionClient,
    private val sessionStorage: SessionStorage
) : ViewModel() {
    private val eventChannel = Channel<HangoutAlbumEvent>()
    val events = eventChannel.receiveAsFlow()

    private val _state = MutableStateFlow(HangoutAlbumState())
    private var hasLoadedInitialData = false

    private var photosPaginator: Paginator<String?, HangoutPhoto>? = null

    // Once the page holding it lands, the viewer opens on this photo
    private var photoIdToOpen: String? = null

    val state = _state
        .onStart {
            if (!hasLoadedInitialData) {
                val authInfo = sessionStorage.observeAuthInfo().firstOrNull()
                _state.update { it.copy(currentUserId = authInfo?.user?.id) }
                observeCanSaveCaption()
                observeLobbyEvents()
                hasLoadedInitialData = true
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = HangoutAlbumState()
        )

    private val captionFlow = snapshotFlow { _state.value.captionState.text.toString() }
        .map { it.trim() }
        .distinctUntilChanged()

    // What the caption is compared against: the edited photo's caption as the album holds it
    private val savedCaptionFlow = state.map { currentState ->
        currentState.photos.find { it.id == currentState.captionEditPhotoId }?.caption.orEmpty()
    }.distinctUntilChanged()

    private val isCaptionDirtyFlow = combine(
        captionFlow,
        savedCaptionFlow
    ) { caption, savedCaption ->
        caption != savedCaption
    }.distinctUntilChanged()

    private val isSavingCaptionFlow = state.map { it.isSavingCaption }.distinctUntilChanged()

    private fun observeCanSaveCaption() {
        combine(isCaptionDirtyFlow, isSavingCaptionFlow) { isCaptionDirty, isSavingCaption ->
            _state.update { it.copy(canSaveCaption = !isSavingCaption && isCaptionDirty) }
        }.launchIn(viewModelScope)
    }

    private fun observeLobbyEvents() {
        connectionClient.events
            .onEach { event -> handleLobbyEvent(event) }
            .launchIn(viewModelScope)
    }

    private suspend fun handleLobbyEvent(event: LobbyEvent) {
        val currentHangoutId = state.value.hangoutId ?: return

        when (event) {
            is LobbyEvent.PhotosAdded -> {
                val hasOthersPhotos = event.uploaderIds.any { it != state.value.currentUserId }
                if (event.hangoutId == currentHangoutId && hasOthersPhotos) {
                    eventChannel.send(HangoutAlbumEvent.NewPhotosAdded)
                }
            }

            is LobbyEvent.PhotoDeleted -> {
                if (event.hangoutId == currentHangoutId) removePhoto(event.photoId)
            }

            else -> Unit
        }
    }

    fun onAction(action: HangoutAlbumAction) {
        when (action) {
            is HangoutAlbumAction.OnSelectHangout -> selectHangout(action.hangoutId, action.initialPhotoId)
            HangoutAlbumAction.LoadNextPage -> loadNextPage()
            HangoutAlbumAction.OnRetryClick -> retryLoad()
            HangoutAlbumAction.OnNewPhotosClick -> showNewPhotos()
            is HangoutAlbumAction.OnPhotoClick -> openViewer(action.photoId)
            is HangoutAlbumAction.OnViewerPageChanged -> _state.update { it.copy(viewerIndex = action.index) }
            HangoutAlbumAction.OnDismissViewer -> _state.update { it.copy(viewerIndex = null) }
            HangoutAlbumAction.OnPhotoLoadFailed -> reloadIfLinksExpiring()
            is HangoutAlbumAction.OnPhotoSaved -> reportPhotoSaved(action.isSaved)
            is HangoutAlbumAction.OnPhotoShared -> reportPhotoShared(action.isShared)
            is HangoutAlbumAction.OnEditCaptionClick -> openCaptionSheet(action.photoId)
            HangoutAlbumAction.OnDismissCaptionSheet -> dismissCaptionSheet()
            HangoutAlbumAction.OnSaveCaptionClick -> saveCaption()
            is HangoutAlbumAction.OnDeleteClick -> _state.update { it.copy(pendingDeletePhotoId = action.photoId) }
            HangoutAlbumAction.OnDismissDeleteDialog -> _state.update { it.copy(pendingDeletePhotoId = null) }
            HangoutAlbumAction.OnDeleteConfirmed -> deletePhoto()
        }
    }

    private fun selectHangout(hangoutId: String, initialPhotoId: String?) {
        if (hangoutId == state.value.hangoutId) return
        _state.update { it.copy(hangoutId = hangoutId) }
        photoIdToOpen = initialPhotoId

        observeHangout(hangoutId)
        viewModelScope.launch { hangoutDetailRepository.refreshHangout(hangoutId) }
        setupPhotosPaginator(hangoutId)
        loadNextPage()
    }

    // The name and the host come from the hangout the detail screen already holds
    private fun observeHangout(hangoutId: String) {
        hangoutDetailRepository
            .observeHangout(hangoutId)
            .onEach { hangout ->
                _state.update { it.copy(hangoutName = hangout?.name, hostId = hangout?.hostId) }
            }
            .launchIn(viewModelScope)
    }

    private fun setupPhotosPaginator(hangoutId: String) {
        photosPaginator?.close()
        photosPaginator = Paginator(
            initialKey = null,
            onLoadUpdated = { isLoading ->
                _state.update { it.copy(isLoading = isLoading) }
            },
            onRequest = { beforeTimestamp ->
                hangoutPhotoService.getPhotos(hangoutId = hangoutId, before = beforeTimestamp)
            },
            getNextKey = { photos ->
                photos.minOfOrNull { it.createdAt }?.toString()
            },
            onError = { throwable ->
                if (throwable is DataErrorException) {
                    val message = throwable.error.toUiText()

                    if (state.value.photos.isEmpty()) {
                        _state.update { it.copy(loadError = message) }
                    } else {
                        eventChannel.send(HangoutAlbumEvent.Error(message))
                    }
                }
            },
            onSuccess = { newPhotos, _ ->
                val newPhotoUis = newPhotos.map { photo -> photo.toHangoutPhotoUi() }
                val isPhotoToOpenInPage = newPhotoUis.any { it.id == photoIdToOpen }

                _state.update { currentState ->
                    val photos = (currentState.photos + newPhotoUis).toImmutableList()
                    currentState.copy(
                        photos = photos,
                        isEndReached = newPhotos.isEmpty(),
                        loadError = null,
                        viewerIndex = if (isPhotoToOpenInPage) {
                            photos.indexOfFirst { it.id == photoIdToOpen }
                        } else {
                            currentState.viewerIndex
                        }
                    )
                }

                // Opened once; a later page must not pull the viewer back to it
                if (isPhotoToOpenInPage) photoIdToOpen = null
            }
        )
    }

    private fun loadNextPage() {
        viewModelScope.launch {
            photosPaginator?.loadNextItems()
        }
    }

    private fun retryLoad() {
        _state.update { it.copy(loadError = null) }
        loadNextPage()
    }

    // Pages run newest first, so newer photos only arrive by starting the album over
    private fun showNewPhotos() {
        val hangoutId = state.value.hangoutId ?: return
        restartAlbum(hangoutId)
    }

    private fun openViewer(photoId: String) {
        val index = state.value.photos.indexOfFirst { it.id == photoId }
        if (index == -1) return
        _state.update { it.copy(viewerIndex = index) }
    }

    // Links last an hour; a photo that fails to load with an old link gets the whole album again
    private fun reloadIfLinksExpiring() {
        val hangoutId = state.value.hangoutId ?: return
        val refreshBefore = Clock.System.now() + 5.minutes
        if (state.value.photos.none { it.urlsExpireAt < refreshBefore }) return

        restartAlbum(hangoutId)
    }

    // The viewer closes while the album is empty and reopens on the same photo once its page lands
    private fun restartAlbum(hangoutId: String) {
        val viewerIndex = state.value.viewerIndex
        photoIdToOpen = viewerIndex?.let { state.value.photos.getOrNull(it)?.id }

        setupPhotosPaginator(hangoutId)
        _state.update {
            it.copy(
                photos = persistentListOf(),
                isEndReached = false,
                photosResetEpoch = it.photosResetEpoch + 1,
                viewerIndex = null
            )
        }
        loadNextPage()
    }

    private fun reportPhotoSaved(isSaved: Boolean) {
        viewModelScope.launch {
            val event = if (isSaved) HangoutAlbumEvent.PhotoSaved
            else HangoutAlbumEvent.Error(UiText.Resource(Res.string.album_save_failed))
            eventChannel.send(event)
        }
    }

    private fun reportPhotoShared(isShared: Boolean) {
        if (isShared) return
        viewModelScope.launch {
            eventChannel.send(HangoutAlbumEvent.Error(UiText.Resource(Res.string.album_share_failed)))
        }
    }

    private fun openCaptionSheet(photoId: String) {
        val photo = state.value.photos.find { it.id == photoId } ?: return
        _state.value.captionState.setTextAndPlaceCursorAtEnd(photo.caption.orEmpty())
        _state.update { it.copy(captionEditPhotoId = photoId) }
    }

    private fun dismissCaptionSheet() {
        _state.value.captionState.clearText()
        _state.update { it.copy(captionEditPhotoId = null) }
    }

    private fun saveCaption() {
        val hangoutId = state.value.hangoutId ?: return
        val photoId = state.value.captionEditPhotoId ?: return
        if (state.value.isSavingCaption) return

        val caption = _state.value.captionState.text.toString().trim().takeIf { it.isNotEmpty() }

        viewModelScope.launch {
            _state.update { it.copy(isSavingCaption = true) }
            hangoutPhotoService
                .updateCaption(hangoutId = hangoutId, photoId = photoId, caption = caption)
                .onSuccess {
                    _state.update { currentState ->
                        currentState.copy(
                            photos = currentState.photos
                                .map { if (it.id == photoId) it.copy(caption = caption) else it }
                                .toImmutableList()
                        )
                    }
                    dismissCaptionSheet()
                    eventChannel.send(HangoutAlbumEvent.CaptionUpdated)
                }
                .onFailure { error ->
                    eventChannel.send(HangoutAlbumEvent.Error(error.toUiText()))
                }
            _state.update { it.copy(isSavingCaption = false) }
        }
    }

    private fun deletePhoto() {
        val hangoutId = state.value.hangoutId ?: return
        val photoId = state.value.pendingDeletePhotoId ?: return

        viewModelScope.launch {
            _state.update { it.copy(deletingPhotoId = photoId) }
            hangoutPhotoService
                .deletePhoto(hangoutId = hangoutId, photoId = photoId)
                .onSuccess {
                    removePhoto(photoId)
                    eventChannel.send(HangoutAlbumEvent.PhotoDeleted)
                    // Updates the counts, which also refreshes the Memories row on the detail screen
                    hangoutPhotoStatsRepository.refreshPhotoStats(hangoutId)
                }
                .onFailure { error ->
                    // Already deleted, from another device or by the host, which is what was asked for
                    if (error == DataError.Remote.NOT_FOUND) {
                        removePhoto(photoId)
                        eventChannel.send(HangoutAlbumEvent.PhotoDeleted)
                        hangoutPhotoStatsRepository.refreshPhotoStats(hangoutId)
                        return@onFailure
                    }

                    eventChannel.send(HangoutAlbumEvent.Error(error.toUiText()))
                }
            _state.update { it.copy(deletingPhotoId = null) }
        }
    }

    private fun removePhoto(photoId: String) {
        _state.update { currentState ->
            val remaining = currentState.photos.filterNot { it.id == photoId }
            currentState.copy(
                photos = remaining.toImmutableList(),
                // Stays on the photo that slid into place, or closes when the album is empty
                viewerIndex = currentState.viewerIndex
                    ?.takeIf { remaining.isNotEmpty() }
                    ?.coerceAtMost(remaining.lastIndex)
            )
        }
    }
}
