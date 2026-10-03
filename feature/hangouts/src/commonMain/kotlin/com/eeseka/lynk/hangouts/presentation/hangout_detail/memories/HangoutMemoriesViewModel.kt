package com.eeseka.lynk.hangouts.presentation.hangout_detail.memories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eeseka.lynk.hangouts.presentation.mappers.toHangoutPhotoUi
import com.eeseka.lynk.shared.domain.auth.SessionStorage
import com.eeseka.lynk.shared.domain.hangout.HangoutConstants.MAX_PHOTOS_PER_UPLOADER
import com.eeseka.lynk.shared.domain.hangout.HangoutConstants.MIN_ATTENDEES_FOR_PHOTOS
import com.eeseka.lynk.shared.domain.hangout.HangoutDetailRepository
import com.eeseka.lynk.shared.domain.hangout.HangoutPhotoRepository
import com.eeseka.lynk.shared.domain.hangout.HangoutPhotoService
import com.eeseka.lynk.shared.domain.hangout.HangoutPhotoStatsRepository
import com.eeseka.lynk.shared.domain.hangout.model.Hangout
import com.eeseka.lynk.shared.domain.hangout.model.HangoutStatus
import com.eeseka.lynk.shared.domain.hangout.model.RsvpStatus
import com.eeseka.lynk.shared.domain.lobby.LobbyConnectionClient
import com.eeseka.lynk.shared.domain.lobby.model.LobbyEvent
import com.eeseka.lynk.shared.domain.util.onFailure
import com.eeseka.lynk.shared.domain.util.onSuccess
import com.eeseka.lynk.shared.presentation.util.toUiText
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes

@OptIn(ExperimentalCoroutinesApi::class)
class HangoutMemoriesViewModel(
    private val hangoutDetailRepository: HangoutDetailRepository,
    private val hangoutPhotoService: HangoutPhotoService,
    private val hangoutPhotoRepository: HangoutPhotoRepository,
    private val hangoutPhotoStatsRepository: HangoutPhotoStatsRepository,
    private val connectionClient: LobbyConnectionClient,
    private val sessionStorage: SessionStorage
) : ViewModel() {
    private val eventChannel = Channel<HangoutMemoriesEvent>()
    val events = eventChannel.receiveAsFlow()

    private val _state = MutableStateFlow(HangoutMemoriesState())
    private var hasLoadedInitialData = false

    private val _hangoutId = MutableStateFlow<String?>(null)

    val state = _state
        .onStart {
            if (!hasLoadedInitialData) {
                observeAlbum()
                observeLobbyEvents()
                hasLoadedInitialData = true
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = HangoutMemoriesState()
        )

    fun onAction(action: HangoutMemoriesAction) {
        when (action) {
            is HangoutMemoriesAction.OnSelectHangout -> selectHangout(action.hangoutId)
            is HangoutMemoriesAction.OnPhotosPicked -> uploadPhotos(action.imagePaths)
            HangoutMemoriesAction.OnPhotoLoadFailed -> reloadIfLinksExpiring()
        }
    }

    private fun selectHangout(hangoutId: String?) {
        if (hangoutId == _hangoutId.value) return
        _hangoutId.update { hangoutId }

        _state.update {
            it.copy(
                photoCount = 0,
                remainingPhotoSlots = 0,
                photos = persistentListOf(),
                isLoadingPhotos = false,
                isUploadingPhotos = false
            )
        }
    }

    // Loads the album as soon as it opens, including when the host completes the hangout while it is on screen
    private fun observeAlbum() {
        val hangout = _hangoutId.flatMapLatest { hangoutId ->
            hangoutId?.let(hangoutDetailRepository::observeHangout) ?: flowOf(null)
        }
        val currentUserId = sessionStorage.observeAuthInfo().map { it?.user?.id }

        combine(hangout, currentUserId) { hangout, userId ->
            hangout?.takeIf { it.isAlbumOpenFor(userId) }?.id
        }
            .distinctUntilChanged()
            .flatMapLatest { albumHangoutId ->
                albumHangoutId?.let(::observePhotoStats) ?: emptyFlow()
            }
            .launchIn(viewModelScope)
    }

    // A new count means photos came or went, here or in the album screen
    private fun observePhotoStats(hangoutId: String): Flow<Int> {
        return hangoutPhotoStatsRepository
            .observePhotoStats(hangoutId)
            .onStart {
                _state.update { it.copy(isLoadingPhotos = true) }
                hangoutPhotoStatsRepository
                    .refreshPhotoStats(hangoutId)
                    .onFailure { error ->
                        _state.update { it.copy(isLoadingPhotos = false) }
                        eventChannel.send(HangoutMemoriesEvent.Error(error.toUiText()))
                    }
            }
            .filterNotNull()
            .onEach { stats ->
                _state.update {
                    it.copy(
                        photoCount = stats.photoCount,
                        remainingPhotoSlots = MAX_PHOTOS_PER_UPLOADER - stats.myPhotoCount
                    )
                }
            }
            .map { stats -> stats.photoCount }
            .distinctUntilChanged()
            .onEach { loadPhotos(hangoutId) }
    }

    private fun loadPhotos(hangoutId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoadingPhotos = true) }
            hangoutPhotoService
                .getPhotos(hangoutId = hangoutId, before = null)
                .onSuccess { photos ->
                    // A late answer for a hangout the user already left must not land on the next one
                    if (hangoutId != _hangoutId.value) return@onSuccess
                    _state.update {
                        it.copy(
                            photos = photos.map { photo -> photo.toHangoutPhotoUi() }.toImmutableList(),
                            isLoadingPhotos = false
                        )
                    }
                }
                .onFailure { error ->
                    if (hangoutId != _hangoutId.value) return@onFailure
                    _state.update { it.copy(isLoadingPhotos = false) }
                    eventChannel.send(HangoutMemoriesEvent.Error(error.toUiText()))
                }
        }
    }

    private fun observeLobbyEvents() {
        connectionClient.events
            .onEach { event -> handleLobbyEvent(event) }
            .launchIn(viewModelScope)
    }

    // Someone else's photos change the count, and a new count reloads the row
    private suspend fun handleLobbyEvent(event: LobbyEvent) {
        val currentHangoutId = _hangoutId.value ?: return

        when (event) {
            is LobbyEvent.PhotosAdded -> {
                if (event.hangoutId == currentHangoutId) {
                    hangoutPhotoStatsRepository.refreshPhotoStats(currentHangoutId)
                }
            }

            is LobbyEvent.PhotoDeleted -> {
                if (event.hangoutId == currentHangoutId) {
                    hangoutPhotoStatsRepository.refreshPhotoStats(currentHangoutId)
                }
            }

            else -> Unit
        }
    }

    private fun uploadPhotos(imagePaths: List<String>) {
        val hangoutId = _hangoutId.value ?: return
        if (imagePaths.isEmpty()) return

        viewModelScope.launch {
            _state.update { it.copy(isUploadingPhotos = true) }
            hangoutPhotoRepository
                .uploadPhotos(hangoutId = hangoutId, imagePaths = imagePaths)
                .onSuccess {
                    eventChannel.send(HangoutMemoriesEvent.PhotosAdded)
                }
                .onFailure { error ->
                    eventChannel.send(HangoutMemoriesEvent.Error(error.toUiText()))
                }
            _state.update { it.copy(isUploadingPhotos = false) }

            // Even a failed batch may have added some, so the new counts decide whether the row reloads
            hangoutPhotoStatsRepository.refreshPhotoStats(hangoutId)
        }
    }

    // Links last an hour; a thumbnail that fails to load with an old link gets the row again
    private fun reloadIfLinksExpiring() {
        val hangoutId = _hangoutId.value ?: return
        if (state.value.isLoadingPhotos) return

        val refreshBefore = Clock.System.now() + 5.minutes
        val hasExpiringLinks = state.value.photos.any { it.urlsExpireAt < refreshBefore }
        if (!hasExpiringLinks) return

        loadPhotos(hangoutId)
    }

    private fun Hangout.isAlbumOpenFor(userId: String?): Boolean {
        val attending = participants.filter { it.rsvpStatus == RsvpStatus.ATTENDING }
        return status == HangoutStatus.COMPLETED &&
                attending.size >= MIN_ATTENDEES_FOR_PHOTOS &&
                attending.any { it.user.userId == userId }
    }
}
