package com.eeseka.lynk.create_hangout.presentation

import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eeseka.lynk.create_hangout.domain.validation.HangoutDateValidationState
import com.eeseka.lynk.create_hangout.domain.validation.HangoutDateValidator
import com.eeseka.lynk.create_hangout.domain.validation.HangoutDescriptionValidationState
import com.eeseka.lynk.create_hangout.domain.validation.HangoutDescriptionValidator
import com.eeseka.lynk.create_hangout.domain.validation.HangoutNameValidationState
import com.eeseka.lynk.create_hangout.domain.validation.HangoutNameValidator
import com.eeseka.lynk.create_hangout.domain.validation.HangoutTimeValidationState
import com.eeseka.lynk.create_hangout.domain.validation.HangoutTimeValidator
import com.eeseka.lynk.create_hangout.presentation.mappers.toUiText
import com.eeseka.lynk.create_hangout.presentation.model.SearchTab
import com.eeseka.lynk.shared.domain.hangout.HangoutConstants.MAX_ATTENDEES
import com.eeseka.lynk.shared.domain.hangout.HangoutService
import com.eeseka.lynk.shared.domain.hangout.model.RsvpStatus
import com.eeseka.lynk.shared.domain.spot.SpotService
import com.eeseka.lynk.shared.domain.spot.model.Spot
import com.eeseka.lynk.shared.domain.util.DataErrorException
import com.eeseka.lynk.shared.domain.util.Paginator
import com.eeseka.lynk.shared.domain.util.map
import com.eeseka.lynk.shared.domain.util.onFailure
import com.eeseka.lynk.shared.domain.util.onSuccess
import com.eeseka.lynk.shared.presentation.hangout.model.HangoutUi
import com.eeseka.lynk.shared.presentation.spot.mappers.toSpotUi
import com.eeseka.lynk.shared.presentation.spot.model.SpotUi
import com.eeseka.lynk.shared.presentation.util.toPickerDate
import com.eeseka.lynk.shared.presentation.util.toUiText
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class CreateHangoutViewModel(
    private val hangoutService: HangoutService,
    private val spotService: SpotService
) : ViewModel() {
    private val eventChannel = Channel<CreateHangoutEvent>()
    val events = eventChannel.receiveAsFlow()

    private val _state = MutableStateFlow(CreateHangoutState())
    private var hasLoadedInitialData = false

    private var spotSearchPaginator: Paginator<String?, Spot>? = null
    private var currentNextPageToken: String? = null

    private var favoriteSpotSearchPaginator: Paginator<String?, Spot>? = null

    val state = _state
        .onStart {
            if (!hasLoadedInitialData) {
                observeGatingStates()
                observeStepOneValidation()
                observeSearchFilters()
                hasLoadedInitialData = true
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = CreateHangoutState()
        )

    private val isStepTwoValidFlow = state.map {
        it.isVotingMode || it.selectedSpot != null
    }.distinctUntilChanged()

    private val isBusyFlow = state.map { it.isSubmitting }.distinctUntilChanged()

    // Errors stay quiet until the first Next press, then track every keystroke.
    private var hasAttemptedStepOne = false

    private fun observeGatingStates() {
        isStepTwoValidFlow
            .onEach { isStepTwoValid ->
                _state.update { it.copy(canProceedToStepThree = isStepTwoValid) }
            }
            .launchIn(viewModelScope)

        isBusyFlow
            .onEach { isBusy -> _state.update { it.copy(canSubmit = !isBusy) } }
            .launchIn(viewModelScope)
    }

    private fun observeStepOneValidation() {
        snapshotFlow { _state.value.hangoutNameTextState.text.toString() }
            .onEach { name ->
                val validationState = HangoutNameValidator.validate(name)
                _state.update {
                    it.copy(hangoutNameError = if (hasAttemptedStepOne) validationState.toUiText() else null)
                }
            }
            .launchIn(viewModelScope)

        snapshotFlow { _state.value.hangoutDescriptionTextState.text.toString() }
            .onEach { description ->
                val validationState = HangoutDescriptionValidator.validate(description)
                _state.update {
                    it.copy(hangoutDescriptionError = if (hasAttemptedStepOne) validationState.toUiText() else null)
                }
            }
            .launchIn(viewModelScope)

        // The time validator reads the date, so a change to either re-runs both.
        state.map { it.hangoutDate to it.hangoutTime }
            .distinctUntilChanged()
            .onEach { (date, time) ->
                val (currentDate, currentTime) = getCurrentDateAndTime()
                val dateState = HangoutDateValidator.validate(date, currentDate)
                val timeState = HangoutTimeValidator.validate(time, date, currentDate, currentTime)

                _state.update {
                    it.copy(
                        hangoutDateError = if (hasAttemptedStepOne) dateState.toUiText() else null,
                        hangoutTimeError = if (hasAttemptedStepOne) timeState.toUiText() else null
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun onAction(action: CreateHangoutAction) {
        when (action) {
            is CreateHangoutAction.InitCreateMode -> initCreateMode(action.spot)
            is CreateHangoutAction.InitEditMode -> initEditMode(action.hangout)
            is CreateHangoutAction.OnLocationFetched -> {
                _state.update {
                    it.copy(
                        userLatitude = action.latitude,
                        userLongitude = action.longitude
                    )
                }
                fetchTrendingSpots(action.latitude, action.longitude)
            }

            is CreateHangoutAction.OnSearchTabSelected -> {
                _state.update { it.copy(activeSearchTab = action.tab) }
            }

            is CreateHangoutAction.OnVibeSelected -> {
                _state.update { it.copy(hangoutVibe = action.vibe) }
            }

            is CreateHangoutAction.OnPickerToggled -> {
                _state.update {
                    it.copy(expandedPicker = if (it.expandedPicker == action.picker) null else action.picker)
                }
            }

            is CreateHangoutAction.OnDateSelected -> {
                val date = action.epochMilliseconds?.toPickerDate()
                _state.update {
                    it.copy(hangoutDate = date ?: it.hangoutDate, expandedPicker = null)
                }
            }

            is CreateHangoutAction.OnTimeSelected -> {
                _state.update {
                    it.copy(
                        hangoutTime = LocalTime(action.hour, action.minute),
                        expandedPicker = null
                    )
                }
            }

            is CreateHangoutAction.OnSpotSelected -> {
                _state.update { it.copy(selectedSpot = action.spot) }
            }

            is CreateHangoutAction.OnLocationModeChanged -> {
                _state.update { it.copy(isVotingMode = action.isVotingMode) }
            }

            CreateHangoutAction.OnNextStep -> {
                when (state.value.currentStep) {
                    1 -> {
                        hasAttemptedStepOne = true
                        if (validateFormInputs()) _state.update { it.copy(currentStep = 2) }
                    }

                    2 -> if (state.value.canProceedToStepThree) {
                        _state.update { it.copy(currentStep = 3) }
                    }
                }
            }

            CreateHangoutAction.OnPreviousStep -> {
                _state.update { it.copy(currentStep = (it.currentStep - 1).coerceAtLeast(1)) }
            }

            CreateHangoutAction.IncrementAttendees -> {
                val current = state.value.maxAttendees
                val nextValue = if (current == null) state.value.minAttendees else current + 1
                if (nextValue <= MAX_ATTENDEES) _state.update { it.copy(maxAttendees = nextValue) }
            }

            CreateHangoutAction.DecrementAttendees -> {
                val current = state.value.maxAttendees
                if (current != null) {
                    val nextValue = current - 1
                    _state.update {
                        it.copy(maxAttendees = if (nextValue < it.minAttendees) null else nextValue)
                    }
                }
            }

            CreateHangoutAction.LoadNextSpotSearchPage -> loadNextSpotSearchPage()
            CreateHangoutAction.LoadNextFavoriteSpotSearchPage -> loadNextFavoriteSpotSearchPage()
            CreateHangoutAction.OnSubmitClick -> submit()
        }
    }

    private fun initCreateMode(spot: SpotUi?) {
        if (state.value.selectedSpot != null) return // Already initialized

        _state.update {
            it.copy(
                selectedSpot = spot,
                isVotingMode = spot == null, // If passed a spot, default to dictator
            )
        }
    }

    private fun initEditMode(hangout: HangoutUi) {
        if (state.value.originalHangout != null) return // Already initialized

        _state.value.hangoutNameTextState.setTextAndPlaceCursorAtEnd(hangout.name)
        _state.value.hangoutDescriptionTextState.setTextAndPlaceCursorAtEnd(hangout.description ?: "")

        val localDateTime = hangout.scheduledAt.toLocalDateTime(TimeZone.currentSystemDefault())
        val activeCount = hangout.participants.count {
            it.rsvpStatus == RsvpStatus.ATTENDING || it.rsvpStatus == RsvpStatus.PENDING
        }
        _state.update {
            it.copy(
                originalHangout = hangout,
                hangoutVibe = hangout.vibe,
                hangoutDate = localDateTime.date,
                hangoutTime = localDateTime.time,
                maxAttendees = hangout.maxAttendees,
                minAttendees = maxOf(2, activeCount),
                isVotingMode = hangout.chosenSpot == null,
                selectedSpot = hangout.chosenSpot
            )
        }
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
                            trendingSpots = spots.map { spot -> spot.toSpotUi() }.toImmutableList()
                        )
                    }
                }
                .onFailure { _ ->
                    // For zero-state, if trending fails, we just fail silently and let them search
                    _state.update { it.copy(isTrendingLoading = false) }
                }
        }
    }

    private fun observeSearchFilters() {
        val searchQueryFlow = snapshotFlow { _state.value.spotSearchTextState.text.toString() }
            .debounce { query -> if (query.isBlank()) 0.milliseconds else 500.milliseconds }

        val tabFlow = state.map { it.activeSearchTab }.distinctUntilChanged()

        val locationFlow = state.map { it.userLatitude to it.userLongitude }.distinctUntilChanged()

        combine(searchQueryFlow, tabFlow, locationFlow) { query, activeTab, location ->
            Triple(query, activeTab, location)
        }.mapLatest { (query, activeTab, location) ->
            when (activeTab) {
                SearchTab.FAVORITES -> {
                    setupFavoriteSpotSearchPaginator(query.takeIf { it.isNotBlank() })
                    _state.update {
                        it.copy(
                            favoriteSpotSearchResults = persistentListOf(),
                            favoriteSpotSearchEndReached = false,
                            favoriteSpotSearchError = null,
                            favoriteSpotSearchResetEpoch = it.favoriteSpotSearchResetEpoch + 1
                        )
                    }
                    favoriteSpotSearchPaginator?.loadNextItems()
                }

                SearchTab.ALL_SPOTS -> {
                    val (latitude, longitude) = location
                    if (query.isBlank() || latitude == null || longitude == null) {
                        _state.update {
                            it.copy(
                                spotSearchResults = persistentListOf(),
                                spotSearchEndReached = false,
                                isSpotSearchLoading = false,
                                spotSearchError = null,
                                spotSearchResetEpoch = it.spotSearchResetEpoch + 1
                            )
                        }
                    } else {
                        setupSpotSearchPaginator(latitude, longitude, query)
                        _state.update {
                            it.copy(
                                spotSearchResults = persistentListOf(),
                                spotSearchEndReached = false,
                                spotSearchError = null,
                                spotSearchResetEpoch = it.spotSearchResetEpoch + 1
                            )
                        }
                        spotSearchPaginator?.loadNextItems()
                    }
                }
            }
        }.launchIn(viewModelScope)
    }

    private fun setupSpotSearchPaginator(lat: Double, lng: Double, query: String) {
        currentNextPageToken = null

        spotSearchPaginator = Paginator(
            initialKey = null,
            onLoadUpdated = { isLoading ->
                _state.update { it.copy(isSpotSearchLoading = isLoading) }
            },
            onRequest = { nextPageToken ->
                spotService.searchSpots(
                    latitude = lat,
                    longitude = lng,
                    query = query,
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
                            spotSearchError = throwable.error.toUiText()
                        )
                    }
                }
            },
            onSuccess = { newSpots, newKey ->
                _state.update {
                    it.copy(
                        spotSearchResults = (it.spotSearchResults + newSpots.map { newSpot -> newSpot.toSpotUi() }).toImmutableList(),
                        spotSearchEndReached = newKey == null,
                        spotSearchError = null
                    )
                }
            }
        )
    }

    private fun setupFavoriteSpotSearchPaginator(searchQuery: String? = null) {
        favoriteSpotSearchPaginator = Paginator(
            initialKey = null,
            onLoadUpdated = { isLoading ->
                _state.update { it.copy(isFavoriteSpotSearchLoading = isLoading) }
            },
            onRequest = { beforeTimestamp ->
                spotService.getSavedSpots(searchQuery, beforeTimestamp)
            },
            getNextKey = { spots ->
                spots.mapNotNull { it.savedAt }.minOrNull()?.toString()
            },
            onError = { throwable ->
                if (throwable is DataErrorException) {
                    _state.update {
                        it.copy(
                            favoriteSpotSearchError = throwable.error.toUiText()
                        )
                    }
                }
            },
            onSuccess = { favoriteSpots, _ ->
                _state.update {
                    it.copy(
                        favoriteSpotSearchResults = (it.favoriteSpotSearchResults + favoriteSpots.map { favoriteSpot -> favoriteSpot.toSpotUi() }).toImmutableList(),
                        favoriteSpotSearchEndReached = favoriteSpots.isEmpty(),
                        favoriteSpotSearchError = null
                    )
                }
            }
        )
    }

    private fun submit() {
        if (state.value.isSubmitting) return

        hasAttemptedStepOne = true

        // The clock moves while the form is open, so a time that passed step one can be stale by
        // now. Its error belongs to a field on step one, so send the user back to where it shows.
        if (!validateFormInputs()) {
            _state.update { it.copy(currentStep = 1) }
            return
        }

        _state.update { it.copy(isSubmitting = true, submitError = null) }

        viewModelScope.launch {
            val currentState = state.value

            val date = currentState.hangoutDate ?: run {
                _state.update { it.copy(isSubmitting = false) }
                return@launch
            }
            val time = currentState.hangoutTime ?: run {
                _state.update { it.copy(isSubmitting = false) }
                return@launch
            }

            val hangoutName = _state.value.hangoutNameTextState.text.toString().trim()
            val hangoutDescription = _state.value.hangoutDescriptionTextState.text.toString()
                .trim()
                .takeIf { it.isNotBlank() }
            val hangoutScheduledAt =
                LocalDateTime(date, time).toInstant(TimeZone.currentSystemDefault())

            val result = if (currentState.originalHangout != null) {
                hangoutService.updateHangout(
                    hangoutId = currentState.originalHangout.id,
                    name = hangoutName,
                    description = hangoutDescription,
                    vibe = currentState.hangoutVibe,
                    scheduledAt = hangoutScheduledAt,
                    maxAttendees = currentState.maxAttendees,
                    spotId = if (currentState.isVotingMode) null else currentState.selectedSpot?.id
                )
            } else {
                hangoutService.createHangout(
                    name = hangoutName,
                    description = hangoutDescription,
                    vibe = currentState.hangoutVibe,
                    scheduledAt = hangoutScheduledAt,
                    maxAttendees = currentState.maxAttendees,
                    spotId = if (currentState.isVotingMode) null else currentState.selectedSpot?.id
                )
            }

            result
                .onSuccess { hangout ->
                    _state.update { it.copy(isSubmitting = false) }
                    eventChannel.send(CreateHangoutEvent.Success(hangout.id))
                }
                .onFailure { error ->
                    _state.update { it.copy(isSubmitting = false, submitError = error.toUiText()) }
                }
        }
    }

    private fun validateFormInputs(): Boolean {
        val currentState = state.value
        val (currentDate, currentTime) = getCurrentDateAndTime()

        val hangoutNameState =
            HangoutNameValidator.validate(_state.value.hangoutNameTextState.text.toString())
        val hangoutDescriptionState =
            HangoutDescriptionValidator.validate(_state.value.hangoutDescriptionTextState.text.toString())
        val hangoutDateState = HangoutDateValidator.validate(
            selectedDate = currentState.hangoutDate,
            today = currentDate
        )
        val hangoutTimeState = HangoutTimeValidator.validate(
            selectedTime = currentState.hangoutTime,
            selectedDate = currentState.hangoutDate,
            currentDate = currentDate,
            currentTime = currentTime
        )

        _state.update {
            it.copy(
                hangoutNameError = hangoutNameState.toUiText(),
                hangoutDescriptionError = hangoutDescriptionState.toUiText(),
                hangoutDateError = hangoutDateState.toUiText(),
                hangoutTimeError = hangoutTimeState.toUiText()
            )
        }

        return hangoutNameState == HangoutNameValidationState.VALID &&
                hangoutDescriptionState == HangoutDescriptionValidationState.VALID &&
                hangoutDateState == HangoutDateValidationState.VALID &&
                hangoutTimeState == HangoutTimeValidationState.VALID
    }

    private fun loadNextSpotSearchPage() {
        viewModelScope.launch {
            spotSearchPaginator?.loadNextItems()
        }
    }

    private fun loadNextFavoriteSpotSearchPage() {
        viewModelScope.launch {
            favoriteSpotSearchPaginator?.loadNextItems()
        }
    }

    private fun getCurrentDateAndTime(): Pair<LocalDate, LocalTime> {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        return Pair(now.date, now.time)
    }
}