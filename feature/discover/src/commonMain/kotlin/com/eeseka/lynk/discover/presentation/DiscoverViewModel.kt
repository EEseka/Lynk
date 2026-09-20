package com.eeseka.lynk.discover.presentation

import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eeseka.lynk.shared.domain.auth.AuthService
import com.eeseka.lynk.shared.domain.auth.SessionStorage
import com.eeseka.lynk.shared.domain.auth.model.User
import com.eeseka.lynk.shared.domain.location.LastKnownLocationStorage
import com.eeseka.lynk.shared.domain.location.LocationCoordinates
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
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
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
    private val appPreferences: AppPreferences,
    private val lastKnownLocationStorage: LastKnownLocationStorage
) : ViewModel() {

    private val eventChannel = Channel<DiscoverEvent>()
    val events = eventChannel.receiveAsFlow()

    private val _state = MutableStateFlow(DiscoverState())
    private var hasLoadedInitialData = false

    private var searchPaginator: Paginator<String?, Spot>? = null
    private var currentNextPageToken: String? = null

    private val saveJobs = mutableMapOf<String, Job>()

    private val trendingLocation = MutableStateFlow<LocationCoordinates?>(null)

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
                observeTrendingLocation()
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
            is DiscoverAction.OnLocationFetched -> handleLocationFetched(action.latitude, action.longitude)
            DiscoverAction.OnLocationUnavailable -> loadTrendingAroundLastKnownLocation()
            is DiscoverAction.OnSpotSelected -> _state.update { it.copy(selectedSpotId = action.spotId) }
            is DiscoverAction.OnToggleSaveSpot -> toggleSaveSpot(action.spotId, action.isCurrentlySaved)
            is DiscoverAction.OnCategorySelected -> _state.update { it.copy(selectedCategory = action.category) }
            is DiscoverAction.OnPriceLevelSelected -> _state.update { it.copy(selectedPriceLevel = action.priceLevel) }
            DiscoverAction.LoadNextSearchPage -> loadNextSearchPage()
            DiscoverAction.RetryTrending -> retryTrending()
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
            authService.deleteAccount()
            _state.update { it.copy(isGuestSigningOut = false) }
            // Clear local session regardless of backend result.
            // MainViewModel.observeSession() detects the null and navigates to auth.
            sessionStorage.set(null)
        }
    }

    private fun handleLocationFetched(latitude: Double, longitude: Double) {
        _state.update {
            it.copy(
                userLatitude = latitude,
                userLongitude = longitude,
                locationFetchEpoch = it.locationFetchEpoch + 1
            )
        }
        trendingLocation.value = LocationCoordinates(latitude = latitude, longitude = longitude)

        viewModelScope.launch {
            lastKnownLocationStorage.setLastKnownLocation(latitude, longitude)
        }
    }

    private fun loadTrendingAroundLastKnownLocation() {
        viewModelScope.launch {
            trendingLocation.value = lastKnownLocationStorage.lastKnownLocation.firstOrNull() ?: return@launch
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeTrendingLocation() {
        trendingLocation
            .filterNotNull()
            .distinctUntilChanged()
            .mapLatest { location -> fetchTrendingSpots(location) }
            .launchIn(viewModelScope)
    }

    private fun retryTrending() {
        val location = trendingLocation.value ?: return

        viewModelScope.launch {
            fetchTrendingSpots(location)
        }
    }

    private suspend fun fetchTrendingSpots(location: LocationCoordinates) {
        _state.update { it.copy(isTrendingLoading = true, trendingError = null) }

        spotService.getTrendingSpots(location.latitude, location.longitude)
            .onSuccess { spots ->
                _state.update {
                    it.copy(
                        isTrendingLoading = false,
                        trendingSpots = spots.map { spot -> spot.toSpotUi() }.toImmutableList()
                    )
                }
            }
            .onFailure { error ->
                _state.update {
                    it.copy(
                        isTrendingLoading = false,
                        trendingError = error.toUiText()
                    )
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

            saveJobs.remove(spotId)
        }
    }

    private fun updateSpotSaveState(spotId: String, isSaved: Boolean) {
        _state.update { currentState ->
            currentState.copy(
                trendingSpots = currentState.trendingSpots.map {
                    if (it.id == spotId) it.copy(isSaved = isSaved) else it
                }.toImmutableList(),
                searchResults = currentState.searchResults.map {
                    if (it.id == spotId) it.copy(isSaved = isSaved) else it
                }.toImmutableList()
            )
        }
    }

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    private fun observeSearchFilters() {
        val searchQueryFlow = snapshotFlow { _state.value.searchTextState.text.toString() }
            .debounce { query -> if (query.isBlank()) 0.milliseconds else 500.milliseconds }

        val categoryFlow = state.map { it.selectedCategory }.distinctUntilChanged()
        val priceLevelFlow = state.map { it.selectedPriceLevel }.distinctUntilChanged()
        val locationFlow = state.map { Pair(it.userLatitude, it.userLongitude) }.distinctUntilChanged()

        combine(
            searchQueryFlow,
            categoryFlow,
            priceLevelFlow,
            locationFlow
        ) { query, category, priceLevel, location ->
            SpotSearchFilters(
                query = query,
                category = category,
                priceLevel = priceLevel,
                latitude = location.first,
                longitude = location.second
            )
        }.mapLatest { filters ->
            val lat = filters.latitude
            val lng = filters.longitude
            if (lat == null || lng == null) return@mapLatest

            val isActivelySearching = filters.query.isNotBlank() ||
                    filters.category != null ||
                    filters.priceLevel != null

            if (isActivelySearching) {
                setupSearchPaginator(lat, lng, filters.query, filters.category, filters.priceLevel)
                _state.update {
                    it.copy(
                        searchResults = persistentListOf(),
                        searchEndReached = false,
                        searchError = null,
                        searchResetEpoch = it.searchResetEpoch + 1
                    )
                }
                searchPaginator?.loadNextItems()
            } else {
                _state.update {
                    it.copy(
                        searchResults = persistentListOf(),
                        searchEndReached = false,
                        isSearchLoading = false,
                        searchError = null,
                        searchResetEpoch = it.searchResetEpoch + 1
                    )
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
                    _state.update { it.copy(searchError = throwable.error.toUiText()) }
                }
            },
            onSuccess = { newSpots, newKey ->
                _state.update {
                    it.copy(
                        searchResults = (it.searchResults + newSpots.map { newSpot -> newSpot.toSpotUi() }).toImmutableList(),
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

private data class SpotSearchFilters(
    val query: String,
    val category: SpotCategory?,
    val priceLevel: PriceLevel?,
    val latitude: Double?,
    val longitude: Double?
)