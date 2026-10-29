package com.eeseka.lynk.assistant.presentation

import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eeseka.lynk.assistant.presentation.mappers.toAssistantMessageUi
import com.eeseka.lynk.assistant.presentation.mappers.toAssistantRecommendationUi
import com.eeseka.lynk.assistant.presentation.mappers.toAssistantUiText
import com.eeseka.lynk.assistant.presentation.model.AssistantMessageUi
import com.eeseka.lynk.shared.domain.assistant.AssistantMessageCountRepository
import com.eeseka.lynk.shared.domain.assistant.AssistantService
import com.eeseka.lynk.shared.domain.assistant.DiscoverChatRepository
import com.eeseka.lynk.shared.domain.assistant.model.AssistantMessage
import com.eeseka.lynk.shared.domain.assistant.model.AssistantMessageRole
import com.eeseka.lynk.shared.domain.assistant.model.AssistantMessageStatus
import com.eeseka.lynk.shared.domain.assistant.model.AssistantStreamEvent
import com.eeseka.lynk.shared.domain.auth.SessionStorage
import com.eeseka.lynk.shared.domain.hangout.HangoutDetailRepository
import com.eeseka.lynk.shared.domain.hangout.model.RsvpStatus
import com.eeseka.lynk.shared.domain.lifecycle.AppLifecycleObserver
import com.eeseka.lynk.shared.domain.lobby.BallotRepository
import com.eeseka.lynk.shared.domain.lobby.LobbyConnectionClient
import com.eeseka.lynk.shared.domain.lobby.LobbyService
import com.eeseka.lynk.shared.domain.lobby.model.ConnectionState
import com.eeseka.lynk.shared.domain.lobby.model.LobbyEvent
import com.eeseka.lynk.shared.domain.location.LocationCoordinates
import com.eeseka.lynk.shared.domain.spot.SpotService
import com.eeseka.lynk.shared.domain.spot.model.Spot
import com.eeseka.lynk.shared.domain.util.DataError
import com.eeseka.lynk.shared.domain.util.DataErrorException
import com.eeseka.lynk.shared.domain.util.Paginator
import com.eeseka.lynk.shared.domain.util.onFailure
import com.eeseka.lynk.shared.domain.util.onSuccess
import com.eeseka.lynk.shared.presentation.spot.mappers.toSpotUi
import com.eeseka.lynk.shared.presentation.util.toUiText
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableSet
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.uuid.Uuid

class AssistantViewModel(
    private val assistantService: AssistantService,
    private val assistantMessageCountRepository: AssistantMessageCountRepository,
    private val discoverChatRepository: DiscoverChatRepository,
    private val hangoutDetailRepository: HangoutDetailRepository,
    private val ballotRepository: BallotRepository,
    private val spotService: SpotService,
    private val connectionClient: LobbyConnectionClient,
    private val lobbyService: LobbyService,
    private val sessionStorage: SessionStorage,
    private val appLifecycleObserver: AppLifecycleObserver
) : ViewModel() {
    private val eventChannel = Channel<AssistantEvent>()
    val events = eventChannel.receiveAsFlow()

    private val _state = MutableStateFlow(AssistantState())
    private var hasLoadedInitialData = false

    private var messagesPaginator: Paginator<String?, AssistantMessage>? = null

    private val saveJobs = mutableMapOf<String, Job>()
    private val refreshedCoverSpotIds = mutableSetOf<String>()

    val state = _state
        .onStart {
            if (!hasLoadedInitialData) {
                viewModelScope.launch { refreshUsage() }
                hasLoadedInitialData = true
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = AssistantState()
        )

    fun onAction(action: AssistantAction) {
        when (action) {
            is AssistantAction.OnOpenHangoutChat -> openHangoutChat(action.hangoutId)
            is AssistantAction.OnOpenDiscoverChat -> openDiscoverChat(
                latitude = action.latitude,
                longitude = action.longitude,
                areaName = action.areaName
            )

            AssistantAction.OnSendClick -> sendTypedQuestion()
            is AssistantAction.OnStarterPromptClick -> askQuestion(action.prompt)
            is AssistantAction.OnRetryAnswerClick -> retryAnswer(action.answerId)
            AssistantAction.LoadNextPage -> loadNextPage()
            AssistantAction.OnRetryClick -> retryLoad()
            AssistantAction.OnCheckPendingAnswerClick -> reloadMessages()
            AssistantAction.OnClearChatConfirmed -> clearChat()
            AssistantAction.OnNewChatClick -> startNewChat()
            is AssistantAction.OnRecommendationClick -> openSpot(action.spotId)
            is AssistantAction.OnCoverPhotoLoadFailed -> refreshCoverPhoto(action.spotId)
            AssistantAction.OnDismissSpotSheet -> _state.update { it.copy(selectedSpot = null) }
            is AssistantAction.OnToggleSaveSpot -> toggleSaveSpot(
                action.spotId,
                action.isCurrentlySaved
            )

            is AssistantAction.OnSuggestSpot -> suggestSpot(action.spotId)
        }
    }

    private fun openHangoutChat(hangoutId: String) {
        if (hangoutId == _state.value.hangoutId) return
        _state.update { it.copy(hangoutId = hangoutId) }

        observeHangout(hangoutId)
        observeBallotSpotIds(hangoutId)
        observeCenter(hangoutId)
        observePendingSuggestions()
        observeSuggestionErrors()
        observeAppReturning()
        setupMessagesPaginator(hangoutId)
        loadNextPage()
    }

    private fun openDiscoverChat(latitude: Double, longitude: Double, areaName: String?) {
        if (_state.value.searchCenter != null) return
        val discoverChatId = discoverChatRepository.getChatId()
        _state.update {
            it.copy(
                searchCenter = LocationCoordinates(latitude, longitude),
                areaName = areaName,
                discoverChatId = discoverChatId,
                isEndReached = discoverChatId == null
            )
        }

        observeAppReturning()
        if (discoverChatId != null) loadDiscoverMessages(discoverChatId)
    }

    // Another device on this account may have cleared the chat or asked in it while this one was away.
    // The first value is skipped, as the chat has just loaded.
    private fun observeAppReturning() {
        appLifecycleObserver.isInForeground
            .distinctUntilChanged()
            .drop(1)
            .onEach { isInForeground ->
                if (isInForeground && !_state.value.isAnswering) refreshHeldMessages()
            }
            .launchIn(viewModelScope)
    }

    private fun observeHangout(hangoutId: String) {
        combine(
            hangoutDetailRepository.observeHangout(hangoutId),
            sessionStorage.observeAuthInfo().map { it?.user?.id }
        ) { hangout, currentUserId ->
            _state.update {
                it.copy(
                    hangoutName = hangout?.name,
                    hangoutStatus = hangout?.status,
                    isAttending = hangout?.participants?.any { participant ->
                        participant.user.userId == currentUserId && participant.rsvpStatus == RsvpStatus.ATTENDING
                    }
                )
            }
        }.launchIn(viewModelScope)
    }

    private fun observeCenter(hangoutId: String) {
        ballotRepository
            .observeCenter(hangoutId)
            .onEach { center -> _state.update { it.copy(center = center) } }
            .launchIn(viewModelScope)
    }

    private fun observeBallotSpotIds(hangoutId: String) {
        ballotRepository
            .observeCandidates(hangoutId)
            .onEach { candidates ->
                val ballotSpotIds = candidates.map { it.id }.toSet()
                val suggestedSpotIds = _state.value.suggestingSpotIds.filter { it in ballotSpotIds }

                _state.update {
                    it.copy(
                        ballotSpotIds = ballotSpotIds.toImmutableSet(),
                        suggestingSpotIds = (it.suggestingSpotIds - ballotSpotIds).toImmutableSet()
                    )
                }
                if (suggestedSpotIds.isNotEmpty()) {
                    eventChannel.send(AssistantEvent.SpotSuggested)
                }
            }
            .launchIn(viewModelScope)
    }

    private fun observePendingSuggestions() {
        connectionClient
            .connectionState
            .onEach { connectionState ->
                if (connectionState != ConnectionState.CONNECTED) {
                    _state.update { it.copy(suggestingSpotIds = persistentSetOf()) }
                }
            }
            .launchIn(viewModelScope)
    }

    private fun observeSuggestionErrors() {
        connectionClient.events
            .onEach { event ->
                if (event is LobbyEvent.LobbyError && _state.value.suggestingSpotIds.isNotEmpty()) {
                    _state.update { it.copy(suggestingSpotIds = persistentSetOf()) }
                    eventChannel.send(AssistantEvent.Error(event.toUiText()))
                }
            }
            .launchIn(viewModelScope)
    }

    private fun sendTypedQuestion() {
        val question = _state.value.messageTextState.text.toString().trim()
        _state.value.messageTextState.clearText()
        askQuestion(question)
    }

    private fun retryAnswer(answerId: String) {
        // Newest first, so the question a failed answer belongs to sits right after it
        val messages = _state.value.messages
        val answerIndex = messages.indexOfFirst { it.id == answerId }
        if (answerIndex == -1) return
        val question = messages.getOrNull(answerIndex + 1)
            ?.takeIf { it.role == AssistantMessageRole.USER }
            ?: return

        askQuestion(question.content)
    }

    private fun askQuestion(question: String) {
        val questionMessage = AssistantMessageUi(
            id = Uuid.random().toString(),
            role = AssistantMessageRole.USER,
            status = null,
            content = question,
            failureReason = null,
            recommendations = persistentListOf()
        )
        _state.update {
            it.copy(
                isAnswering = true,
                answerStatus = null,
                messages = (listOf(questionMessage) + it.messages).toImmutableList()
            )
        }

        viewModelScope.launch {
            var answerId: String? = null

            assistantService
                .sendMessage(
                    hangoutId = _state.value.hangoutId,
                    discoverChatId = _state.value.discoverChatId,
                    latitude = _state.value.searchCenter?.latitude,
                    longitude = _state.value.searchCenter?.longitude,
                    content = question
                ) { event ->
                    if (event is AssistantStreamEvent.Started) {
                        answerId = event.messageId
                        startAnswer(event.messageId, event.discoverChatId)
                    } else {
                        answerId?.let { applyStreamEvent(it, event) }
                    }
                }
                .onSuccess {
                    val endedAnswerId = answerId ?: return@onSuccess
                    val answer = _state.value.messages.find { it.id == endedAnswerId }
                    // The stream closed without saying the answer was done, so it never fully arrived
                    if (answer?.status == AssistantMessageStatus.PENDING) {
                        reloadMessages()
                    }
                }
                .onFailure { error ->
                    val lostAnswerId = answerId
                    if (lostAnswerId == null) {
                        handleRefusedQuestion(question, questionMessage.id, error)
                        return@onFailure
                    }
                    // A connection that breaks after the answer said it was done, or failed, has lost nothing.
                    val answer = _state.value.messages.find { it.id == lostAnswerId }
                    if (answer?.status == AssistantMessageStatus.PENDING) {
                        reloadMessages()
                    }
                }

            _state.update { it.copy(isAnswering = false) }
            refreshUsage()
        }
    }

    private fun startAnswer(answerId: String, discoverChatId: String?) {
        val answer = AssistantMessageUi(
            id = answerId,
            role = AssistantMessageRole.ASSISTANT,
            status = AssistantMessageStatus.PENDING,
            content = "",
            failureReason = null,
            recommendations = persistentListOf()
        )
        discoverChatId?.let { discoverChatRepository.setChatId(it) }
        _state.update {
            it.copy(
                discoverChatId = discoverChatId ?: it.discoverChatId,
                messages = (listOf(answer) + it.messages).toImmutableList()
            )
        }
    }

    private fun applyStreamEvent(answerId: String, event: AssistantStreamEvent) {
        when (event) {
            is AssistantStreamEvent.Status -> _state.update { it.copy(answerStatus = event.kind) }

            is AssistantStreamEvent.Text -> updateMessage(answerId) {
                it.copy(content = it.content + event.text)
            }

            is AssistantStreamEvent.Spots -> updateMessage(answerId) {
                it.copy(recommendations = event.recommendations.map { recommendation -> recommendation.toAssistantRecommendationUi() }.toImmutableList())
            }

            AssistantStreamEvent.Done -> updateMessage(answerId) {
                it.copy(status = AssistantMessageStatus.ANSWERED)
            }

            is AssistantStreamEvent.Error -> {
                updateMessage(answerId) {
                    it.copy(
                        status = AssistantMessageStatus.FAILED,
                        content = "",
                        failureReason = event.reason
                    )
                }
                sendAnswerEvent(AssistantEvent.AnswerFailed)
            }

            is AssistantStreamEvent.Started -> Unit
        }
    }

    private suspend fun handleRefusedQuestion(
        question: String,
        questionId: String,
        error: DataError.Remote
    ) {
        val isHangoutChat = _state.value.hangoutId != null
        _state.update { it.copy(messages = it.messages.filterNot { message -> message.id == questionId }.toImmutableList()) }
        if (_state.value.messageTextState.text.isEmpty()) {
            _state.value.messageTextState.setTextAndPlaceCursorAtEnd(question)
        }

        when (error) {
            DataError.Remote.TOO_MANY_REQUESTS -> Unit
            DataError.Remote.NOT_FOUND if !isHangoutChat -> {
                discoverChatRepository.setChatId(null)
                _state.update { it.copy(discoverChatId = null) }
                eventChannel.send(AssistantEvent.ChatExpired)
            }

            else -> {
                // Voting closed, the user left, or another answer is still being written; the hangout says which
                _state.value.hangoutId?.let { hangoutId ->
                    if (error == DataError.Remote.CONFLICT || error == DataError.Remote.FORBIDDEN) {
                        hangoutDetailRepository.refreshHangout(hangoutId)
                        reloadMessages()
                    }
                }
                eventChannel.send(AssistantEvent.Error(error.toAssistantUiText(isHangoutChat)))
            }
        }
    }

    private fun clearChat() {
        val hangoutId = _state.value.hangoutId ?: return
        viewModelScope.launch {
            _state.update { it.copy(isClearingChat = true) }
            assistantService
                .clearHangoutChat(hangoutId)
                .onSuccess {
                    messagesPaginator?.close()
                    _state.update {
                        it.copy(
                            messages = persistentListOf(),
                            isEndReached = true,
                            messagesResetEpoch = it.messagesResetEpoch + 1,
                            isClearingChat = false
                        )
                    }
                    assistantMessageCountRepository.refreshMessageCount(hangoutId)
                    eventChannel.send(AssistantEvent.ChatCleared)
                }
                .onFailure { error ->
                    _state.update { it.copy(isClearingChat = false) }
                    if (error == DataError.Remote.CONFLICT) reloadMessages() // A conflict means an answer is still being written, which the reread shows
                    eventChannel.send(AssistantEvent.Error(error.toAssistantUiText(isHangoutChat = true)))
                }
        }
    }

    private fun startNewChat() {
        if (_state.value.isAnswering) return
        discoverChatRepository.setChatId(null)
        _state.update {
            it.copy(
                discoverChatId = null,
                messages = persistentListOf(),
                isEndReached = true,
                loadError = null,
                messagesResetEpoch = it.messagesResetEpoch + 1
            )
        }
    }

    private fun openSpot(spotId: String) {
        viewModelScope.launch {
            _state.update { it.copy(loadingSpotId = spotId) }
            spotService
                .getSpotDetails(spotId)
                .onSuccess { spot ->
                    replaceCardSpot(spot)
                    _state.update { it.copy(selectedSpot = spot.toSpotUi(), loadingSpotId = null) }
                }
                .onFailure { error ->
                    _state.update { it.copy(loadingSpotId = null) }
                    eventChannel.send(AssistantEvent.Error(error.toUiText()))
                }
        }
    }

    // Google expires photo names, so a card cover that fails to load gets the place fetched afresh, once
    private fun refreshCoverPhoto(spotId: String) {
        val isFirstAttempt = refreshedCoverSpotIds.add(spotId)
        if (!isFirstAttempt) return

        viewModelScope.launch {
            spotService
                .getSpotDetails(spotId)
                .onSuccess { spot -> replaceCardSpot(spot) }
        }
    }

    private fun replaceCardSpot(spot: Spot) {
        _state.update { currentState ->
            currentState.copy(
                messages = currentState.messages.map { message ->
                    if (message.recommendations.none { it.spot.id == spot.id }) return@map message
                    message.copy(
                        recommendations = message.recommendations.map { recommendation ->
                            if (recommendation.spot.id == spot.id) {
                                recommendation.copy(spot = spot.toSpotUi().copy(isSaved = recommendation.spot.isSaved))
                            } else recommendation
                        }.toImmutableList()
                    )
                }.toImmutableList()
            )
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
                eventChannel.send(AssistantEvent.Error(error.toUiText()))
            }

            saveJobs.remove(spotId)
        }
    }

    private fun updateSpotSaveState(spotId: String, isSaved: Boolean) {
        _state.update { currentState ->
            val selectedSpot = currentState.selectedSpot
            currentState.copy(
                selectedSpot = if (selectedSpot?.id == spotId) selectedSpot.copy(isSaved = isSaved) else selectedSpot,
                messages = currentState.messages.map { message ->
                    if (message.recommendations.none { it.spot.id == spotId }) return@map message
                    message.copy(
                        recommendations = message.recommendations.map { recommendation ->
                            if (recommendation.spot.id == spotId) {
                                recommendation.copy(spot = recommendation.spot.copy(isSaved = isSaved))
                            } else recommendation
                        }.toImmutableList()
                    )
                }.toImmutableList()
            )
        }
    }

    private fun suggestSpot(spotId: String) {
        val hangoutId = _state.value.hangoutId ?: return
        viewModelScope.launch {
            _state.update {
                it.copy(suggestingSpotIds = (it.suggestingSpotIds + spotId).toImmutableSet())
            }
            lobbyService
                .proposeSpot(hangoutId, spotId)
                .onFailure { error ->
                    _state.update {
                        it.copy(suggestingSpotIds = (it.suggestingSpotIds - spotId).toImmutableSet())
                    }
                    eventChannel.send(AssistantEvent.Error(error.toUiText()))
                }
        }
    }

    private fun retryLoad() {
        _state.update { it.copy(loadError = null) }
        if (_state.value.hangoutId == null) {
            _state.value.discoverChatId?.let { loadDiscoverMessages(it) }
        } else {
            loadNextPage()
        }
    }

    private fun refreshHeldMessages() {
        val hangoutId = _state.value.hangoutId
        if (hangoutId == null) {
            _state.value.discoverChatId?.let { loadDiscoverMessages(it, showLoading = false) }
            return
        }
        if (_state.value.messages.isEmpty()) {
            reloadMessages()
            return
        }

        viewModelScope.launch {
            assistantService.getHangoutMessages(hangoutId = hangoutId)
                .onSuccess { newestMessages ->
                    // A question asked meanwhile is newer than this page, so the page is dropped
                    if (_state.value.isAnswering) return@onSuccess

                    setupMessagesPaginator(
                        hangoutId = hangoutId,
                        initialKey = newestMessages.minOfOrNull { it.createdAt }?.toString()
                    )
                    _state.update {
                        it.copy(
                            messages = newestMessages.map { message -> message.toAssistantMessageUi() }.toImmutableList(),
                            isEndReached = newestMessages.isEmpty(),
                            messagesResetEpoch = it.messagesResetEpoch + 1
                        )
                    }
                }
        }
    }

    private fun reloadMessages() {
        val hangoutId = _state.value.hangoutId
        if (hangoutId == null) {
            _state.value.discoverChatId?.let { loadDiscoverMessages(it) }
            return
        }

        _state.update {
            it.copy(
                messages = persistentListOf(),
                isEndReached = false,
                loadError = null,
                messagesResetEpoch = it.messagesResetEpoch + 1
            )
        }
        setupMessagesPaginator(hangoutId)
        loadNextPage()
    }

    private fun setupMessagesPaginator(hangoutId: String, initialKey: String? = null) {
        messagesPaginator?.close()
        messagesPaginator = Paginator(
            initialKey = initialKey,
            onLoadUpdated = { isLoading ->
                _state.update { it.copy(isLoading = isLoading) }
            },
            onRequest = { beforeTimestamp ->
                assistantService.getHangoutMessages(hangoutId = hangoutId, before = beforeTimestamp)
            },
            getNextKey = { messages ->
                messages.minOfOrNull { it.createdAt }?.toString()
            },
            onError = { throwable ->
                if (throwable is DataErrorException) {
                    val message = throwable.error.toUiText()

                    if (_state.value.messages.isEmpty()) {
                        _state.update { it.copy(loadError = message) }
                    } else {
                        eventChannel.send(AssistantEvent.Error(message))
                    }
                }
            },
            onSuccess = { olderMessages, _ ->
                _state.update {
                    it.copy(
                        messages = (it.messages + olderMessages.map { message -> message.toAssistantMessageUi() }).toImmutableList(),
                        isEndReached = olderMessages.isEmpty(),
                        loadError = null
                    )
                }
            }
        )
    }

    private fun loadDiscoverMessages(discoverChatId: String, showLoading: Boolean = true) {
        viewModelScope.launch {
            if (showLoading) _state.update { it.copy(isLoading = true) }
            val result = assistantService.getDiscoverMessages(discoverChatId)
            if (_state.value.discoverChatId != discoverChatId) {
                _state.update { it.copy(isLoading = false) }
                return@launch
            }

            result
                .onSuccess { messages ->
                    _state.update {
                        it.copy(
                            messages = messages.map { message -> message.toAssistantMessageUi() }.toImmutableList(),
                            isLoading = false,
                            isEndReached = true,
                            loadError = null
                        )
                    }
                }
                .onFailure { error ->
                    val wasShowingMessages = _state.value.messages.isNotEmpty()
                    when {
                        error == DataError.Remote.NOT_FOUND -> {
                            discoverChatRepository.setChatId(null)
                            _state.update {
                                it.copy(
                                    discoverChatId = null,
                                    messages = persistentListOf(),
                                    isLoading = false,
                                    isEndReached = true,
                                    messagesResetEpoch = it.messagesResetEpoch + 1
                                )
                            }
                            // Reopening after it expired just starts afresh; losing the one on screen is worth saying
                            if (wasShowingMessages) eventChannel.send(AssistantEvent.ChatExpired)
                        }

                        wasShowingMessages -> {
                            _state.update { it.copy(isLoading = false) }
                            eventChannel.send(AssistantEvent.Error(error.toUiText()))
                        }

                        else -> _state.update { it.copy(isLoading = false, loadError = error.toUiText()) }
                    }
                }
        }
    }

    private fun loadNextPage() {
        viewModelScope.launch {
            messagesPaginator?.loadNextItems()
        }
    }

    private fun updateMessage(
        messageId: String,
        change: (AssistantMessageUi) -> AssistantMessageUi
    ) {
        _state.update {
            it.copy(
                messages = it.messages.map { message ->
                    if (message.id == messageId) change(message) else message
                }.toImmutableList()
            )
        }
    }

    private fun sendAnswerEvent(event: AssistantEvent) {
        viewModelScope.launch { eventChannel.send(event) }
    }

    private suspend fun refreshUsage() {
        assistantService
            .getUsage()
            .onSuccess { usage ->
                _state.update {
                    it.copy(
                        messagesLeft = (usage.limit - usage.used).coerceAtLeast(0),
                        messageLimit = usage.limit,
                        usageResetsAt = usage.resetsInSeconds?.let { seconds -> Clock.System.now() + seconds.seconds }
                    )
                }
            }
    }
}
