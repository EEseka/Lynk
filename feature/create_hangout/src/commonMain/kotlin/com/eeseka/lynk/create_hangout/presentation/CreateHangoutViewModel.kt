package com.eeseka.lynk.create_hangout.presentation

import androidx.compose.foundation.text.input.clearText
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
import com.eeseka.lynk.create_hangout.presentation.model.HangoutFormMode
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
import com.eeseka.lynk.shared.presentation.util.UiText
import com.eeseka.lynk.shared.presentation.util.toUiText
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
import lynk.feature.create_hangout.generated.resources.Res
import lynk.feature.create_hangout.generated.resources.error_hangout_date_missing
import lynk.feature.create_hangout.generated.resources.error_hangout_date_past
import lynk.feature.create_hangout.generated.resources.error_hangout_description_too_long
import lynk.feature.create_hangout.generated.resources.error_hangout_name_blank
import lynk.feature.create_hangout.generated.resources.error_hangout_name_too_long
import lynk.feature.create_hangout.generated.resources.error_hangout_time_missing
import lynk.feature.create_hangout.generated.resources.error_hangout_time_past
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
                observeValidationStates()
                observeSearchFilters()
                hasLoadedInitialData = true
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = CreateHangoutState()
        )

    // Blank-check only — real validation (too long) runs on Next press via validateFormInputs()
    private val isHangoutNameValidFlow =
        snapshotFlow { _state.value.hangoutNameTextState.text.toString() }
            .map { it.isNotBlank() }
            .distinctUntilChanged()
    // Description is optional — TOO_LONG caught on Next press, never blocks the button

    // Null-check only — real validation (past date/time) runs on Next press via validateFormInputs()
    private val isHangoutDateValidFlow = state.map { it.hangoutDate != null }.distinctUntilChanged()
    private val isHangoutTimeValidFlow = state.map { it.hangoutTime != null }.distinctUntilChanged()

    private val isStepTwoValidFlow = state.map {
        it.isVotingMode || it.selectedSpot != null
    }.distinctUntilChanged()

    private val isBusyFlow = state.map { it.isSubmitting }.distinctUntilChanged()

    private fun observeValidationStates() {
        val stepOneValidFlow = combine(
            isHangoutNameValidFlow,
            isHangoutDateValidFlow,
            isHangoutTimeValidFlow
        ) { isNameValid, isDateValid, isTimeValid ->
            isNameValid && isDateValid && isTimeValid
        }.distinctUntilChanged()

        combine(
            stepOneValidFlow,
            isStepTwoValidFlow,
            isBusyFlow
        ) { isStepOneValid, isStepTwoValid, isBusy ->
            _state.update {
                it.copy(
                    canProceedToStepTwo = isStepOneValid,
                    canProceedToStepThree = isStepTwoValid,
                    canSubmit = isStepOneValid && isStepTwoValid && !isBusy
                )
            }
        }.launchIn(viewModelScope)
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

            is CreateHangoutAction.OnDateSelected -> {
                _state.update { it.copy(hangoutDate = action.date) }
            }

            is CreateHangoutAction.OnTimeSelected -> {
                _state.update { it.copy(hangoutTime = action.time) }
            }

            is CreateHangoutAction.OnSpotSelected -> {
                _state.update { it.copy(selectedSpot = action.spot) }
            }

            is CreateHangoutAction.OnLocationModeChanged -> {
                _state.update { it.copy(isVotingMode = action.isVotingMode) }
            }

            CreateHangoutAction.OnNextStep -> {
                if (state.value.currentStep == 1 && state.value.canProceedToStepTwo) {
                    // Real date/time validation (past checks) runs here and surfaces errors if needed
                    if (validateFormInputs()) _state.update { it.copy(currentStep = 2) }
                } else if (state.value.currentStep == 2 && state.value.canProceedToStepThree) {
                    _state.update { it.copy(currentStep = 3) }
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
            CreateHangoutAction.OnSearchQueryCleared -> _state.value.spotSearchTextState.clearText()
            CreateHangoutAction.OnSubmitClick -> submit()
        }
    }

    private fun initCreateMode(spot: SpotUi?) {
        if (state.value.selectedSpot != null) return // Already initialized

        _state.update {
            it.copy(
                mode = HangoutFormMode.CREATE,
                selectedSpot = spot,
                isVotingMode = spot == null, // If passed a spot, default to dictator
            )
        }
    }

    private fun initEditMode(hangout: HangoutUi) {
        if (state.value.originalHangout != null) return // Already initialized

        _state.value.hangoutNameTextState.setTextAndPlaceCursorAtEnd(hangout.name)
        _state.value.hangoutDescriptionTextState.setTextAndPlaceCursorAtEnd(
            hangout.description ?: ""
        )

        val localDateTime = hangout.scheduledAt.toLocalDateTime(TimeZone.currentSystemDefault())
        val activeCount = hangout.participants.count {
            it.rsvpStatus == RsvpStatus.ATTENDING || it.rsvpStatus == RsvpStatus.PENDING
        }
        _state.update {
            it.copy(
                mode = HangoutFormMode.EDIT,
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
                            trendingSpots = spots.map { spot -> spot.toSpotUi() }
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
            .distinctUntilChanged()
            .debounce { query -> if (query.isBlank()) 0.milliseconds else 500.milliseconds }

        val tabFlow = state.map { it.activeSearchTab }.distinctUntilChanged()

        combine(searchQueryFlow, tabFlow) { query, activeTab ->
            query to activeTab
        }.mapLatest { (query, activeTab) ->
            when (activeTab) {
                SearchTab.FAVORITES -> {
                    setupFavoriteSpotSearchPaginator(query.takeIf { it.isNotBlank() })
                    _state.update {
                        it.copy(
                            favoriteSpotSearchResults = emptyList(),
                            favoriteSpotSearchEndReached = false,
                            favoriteSearchResetEpoch = it.favoriteSearchResetEpoch + 1
                        )
                    }
                    favoriteSpotSearchPaginator?.loadNextItems()
                }

                SearchTab.ALL_SPOTS -> {
                    if (query.isBlank()) {
                        _state.update {
                            it.copy(
                                spotSearchResults = emptyList(),
                                spotSearchEndReached = false,
                                isSpotSearchLoading = false,
                                spotSearchResetEpoch = it.spotSearchResetEpoch + 1
                            )
                        }
                    } else {
                        val lat = state.value.userLatitude
                        val lng = state.value.userLongitude
                        if (lat != null && lng != null) {
                            setupSpotSearchPaginator(lat, lng, query)
                            _state.update {
                                it.copy(
                                    spotSearchResults = emptyList(),
                                    spotSearchEndReached = false,
                                    spotSearchResetEpoch = it.spotSearchResetEpoch + 1
                                )
                            }
                            spotSearchPaginator?.loadNextItems()
                        }
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
                        spotSearchResults = it.spotSearchResults + newSpots.map { newSpot -> newSpot.toSpotUi() },
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
                        favoriteSpotSearchResults = it.favoriteSpotSearchResults + favoriteSpots.map { favoriteSpot -> favoriteSpot.toSpotUi() },
                        favoriteSpotSearchEndReached = favoriteSpots.isEmpty(),
                        favoriteSpotSearchError = null
                    )
                }
            }
        )
    }

    private fun submit() {
        if (!validateFormInputs() || state.value.isSubmitting) return

        if (!state.value.isVotingMode && state.value.selectedSpot == null) return

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
            val hangoutDescription =
                _state.value.hangoutDescriptionTextState.text.toString().trim()
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

    private fun clearAllFormErrors() {
        _state.update {
            it.copy(
                hangoutNameError = null,
                hangoutDescriptionError = null,
                hangoutDateError = null,
                hangoutTimeError = null
            )
        }
    }

    private fun validateFormInputs(): Boolean {
        clearAllFormErrors()

        val currentState = state.value

        val hangoutNameState =
            HangoutNameValidator.validate(_state.value.hangoutNameTextState.text.toString())
        val hangoutDescriptionState =
            HangoutDescriptionValidator.validate(_state.value.hangoutDescriptionTextState.text.toString())
        val (currentDate, currentTime) = getCurrentDateAndTime()
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

        val hangoutNameError = when (hangoutNameState) {
            HangoutNameValidationState.BLANK -> UiText.Resource(Res.string.error_hangout_name_blank)
            HangoutNameValidationState.TOO_LONG -> UiText.Resource(Res.string.error_hangout_name_too_long)
            HangoutNameValidationState.VALID -> null
        }

        val hangoutDescriptionError = when (hangoutDescriptionState) {
            HangoutDescriptionValidationState.TOO_LONG -> UiText.Resource(Res.string.error_hangout_description_too_long)
            HangoutDescriptionValidationState.VALID -> null
        }

        val hangoutDateError = when (hangoutDateState) {
            HangoutDateValidationState.MISSING -> UiText.Resource(Res.string.error_hangout_date_missing)
            HangoutDateValidationState.PAST_DATE -> UiText.Resource(Res.string.error_hangout_date_past)
            HangoutDateValidationState.VALID -> null
        }

        val hangoutTimeError = when (hangoutTimeState) {
            HangoutTimeValidationState.MISSING -> UiText.Resource(Res.string.error_hangout_time_missing)
            HangoutTimeValidationState.PAST_TIME -> UiText.Resource(Res.string.error_hangout_time_past)
            HangoutTimeValidationState.VALID -> null
        }

        _state.update {
            it.copy(
                hangoutNameError = hangoutNameError,
                hangoutDescriptionError = hangoutDescriptionError,
                hangoutDateError = hangoutDateError,
                hangoutTimeError = hangoutTimeError
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