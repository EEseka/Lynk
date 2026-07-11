package com.eeseka.lynk.discover.presentation

import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eeseka.lynk.shared.domain.auth.AuthService
import com.eeseka.lynk.shared.domain.auth.SessionStorage
import com.eeseka.lynk.shared.domain.auth.model.User
import com.eeseka.lynk.shared.domain.settings.AppPreferences
import com.eeseka.lynk.shared.domain.spot.SpotService
import com.eeseka.lynk.shared.domain.spot.model.PriceLevel
import com.eeseka.lynk.shared.domain.spot.model.Spot
import com.eeseka.lynk.shared.domain.spot.model.SpotCategory
import com.eeseka.lynk.shared.domain.util.DataErrorException
import com.eeseka.lynk.shared.domain.util.Paginator
import com.eeseka.lynk.shared.domain.util.map
import com.eeseka.lynk.shared.domain.util.onFailure
import com.eeseka.lynk.shared.domain.util.onSuccess
import com.eeseka.lynk.shared.presentation.spot.mappers.toSpotUi
import com.eeseka.lynk.shared.presentation.util.toUiText
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

class DiscoverViewModel(
    private val spotService: SpotService,
    private val sessionStorage: SessionStorage,
    private val authService: AuthService,
    private val appPreferences: AppPreferences
) : ViewModel() {

    private val eventChannel = Channel<DiscoverEvent>()
    val events = eventChannel.receiveAsFlow()

    private val _state = MutableStateFlow(DiscoverState())
    private var hasLoadedInitialData = false

    private var searchPaginator: Paginator<String?, Spot>? = null
    private var currentNextPageToken: String? = null

    private val saveJobs = mutableMapOf<String, Job>()

    val state = combine(
        _state,
        appPreferences.theme
    ) { currentState, theme ->
        currentState.copy(mapTheme = theme)
    }
        .onStart {
            if (!hasLoadedInitialData) {
                val authInfo = sessionStorage.observeAuthInfo().firstOrNull()
                _state.update { it.copy(isGuest = authInfo?.user is User.Guest) }
                observeSearchFilters()
                hasLoadedInitialData = true
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = DiscoverState()
        )

    fun onAction(action: DiscoverAction) {
        when (action) {
            is DiscoverAction.OnLocationFetched -> handleLocationFetched(
                action.latitude,
                action.longitude
            )

            is DiscoverAction.OnSpotSelected -> _state.update { it.copy(selectedSpotId = action.spotId) }
            is DiscoverAction.OnToggleSaveSpot -> toggleSaveSpot(
                action.spotId,
                action.isCurrentlySaved
            )

            is DiscoverAction.OnCategorySelected -> _state.update { it.copy(selectedCategory = action.category) }
            is DiscoverAction.OnPriceLevelSelected -> _state.update { it.copy(selectedPriceLevel = action.priceLevel) }
            DiscoverAction.LoadNextSearchPage -> loadNextSearchPage()
            DiscoverAction.ToggleShowSearchSheet -> _state.update { it.copy(showSearchSheet = !it.showSearchSheet) }
            is DiscoverAction.ShowGuestPrompt -> _state.update { it.copy(guestPromptContext = action.context) }
            DiscoverAction.HideGuestPrompt -> _state.update { it.copy(guestPromptContext = null) }
            DiscoverAction.SignOutGuest -> signOutGuest()

            is DiscoverAction.OnHangoutCreationSelected -> _state.update { it.copy(hangoutCreationSpotId = action.spotId) }
        }
    }

    private fun signOutGuest() {
        _state.update { it.copy(isGuestSigningOut = true) }

        viewModelScope.launch {
            val authInfo = sessionStorage.observeAuthInfo().first()
            val refreshToken = authInfo?.refreshToken ?: return@launch

            authService.logout(refreshToken)
                .onSuccess {
                    _state.update { it.copy(isGuestSigningOut = false) }
                }
                .onFailure { _ ->
                    _state.update { it.copy(isGuestSigningOut = false) }
                }

            // Clear local session regardless of backend result.
            // MainViewModel.observeSession() detects the null and navigates to auth.
            sessionStorage.set(null)
        }
    }

    private fun handleLocationFetched(latitude: Double, longitude: Double) {
        _state.update {
            it.copy(
                userLatitude = latitude,
                userLongitude = longitude
            )
        }
        fetchTrendingSpots(latitude, longitude)
    }

    private fun fetchTrendingSpots(latitude: Double, longitude: Double) {
        if (state.value.isTrendingLoading || state.value.trendingSpots.isNotEmpty()) return

        _state.update { it.copy(isTrendingLoading = true) }

        viewModelScope.launch {
            spotService.getTrendingSpots(latitude, longitude)
                .onSuccess { spots ->
                    _state.update {
                        it.copy(
                            isTrendingLoading = false,
                            trendingSpots = spots.map { spot -> spot.toSpotUi() }
                        )
                    }
                }
                .onFailure { error ->
                    _state.update { it.copy(isTrendingLoading = false) }
                    eventChannel.send(DiscoverEvent.Error(error.toUiText()))
                }
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
                eventChannel.send(DiscoverEvent.Error(error.toUiText()))
            }
        }
    }

    private fun updateSpotSaveState(spotId: String, isSaved: Boolean) {
        _state.update { currentState ->
            currentState.copy(
                trendingSpots = currentState.trendingSpots.map {
                    if (it.id == spotId) it.copy(isSaved = isSaved) else it
                },
                searchResults = currentState.searchResults.map {
                    if (it.id == spotId) it.copy(isSaved = isSaved) else it
                }
            )
        }
    }

    @OptIn(FlowPreview::class)
    private fun observeSearchFilters() {
        val searchQueryFlow = snapshotFlow { _state.value.searchTextState.text.toString() }
            .debounce(500L.milliseconds)
            .distinctUntilChanged()

        val categoryFlow = state.map { it.selectedCategory }.distinctUntilChanged()
        val priceLevelFlow = state.map { it.selectedPriceLevel }.distinctUntilChanged()
        val locationFlow =
            state.map { Pair(it.userLatitude, it.userLongitude) }.distinctUntilChanged()

        combine(
            searchQueryFlow,
            categoryFlow,
            priceLevelFlow,
            locationFlow
        ) { query, category, priceLevel, location ->
            val (lat, lng) = location
            if (lat != null && lng != null) {
                val isActivelySearching =
                    query.isNotBlank() || category != null || priceLevel != null

                if (isActivelySearching) {
                    setupSearchPaginator(lat, lng, query, category, priceLevel)
                    _state.update {
                        it.copy(
                            searchResults = emptyList(),
                            searchEndReached = false,
                            searchResetEpoch = it.searchResetEpoch + 1
                        )
                    }
                    loadNextSearchPage()
                } else {
                    _state.update {
                        it.copy(
                            searchResults = emptyList(),
                            searchEndReached = false,
                            isSearchLoading = false,
                            searchResetEpoch = it.searchResetEpoch + 1
                        )
                    }
                }
            }
        }.launchIn(viewModelScope)
    }

    private fun setupSearchPaginator(
        lat: Double,
        lng: Double,
        query: String,
        category: SpotCategory?,
        priceLevel: PriceLevel?
    ) {
        currentNextPageToken = null

        searchPaginator = Paginator(
            initialKey = null,
            onLoadUpdated = { isLoading ->
                _state.update { it.copy(isSearchLoading = isLoading) }
            },
            onRequest = { nextPageToken ->
                spotService.searchSpots(
                    latitude = lat,
                    longitude = lng,
                    query = query.takeIf { it.isNotBlank() },
                    category = category,
                    priceLevel = priceLevel,
                    nextPageToken = nextPageToken
                ).map { paginatedSpots ->
                    currentNextPageToken = paginatedSpots.nextPageToken
                    paginatedSpots.spots
                }
            },
            getNextKey = { _ ->
                currentNextPageToken
            },
            onError = { throwable ->
                if (throwable is DataErrorException) {
                    _state.update {
                        it.copy(
                            searchError = throwable.error.toUiText()
                        )
                    }
                }
            },
            onSuccess = { newSpots, newKey ->
                _state.update {
                    it.copy(
                        searchResults = it.searchResults + newSpots.map { newSpot -> newSpot.toSpotUi() },
                        searchEndReached = newKey == null,
                        searchError = null
                    )
                }
            }
        )
    }

    private fun loadNextSearchPage() {
        viewModelScope.launch {
            searchPaginator?.loadNextItems()
        }
    }
}