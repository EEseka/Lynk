package com.eeseka.lynk.profile.presentation.saved_spots

import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eeseka.lynk.shared.domain.spot.SpotService
import com.eeseka.lynk.shared.domain.spot.model.Spot
import com.eeseka.lynk.shared.domain.util.DataErrorException
import com.eeseka.lynk.shared.domain.util.Paginator
import com.eeseka.lynk.shared.domain.util.onFailure
import com.eeseka.lynk.shared.presentation.spot.mappers.toSpotUi
import com.eeseka.lynk.shared.presentation.util.toUiText
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

class SavedSpotsViewModel(
    private val spotService: SpotService
) : ViewModel() {

    private val eventChannel = Channel<SavedSpotsEvent>()
    val events = eventChannel.receiveAsFlow()

    private val _state = MutableStateFlow(SavedSpotsState())
    private var hasLoadedInitialData = false

    private var savedSpotsPaginator: Paginator<String?, Spot>? = null

    private val saveJobs = mutableMapOf<String, Job>()

    val state = _state
        .onStart {
            if (!hasLoadedInitialData) {
                observeSearchQuery()
                hasLoadedInitialData = true
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = SavedSpotsState()
        )

    fun onAction(action: SavedSpotsAction) {
        when (action) {
            is SavedSpotsAction.OnSpotSelected -> _state.update { it.copy(selectedSpotId = action.spotId) }
            SavedSpotsAction.OnDismissSpotDetail -> _state.update { it.copy(selectedSpotId = null) }
            is SavedSpotsAction.OnToggleSaveSpot -> toggleSaveSpot(
                action.spotId,
                action.isCurrentlySaved
            )
            SavedSpotsAction.LoadNextPage -> loadNextPage()
        }
    }

    private fun toggleSaveSpot(spotId: String, isCurrentlySaved: Boolean) {
        updateSpotSaveState(spotId, !isCurrentlySaved)

        saveJobs[spotId]?.cancel()

        saveJobs[spotId] = viewModelScope.launch {
            delay(300.milliseconds)

            val result = if (isCurrentlySaved) {
                spotService.unsaveSpot(spotId)
            } else {
                spotService.saveSpot(spotId)
            }

            result.onFailure { error ->
                updateSpotSaveState(spotId, isCurrentlySaved)
                eventChannel.send(SavedSpotsEvent.Error(error.toUiText()))
            }
        }
    }

    private fun updateSpotSaveState(spotId: String, isSaved: Boolean) {
        _state.update { currentState ->
            currentState.copy(
                spots = currentState.spots.map {
                    if (it.id == spotId) it.copy(isSaved = isSaved) else it
                }
            )
        }
    }

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    private fun observeSearchQuery() {
        snapshotFlow { _state.value.searchTextState.text.toString() }
            .distinctUntilChanged()
            .debounce { query -> if (query.isBlank()) 0.milliseconds else 500.milliseconds }
            .mapLatest { query ->
                setupSavedSpotsPaginator(query.takeIf { it.isNotBlank() })
                _state.update {
                    it.copy(
                        spots = emptyList(),
                        isEndReached = false,
                        searchResetEpoch = it.searchResetEpoch + 1
                    )
                }
                savedSpotsPaginator?.loadNextItems()
            }
            .launchIn(viewModelScope)
    }

    private fun setupSavedSpotsPaginator(searchQuery: String? = null) {
        savedSpotsPaginator = Paginator(
            initialKey = null,
            onLoadUpdated = { isLoading ->
                _state.update { it.copy(isLoading = isLoading) }
            },
            onRequest = { beforeTimestamp ->
                spotService.getSavedSpots(searchQuery, beforeTimestamp)
            },
            getNextKey = { spots ->
                spots.mapNotNull { it.savedAt }.minOrNull()?.toString()
            },
            onError = { throwable ->
                if (throwable is DataErrorException) {
                    eventChannel.send(SavedSpotsEvent.Error(throwable.error.toUiText()))
                }
            },
            onSuccess = { savedSpots, _ ->
                _state.update {
                    it.copy(
                        spots = it.spots + savedSpots.map { spot -> spot.toSpotUi() },
                        isEndReached = savedSpots.isEmpty()
                    )
                }
            }
        )
    }

    private fun loadNextPage() {
        viewModelScope.launch {
            savedSpotsPaginator?.loadNextItems()
        }
    }
}