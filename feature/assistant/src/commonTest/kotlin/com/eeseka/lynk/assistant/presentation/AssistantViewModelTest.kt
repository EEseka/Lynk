package com.eeseka.lynk.assistant.presentation

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isInstanceOf
import assertk.assertions.isNull
import assertk.assertions.isTrue
import com.eeseka.lynk.shared.data.assistant.InMemoryAssistantMessageCountRepository
import com.eeseka.lynk.shared.data.assistant.InMemoryDiscoverChatRepository
import com.eeseka.lynk.shared.data.hangout.InMemoryHangoutDetailRepository
import com.eeseka.lynk.shared.data.lobby.InMemoryBallotRepository
import com.eeseka.lynk.shared.domain.assistant.model.AssistantFailureReason
import com.eeseka.lynk.shared.domain.assistant.model.AssistantMessage
import com.eeseka.lynk.shared.domain.assistant.model.AssistantMessageRole
import com.eeseka.lynk.shared.domain.assistant.model.AssistantMessageStatus
import com.eeseka.lynk.shared.domain.assistant.model.AssistantRecommendation
import com.eeseka.lynk.shared.domain.assistant.model.AssistantStatusKind
import com.eeseka.lynk.shared.domain.assistant.model.AssistantStreamEvent
import com.eeseka.lynk.shared.domain.assistant.model.AssistantUsage
import com.eeseka.lynk.shared.domain.auth.model.AuthInfo
import com.eeseka.lynk.shared.domain.auth.model.AuthProvider
import com.eeseka.lynk.shared.domain.auth.model.User
import com.eeseka.lynk.shared.domain.hangout.model.HangoutVibe
import com.eeseka.lynk.shared.domain.lobby.model.ConnectionState
import com.eeseka.lynk.shared.domain.location.LocationCoordinates
import com.eeseka.lynk.shared.domain.lobby.model.LobbyEvent
import com.eeseka.lynk.shared.domain.spot.model.Spot
import com.eeseka.lynk.shared.domain.spot.model.SpotCategory
import com.eeseka.lynk.shared.domain.util.DataError
import com.eeseka.lynk.shared.domain.util.Result
import com.eeseka.lynk.testing.collectInBackground
import com.eeseka.lynk.testing.data.FakeAppLifecycleObserver
import com.eeseka.lynk.testing.data.FakeAssistantService
import com.eeseka.lynk.testing.data.FakeHangoutService
import com.eeseka.lynk.testing.data.FakeLobbyConnectionClient
import com.eeseka.lynk.testing.data.FakeLobbyService
import com.eeseka.lynk.testing.data.FakeSessionStorage
import com.eeseka.lynk.testing.data.FakeSpotService
import com.eeseka.lynk.testing.typeText
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class AssistantViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var assistantService: FakeAssistantService
    private lateinit var hangoutService: FakeHangoutService
    private lateinit var spotService: FakeSpotService
    private lateinit var connectionClient: FakeLobbyConnectionClient
    private lateinit var lobbyService: FakeLobbyService
    private lateinit var sessionStorage: FakeSessionStorage
    private lateinit var appLifecycleObserver: FakeAppLifecycleObserver
    private lateinit var hangoutDetailRepository: InMemoryHangoutDetailRepository
    private lateinit var assistantMessageCountRepository: InMemoryAssistantMessageCountRepository
    private lateinit var discoverChatRepository: InMemoryDiscoverChatRepository
    private lateinit var ballotRepository: InMemoryBallotRepository

    // The id FakeHangoutService gives the first hangout it creates
    private val hangoutId = "hangout_1"
    private val lounge = spot(id = "spot_1", name = "The Lounge")
    private val rooftop = spot(id = "spot_2", name = "Rooftop Bar")

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        assistantService = FakeAssistantService()
        hangoutService = FakeHangoutService()
        spotService = FakeSpotService()
        connectionClient = FakeLobbyConnectionClient()
        lobbyService = FakeLobbyService()
        sessionStorage = FakeSessionStorage()
        appLifecycleObserver = FakeAppLifecycleObserver().apply { isInForeground.value = true }
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `usage is read when the chat opens`() = runTest {
        assistantService.usage = AssistantUsage(used = 16, limit = 20, resetsInSeconds = 3600)

        val viewModel = createDiscoverChat()

        assertThat(viewModel.state.value.messagesLeft).isEqualTo(4)
        assertThat(viewModel.state.value.messageLimit).isEqualTo(20)
    }

    @Test
    fun `a discover question streams its answer and cards into the chat`() = runTest {
        val viewModel = createDiscoverChat()
        assistantService.streamEvents = mutableListOf(
            AssistantStreamEvent.Started(messageId = "answer_1", discoverChatId = "chat_1"),
            AssistantStreamEvent.Status(AssistantStatusKind.SEARCHING),
            AssistantStreamEvent.Text("Try "),
            AssistantStreamEvent.Text("these two."),
            AssistantStreamEvent.Spots(listOf(AssistantRecommendation(lounge, "Quiet and close"))),
            AssistantStreamEvent.Done
        )

        ask(viewModel, "Somewhere chill")

        val state = viewModel.state.value
        val (answer, question) = state.messages
        assertThat(question.content).isEqualTo("Somewhere chill")
        assertThat(answer.content).isEqualTo("Try these two.")
        assertThat(answer.status).isEqualTo(AssistantMessageStatus.ANSWERED)
        assertThat(answer.recommendations.map { it.spot.id }).containsExactly("spot_1")
        assertThat(state.discoverChatId).isEqualTo("chat_1")
        assertThat(state.isAnswering).isFalse()
        assertThat(state.messageTextState.text.toString()).isEqualTo("")
    }

    @Test
    fun `a discover question sends the map area it was opened on`() = runTest {
        val viewModel = createDiscoverChat()
        assistantService.streamEvents = answeredStream(discoverChatId = "chat_1")

        ask(viewModel, "Somewhere chill")

        val sent = assistantService.sentMessages.single()
        assertThat(sent.hangoutId).isNull()
        assertThat(sent.latitude).isEqualTo(6.44)
        assertThat(sent.longitude).isEqualTo(3.42)
        assertThat(sent.discoverChatId).isNull()
    }

    @Test
    fun `the next discover question carries the chat id from the first answer`() = runTest {
        val viewModel = createDiscoverChat()
        assistantService.streamEvents = answeredStream(discoverChatId = "chat_1")
        ask(viewModel, "Somewhere chill")

        ask(viewModel, "Something cheaper")

        assertThat(assistantService.sentMessages.map { it.discoverChatId }).containsExactly(null, "chat_1")
    }

    @Test
    fun `a question refused before streaming goes back into the input`() = runTest {
        val viewModel = createDiscoverChat()
        assistantService.refusal = DataError.Remote.SERVICE_UNAVAILABLE

        viewModel.events.test {
            ask(viewModel, "Somewhere chill")

            assertThat(awaitItem()).isInstanceOf(AssistantEvent.Error::class)
            assertThat(viewModel.state.value.messages).isEmpty()
            assertThat(viewModel.state.value.messageTextState.text.toString()).isEqualTo("Somewhere chill")
            assertThat(viewModel.state.value.isAnswering).isFalse()
        }
    }

    @Test
    fun `an expired discover chat starts a new one`() = runTest {
        val viewModel = createDiscoverChat()
        assistantService.streamEvents = answeredStream(discoverChatId = "chat_1")
        ask(viewModel, "Somewhere chill")
        assistantService.refusal = DataError.Remote.NOT_FOUND

        viewModel.events.test {
            ask(viewModel, "Something cheaper")

            assertThat(awaitItem()).isEqualTo(AssistantEvent.ChatExpired)
            assertThat(viewModel.state.value.discoverChatId).isNull()
        }
    }

    @Test
    fun `running out of messages shows the empty count instead of an error`() = runTest {
        val viewModel = createDiscoverChat()
        assistantService.refusal = DataError.Remote.TOO_MANY_REQUESTS
        assistantService.usage = AssistantUsage(used = 20, limit = 20, resetsInSeconds = 600)

        viewModel.events.test {
            ask(viewModel, "Somewhere chill")

            assertThat(viewModel.state.value.messagesLeft).isEqualTo(0)
            assertThat(viewModel.state.value.usageResetsAt != null).isTrue()
            expectNoEvents()
        }
    }

    @Test
    fun `a failed answer keeps the reason the server gave`() = runTest {
        val viewModel = createDiscoverChat()
        assistantService.streamEvents = mutableListOf(
            AssistantStreamEvent.Started("answer_1", "chat_1"),
            AssistantStreamEvent.Error(AssistantFailureReason.TIMEOUT)
        )

        ask(viewModel, "Somewhere chill")

        val answer = viewModel.state.value.messages.first()
        assertThat(answer.status).isEqualTo(AssistantMessageStatus.FAILED)
        assertThat(answer.failureReason).isEqualTo(AssistantFailureReason.TIMEOUT)
    }

    @Test
    fun `a failed answer drops the words it streamed`() = runTest {
        val viewModel = createDiscoverChat()
        assistantService.streamEvents = mutableListOf(
            AssistantStreamEvent.Started("answer_1", "chat_1"),
            AssistantStreamEvent.Text("Try "),
            AssistantStreamEvent.Error(AssistantFailureReason.TIMEOUT)
        )

        ask(viewModel, "Somewhere chill")

        assertThat(viewModel.state.value.messages.first().content).isEqualTo("")
    }

    @Test
    fun `a finished answer is marked answered with all its words`() = runTest {
        val viewModel = createDiscoverChat()
        assistantService.streamEvents = mutableListOf(
            AssistantStreamEvent.Started("answer_1", "chat_1"),
            AssistantStreamEvent.Text("Try "),
            AssistantStreamEvent.Text("these two."),
            AssistantStreamEvent.Done
        )

        ask(viewModel, "Somewhere chill")

        val answer = viewModel.state.value.messages.first()
        assertThat(answer.status).isEqualTo(AssistantMessageStatus.ANSWERED)
        assertThat(answer.content).isEqualTo("Try these two.")
    }

    @Test
    fun `an answer the server gives up on tells the screen it failed`() = runTest {
        val viewModel = createDiscoverChat()
        assistantService.streamEvents = mutableListOf(
            AssistantStreamEvent.Started("answer_1", "chat_1"),
            AssistantStreamEvent.Error(AssistantFailureReason.ERROR)
        )

        viewModel.events.test {
            ask(viewModel, "Somewhere chill")

            assertThat(awaitItem()).isEqualTo(AssistantEvent.AnswerFailed)
        }
    }

    @Test
    fun `a dropped discover answer reads the chat again from the server`() = runTest {
        val viewModel = createDiscoverChat()
        assistantService.streamEvents = mutableListOf(
            AssistantStreamEvent.Started("a1", "chat_1"),
            AssistantStreamEvent.Text("Try ")
        )
        assistantService.streamEndResult = Result.Failure(DataError.Remote.NO_INTERNET)
        assistantService.discoverChats["chat_1"] = listOf(
            message("a1", AssistantMessageRole.ASSISTANT, createdAtMillis = 2_000),
            message("q1", AssistantMessageRole.USER, createdAtMillis = 1_000)
        )

        ask(viewModel, "Somewhere chill")

        assertThat(assistantService.discoverRequestChatIds).containsExactly("chat_1")
        val answer = viewModel.state.value.messages.first()
        assertThat(answer.status).isEqualTo(AssistantMessageStatus.ANSWERED)
        assertThat(answer.content).isEqualTo("Message a1")
    }

    @Test
    fun `reopening discover picks the last chat back up`() = runTest {
        val firstViewModel = createDiscoverChat()
        assistantService.streamEvents = answeredStream(messageId = "a1", discoverChatId = "chat_1")
        ask(firstViewModel, "Somewhere chill")
        assistantService.discoverChats["chat_1"] = listOf(
            message("a1", AssistantMessageRole.ASSISTANT, createdAtMillis = 2_000),
            message("q1", AssistantMessageRole.USER, createdAtMillis = 1_000)
        )

        val reopened = openDiscoverChat(createViewModel(keepDiscoverChat = true))

        assertThat(reopened.state.value.discoverChatId).isEqualTo("chat_1")
        assertThat(reopened.state.value.messages.map { it.id }).containsExactly("a1", "q1")
    }

    @Test
    fun `reopening discover after the chat expired starts afresh without a message`() = runTest {
        val firstViewModel = createDiscoverChat()
        assistantService.streamEvents = answeredStream(messageId = "a1", discoverChatId = "chat_1")
        ask(firstViewModel, "Somewhere chill")

        val reopened = createViewModel(keepDiscoverChat = true)
        reopened.events.test {
            openDiscoverChat(reopened)

            assertThat(reopened.state.value.discoverChatId).isNull()
            assertThat(reopened.state.value.messages).isEmpty()
            expectNoEvents()
        }
    }

    @Test
    fun `a new chat empties the screen and the next question starts a fresh one`() = runTest {
        val viewModel = createDiscoverChat()
        assistantService.streamEvents = answeredStream(discoverChatId = "chat_1")
        ask(viewModel, "Somewhere chill")

        viewModel.onAction(AssistantAction.OnNewChatClick)
        ask(viewModel, "Something cheaper")

        assertThat(assistantService.sentMessages.map { it.discoverChatId }).containsExactly(null, null)
    }

    @Test
    fun `a new chat is not picked back up on reopening`() = runTest {
        val viewModel = createDiscoverChat()
        assistantService.streamEvents = answeredStream(discoverChatId = "chat_1")
        ask(viewModel, "Somewhere chill")

        viewModel.onAction(AssistantAction.OnNewChatClick)
        val reopened = openDiscoverChat(createViewModel(keepDiscoverChat = true))

        assertThat(reopened.state.value.discoverChatId).isNull()
        assertThat(assistantService.discoverRequestChatIds).isEmpty()
    }

    @Test
    fun `a connection that breaks after the answer is done keeps the answer`() = runTest {
        val viewModel = createDiscoverChat()
        assistantService.streamEvents = mutableListOf(
            AssistantStreamEvent.Started("answer_1", "chat_1"),
            AssistantStreamEvent.Text("Try these two."),
            AssistantStreamEvent.Done
        )
        assistantService.streamEndResult = Result.Failure(DataError.Remote.UNKNOWN)

        ask(viewModel, "Somewhere chill")

        val answer = viewModel.state.value.messages.first()
        assertThat(answer.status).isEqualTo(AssistantMessageStatus.ANSWERED)
        assertThat(answer.content).isEqualTo("Try these two.")
    }

    @Test
    fun `a connection that breaks after the server gave up keeps its reason`() = runTest {
        val viewModel = createDiscoverChat()
        assistantService.streamEvents = mutableListOf(
            AssistantStreamEvent.Started("answer_1", "chat_1"),
            AssistantStreamEvent.Error(AssistantFailureReason.TIMEOUT)
        )
        assistantService.streamEndResult = Result.Failure(DataError.Remote.UNKNOWN)

        ask(viewModel, "Somewhere chill")

        assertThat(viewModel.state.value.messages.first().failureReason).isEqualTo(AssistantFailureReason.TIMEOUT)
    }

    @Test
    fun `retrying a failed answer asks the same question again`() = runTest {
        val viewModel = createDiscoverChat()
        assistantService.streamEvents = mutableListOf(
            AssistantStreamEvent.Started("answer_1", "chat_1"),
            AssistantStreamEvent.Error(AssistantFailureReason.ERROR)
        )
        ask(viewModel, "Somewhere chill")
        val failedAnswerId = viewModel.state.value.messages.first().id

        viewModel.onAction(AssistantAction.OnRetryAnswerClick(failedAnswerId))
        advanceUntilIdle()

        assertThat(assistantService.sentMessages.map { it.content }).containsExactly("Somewhere chill", "Somewhere chill")
    }

    @Test
    fun `opening a hangout chat loads its history newest first`() = runTest {
        assistantService.hangoutMessages = mutableListOf(
            message("a1", AssistantMessageRole.ASSISTANT, createdAtMillis = 2_000, recommendations = listOf(AssistantRecommendation(lounge, "Fits"))),
            message("q1", AssistantMessageRole.USER, createdAtMillis = 1_000)
        )

        val viewModel = createHangoutChat()

        val messages = viewModel.state.value.messages
        assertThat(messages.map { it.id }).containsExactly("a1", "q1")
        assertThat(messages.first().recommendations.map { it.spot.id }).containsExactly("spot_1")
        assertThat(viewModel.state.value.isAttending).isEqualTo(true)
    }

    @Test
    fun `an empty history page is the last one`() = runTest {
        assistantService.hangoutMessages = mutableListOf(
            message("a1", AssistantMessageRole.ASSISTANT, createdAtMillis = 2_000),
            message("q1", AssistantMessageRole.USER, createdAtMillis = 1_000)
        )

        val viewModel = createHangoutChat()
        assertThat(viewModel.state.value.isEndReached).isFalse()

        viewModel.onAction(AssistantAction.LoadNextPage)
        advanceUntilIdle()

        assertThat(viewModel.state.value.isEndReached).isTrue()
        assertThat(viewModel.state.value.messages.map { it.id }).containsExactly("a1", "q1")
        assertThat(assistantService.historyRequestBefores).containsExactly(null, Instant.fromEpochMilliseconds(1_000).toString())
    }

    @Test
    fun `a hangout question sends the hangout and no map area`() = runTest {
        val viewModel = createHangoutChat()
        assistantService.streamEvents = answeredStream(messageId = "a1")

        ask(viewModel, "What fits our vibe")

        val sent = assistantService.sentMessages.single()
        assertThat(sent.hangoutId).isEqualTo(hangoutId)
        assertThat(sent.latitude).isNull()
        assertThat(viewModel.state.value.messages.first().id).isEqualTo("a1")
    }

    @Test
    fun `a dropped hangout answer reads the chat again from the server`() = runTest {
        val viewModel = createHangoutChat()
        assistantService.streamEvents = mutableListOf(AssistantStreamEvent.Started("a1", null))
        assistantService.streamEndResult = Result.Failure(DataError.Remote.NO_INTERNET)
        assistantService.hangoutMessages = mutableListOf(
            message("a1", AssistantMessageRole.ASSISTANT, createdAtMillis = 2_000, status = AssistantMessageStatus.PENDING),
            message("q1", AssistantMessageRole.USER, createdAtMillis = 1_000)
        )

        ask(viewModel, "What fits our vibe")

        assertThat(assistantService.historyRequestBefores).containsExactly(null, null)
        assertThat(viewModel.state.value.messages.first().status).isEqualTo(AssistantMessageStatus.PENDING)
    }

    @Test
    fun `a chat cleared on another device is reread when the app comes back`() = runTest {
        assistantService.hangoutMessages = mutableListOf(message("q1", AssistantMessageRole.USER, createdAtMillis = 1_000))
        val viewModel = createHangoutChat()
        assertThat(viewModel.state.value.messages.size).isEqualTo(1)

        appLifecycleObserver.isInForeground.value = false
        assistantService.hangoutMessages.clear()
        appLifecycleObserver.isInForeground.value = true
        advanceUntilIdle()

        assertThat(viewModel.state.value.messages).isEmpty()
    }

    @Test
    fun `coming back to the app keeps the chat on screen while it is reread`() = runTest {
        assistantService.hangoutMessages = mutableListOf(message("q1", AssistantMessageRole.USER, createdAtMillis = 1_000))
        val viewModel = createHangoutChat()

        val seenStates = mutableListOf<AssistantState>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.state.collect { seenStates += it }
        }
        appLifecycleObserver.isInForeground.value = false
        assistantService.hangoutMessages.add(0, message("q2", AssistantMessageRole.USER, createdAtMillis = 2_000))
        appLifecycleObserver.isInForeground.value = true
        advanceUntilIdle()

        assertThat(seenStates.none { it.messages.isEmpty() }).isTrue()
        assertThat(seenStates.none { it.isLoading }).isTrue()
        assertThat(viewModel.state.value.messages.map { it.id }).containsExactly("q2", "q1")
    }

    @Test
    fun `a failed reread on coming back keeps the chat as it was`() = runTest {
        assistantService.hangoutMessages = mutableListOf(message("q1", AssistantMessageRole.USER, createdAtMillis = 1_000))
        val viewModel = createHangoutChat()

        appLifecycleObserver.isInForeground.value = false
        assistantService.shouldReturnError = true
        appLifecycleObserver.isInForeground.value = true
        advanceUntilIdle()

        assertThat(viewModel.state.value.messages.map { it.id }).containsExactly("q1")
        assertThat(viewModel.state.value.loadError).isNull()
    }

    @Test
    fun `clearing the chat empties it and tells the hangout screen`() = runTest {
        assistantService.hangoutMessages = mutableListOf(message("q1", AssistantMessageRole.USER, createdAtMillis = 1_000))
        val viewModel = createHangoutChat()

        viewModel.events.test {
            viewModel.onAction(AssistantAction.OnClearChatConfirmed)

            assertThat(awaitItem()).isEqualTo(AssistantEvent.ChatCleared)
            assertThat(viewModel.state.value.messages).isEmpty()
            assertThat(assistantService.clearedHangoutIds).containsExactly(hangoutId)
            assertThat(assistantMessageCountRepository.observeMessageCount(hangoutId).first()).isEqualTo(0L)
        }
    }

    @Test
    fun `opening a card fetches the whole spot first`() = runTest {
        spotService.spotDetailsList = mutableListOf(lounge.copy(rating = 4.8))
        assistantService.hangoutMessages = mutableListOf(
            message("a1", AssistantMessageRole.ASSISTANT, createdAtMillis = 2_000, recommendations = listOf(AssistantRecommendation(lounge, "Fits")))
        )
        val viewModel = createHangoutChat()

        viewModel.onAction(AssistantAction.OnRecommendationClick("spot_1"))
        advanceUntilIdle()

        assertThat(spotService.detailsRequestIds).containsExactly("spot_1")
        assertThat(viewModel.state.value.selectedSpot?.rating).isEqualTo(4.8)
        assertThat(viewModel.state.value.loadingSpotId).isNull()
    }

    @Test
    fun `a card cover that fails to load fetches the place afresh only once`() = runTest {
        spotService.spotDetailsList = mutableListOf(lounge.copy(photoUrls = listOf("fresh_1")))
        assistantService.hangoutMessages = mutableListOf(
            message("a1", AssistantMessageRole.ASSISTANT, createdAtMillis = 2_000, recommendations = listOf(AssistantRecommendation(lounge, "Fits")))
        )
        val viewModel = createHangoutChat()

        viewModel.onAction(AssistantAction.OnCoverPhotoLoadFailed("spot_1"))
        viewModel.onAction(AssistantAction.OnCoverPhotoLoadFailed("spot_1"))
        advanceUntilIdle()

        assertThat(spotService.detailsRequestIds).containsExactly("spot_1")
        val cardSpot = viewModel.state.value.messages.single().recommendations.single().spot
        assertThat(cardSpot.photoUrls).containsExactly("fresh_1")
    }

    @Test
    fun `a suggestion is done once its spot reaches the ballot`() = runTest {
        val viewModel = createHangoutChat()

        viewModel.events.test {
            viewModel.onAction(AssistantAction.OnSuggestSpot("spot_2"))
            assertThat(viewModel.state.value.suggestingSpotIds).isEqualTo(setOf("spot_2"))

            // What the voting section does when the lobby reports the new candidate
            ballotRepository.addCandidate(hangoutId, rooftop)

            assertThat(awaitItem()).isEqualTo(AssistantEvent.SpotSuggested)
            assertThat(viewModel.state.value.suggestingSpotIds).isEmpty()
            assertThat(viewModel.state.value.ballotSpotIds).isEqualTo(setOf("spot_2"))
            assertThat(lobbyService.proposedSpotIds).containsExactly("spot_2")
        }
    }

    @Test
    fun `a hangout chat follows the group center the voting section reports`() = runTest {
        val viewModel = createHangoutChat()
        assertThat(viewModel.state.value.center).isNull()

        ballotRepository.replaceCenter(hangoutId, LocationCoordinates(6.5, 3.3))

        assertThat(viewModel.state.value.center).isEqualTo(LocationCoordinates(6.5, 3.3))
    }

    @Test
    fun `a full ballot ends the pending suggestion with an error`() = runTest {
        val viewModel = createHangoutChat()

        viewModel.events.test {
            viewModel.onAction(AssistantAction.OnSuggestSpot("spot_2"))
            connectionClient.sendEvent(LobbyEvent.LobbyError("CANDIDATES_FULL", "Full"))

            assertThat(awaitItem()).isInstanceOf(AssistantEvent.Error::class)
            assertThat(viewModel.state.value.suggestingSpotIds).isEmpty()
        }
    }

    @Test
    fun `a lobby error with nothing pending says nothing`() = runTest {
        val viewModel = createHangoutChat()

        viewModel.events.test {
            connectionClient.sendEvent(LobbyEvent.LobbyError("NOT_A_CANDIDATE", "Nope"))

            expectNoEvents()
        }
    }

    private fun TestScope.ask(viewModel: AssistantViewModel, question: String) {
        viewModel.state.value.messageTextState.typeText(question)
        viewModel.onAction(AssistantAction.OnSendClick)
        advanceUntilIdle()
    }

    private fun TestScope.createDiscoverChat(): AssistantViewModel = openDiscoverChat(createViewModel())

    private fun TestScope.openDiscoverChat(viewModel: AssistantViewModel): AssistantViewModel {
        viewModel.onAction(AssistantAction.OnOpenDiscoverChat(latitude = 6.44, longitude = 3.42, areaName = "Lekki"))
        advanceUntilIdle()
        return viewModel
    }

    // A voting hangout the signed in host is going to, with its copy already in the store like the detail screen leaves it
    private suspend fun TestScope.createHangoutChat(): AssistantViewModel {
        signIn(HOST_ID)
        hangoutService.currentUserId = HOST_ID
        hangoutService.createHangout(
            name = "Night Out",
            description = null,
            vibe = HangoutVibe.CHILL,
            scheduledAt = Instant.fromEpochMilliseconds(4102444800000L),
            maxAttendees = null,
            spotId = null
        )
        val viewModel = createViewModel()
        hangoutDetailRepository.refreshHangout(hangoutId)
        connectionClient.connectionState.value = ConnectionState.CONNECTED
        viewModel.onAction(AssistantAction.OnOpenHangoutChat(hangoutId))
        advanceUntilIdle()
        return viewModel
    }

    private fun TestScope.createViewModel(keepDiscoverChat: Boolean = false): AssistantViewModel {
        hangoutDetailRepository = InMemoryHangoutDetailRepository(hangoutService, sessionStorage, backgroundScope)
        assistantMessageCountRepository = InMemoryAssistantMessageCountRepository(assistantService, sessionStorage, backgroundScope)
        if (!keepDiscoverChat) discoverChatRepository = InMemoryDiscoverChatRepository(sessionStorage, backgroundScope)
        ballotRepository = InMemoryBallotRepository(sessionStorage, backgroundScope)
        runCurrent()

        val viewModel = AssistantViewModel(
            assistantService = assistantService,
            assistantMessageCountRepository = assistantMessageCountRepository,
            discoverChatRepository = discoverChatRepository,
            hangoutDetailRepository = hangoutDetailRepository,
            ballotRepository = ballotRepository,
            spotService = spotService,
            connectionClient = connectionClient,
            lobbyService = lobbyService,
            sessionStorage = sessionStorage,
            appLifecycleObserver = appLifecycleObserver
        )
        collectInBackground(viewModel.state)
        return viewModel
    }

    private fun answeredStream(
        messageId: String = "answer_1",
        discoverChatId: String? = null
    ) = mutableListOf(
        AssistantStreamEvent.Started(messageId, discoverChatId),
        AssistantStreamEvent.Text("Try The Lounge."),
        AssistantStreamEvent.Done
    )

    private fun message(
        id: String,
        role: AssistantMessageRole,
        createdAtMillis: Long,
        status: AssistantMessageStatus? = if (role == AssistantMessageRole.ASSISTANT) AssistantMessageStatus.ANSWERED else null,
        recommendations: List<AssistantRecommendation> = emptyList()
    ) = AssistantMessage(
        id = id,
        role = role,
        status = status,
        content = "Message $id",
        failureReason = null,
        recommendations = recommendations,
        createdAt = Instant.fromEpochMilliseconds(createdAtMillis)
    )

    private suspend fun signIn(userId: String) {
        sessionStorage.set(
            AuthInfo(
                accessToken = "access",
                refreshToken = "refresh",
                user = User.Authenticated(
                    id = userId,
                    provider = AuthProvider.GOOGLE,
                    email = "$userId@test.com",
                    displayName = userId,
                    username = userId
                )
            )
        )
    }

    private fun spot(id: String, name: String) = Spot(
        id = id, name = name, category = SpotCategory.CAFE,
        latitude = 6.5, longitude = 3.3, isSaved = false,
        photoUrls = emptyList(), rating = 4.5, reviewCount = 100,
        openingHours = null, shortAddress = "VI, Lagos",
        websiteUrl = null, googleMapsUrl = null, priceLevel = null,
        description = null, savedAt = null,
        typeLabel = null, generativeSummary = null, reviewSummary = null, priceRange = null,
        businessStatus = null, amenities = null, parking = null, payment = null,
        phoneNumber = null, directionsUrl = null
    )

    private companion object {
        const val HOST_ID = "host_1"
    }
}
