package com.eeseka.lynk.hangouts.presentation.hangouts_list

import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eeseka.lynk.hangouts.presentation.mappers.toHangoutStatus
import com.eeseka.lynk.hangouts.presentation.model.HangoutStatusFilter
import com.eeseka.lynk.shared.domain.auth.AuthService
import com.eeseka.lynk.shared.domain.auth.SessionStorage
import com.eeseka.lynk.shared.domain.auth.model.User
import com.eeseka.lynk.shared.domain.hangout.HangoutService
import com.eeseka.lynk.shared.domain.hangout.model.HangoutSummary
import com.eeseka.lynk.shared.domain.hangout.model.HangoutVibe
import com.eeseka.lynk.shared.domain.util.DataErrorException
import com.eeseka.lynk.shared.domain.util.Paginator
import com.eeseka.lynk.shared.domain.util.onFailure
import com.eeseka.lynk.shared.domain.util.onSuccess
import com.eeseka.lynk.shared.presentation.hangout.mappers.toHangoutSummaryUi
import com.eeseka.lynk.shared.presentation.util.toUiText
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
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

class HangoutsListViewModel(
    private val hangoutService: HangoutService,
    private val sessionStorage: SessionStorage,
    private val authService: AuthService
) : ViewModel() {

    private val eventChannel = Channel<HangoutsListEvent>()
    val events = eventChannel.receiveAsFlow()

    private val _state = MutableStateFlow(HangoutsListState())
    private var hasLoadedInitialData = false

    private var hangoutsPaginator: Paginator<String?, HangoutSummary>? = null

    val state = _state
        .onStart {
            if (!hasLoadedInitialData) {
                val authInfo = sessionStorage.observeAuthInfo().firstOrNull()
                _state.update {
                    it.copy(
                        isGuest = authInfo?.user is User.Guest,
                        currentUserId = authInfo?.user?.id
                    )
                }
                observeFilters()
                hasLoadedInitialData = true
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = HangoutsListState()
        )

    fun onAction(action: HangoutsListAction) {
        when (action) {
            is HangoutsListAction.OnSelectHangout -> {
                _state.update { it.copy(selectedHangoutId = action.hangoutId) }
            }

            is HangoutsListAction.OnStatusFilterSelected -> {
                _state.update { it.copy(selectedStatusFilter = action.filter) }
            }

            is HangoutsListAction.OnVibeSelected -> {
                _state.update { it.copy(selectedVibe = action.vibe) }
            }

            HangoutsListAction.LoadNextPage -> loadNextPage()
            HangoutsListAction.Refresh -> refresh()
            HangoutsListAction.SignOutGuest -> signOutGuest()
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

    @OptIn(FlowPreview::class)
    private fun observeFilters() {
        val searchQueryFlow = snapshotFlow { _state.value.searchTextState.text.toString() }
            .debounce { query -> if (query.isBlank()) 0.milliseconds else 500.milliseconds }
            .distinctUntilChanged()

        val statusFilterFlow = state.map { it.selectedStatusFilter }.distinctUntilChanged()
        val vibeFlow = state.map { it.selectedVibe }.distinctUntilChanged()

        combine(searchQueryFlow, statusFilterFlow, vibeFlow) { query, statusFilter, vibe ->
            rebuildAndReload(query.takeIf { it.isNotBlank() }, statusFilter, vibe)
        }.launchIn(viewModelScope)
    }

    private fun refresh() {
        rebuildAndReload(
            query = _state.value.searchTextState.text.toString().takeIf { it.isNotBlank() },
            statusFilter = state.value.selectedStatusFilter,
            vibe = state.value.selectedVibe
        )
    }

    // Re-fetches page 1 with the given filters, wiping the current list.
    private fun rebuildAndReload(
        query: String?,
        statusFilter: HangoutStatusFilter,
        vibe: HangoutVibe?
    ) {
        setupHangoutsPaginator(query, statusFilter, vibe)
        _state.update {
            it.copy(
                hangouts = emptyList(),
                isSearchEndReached = false,
                searchResetEpoch = it.searchResetEpoch + 1
            )
        }
        loadNextPage()
    }

    private fun setupHangoutsPaginator(
        query: String?,
        statusFilter: HangoutStatusFilter,
        vibe: HangoutVibe?
    ) {
        hangoutsPaginator = Paginator(
            initialKey = null,
            onLoadUpdated = { isLoading ->
                _state.update { it.copy(isSearchLoading = isLoading) }
            },
            onRequest = { beforeTimestamp ->
                hangoutService.getHangouts(
                    query = query,
                    status = statusFilter.toHangoutStatus(),
                    vibe = vibe,
                    before = beforeTimestamp
                )
            },
            getNextKey = { hangoutSummaries ->
                hangoutSummaries.minOfOrNull { it.createdAt }?.toString()
            },
            onError = { throwable ->
                if (throwable is DataErrorException) {
                    eventChannel.send(HangoutsListEvent.Error(throwable.error.toUiText()))
                }
            },
            onSuccess = { newHangouts, _ ->
                _state.update {
                    it.copy(
                        hangouts = it.hangouts + newHangouts.map { hangout -> hangout.toHangoutSummaryUi() },
                        isSearchEndReached = newHangouts.isEmpty()
                    )
                }
            }
        )
    }

    private fun loadNextPage() {
        viewModelScope.launch {
            hangoutsPaginator?.loadNextItems()
        }
    }
}